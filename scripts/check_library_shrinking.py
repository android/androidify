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
"""Standalone Library (.aar / .jar) Shrinking & Consumer Keep-Rule Gate powered by `android analyze`."""

from __future__ import annotations

import argparse
import json
import os
from pathlib import Path
import subprocess
import sys
from typing import Any

RED = "\033[91m"
GREEN = "\033[92m"
YELLOW = "\033[93m"
BOLD = "\033[1m"
RESET = "\033[0m"


def run_android_analyze(artifact_path: Path) -> dict[str, Any]:
    """Invokes `android analyze --analysis=optimization --format=json` on an AAR/JAR."""
    env = os.environ.copy()
    env["ANDROID_CLI_ANALYZE"] = "1"

    cmd = [
        "android",
        "analyze",
        "--analysis=optimization",
        "--format=json",
        str(artifact_path),
    ]
    proc = subprocess.run(cmd, env=env, capture_output=True, text=True)
    if proc.returncode != 0:
        print(f"{RED}Error running `{' '.join(cmd)}`:{RESET}", file=sys.stderr)
        print(proc.stderr or proc.stdout, file=sys.stderr)
        sys.exit(2)

    stdout = proc.stdout
    json_start = stdout.find("{")
    if json_start == -1:
        print(f"{RED}Error: No JSON output returned from `android analyze`.{RESET}", file=sys.stderr)
        print(stdout, file=sys.stderr)
        sys.exit(2)

    return json.loads(stdout[json_start:])


def _unwrap_optimization(data: dict[str, Any]) -> dict[str, Any]:
    """Supports both `{"results": [{"optimization": ...}]}` and `{"optimization": ...}`."""
    if "optimization" in data and isinstance(data["optimization"], dict):
        return data["optimization"]
    results = data.get("results", [])
    if isinstance(results, list) and results and isinstance(results[0], dict):
        return results[0].get("optimization", {})
    return {}


def extract_library_metrics(artifact_path: Path, data: dict[str, Any]) -> dict[str, Any]:
    """Extracts library shrinking percentages and consumer keep-rule metrics from AAR/JAR JSON."""
    lib = _unwrap_optimization(data).get("libraryAnalysisResult", {})
    if not lib:
        raise ValueError(
            f"No `libraryAnalysisResult` found for {artifact_path}. Make sure it is an `.aar` or `.jar`."
        )

    dex = lib.get("dex", {})
    d8_bytes = int(dex.get("d8CompileResult", {}).get("dexSizeBytes", 0))
    r8_compile = dex.get("r8CompileResult", {})
    r8_bytes = int(r8_compile.get("dexSizeBytes", 0))

    keep_ratio_pct = (r8_bytes / d8_bytes * 100.0) if d8_bytes > 0 else 0.0
    dex_shrinking_pct = 100.0 - keep_ratio_pct if d8_bytes > 0 else 100.0

    def _item_stats(section_name: str) -> tuple[int, int, float]:
        sec = r8_compile.get(section_name, {})
        total = int(sec.get("itemCount", 0))
        kept = int(sec.get("keptCount", 0))
        shrunk_pct = ((total - kept) / total * 100.0) if total > 0 else 100.0
        return total, kept, shrunk_pct

    cls_total, cls_kept, cls_shrunk_pct = _item_stats("classes")
    mth_total, mth_kept, mth_shrunk_pct = _item_stats("methods")
    fld_total, fld_kept, fld_shrunk_pct = _item_stats("fields")

    validate = dex.get("validateConsumerKeepRulesResult", {})
    used_pkg_wide = int(validate.get("usedPackageWideKeepRules", 0))
    unused_pkg_wide = int(validate.get("unusedPackageWideKeepRules", 0))
    total_pkg_wide = used_pkg_wide + unused_pkg_wide
    blocked_rules = [r.get("value", "") for r in validate.get("blockedKeepRules", [])]

    config_files = r8_compile.get("configuration", {}).get("files", [])
    keep_rules = [r.strip() for f in config_files for r in f.get("keepRules", []) if r.strip()]

    return {
        "name": artifact_path.name,
        "path": str(artifact_path),
        "r8_version": lib.get("r8Version", "N/A"),
        "d8_bytes": d8_bytes,
        "r8_bytes": r8_bytes,
        "keep_ratio_pct": keep_ratio_pct,
        "dex_shrinking_pct": dex_shrinking_pct,
        "cls_total": cls_total,
        "cls_kept": cls_kept,
        "cls_shrunk_pct": cls_shrunk_pct,
        "mth_total": mth_total,
        "mth_kept": mth_kept,
        "mth_shrunk_pct": mth_shrunk_pct,
        "fld_total": fld_total,
        "fld_kept": fld_kept,
        "fld_shrunk_pct": fld_shrunk_pct,
        "used_pkg_wide": used_pkg_wide,
        "unused_pkg_wide": unused_pkg_wide,
        "total_pkg_wide": total_pkg_wide,
        "keep_rules": keep_rules,
        "blocked_rules": blocked_rules,
    }


