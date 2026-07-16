#!/usr/bin/env python3
"""Run release readiness checks for Vanga."""
from __future__ import annotations

import argparse
import re
import shlex
import shutil
import subprocess
import sys
from dataclasses import dataclass
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
VERIFY_SCRIPT = ROOT / "scripts" / "verify_app_version.py"
PUSH_SCRIPT = ROOT / "scripts" / "push-via-api.py"
ANDROID_WORKFLOW = ROOT / ".github" / "workflows" / "android-build.yml"
OFFLINE_USER_FACING_PATHS = (
    ROOT / "vanga-domain" / "offline" / "src" / "commonMain" / "kotlin",
    ROOT / "vanga-domain" / "offline" / "src" / "androidMain" / "kotlin",
)
SOURCE_SUFFIXES = {".java", ".kt", ".kts"}
PLACEHOLDER_PATTERNS = (
    ("TODO(", re.compile(r"\bTODO\s*\(")),
    ("Not yet implemented", re.compile(r"Not yet implemented")),
)


@dataclass(frozen=True)
class CheckResult:
    name: str
    status: str
    details: tuple[str, ...]


def pass_result(name: str, *details: str) -> CheckResult:
    return CheckResult(name, "PASS", details)


def skip_result(name: str, *details: str) -> CheckResult:
    return CheckResult(name, "SKIP", details)


def blocker_result(name: str, *details: str) -> CheckResult:
    return CheckResult(name, "BLOCKER", details)


def warn_result(name: str, *details: str) -> CheckResult:
    return CheckResult(name, "WARN", details)


def path_label(path: Path) -> str:
    return path.relative_to(ROOT).as_posix()


def command_label(args: list[str]) -> str:
    return " ".join(shlex.quote(arg) for arg in args)


def output_lines(stdout: str, stderr: str) -> list[str]:
    output = "\n".join(part.strip() for part in (stdout, stderr) if part.strip())
    return output.splitlines()


def run_command(name: str, args: list[str], timeout_seconds: int) -> CheckResult:
    try:
        result = subprocess.run(
            args,
            cwd=ROOT,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            timeout=timeout_seconds,
        )
    except FileNotFoundError as exc:
        return blocker_result(
            name,
            f"command not found: {exc.filename}",
            f"command: {command_label(args)}",
        )
    except subprocess.TimeoutExpired:
        return blocker_result(
            name,
            f"timed out after {timeout_seconds}s",
            f"command: {command_label(args)}",
        )
    except OSError as exc:
        return blocker_result(name, f"failed to run command: {exc}", f"command: {command_label(args)}")

    lines = [f"command: {command_label(args)}", *output_lines(result.stdout, result.stderr)]
    if result.returncode == 0:
        return pass_result(name, *lines)

    return blocker_result(name, f"exit code: {result.returncode}", *lines)


def check_app_version() -> CheckResult:
    if not VERIFY_SCRIPT.is_file():
        return blocker_result("App version consistency", f"missing {path_label(VERIFY_SCRIPT)}")
    return run_command(
        "App version consistency",
        [sys.executable, path_label(VERIFY_SCRIPT)],
        timeout_seconds=60,
    )


def check_api_push_dry_run(skip_network: bool) -> CheckResult:
    if skip_network:
        return skip_result(
            "GitHub API push dry-run",
            "skipped by --skip-network",
            f"would run: {path_label(PUSH_SCRIPT)} --dry-run",
        )
    if not PUSH_SCRIPT.is_file():
        return blocker_result("GitHub API push dry-run", f"missing {path_label(PUSH_SCRIPT)}")
    return run_command(
        "GitHub API push dry-run",
        [sys.executable, path_label(PUSH_SCRIPT), "--dry-run"],
        timeout_seconds=120,
    )


def source_files_under(path: Path) -> list[Path]:
    return sorted(
        candidate
        for candidate in path.rglob("*")
        if candidate.is_file() and candidate.suffix in SOURCE_SUFFIXES
    )


