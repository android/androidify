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
"""Report-only keep ratios of the app's third-party libraries (.aar / .jar), powered by `android analyze`.

Lists the libraries with Gradle's built-in `dependencies` report, finds their AARs and JARs in the Gradle
cache, and analyzes them. The keep ratio is the R8 DEX size divided by the D8 DEX size of a library analyzed
on its own: the share of the library that its own keep rules force into every app that uses it. When a
baseline file exists, the report also shows how added, removed, and updated libraries changed the kept code.
Run it from the project root after a release build, so that the libraries are in the Gradle cache. This
report never fails.
"""

from __future__ import annotations

import argparse
from dataclasses import dataclass
import glob
import json
import os
from pathlib import Path
import re
import subprocess
import sys
from typing import Any

DEFAULT_MODULE = ":app"
DEFAULT_CONFIGURATION = "releaseRuntimeClasspath"
DEFAULT_BASELINE = "third-party-keep-ratios.json"
BATCH_SIZE = 20

# A dependency in Gradle's `dependencies` report, such as `+--- group:name:1.0 -> 1.1 (*)`.
DEPENDENCY_LINE = re.compile(r"--- ([^\s:]+):([^\s:]+)(?::([^\s]+))?(?: -> ([^\s]+))?")


@dataclass
class Library:
    coordinate: str
    path: str
    d8_bytes: int | None = None  # None when the analysis failed.
    r8_bytes: int = 0

    @property
    def key(self) -> str:
        """The library's group and name, without the version."""
        return self.coordinate.rsplit(":", 1)[0]

    @property
    def version(self) -> str:
        return self.coordinate.rsplit(":", 1)[1]

    @property
    def keep_ratio(self) -> float | None:
        if not self.d8_bytes:
            return None
        return self.r8_bytes / self.d8_bytes


@dataclass
class Change:
    """A library that was added, removed, or updated, and whose kept code changed."""

    key: str
    before: Library | None
    after: Library | None

    @property
    def kept_change(self) -> int:
        return (self.after.r8_bytes if self.after else 0) - (self.before.r8_bytes if self.before else 0)


def parse_dependency_report(report: str) -> set[tuple[str, str, str]]:
    """Returns the resolved (group, name, version) of every external module in a `dependencies` report."""
    modules: set[tuple[str, str, str]] = set()
    for line in report.splitlines():
        if line.rstrip().endswith(("(c)", "(n)")):
            continue  # Constraints, such as the ones from BOMs, and dependencies that weren't resolved.
        match = DEPENDENCY_LINE.search(line)
        if not match:
            continue
        group, name, requested, resolved = match.groups()
        version = resolved or requested
        if version and not version.startswith("{"):
            modules.add((group, name, version))
    return modules


def find_archive(cache: Path, group: str, name: str, version: str) -> str | None:
    """Finds a module's AAR or JAR in the Gradle cache."""
    archives = sorted(
        path
        for path in glob.glob(str(cache / group / name / version / "*" / "*"))
        if path.endswith((".aar", ".jar")) and not path.endswith(("-sources.jar", "-javadoc.jar"))
    )
    aars = [path for path in archives if path.endswith(".aar")]
    return (aars or archives or [None])[0]


def list_libraries(module: str, configuration: str) -> list[Library] | None:
    """Lists the third-party AARs and JARs of a configuration with Gradle's `dependencies` report."""
    cmd = ["./gradlew", f"{module}:dependencies", "--configuration", configuration, "--quiet"]
    proc = subprocess.run(cmd, capture_output=True, text=True)
    if proc.returncode != 0:
        print(f"Error running `{' '.join(cmd)}`:", file=sys.stderr)
        print(proc.stderr or proc.stdout, file=sys.stderr)
        return None

    cache = Path(os.environ.get("GRADLE_USER_HOME", Path.home() / ".gradle")) / "caches" / "modules-2" / "files-2.1"
    libraries, skipped = [], 0
    for group, name, version in sorted(parse_dependency_report(proc.stdout)):
        archive = find_archive(cache, group, name, version)
        if archive:
            libraries.append(Library(f"{group}:{name}:{version}", archive))
        else:
            skipped += 1
    print(
        f"Found {len(libraries)} third-party libraries in {module} {configuration}. Skipped {skipped} modules "
        "without an AAR or JAR in the Gradle cache, such as BOMs."
    )
    return libraries


def analyze_batch(paths: list[str]) -> dict[str, dict[str, Any]] | None:
    """Runs `android analyze` on several artifacts in one run. Returns results by path, or None on failure."""
    cmd = ["android", "analyze", "--analysis=optimization", "--format=json", *paths]
    proc = subprocess.run(cmd, capture_output=True, text=True)
    json_start = proc.stdout.find("{")
    if proc.returncode != 0 or json_start == -1:
        return None
    # Parse only the JSON. An outdated CLI prints an update notice after it on stdout.
    data, _ = json.JSONDecoder().raw_decode(proc.stdout, json_start)
    results = data.get("results", [])
    by_path = {r.get("artifactPath", ""): r for r in results}
    if all(p in by_path for p in paths):
        return by_path
    if len(results) == len(paths):
        return dict(zip(paths, results))
    return None