def format_kb(num_bytes: int) -> str:
    if num_bytes >= 1024 * 1024:
        return f"{num_bytes / (1024 * 1024):.2f} MB"
    return f"{num_bytes / 1024.0:.2f} KB"


def discover_module_aars(root: Path) -> list[Path]:
    """Finds all module release AARs (`**/build/outputs/aar/*-release.aar`) in the repo."""
    return sorted(root.glob("**/build/outputs/aar/*-release.aar"))


def build_markdown_summary(
    lib_metrics_list: list[dict[str, Any]],
    min_lib_shrinking_pct: float,
    fail_on_pkg_rules: bool,
    failures: list[str],
) -> str:
    lines: list[str] = [
        f"### 📚 Library Shrinking Percentage (Min Threshold: `{min_lib_shrinking_pct:.1f}%`)",
        "",
        "| Library (`.aar` / `.jar`) | D8 DEX | R8 Kept DEX | DEX Shrinking % | Classes Shrunk | Methods Shrunk | Pkg-Wide Rules | Status |",
        "| :--- | ---: | ---: | ---: | ---: | ---: | ---: | :---: |",
    ]
    for lm in lib_metrics_list:
        failed = (
            lm["dex_shrinking_pct"] < min_lib_shrinking_pct
            or (fail_on_pkg_rules and lm["total_pkg_wide"] > 0)
            or len(lm["blocked_rules"]) > 0
        )
        warn = (not failed) and lm["total_pkg_wide"] > 0
        icon = "❌ FAIL" if failed else ("⚠️ WARN" if warn else "✅ PASS")
        cls_str = f"{lm['cls_shrunk_pct']:.1f}% ({lm['cls_total'] - lm['cls_kept']}/{lm['cls_total']})"
        mth_str = f"{lm['mth_shrunk_pct']:.1f}% ({lm['mth_total'] - lm['mth_kept']}/{lm['mth_total']})"
        lines.append(
            f"| `{lm['name']}` | {format_kb(lm['d8_bytes'])} | {format_kb(lm['r8_bytes'])} | "
            f"**{lm['dex_shrinking_pct']:.2f}%** | {cls_str} | {mth_str} | "
            f"{lm['total_pkg_wide']} | {icon} |"
        )
    lines.append("")

    if failures:
        lines.append("### ❌ Library Shrinking Regressions")
        for f in failures:
            lines.append(f"- {f}")
        lines.append("")

    return "\n".join(lines) + "\n"


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--library",
        type=Path,
        action="append",
        default=[],
        help="Path to a library (.aar or .jar) to check shrinking percentage (repeatable)",
    )
    parser.add_argument(
        "--scan-libraries",
        action="store_true",
        help="Automatically discover and analyze all module `*-release.aar` files in the workspace",
    )
    parser.add_argument(
        "--min-lib-shrinking-pct",
        type=float,
        default=90.0,
        help="Minimum required library DEX shrinking percentage (100%% - Keep Ratio) for .aar/.jar files (default: 90.0)",
    )
    parser.add_argument(
        "--fail-on-package-wide-rules",
        action="store_true",
        help="Fail the gate if any analyzed library exports package-wide consumer keep rules (`-keep class pkg.** { *; }`)",
    )
    parser.add_argument(
        "--markdown-summary",
        type=Path,
        default=None,
        help="Optional file path to write the Markdown summary report (in addition to $GITHUB_STEP_SUMMARY if set)",
    )
    parser.add_argument(
        "--append-summary",
        action="store_true",
        help="Append to --markdown-summary instead of overwriting",
    )
    args = parser.parse_args()

    lib_paths: list[Path] = list(args.library)
    if args.scan_libraries:
        for p in discover_module_aars(Path(".")):
            if p not in lib_paths:
                lib_paths.append(p)

    if not lib_paths:
        print(
            f"{RED}Error: No libraries specified. Pass --library <path> or --scan-libraries.{RESET}",
            file=sys.stderr,
        )
        return 2

    print()
    print(
        f"{BOLD}Library Shrinking Percentage Gate "
        f"(Min Shrinking: {args.min_lib_shrinking_pct:.1f}%, "
        f"Max Keep Ratio: {100.0 - args.min_lib_shrinking_pct:.1f}%){RESET}"
    )
    lib_header = (
        f"{'Library':<28} {'D8 DEX':>10} {'R8 Kept':>10} "
        f"{'DEX Shrunk %':>13} {'Classes Shrunk':>18} {'Pkg-Wide Rules':>15}  {'Status'}"
    )
    print(BOLD + lib_header + RESET)
    print("-" * len(lib_header))

    failures: list[str] = []
    lib_metrics_list: list[dict[str, Any]] = []

    for lib_path in lib_paths:
        if not lib_path.exists():
            print(f"{RED}Error: Library file not found: {lib_path}{RESET}", file=sys.stderr)
            return 2
        raw_lib = run_android_analyze(lib_path)
        lm = extract_library_metrics(lib_path, raw_lib)
        lib_metrics_list.append(lm)

        shrink_failed = lm["dex_shrinking_pct"] < args.min_lib_shrinking_pct
        pkg_rule_failed = args.fail_on_package_wide_rules and lm["total_pkg_wide"] > 0
        blocked_failed = len(lm["blocked_rules"]) > 0
        failed = shrink_failed or pkg_rule_failed or blocked_failed
        warn = (not failed) and lm["total_pkg_wide"] > 0

        status = (
            f"{RED}FAIL ❌{RESET}"
            if failed
            else (f"{YELLOW}WARN ⚠️{RESET}" if warn else f"{GREEN}PASS ✅{RESET}")
        )
        cls_shrunk_str = (
            f"{lm['cls_shrunk_pct']:.1f}% ({lm['cls_total'] - lm['cls_kept']}/{lm['cls_total']})"
        )
        print(
            f"{lm['name']:<28} {format_kb(lm['d8_bytes']):>10} {format_kb(lm['r8_bytes']):>10} "
            f"{lm['dex_shrinking_pct']:>12.2f}% {cls_shrunk_str:>18} "
            f"{lm['total_pkg_wide']:>15}  {status}"
        )

        if shrink_failed:
            failures.append(
                f"Library `{lm['name']}` DEX shrinking is {lm['dex_shrinking_pct']:.2f}% "
                f"(Keep Ratio: {lm['keep_ratio_pct']:.2f}%, required shrinking >= {args.min_lib_shrinking_pct:.1f}%)"
            )
        if pkg_rule_failed:
            rules_preview = ", ".join(lm["keep_rules"][:3])
            failures.append(
                f"Library `{lm['name']}` exports {lm['total_pkg_wide']} package-wide consumer keep rule(s)"
                + (f": {rules_preview}" if rules_preview else "")
            )
        if blocked_failed:
            failures.append(
                f"Library `{lm['name']}` contains blocked consumer keep rule(s): {', '.join(lm['blocked_rules'])}"
            )

    print("-" * len(lib_header))
    print()

    md_summary = build_markdown_summary(
        lib_metrics_list, args.min_lib_shrinking_pct, args.fail_on_package_wide_rules, failures
    )
    step_summary_path = os.environ.get("GITHUB_STEP_SUMMARY")
    if step_summary_path:
        with open(step_summary_path, "a", encoding="utf-8") as f:
            f.write(md_summary)
    if args.markdown_summary is not None:
        args.markdown_summary.parent.mkdir(parents=True, exist_ok=True)
        mode = "a" if args.append_summary else "w"
        with open(args.markdown_summary, mode, encoding="utf-8") as f:
            f.write(md_summary)

    if failures:
        print(f"{RED}{BOLD}❌ Library Shrinking Gate FAILED ({len(failures)} regression(s) detected):{RESET}")
        for f in failures:
            print(f"  {RED}• {f}{RESET}")
        return 1

    print(f"{GREEN}{BOLD}✅ Library Shrinking Gate PASSED!{RESET}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