def check_offline_placeholders() -> CheckResult:
    missing_paths = [path for path in OFFLINE_USER_FACING_PATHS if not path.is_dir()]
    if missing_paths:
        return blocker_result(
            "Offline placeholder scan",
            *[f"missing scan path: {path_label(path)}" for path in missing_paths],
        )

    matches: list[str] = []
    scanned_files = 0
    for base_path in OFFLINE_USER_FACING_PATHS:
        for source_file in source_files_under(base_path):
            scanned_files += 1
            try:
                lines = source_file.read_text(encoding="utf-8").splitlines()
            except UnicodeDecodeError as exc:
                return blocker_result("Offline placeholder scan", f"failed to decode {path_label(source_file)}: {exc}")
            except OSError as exc:
                return blocker_result("Offline placeholder scan", f"failed to read {path_label(source_file)}: {exc}")

            for line_number, line in enumerate(lines, start=1):
                for label, pattern in PLACEHOLDER_PATTERNS:
                    if pattern.search(line):
                        matches.append(f"{path_label(source_file)}:{line_number}: {label}: {line.strip()}")

    if matches:
        return blocker_result(
            "Offline placeholder scan",
            "remove or replace high-risk placeholders:",
            *matches,
        )

    scanned_paths = ", ".join(path_label(path) for path in OFFLINE_USER_FACING_PATHS)
    return pass_result(
        "Offline placeholder scan",
        f"no TODO()/Not yet implemented placeholders found in {scanned_files} files",
        f"paths: {scanned_paths}",
    )


def check_android_workflow() -> CheckResult:
    if ANDROID_WORKFLOW.is_file():
        return pass_result("Android build workflow", f"found {path_label(ANDROID_WORKFLOW)}")
    return blocker_result("Android build workflow", f"missing {path_label(ANDROID_WORKFLOW)}")


def check_java() -> CheckResult:
    java = shutil.which("java")
    if java is None:
        return warn_result(
            "Java availability",
            "java not found on PATH",
            "Android release build steps requiring Java must be skipped until a JDK is installed.",
        )

    try:
        result = subprocess.run(
            [java, "-version"],
            cwd=ROOT,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            timeout=15,
        )
    except subprocess.TimeoutExpired:
        return warn_result("Java availability", "java -version timed out after 15s", f"java: {java}")
    except OSError as exc:
        return warn_result("Java availability", f"failed to run java -version: {exc}", f"java: {java}")

    version_lines = output_lines(result.stdout, result.stderr)
    details = [f"java: {java}", *(version_lines[:3] or ["java -version produced no output"])]
    if result.returncode != 0:
        return warn_result("Java availability", f"exit code: {result.returncode}", *details)
    return pass_result("Java availability", *details)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--skip-network", action="store_true", help="skip GitHub API push dry-run")
    return parser.parse_args()


def print_result(result: CheckResult) -> None:
    print(f"[{result.status}] {result.name}")
    for detail in result.details:
        for line in detail.splitlines() or [""]:
            print(f"  {line}")


def main() -> int:
    args = parse_args()
    results = (
        check_app_version(),
        check_api_push_dry_run(args.skip_network),
        check_offline_placeholders(),
        check_android_workflow(),
        check_java(),
    )

    print("Vanga release doctor")
    print()
    for index, result in enumerate(results):
        if index:
            print()
        print_result(result)

    blockers = [result for result in results if result.status == "BLOCKER"]
    print()
    if blockers:
        noun = "blocker" if len(blockers) == 1 else "blockers"
        print(f"Release doctor found {len(blockers)} {noun}.")
        return 1

    skipped = [result for result in results if result.status == "SKIP"]
    warnings = [result for result in results if result.status == "WARN"]
    notes: list[str] = []
    if skipped:
        noun = "check was" if len(skipped) == 1 else "checks were"
        notes.append(f"{len(skipped)} {noun} skipped")
    if warnings:
        noun = "warning" if len(warnings) == 1 else "warnings"
        notes.append(f"{len(warnings)} {noun}")

    if notes:
        print(f"Release doctor passed with {'; '.join(notes)}.")
    else:
        print("Release doctor passed.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