def analyze_all(paths: list[str]) -> dict[str, dict[str, Any]]:
    """Analyzes artifacts in batches, and retries a failed batch one artifact at a time."""
    results: dict[str, dict[str, Any]] = {}
    for start in range(0, len(paths), BATCH_SIZE):
        batch = paths[start : start + BATCH_SIZE]
        batch_results = analyze_batch(batch)
        if batch_results is None:
            batch_results = {}
            for path in batch:
                batch_results.update(analyze_batch([path]) or {})
        results.update(batch_results)
        print(f"  Analyzed {min(start + BATCH_SIZE, len(paths))}/{len(paths)} libraries", flush=True)
    return results


def apply_results(libraries: list[Library], results: dict[str, dict[str, Any]]) -> None:
    for lib in libraries:
        result = results.get(lib.path, {})
        analysis = result.get("optimization", {}).get("libraryAnalysisResult")
        if analysis is None:
            continue
        dex = analysis.get("dex", {})
        # The DEX sizes are int64, which newer CLI versions write to JSON as strings; int() reads both.
        lib.d8_bytes = int(dex.get("d8CompileResult", {}).get("dexSizeBytes", 0))
        lib.r8_bytes = int(dex.get("r8CompileResult", {}).get("dexSizeBytes", 0))


def load_baseline(path: Path) -> dict[str, Library] | None:
    """Reads the libraries of a baseline file, by group and name, or None if there's no baseline."""
    if not path.exists():
        return None
    data = json.loads(path.read_text(encoding="utf-8"))
    return {
        key: Library(f"{key}:{entry['version']}", "", int(entry["d8DexSizeBytes"]), int(entry["r8DexSizeBytes"]))
        for key, entry in data.get("libraries", {}).items()
    }


def save_baseline(path: Path, libraries: list[Library]) -> None:
    """Writes every analyzed library to the baseline file."""
    entries = {
        lib.key: {"version": lib.version, "d8DexSizeBytes": lib.d8_bytes, "r8DexSizeBytes": lib.r8_bytes}
        for lib in sorted(libraries, key=lambda lib: lib.key)
        if lib.d8_bytes is not None
    }
    path.write_text(json.dumps({"libraries": entries}, indent=2) + "\n", encoding="utf-8")
    print(f"Saved the keep ratios of {len(entries)} libraries to {path}")


def compare_with_baseline(libraries: list[Library], baseline: dict[str, Library]) -> list[Change]:
    """Finds the added, removed, and updated libraries whose kept code changed, the largest increase first."""
    current = {lib.key: lib for lib in libraries if lib.d8_bytes is not None}
    changes = []
    for key in sorted(set(current) | set(baseline)):
        before, after = baseline.get(key), current.get(key)
        if before and after and before.version == after.version:
            continue  # A library's own keep rules can't change without a new version.
        change = Change(key, before, after)
        if change.kept_change != 0:
            changes.append(change)
    return sorted(changes, key=lambda change: (-change.kept_change, change.key))


def sort_key(lib: Library) -> tuple[int, float, int, str]:
    """Highest keep ratio first, then the most kept code; failed analyses last."""
    if lib.d8_bytes is None:
        return (1, 0.0, 0, lib.coordinate)
    return (0, -(lib.keep_ratio or 0.0), -lib.r8_bytes, lib.coordinate)


def reported_libraries(libraries: list[Library]) -> list[Library]:
    """The libraries that keep part of their code, plus failed analyses. Libraries that keep nothing are left out."""
    return sorted((lib for lib in libraries if lib.d8_bytes is None or lib.r8_bytes > 0), key=sort_key)


def format_size(num_bytes: int) -> str:
    if num_bytes >= 1024 * 1024:
        return f"{num_bytes / (1024 * 1024):.2f} MB"
    return f"{num_bytes / 1024.0:.2f} KB"


def format_ratio(lib: Library | None) -> str:
    if lib is None:
        return "–"
    if lib.d8_bytes is None:
        return "analysis failed"
    if lib.d8_bytes == 0:
        return "no code"
    if 0 < lib.r8_bytes and lib.keep_ratio < 0.005:
        return "<0.01"
    return f"{lib.keep_ratio:.2f}"


def describe_change(change: Change) -> tuple[str, str, str, str]:
    """Returns the version, keep ratio, kept code, and kept code change of a change, as before → after."""
    before, after = change.before, change.after
    version = f"{before.version if before else 'added'} → {after.version if after else 'removed'}"
    ratio = f"{format_ratio(before)} → {format_ratio(after)}"
    kept = f"{format_size(before.r8_bytes) if before else '–'} → {format_size(after.r8_bytes) if after else '–'}"
    sign = "+" if change.kept_change > 0 else "-"
    return version, ratio, kept, f"{sign}{format_size(abs(change.kept_change))}"


