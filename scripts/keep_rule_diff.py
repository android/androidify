#!/usr/bin/env python3
# Copyright 2026 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
"""GitHub-style diff of the keep rules a PR adds, removes or changes, and the classes they keep.

Compares two keep rule .pb files written by R8's configuration analyzer: one from the base branch,
one from the PR. With AGP 9.3.2, every release build writes one by default, to
app/build/outputs/mapping/release/configanalyzer.pb. `./gradlew :app:analyzeReleaseR8Config` writes
the same file to app/build/reports/r8/r8-config-analyzer-release.pb.

    python3 scripts/keep_rule_diff.py base.pb pr.pb -o keep_rule_diff.md

Reading .pb files needs `pip install 'protobuf>=6.33.4'` and schema_pb2.py next to this script.
See KEEP_RULE_DIFF.md for what the report shows and how CI runs it.
"""
import argparse
import json
import os
import re
import sys

MARKER = "<!-- keepdiff-report -->"  # lets CI find and update its earlier comment
SCHEMA_DIR = os.path.dirname(os.path.abspath(__file__))
PRIMITIVES = {"V": "void", "Z": "boolean", "B": "byte", "C": "char", "S": "short",
              "I": "int", "J": "long", "F": "float", "D": "double"}
NO_CHANGES = "✅ No keep rules were added, removed or changed, and they keep the same classes."


# ---------- Reading the .pb files ----------

def read_pb(path):
    """Returns the .pb file (or convert.py's JSON) as a dict keyed by proto field names."""
    if not path.endswith(".pb"):
        with open(path, encoding="utf-8") as f:
            data = json.load(f)
        if not isinstance(data, dict):
            raise ValueError(f"{path}: expected the JSON object that convert.py writes")
        return snake_keys(data)
    if SCHEMA_DIR not in sys.path:
        sys.path.insert(0, SCHEMA_DIR)
    try:
        import schema_pb2
        from google.protobuf.json_format import MessageToDict
        from google.protobuf.message import DecodeError
    except Exception as e:  # protobuf missing, or older than the one schema_pb2.py was generated with
        raise ImportError(f"reading .pb files needs protobuf>=6.33.4 (pip install 'protobuf>=6.33.4'): {e}") from e
    container = schema_pb2.BlastRadiusContainer()
    with open(path, "rb") as f:
        try:
            container.ParseFromString(f.read())
        except DecodeError as e:
            raise ValueError(f"{path}: not an R8 blast radius file ({e})") from e
    return MessageToDict(container, preserving_proto_field_name=True)


def snake_keys(obj):
    """Also accepts protobuf's default camelCase JSON (keepRuleBlastRadiusTable, ...)."""
    if isinstance(obj, dict):
        return {re.sub("([A-Z])", r"_\1", k).lower(): snake_keys(v) for k, v in obj.items()}
    if isinstance(obj, list):
        return [snake_keys(v) for v in obj]
    return obj


def java_type(desc):
    """JVM descriptor to Java name: 'I' -> 'int', '[Ljava/lang/String;' -> 'java.lang.String[]'."""
    base = desc.lstrip("[")
    dims = len(desc) - len(base)
    if base.startswith("L") and base.endswith(";"):
        base = base[1:-1].replace("/", ".")
    else:
        base = PRIMITIVES.get(base, base)
    return base + "[]" * dims


def short_path(path):
    """Drops machine-specific prefixes so the same rule reads the same on every machine."""
    path = path.replace("\\", "/")
    if "/transformed/" in path:  # Gradle transform cache: keep '<library>/proguard.txt'
        return path.split("/transformed/", 1)[1]
    cwd = os.getcwd().replace("\\", "/").rstrip("/") + "/"
    return path[len(cwd):] if path.startswith(cwd) else path


def load(path):
    """Indexes one .pb file: keep rules by their text, each with where it comes from and the classes it keeps."""
    d = read_pb(path)

    def table(name):  # proto3 leaves out zero values, so a missing id is 0
        return {row.get("id", 0): row for row in d.get(name, [])}

    types = {i: java_type(t.get("java_descriptor", "")) for i, t in table("type_reference_table").items()}
    files = table("file_origin_table")
    field_refs, method_refs = table("field_reference_table"), table("method_reference_table")

    def origin(o):
        """(source, file, line): the rule's file, or the library it ships in, for grouping and sorting."""
        f = files.get((o or {}).get("file_origin_id", 0), {})
        name = short_path(f.get("filename", "unknown file")).split(" (extracted file:")[0]
        maven = f.get("maven_coordinate")
        if maven:
            source = f"{maven.get('group_id', '')}:{maven.get('artifact_id', '')}:{maven.get('version', '')}"
        else:
            source = name
        return source, name, int((o or {}).get("line_number", 0))

    def owner(refs, ref_id):
        return types.get(refs.get(ref_id, {}).get("class_reference_id", 0), "?")

    # Every kept class, field and method, mapped to the class it is in.
    classes_of = {
        "class": {k.get("id", 0): types.get(k.get("class_reference_id", 0), "?")
                  for k in d.get("kept_class_info_table", [])},
        "field": {k.get("id", 0): owner(field_refs, k.get("field_reference_id", 0))
                  for k in d.get("kept_field_info_table", [])},
        "method": {k.get("id", 0): owner(method_refs, k.get("method_reference_id", 0))
                   for k in d.get("kept_method_info_table", [])},
    }

    rules = {}
    for row in d.get("keep_rule_blast_radius_table", []):
        text = " ".join(row.get("source", "").split())  # one line; whitespace edits keep the same rule
        rule = rules.setdefault(text, {"text": text, "classes": set(), "origins": set()})
        radius = row.get("blast_radius", {})
        for kind, owners in classes_of.items():
            rule["classes"].update(owners[i] for i in radius.get(f"{kind}_blast_radius", []) if i in owners)
        rule["origins"].add(origin(row.get("origin")))

    global_rules = {}
    for row in d.get("global_keep_rule_blast_radius_table", []):
        text = " ".join(row.get("source", "").split())
        global_rules.setdefault(text, {"text": text, "classes": set(), "origins": set()})["origins"].add(
            origin(row.get("origin")))

    return {"rules": rules, "global_rules": global_rules}


