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
"""Presubmit / post-build R8 regression & library shrinking gate powered by `android analyze`."""

from __future__ import annotations

import argparse
import json
import os
import subprocess
import sys
from pathlib import Path
from typing import Any

DEFAULT_AAB = "app/build/outputs/bundle/release/app-release.aab"
FALLBACK_AAB = "app/app-release.aab"
DEFAULT_BASELINE = "r8-baseline.json"

GREEN = "\033[92m"
RED = "\033[91m"
YELLOW = "\033[93m"
BOLD = "\033[1m"
RESET = "\033[0m"


def run_android_analyze(artifact_path: Path) -> dict[str, Any]:
    """Invokes `android analyze --analysis=optimization --format=json` on an AAB/APK/AAR/JAR."""
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

    # Strip any leading progress output (e.g. "Downloading optimization pack...")
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


def extract_bundle_metrics(data: dict[str, Any]) -> dict[str, Any]:
    """Extracts R8 ratios, DEX size, and config flags from `android analyze` bundle JSON."""
    bundle = _unwrap_optimization(data).get("bundleAnalysisResult", {})
    if not bundle:
        raise ValueError(
            "No `bundleAnalysisResult` found in output. Make sure you pass a release `.aab` file."
        )

    r8_json = bundle.get("r8Json", {})
    stats = r8_json.get("stats", {})
    options = r8_json.get("options", {})
    marker = bundle.get("r8DexMarkerMetadata", {})

    return {
        "package_name": bundle.get("packageName", "N/A"),
        "agp_version": bundle.get("agpVersion", "N/A"),
        "r8_version": r8_json.get("version", "N/A"),
        "r8_enabled": marker.get("r8Enabled", False),
        "full_mode": marker.get(
            "fullModeEnabled", not options.get("isProguardCompatibilityModeEnabled", True)
        ),
        "resource_opt": r8_json.get("resourceOptimizationStatus", "UNKNOWN"),
        "shrinking_pct": float(stats.get("shrinkingRatio", 0.0)) * 100.0,
        "optimization_pct": float(stats.get("optimizationRatio", 0.0)) * 100.0,
        "obfuscation_pct": float(stats.get("obfuscationRatio", 0.0)) * 100.0,
        "dex_bytes": int(bundle.get("uncompressedDexSizeBytes", 0)),
    }


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

    def item_shrinking(section_name: str) -> tuple[int, int, float]:
        sec = r8_compile.get(section_name, {})
        total = int(sec.get("itemCount", 0))
        kept = int(sec.get("keptItemCount", 0))
        shrunk_pct = ((total - kept) / total * 100.0) if total > 0 else 100.0
        return kept, total, shrunk_pct

    cls_kept, cls_total, cls_shrunk_pct = item_shrinking("classes")
    mtd_kept, mtd_total, mtd_shrunk_pct = item_shrinking("methods")
    fld_kept, fld_total, fld_shrunk_pct = item_shrinking("fields")

    config = r8_compile.get("configuration", {})
    used_pkg_rules = [r.get("source", "") for r in config.get("usedPackageWideKeepRules", [])]
    unused_pkg_rules = [r.get("source", "") for r in config.get("unusedPackageWideKeepRules", [])]
    keep_rules = config.get("keepRules", [])

    validate_res = dex.get("validateConsumerKeepRulesResult", {})
    blocked_rules = [r.get("source", "") for r in validate_res.get("blockedKeepRules", [])]

    return {
        "name": artifact_path.name,
        "path": str(artifact_path),
        "r8_version": lib.get("r8Version", "N/A"),
        "d8_bytes": d8_bytes,
        "r8_bytes": r8_bytes,
        "keep_ratio_pct": keep_ratio_pct,
        "dex_shrinking_pct": dex_shrinking_pct,
        "classes_kept": cls_kept,
        "classes_total": cls_total,
        "classes_shrinking_pct": cls_shrunk_pct,
        "methods_kept": mtd_kept,
        "methods_total": mtd_total,
        "methods_shrinking_pct": mtd_shrunk_pct,
        "fields_kept": fld_kept,
        "fields_total": fld_total,
        "fields_shrinking_pct": fld_shrunk_pct,
        "used_pkg_rules": used_pkg_rules,
        "unused_pkg_rules": unused_pkg_rules,
        "keep_rules": keep_rules,
        "blocked_rules": blocked_rules,
    }