def library_table(libraries: list[Library]) -> list[str]:
    lines = ["| Library | Keep Ratio | R8 Kept DEX | D8 DEX |", "| :--- | ---: | ---: | ---: |"]
    for lib in libraries:
        kept = format_size(lib.r8_bytes) if lib.d8_bytes is not None else ""
        total = format_size(lib.d8_bytes) if lib.d8_bytes is not None else ""
        lines.append(f"| `{lib.coordinate}` | {format_ratio(lib)} | {kept} | {total} |")
    return lines


def changes_table(changes: list[Change]) -> list[str]:
    lines = ["| Library | Version | Keep Ratio | R8 Kept DEX | Change |", "| :--- | :--- | ---: | ---: | ---: |"]
    for change in changes:
        version, ratio, kept, delta = describe_change(change)
        lines.append(f"| `{change.key}` | {version} | {ratio} | {kept} | {delta} |")
    return lines


def build_markdown_summary(libraries: list[Library], changes: list[Change] | None) -> str:
    """Shows how library changes affected the kept code, then the libraries that keep part of their code."""
    with_code = [lib for lib in libraries if lib.d8_bytes]
    keeping_count = sum(1 for lib in with_code if lib.r8_bytes > 0)
    total_kept = sum(lib.r8_bytes for lib in with_code)
    reported = reported_libraries(libraries)
    lines = [
        "### 📦 Third-Party Library Keep Ratios (Report Only)",
        "",
        f"{keeping_count} of {len(with_code)} third-party libraries with code keep part of it with their own keep rules "
        f"({format_size(total_kept)} in total). The keep ratio is the R8 DEX size divided by the D8 DEX size of each "
        "library analyzed on its own.",
        "",
    ]
    if changes is not None:
        lines += ["#### Changes since the baseline", ""]
        if changes:
            lines += changes_table(changes) + [""]
        else:
            lines += ["No added, removed, or updated library changed the kept code.", ""]
    if reported:
        lines += ["#### Libraries that keep code", "", "Libraries that keep none of their code aren't listed.", ""]
        lines += library_table(reported) + [""]
    return "\n".join(lines) + "\n"


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument(
        "--module",
        default=DEFAULT_MODULE,
        help=f"Gradle path of the app module (default: {DEFAULT_MODULE})",
    )
    parser.add_argument(
        "--configuration",
        default=DEFAULT_CONFIGURATION,
        help=f"Configuration that lists the libraries the app ships (default: {DEFAULT_CONFIGURATION})",
    )
    parser.add_argument(
        "--baseline",
        type=Path,
        default=Path(DEFAULT_BASELINE),
        help=f"Baseline file to compare with, if it exists (default: {DEFAULT_BASELINE})",
    )
    parser.add_argument(
        "--save-baseline",
        action="store_true",
        help="Write the current keep ratios to the baseline file after the report",
    )
    parser.add_argument(
        "--markdown-summary",
        type=Path,
        default=None,
        help="Optional file path to write the Markdown report (in addition to $GITHUB_STEP_SUMMARY if set)",
    )
    parser.add_argument(
        "--append-summary",
        action="store_true",
        help="Append to --markdown-summary instead of overwriting",
    )
    args = parser.parse_args()

    print(f"==> Listing third-party libraries with `./gradlew {args.module}:dependencies`...", flush=True)
    libraries = list_libraries(args.module, args.configuration)
    if libraries is None:
        return 2

    print(f"==> Running `android analyze` on {len(libraries)} third-party libraries...", flush=True)
    apply_results(libraries, analyze_all([lib.path for lib in libraries]))
    baseline = load_baseline(args.baseline)
    changes = compare_with_baseline(libraries, baseline) if baseline is not None else None
    reported = reported_libraries(libraries)

    if changes is not None:
        print(f"\nChanges since {args.baseline}:")
        for change in changes:
            version, ratio, kept, delta = describe_change(change)
            print(f"  {change.key:<55} {version:<24} keep ratio {ratio:<14} kept {kept:<22} {delta}")
        if not changes:
            print("  No added, removed, or updated library changed the kept code.")

    print()
    header = f"{'Library':<70} {'Keep Ratio':>15} {'R8 Kept':>11} {'D8 DEX':>11}"
    print(header)
    print("-" * len(header))
    for lib in reported:
        kept = format_size(lib.r8_bytes) if lib.d8_bytes is not None else ""
        total = format_size(lib.d8_bytes) if lib.d8_bytes is not None else ""
        print(f"{lib.coordinate:<70} {format_ratio(lib):>15} {kept:>11} {total:>11}")
    print("-" * len(header))
    print(f"{len(libraries) - len(reported)} libraries keep none of their code and aren't listed.")

    md_summary = build_markdown_summary(libraries, changes)
    step_summary_path = os.environ.get("GITHUB_STEP_SUMMARY")
    if step_summary_path:
        with open(step_summary_path, "a", encoding="utf-8") as f:
            f.write(md_summary)
    if args.markdown_summary is not None:
        args.markdown_summary.parent.mkdir(parents=True, exist_ok=True)
        mode = "a" if args.append_summary else "w"
        with open(args.markdown_summary, mode, encoding="utf-8") as f:
            f.write(md_summary)

    if args.save_baseline:
        save_baseline(args.baseline, libraries)
    return 0


if __name__ == "__main__":
    sys.exit(main())
