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
"""Standalone App Bundle (.aab) R8 Score & DEX Size Regression Gate powered by `android analyze`."""

from __future__ import annotations

import argparse
import json
import os
from pathlib import Path
import subprocess
import sys
from typing import Any

DEFAULT_AAB = "app/build/outputs/bundle/release/app-release.aab"
FALLBACK_AAB = "app/app-release.aab"
DEFAULT_BASELINE = "r8-baseline.json"

RED = "\033[91m"
GREEN = "\033[92m"
YELLOW = "\033[93m"
BOLD = "\033[1m"
RESET = "\033[0m"


def run_android_analyze(artifact_path: Path) -> dict[str, Any]:
    """Invokes `android analyze --analysis=optimization --format=json` on an AAB."""
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


def format_mb(num_bytes: int) -> str:
    return f"{num_bytes / (1024 * 1024):.2f} MB"


def build_markdown_summary(
    cur_bundle: dict[str, Any],
    bundle_rows: list[tuple[str, str, str, str, str, bool]],
    failures: list[str],
    baseline_path: Path,
) -> str:
    lines: list[str] = [
        "## 🛡️ R8 Optimization & Shrinking Gate (`android analyze`)",
        "",
        "### 📦 App Bundle R8 Scores & DEX Size",
        f"- **Package**: `{cur_bundle['package_name']}`",
        f"- **Compiler**: AGP `{cur_bundle['agp_version']}` / R8 `{cur_bundle['r8_version']}` (Full Mode: `{cur_bundle['full_mode']}`)",
        f"- **Baseline**: `{baseline_path}`",
        "",
        "| Metric | Baseline | Current | Delta | Threshold | Status |",
        "| :--- | ---: | ---: | ---: | ---: | :---: |",
    ]
    for label, b_str, c_str, d_str, t_str, failed in bundle_rows:
        icon = "❌ FAIL" if failed else "✅ PASS"
        lines.append(f"| **{label}** | {b_str} | {c_str} | `{d_str}` | `{t_str}` | {icon} |")
    lines.append("")

    if failures:
        lines.append("### ❌ App Bundle R8 Regressions")
        for f in failures:
            lines.append(f"- {f}")
        lines.append("")

    return "\n".join(lines) + "\n"


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--aab",
        type=Path,
        default=None,
        help=f"Path to release AAB (defaults to {DEFAULT_AAB} or {FALLBACK_AAB})",
    )
    parser.add_argument(
        "--baseline",
        type=Path,
        default=Path(DEFAULT_BASELINE),
        help=f"Path to baseline JSON (default: {DEFAULT_BASELINE})",
    )
    parser.add_argument(
        "--save-baseline",
        action="store_true",
        help="Write the current `android analyze` output as the new baseline and exit",
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
        help="Maximum allowed drop in R8 shrinking/optimization/obfuscation score in percentage points (default: 2.0)",
    )
    parser.add_argument(
        "--max-dex-growth-pct",
        type=float,
        default=5.0,
        help="Maximum allowed uncompressed DEX size increase in %% (default: 5.0)",
    )
    parser.add_argument(
        "--max-dex-growth-kb",
        type=float,
        default=500.0,
        help="Maximum allowed uncompressed DEX size increase in KB (default: 500.0)",
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

    aab_path = args.aab
    if aab_path is None:
        if Path(DEFAULT_AAB).exists():
            aab_path = Path(DEFAULT_AAB)
        elif Path(FALLBACK_AAB).exists():
            aab_path = Path(FALLBACK_AAB)
        else:
            print(
                f"{RED}Error: No AAB found at {DEFAULT_AAB} or {FALLBACK_AAB}. "
                f"Pass --aab <path> or --build.{RESET}",
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

    failures: list[str] = []
    summary_rows: list[tuple[str, str, str, str, str, bool]] = []

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
    dex_diff_pct = (dex_diff_bytes / b_dex * 100.0) if b_dex > 0 else 0.0

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

    if base["full_mode"] and not cur_bundle["full_mode"]:
        failures.append("R8 Full Mode was disabled (regressed to ProGuard compatibility mode)")

    print("-" * len(header))
    print()

    md_summary = build_markdown_summary(cur_bundle, summary_rows, failures, args.baseline)
    step_summary_path = os.environ.get("GITHUB_STEP_SUMMARY")
    if step_summary_path:
        with open(step_summary_path, "a", encoding="utf-8") as f:
            f.write(md_summary)
    if args.markdown_summary is not None:
        args.markdown_summary.parent.mkdir(parents=True, exist_ok=True)
        args.markdown_summary.write_text(md_summary, encoding="utf-8")

    if failures:
        print(f"{RED}{BOLD}❌ App Bundle R8 Gate FAILED ({len(failures)} regression(s) detected):{RESET}")
        for f in failures:
            print(f"  {RED}• {f}{RESET}")
        return 1

    print(f"{GREEN}{BOLD}✅ App Bundle R8 Gate PASSED!{RESET}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