# ---------- Comparing ----------

def compare(base, pr):
    """Rules added, removed, or keeping different classes in the PR."""
    b, p = base["rules"], pr["rules"]
    changed = []
    for k in b.keys() & p.keys():
        gained, lost = p[k]["classes"] - b[k]["classes"], b[k]["classes"] - p[k]["classes"]
        if gained or lost:
            changed.append(dict(p[k], gained=gained, lost=lost))
    bg, pg = base["global_rules"], pr["global_rules"]
    return {
        "added": [p[k] for k in p.keys() - b.keys()] + [pg[k] for k in pg.keys() - bg.keys()],
        "removed": [b[k] for k in b.keys() - p.keys()] + [bg[k] for k in bg.keys() - pg.keys()],
        "changed": changed,
    }


# ---------- Markdown ----------

def class_lines(sign, classes, max_items):
    names = sorted(classes)
    lines = [f"{sign}     {name}" for name in names[:max_items]]
    if len(names) > max_items:
        lines.append(f"{sign}     … and {len(names) - max_items:,} more")
    return lines


def render(base, pr, max_items=50, max_chars=60000):
    """A ```diff block, grouped by the file (or library) each rule comes from, in file order."""
    diff = compare(base, pr)
    head = f"{MARKER}\n## 🔍 R8 keep rule diff"
    hunks = []  # (origin, lines)
    for sign, key in (("+", "added"), ("-", "removed")):
        for rule in diff[key]:
            hunks.append((min(rule["origins"]), [f"{sign} {rule['text']}"] + class_lines(sign, rule["classes"], max_items)))
    for rule in diff["changed"]:
        hunks.append((min(rule["origins"]), [f"  {rule['text']}"] + class_lines("+", rule["gained"], max_items)
                      + class_lines("-", rule["lost"], max_items)))
    if not hunks:
        return f"{head}\n\n{NO_CHANGES}\n"

    lines, source, cut = [], None, 0
    budget = max_chars - len(head) - 600 if max_chars else None  # room for the legend and the notice
    for origin, hunk in sorted(hunks, key=lambda h: (h[0], h[1][0][2:])):
        piece = ([f"@@ {origin[0]} @@"] if origin[0] != source else []) + hunk
        if cut or (budget is not None and sum(len(x) + 1 for x in lines + piece) > budget):
            cut += len(hunk)
            continue
        source = origin[0]
        lines += piece

    text = "\n".join(lines)
    fence = "`" * max(3, max(map(len, re.findall("`+", text)), default=0) + 1)  # build text can't end the block
    out = (f"{head}\n\n`+` and `-` lines are keep rules the PR adds or removes. The indented lines under a rule "
           f"are the classes it keeps. A rule with no sign didn't change, but now keeps different classes.\n\n"
           f"{fence}diff\n{text}\n{fence}\n")
    if cut:
        out += (f"\n> ✂️ {cut:,} more line{'' if cut == 1 else 's'} not shown, to keep this report under "
                f"{max_chars:,} characters. Run `scripts/keep_rule_diff.py --max-chars 0` to see them all.\n")
    return out


def main(argv=None):
    parser = argparse.ArgumentParser(
        description="Write a GitHub-style diff of the keep rules that changed between two keep rule .pb files.")
    parser.add_argument("base", help="main's .pb file (or the JSON from convert.py)")
    parser.add_argument("pr", help="the PR's .pb file (or .json)")
    parser.add_argument("-o", "--output", default="keep_rule_diff.md", help="Markdown file to write (default: %(default)s)")
    parser.add_argument("--max-items", type=int, default=50, metavar="N",
                        help="classes listed per rule (default: %(default)s)")
    parser.add_argument("--max-chars", type=int, default=60000, metavar="N",
                        help="report size cap, 0 for none; GitHub comments allow 65,536 (default: %(default)s)")
    args = parser.parse_args(argv)
    try:
        base, pr = load(args.base), load(args.pr)
    except (OSError, ValueError, ImportError) as e:
        sys.exit(f"error: {e}")
    with open(args.output, "w", encoding="utf-8") as f:
        f.write(render(base, pr, args.max_items, args.max_chars))
    print(f"Report written to {args.output}")


if __name__ == "__main__":
    main()
