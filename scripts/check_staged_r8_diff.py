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
"""Fast presubmit diff & newly added dependency auditor for `git commit` / CI."""

from __future__ import annotations

import argparse
import glob
from pathlib import Path
import re
import subprocess
import sys

RED = "\033[91m"
GREEN = "\033[92m"
BOLD = "\033[1m"
RESET = "\033[0m"


def inspect_diff(diff_range: str | None) -> tuple[list[str], list[Path]]:
    """
    Inspects `git diff --cached` (or `git diff <diff_range>`) for:
      1. Broad package-wide `-keep class <pkg>.** { *; }` rules or `android.enableR8.fullMode=false`
      2. Newly added Maven dependencies (`group:artifact:version`) and resolves their `.aar`/`.jar`
         from `~/.gradle/caches/modules-2/files-2.1`.
    """
    diff_failures: list[str] = []
    dep_archives: list[Path] = []

    base_cmd = ["git", "diff", "-U0"]
    if diff_range:
        base_cmd.append(diff_range)
    else:
        base_cmd.append("--cached")

    try:
        pro_diff = subprocess.check_output(
            [*base_cmd, "--", "*.pro", "gradle.properties"],
            text=True,
            stderr=subprocess.DEVNULL,
        )
    except Exception:
        pro_diff = ""

    current_file = "staged file"
    for line in pro_diff.splitlines():
        if line.startswith("+++ b/"):
            current_file = line[6:].strip()
        elif line.startswith("+") and not line.startswith("+++"):
            added = line[1:].strip()
            if re.search(r"android\.enableR8\.fullMode\s*=\s*false", added):
                diff_failures.append(
                    f"Change in `{current_file}` disables R8 Full Mode (`{added}`)"
                )
            elif re.match(
                r"^-keep\s+(class|interface)\s+[a-zA-Z0-9_.]+\.\*\*\s*\{\s*\*\s*;\s*\}",
                added,
            ):
                diff_failures.append(
                    f"Rule in `{current_file}` introduces broad package-wide keep rule: `{added}`"
                )

    try:
        gradle_diff = subprocess.check_output(
            [*base_cmd, "--", "*.gradle.kts", "gradle/libs.versions.toml"],
            text=True,
            stderr=subprocess.DEVNULL,
        )
    except Exception:
        gradle_diff = ""

    coords: set[tuple[str, str, str]] = set()
    for m in re.finditer(
        r'^\+.*["\']([a-zA-Z0-9_.-]+):([a-zA-Z0-9_.-]+):([a-zA-Z0-9_.-]+)["\']',
        gradle_diff,
        re.M,
    ):
        coords.add((m.group(1), m.group(2), m.group(3)))
    for m in re.finditer(
        r'^\+.*group\s*=\s*"([^"]+)"\s*,\s*name\s*=\s*"([^"]+)"\s*,\s*version\s*=\s*"([^"]+)"',
        gradle_diff,
        re.M,
    ):
        coords.add((m.group(1), m.group(2), m.group(3)))

    cache_root = Path.home() / ".gradle" / "caches" / "modules-2" / "files-2.1"
    if cache_root.exists():
        for group, name, version in sorted(coords):
            pattern = str(cache_root / group / name / version / "*" / f"{name}-{version}.*")
            for match in sorted(glob.glob(pattern)):
                if match.endswith((".aar", ".jar")) and not match.endswith(
                    ("-sources.jar", "-javadoc.jar")
                ):
                    dep_archives.append(Path(match))
                    break

    return diff_failures, dep_archives


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--diff-range",
        type=str,
        default=None,
        help="Optional git diff range (e.g. origin/main...HEAD). Defaults to --cached (staged changes).",
    )
    parser.add_argument(
        "--min-lib-shrinking-pct",
        type=float,
        default=90.0,
        help="Minimum required library DEX shrinking percentage for newly added dependencies (default: 90.0)",
    )
    args = parser.parse_args()

    diff_failures, dep_archives = inspect_diff(args.diff_range)

    lib_exit_code = 0
    if dep_archives:
        for dep_path in dep_archives:
            print(f"{BOLD}==> Auditing newly added dependency archive:{RESET} {dep_path.name}")
        cmd = [
            sys.executable,
            str(Path(__file__).parent / "check_library_shrinking.py"),
            "--min-lib-shrinking-pct",
            str(args.min_lib_shrinking_pct),
        ]
        for dep_path in dep_archives:
            cmd.extend(["--library", str(dep_path)])
        lib_exit_code = subprocess.call(cmd)

    if diff_failures:
        print(f"{RED}{BOLD}❌ Staged R8 Diff Check FAILED ({len(diff_failures)} issue(s)):{RESET}")
        for f in diff_failures:
            print(f"  {RED}• {f}{RESET}")
            print(f"  💡 Narrow the rule to specific classes/members or add allowshrinking,allowobfuscation.")
        return 1

    if lib_exit_code != 0:
        return lib_exit_code

    print(f"{GREEN}{BOLD}✅ Staged R8 diff & dependency check passed.{RESET}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