def format_mb(num_bytes: int) -> str:
    return f"{num_bytes / (1024 * 1024):.2f} MB"


def format_kb(num_bytes: int) -> str:
    if num_bytes >= 1024 * 1024:
        return format_mb(num_bytes)
    return f"{num_bytes / 1024.0:.2f} KB"


def discover_module_aars(root: Path) -> list[Path]:
    """Finds all module release AARs (`**/build/outputs/aar/*-release.aar`) in the repo."""
    return sorted(root.glob("**/build/outputs/aar/*-release.aar"))


def build_markdown_summary(
    cur_bundle: dict[str, Any] | None,
    bundle_rows: list[tuple[str, str, str, str, str, bool]],
    lib_metrics_list: list[dict[str, Any]],
    min_lib_shrinking_pct: float,
    fail_on_pkg_rules: bool,
    failures: list[str],
    baseline_path: Path,
) -> str:
    """Builds a Markdown summary table for GitHub Actions / PR comments."""
    lines: list[str] = ["## 🛡️ R8 Optimization & Shrinking Gate (`android analyze`)", ""]

    if cur_bundle is not None:
        lines.extend(
            [
                "### 📦 App Bundle R8 Scores & DEX Size",
                f"- **Package**: `{cur_bundle['package_name']}`",
                f"- **Compiler**: AGP `{cur_bundle['agp_version']}` / R8 `{cur_bundle['r8_version']}` (Full Mode: `{cur_bundle['full_mode']}`)",
                f"- **Baseline**: `{baseline_path}`",
                "",
                "| Metric | Baseline | Current | Delta | Threshold | Status |",
                "| :--- | ---: | ---: | ---: | ---: | :---: |",
            ]
        )
        for label, b_str, c_str, delta_str, thresh_str, failed in bundle_rows:
            status_md = "❌ FAIL" if failed else "✅ PASS"
            lines.append(
                f"| **{label}** | {b_str} | {c_str} | `{delta_str}` | `{thresh_str}` | {status_md} |"
            )
        lines.append("")

    if lib_metrics_list:
        lines.extend(
            [
                f"### 📚 Library Shrinking Percentage (Min Threshold: `{min_lib_shrinking_pct:.1f}%`)",
                "",
                "| Library (`.aar` / `.jar`) | D8 DEX | R8 Kept DEX | DEX Shrinking % | Classes Shrunk | Methods Shrunk | Pkg-Wide Rules | Status |",
                "| :--- | ---: | ---: | ---: | ---: | ---: | ---: | :---: |",
            ]
        )
        for lm in lib_metrics_list:
            pkg_rule_count = len(lm["used_pkg_rules"]) + len(lm["unused_pkg_rules"])
            failed = (
                lm["dex_shrinking_pct"] < min_lib_shrinking_pct
                or len(lm["blocked_rules"]) > 0
                or (fail_on_pkg_rules and pkg_rule_count > 0)
            )
            warn = not failed and pkg_rule_count > 0
            status_md = "❌ FAIL" if failed else ("⚠️ WARN" if warn else "✅ PASS")
            cls_str = f"{lm['classes_shrinking_pct']:.1f}% ({lm['classes_total'] - lm['classes_kept']}/{lm['classes_total']})"
            mtd_str = f"{lm['methods_shrinking_pct']:.1f}% ({lm['methods_total'] - lm['methods_kept']}/{lm['methods_total']})"
            lines.append(
                f"| `{lm['name']}` | {format_kb(lm['d8_bytes'])} | {format_kb(lm['r8_bytes'])} | "
                f"**{lm['dex_shrinking_pct']:.2f}%** | {cls_str} | {mtd_str} | {pkg_rule_count} | {status_md} |"
            )
        lines.append("")

    if failures:
        lines.append(f"### ❌ Gate Failed ({len(failures)} regression(s) detected)")
        for item in failures:
            lines.append(f"- {item}")
        lines.append("")
        lines.append(
            "> 💡 **Next Step:** Download the `r8-config-analyzer-report` artifact from this workflow run "
            "or run `./gradlew :app:analyzeReleaseR8Config` locally to inspect the keep rules with the largest blast radius."
        )
    else:
        lines.append("### ✅ Gate Passed! All R8 scores, library shrinking ratios, and DEX sizes are within thresholds.")

    return "\n".join(lines) + "\n"


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Check release AAB R8 scores and library (.aar/.jar) shrinking percentages using `android analyze`."
    )
    parser.add_argument(
        "--aab",
        type=Path,
        default=None,
        help=f"Path to release .aab (defaults to {DEFAULT_AAB} or {FALLBACK_AAB})",
    )
    parser.add_argument(
        "--skip-app",
        action="store_true",
        help="Skip the app bundle (.aab) check and only run library (.aar/.jar) checks",
    )
    parser.add_argument(
        "--baseline",
        type=Path,
        default=Path(DEFAULT_BASELINE),
        help=f"Path to baseline JSON file (default: {DEFAULT_BASELINE})",
    )
    parser.add_argument(
        "--save-baseline",
        action="store_true",
        help="Write the current `android analyze` AAB output to --baseline and exit",
    )
    parser.add_argument(
        "--build",
        action="store_true",
        help="Run `./gradlew :app:bundleRelease` before analyzing",
    )
    parser.add_argument(
        "--max-score-drop",
        type=float,
        default=2.0,
        help="Max allowed drop in R8 shrinking/optimization/obfuscation score in percentage points (default: 2.0)",
    )
    parser.add_argument(
        "--max-dex-growth-pct",
        type=float,
        default=5.0,
        help="Max allowed uncompressed DEX size increase in percent (default: 5.0)",
    )
    parser.add_argument(
        "--max-dex-growth-kb",
        type=float,
        default=500.0,
        help="Max allowed uncompressed DEX size increase in KB (default: 500.0)",
    )
    parser.add_argument(
        "--library",
        type=Path,
        action="append",
        default=[],
        help="Path to a library (.aar or .jar) to check shrinking percentage (can be specified multiple times)",
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
    args = parser.parse_args()

    if args.build:
        print(f"{BOLD}==> Building release app bundle (:app:bundleRelease)...{RESET}")
        subprocess.run(["./gradlew", ":app:bundleRelease"], check=True)

    failures: list[str] = []
    summary_rows: list[tuple[str, str, str, str, str, bool]] = []
    cur_bundle: dict[str, Any] | None = None

    # 1. App Bundle (.aab) R8 Score & DEX Check
    if not args.skip_app:
        aab_path = args.aab
        if aab_path is None:
            if Path(DEFAULT_AAB).exists():
                aab_path = Path(DEFAULT_AAB)
            elif Path(FALLBACK_AAB).exists():
                aab_path = Path(FALLBACK_AAB)
            else:
                print(
                    f"{RED}Error: No AAB found at {DEFAULT_AAB} or {FALLBACK_AAB}. "
                    f"Pass --aab <path>, --build, or --skip-app.{RESET}",
                    file=sys.stderr,
                )
                return 2

        print(f"{BOLD}==> Running `ANDROID_CLI_ANALYZE=1 android analyze` on {aab_path}...{RESET}")
        current_raw = run_android_analyze(aab_path)

        if args.save_baseline:
            args.baseline.write_text(json.dumps(current_raw, indent=2) + "\n")
            cur = extract_bundle_metrics(current_raw)
            print(
                f"{GREEN}✓ Saved R8 baseline to {args.baseline}{RESET} "
                f"(Shrinking: {cur['shrinking_pct']:.2f}%, "
                f"Optimization: {cur['optimization_pct']:.2f}%, "
                f"Obfuscation: {cur['obfuscation_pct']:.2f}%, "
                f"DEX: {format_mb(cur['dex_bytes'])})"
            )
            return 0

        if not args.baseline.exists():
            print(
                f"{RED}Error: Baseline file {args.baseline} not found. "
                f"Run with --save-baseline first.{RESET}",
                file=sys.stderr,
            )
            return 2

        baseline_raw = json.loads(args.baseline.read_text())
        base = extract_bundle_metrics(baseline_raw)
        cur_bundle = extract_bundle_metrics(current_raw)

        print()
        print(f"{BOLD}R8 Optimization & DEX Regression Gate{RESET}")
        print(f"  Package : {cur_bundle['package_name']}")
        print(
            f"  Compiler: AGP {cur_bundle['agp_version']} / R8 {cur_bundle['r8_version']} "
            f"(Full Mode: {cur_bundle['full_mode']})"
        )
        print(f"  Baseline: {args.baseline}")
        print()

        header = f"{'Metric':<24} {'Baseline':>12} {'Current':>12} {'Delta':>18} {'Threshold':>16}  {'Status'}"
        print(BOLD + header + RESET)
        print("-" * len(header))

        score_checks = [
            ("Shrinking Score", "shrinking_pct"),
            ("Optimization Score", "optimization_pct"),
            ("Obfuscation Score", "obfuscation_pct"),
        ]

        for label, key in score_checks:
            b_val = base[key]
            c_val = cur_bundle[key]
            delta = c_val - b_val
            failed = delta < -args.max_score_drop
            status = f"{RED}FAIL ❌{RESET}" if failed else f"{GREEN}PASS ✅{RESET}"
            delta_str = f"{delta:+.2f}% pts"
            thresh_str = f"max -{args.max_score_drop:.2f}% pts"
            print(f"{label:<24} {b_val:>11.2f}% {c_val:>11.2f}% {delta_str:>18} {thresh_str:>16}  {status}")
            summary_rows.append((label, f"{b_val:.2f}%", f"{c_val:.2f}%", delta_str, thresh_str, failed))
            if failed:
                failures.append(
                    f"{label} dropped by {abs(delta):.2f}% pts ({b_val:.2f}% -> {c_val:.2f}%, allowed drop: {args.max_score_drop:.2f}% pts)"
                )

        b_dex = base["dex_bytes"]
        c_dex = cur_bundle["dex_bytes"]
        dex_diff_bytes = c_dex - b_dex
        dex_diff_kb = dex_diff_bytes / 1024.0
        dex_diff_pct = ((c_dex - b_dex) / b_dex * 100.0) if b_dex > 0 else 0.0

        dex_failed = (dex_diff_pct > args.max_dex_growth_pct) or (dex_diff_kb > args.max_dex_growth_kb)
        dex_status = f"{RED}FAIL ❌{RESET}" if dex_failed else f"{GREEN}PASS ✅{RESET}"
        dex_delta_str = f"{dex_diff_bytes / (1024 * 1024):+.2f} MB ({dex_diff_pct:+.1f}%)"
        dex_thresh_str = f"max +{args.max_dex_growth_pct:.1f}% / +{args.max_dex_growth_kb:.0f}KB"
        print(
            f"{'Uncompressed DEX Size':<24} {format_mb(b_dex):>12} {format_mb(c_dex):>12} "
            f"{dex_delta_str:>18} {dex_thresh_str:>16}  {dex_status}"
        )
        summary_rows.append(
            ("Uncompressed DEX Size", format_mb(b_dex), format_mb(c_dex), dex_delta_str, dex_thresh_str, dex_failed)
        )
        if dex_failed:
            failures.append(
                f"Uncompressed DEX size grew by {dex_diff_kb:+.1f} KB / {dex_diff_pct:+.2f}% "
                f"({format_mb(b_dex)} -> {format_mb(c_dex)})"
            )

        print("-" * len(header))

    # 2. Library (.aar / .jar) Shrinking Percentage Check
    lib_paths: list[Path] = list(args.library)
    if args.scan_libraries:
        for p in discover_module_aars(Path(".")):
            if p not in lib_paths:
                lib_paths.append(p)

    lib_metrics_list: list[dict[str, Any]] = []
    if lib_paths:
        print()
        print(
            f"{BOLD}Library Shrinking Percentage Gate "
            f"(Min Shrinking: {args.min_lib_shrinking_pct:.1f}%, Max Keep Ratio: {100.0 - args.min_lib_shrinking_pct:.1f}%){RESET}"
        )
        lib_header = (
            f"{'Library':<28} {'D8 DEX':>10} {'R8 Kept':>10} {'DEX Shrunk %':>13} "
            f"{'Classes Shrunk':>18} {'Pkg-Wide Rules':>15}  {'Status'}"
        )
        print(BOLD + lib_header + RESET)
        print("-" * len(lib_header))

        for lib_path in lib_paths:
            if not lib_path.exists():
                print(f"{RED}Error: Library file not found at {lib_path}{RESET}", file=sys.stderr)
                return 2
            raw_lib = run_android_analyze(lib_path)
            lm = extract_library_metrics(lib_path, raw_lib)
            lib_metrics_list.append(lm)

            pkg_rule_count = len(lm["used_pkg_rules"]) + len(lm["unused_pkg_rules"])
            failed = (
                lm["dex_shrinking_pct"] < args.min_lib_shrinking_pct
                or len(lm["blocked_rules"]) > 0
                or (args.fail_on_package_wide_rules and pkg_rule_count > 0)
            )
            warn = not failed and pkg_rule_count > 0
            status = (
                f"{RED}FAIL ❌{RESET}"
                if failed
                else (f"{YELLOW}WARN ⚠️{RESET}" if warn else f"{GREEN}PASS ✅{RESET}")
            )

            cls_shrunk_count = lm["classes_total"] - lm["classes_kept"]
            cls_str = f"{lm['classes_shrinking_pct']:5.1f}% ({cls_shrunk_count}/{lm['classes_total']})"
            print(
                f"{lm['name']:<28} {format_kb(lm['d8_bytes']):>10} {format_kb(lm['r8_bytes']):>10} "
                f"{lm['dex_shrinking_pct']:>12.2f}% {cls_str:>18} {pkg_rule_count:>15}  {status}"
            )

            if lm["dex_shrinking_pct"] < args.min_lib_shrinking_pct:
                failures.append(
                    f"Library `{lm['name']}` DEX shrinking is {lm['dex_shrinking_pct']:.2f}% "
                    f"(Keep Ratio: {lm['keep_ratio_pct']:.2f}%, required shrinking >= {args.min_lib_shrinking_pct:.1f}%)"
                )
            if lm["blocked_rules"]:
                failures.append(
                    f"Library `{lm['name']}` contains {len(lm['blocked_rules'])} blocked consumer keep rule(s): "
                    f"{', '.join(lm['blocked_rules'][:3])}"
                )
            if args.fail_on_package_wide_rules and pkg_rule_count > 0:
                sample_rules = (lm["used_pkg_rules"] + lm["unused_pkg_rules"])[:3]
                failures.append(
                    f"Library `{lm['name']}` exports {pkg_rule_count} package-wide consumer keep rule(s): "
                    f"{', '.join(sample_rules)}"
                )

        print("-" * len(lib_header))

    md_text = build_markdown_summary(
        cur_bundle,
        summary_rows,
        lib_metrics_list,
        args.min_lib_shrinking_pct,
        args.fail_on_package_wide_rules,
        failures,
        args.baseline,
    )
    if args.markdown_summary:
        args.markdown_summary.parent.mkdir(parents=True, exist_ok=True)
        args.markdown_summary.write_text(md_text, encoding="utf-8")

    gh_summary = os.environ.get("GITHUB_STEP_SUMMARY")
    if gh_summary:
        gh_summary_path = Path(gh_summary)
        if not args.markdown_summary or gh_summary_path.resolve() != args.markdown_summary.resolve():
            with gh_summary_path.open("a", encoding="utf-8") as f:
                f.write(md_text)

    if failures:
        print(f"\n{RED}{BOLD}❌ R8 Presubmit Gate FAILED ({len(failures)} regression(s) detected):{RESET}")
        for item in failures:
            print(f"  • {item}")
        print(
            f"\n{YELLOW}💡 Tip: Run `./gradlew :app:analyzeReleaseR8Config` to inspect the keep rules "
            f"with the largest blast radius.{RESET}"
        )
        return 1

    print(
        f"\n{GREEN}{BOLD}✅ R8 Presubmit Gate PASSED! All R8 scores, library shrinking ratios, "
        f"and DEX sizes are within thresholds.{RESET}"
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())
