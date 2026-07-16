#!/usr/bin/env python3
"""Verify app version declarations stay consistent."""
from __future__ import annotations

import re
import sys
from collections.abc import Callable
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
VERSIONS_FILE = ROOT / "gradle" / "libs.versions.toml"
APP_VERSION_FILE = ROOT / "vanga-domain" / "core" / "src" / "commonMain" / "kotlin" / "io" / "github" / "vivitoto" / "vanga" / "updates" / "AppVersion.kt"
APP_BUILD_FILE = ROOT / "vanga-app" / "build.gradle.kts"
RELEASE_NOTES_FILE = ROOT / "RELEASE_NOTES.md"
README_FILE = ROOT / "README.md"
SEMVER_PATTERN = r"\d+\.\d+\.\d+"


def read_text(path: Path) -> str:
    try:
        return path.read_text(encoding="utf-8")
    except OSError as exc:
        raise SystemExit(f"Failed to read {path}: {exc}") from exc


def path_label(path: Path) -> str:
    return path.relative_to(ROOT).as_posix()


def format_app_version(match: re.Match[str]) -> str:
    return ".".join(match.group(name) for name in ("major", "minor", "patch"))


def add_missing_error(errors: list[str], field: str) -> None:
    errors.append(f"Missing or malformed field: {field}")


def add_duplicate_error(errors: list[str], field: str, count: int) -> None:
    errors.append(f"Duplicate field: {field} ({count} matches)")


def extract_single(
    text: str,
    pattern: str,
    field: str,
    errors: list[str],
    formatter: Callable[[re.Match[str]], str] | None = None,
) -> str | None:
    matches = list(re.finditer(pattern, text, re.MULTILINE))
    if not matches:
        add_missing_error(errors, field)
        return None
    if len(matches) > 1:
        add_duplicate_error(errors, field, len(matches))

    match = matches[0]
    if formatter is not None:
        return formatter(match)
    return match.group("value")


def parse_release_notes_top(text: str, errors: list[str]) -> tuple[str | None, str | None]:
    heading_pattern = re.compile(rf"^# Vanga (?P<value>{SEMVER_PATTERN})(?:\s.*)?$")
    first_line = text.splitlines()[0] if text else ""
    heading = heading_pattern.match(first_line)
    if not heading:
        add_missing_error(
            errors,
            f"{path_label(RELEASE_NOTES_FILE)} top heading/top version (expected first line to start with '# Vanga X.Y.Z')",
        )
        return None, None

    next_heading = re.search(
        rf"\n# Vanga {SEMVER_PATTERN}(?:\s.*)?$",
        text[heading.end():],
        re.MULTILINE,
    )
    top_section_end = heading.end() + next_heading.start() if next_heading else len(text)
    top_section = text[:top_section_end]
    version_code = extract_single(
        top_section,
        r"Android\s+versionCode\s+为\s+(?P<value>\d+)",
        f"{path_label(RELEASE_NOTES_FILE)} top versionCode",
        errors,
    )

    return heading.group("value"), version_code


def add_mismatch_errors(errors: list[str], title: str, values: dict[str, str]) -> None:
    if len(set(values.values())) <= 1:
        return

    details = "\n".join(f"  {name}: {value}" for name, value in values.items())
    errors.append(f"{title} mismatch:\n{details}")


def main() -> int:
    versions_text = read_text(VERSIONS_FILE)
    app_version_text = read_text(APP_VERSION_FILE)
    app_build_text = read_text(APP_BUILD_FILE)
    release_notes_text = read_text(RELEASE_NOTES_FILE)
    readme_text = read_text(README_FILE)

    errors: list[str] = []
    declared = extract_single(
        versions_text,
        rf'^app-version\s*=\s*["\'](?P<value>{SEMVER_PATTERN})["\']\s*$',
        f"{path_label(VERSIONS_FILE)} app-version",
        errors,
    )
    current = extract_single(
        app_version_text,
        r"val\s+current\s*=\s*AppVersion\s*\(\s*(?P<major>\d+)\s*,\s*(?P<minor>\d+)\s*,\s*(?P<patch>\d+)\s*\)",
        f"{path_label(APP_VERSION_FILE)} AppVersion.current",
        errors,
        format_app_version,
    )
    app_version_code = extract_single(
        app_build_text,
        r"^\s*versionCode\s*=\s*(?P<value>\d+)\s*$",
        f"{path_label(APP_BUILD_FILE)} versionCode",
        errors,
    )
    readme_version = extract_single(
        readme_text,
        rf"当前版本\s*[:：]\s*`?v?(?P<value>{SEMVER_PATTERN})`?",
        f"{path_label(README_FILE)} current version",
        errors,
    )

    release_notes_version, release_notes_version_code = parse_release_notes_top(release_notes_text, errors)

    app_versions: dict[str, str] = {}
    if declared:
        app_versions[f"{path_label(VERSIONS_FILE)} app-version"] = declared
    if current:
        app_versions[f"{path_label(APP_VERSION_FILE)} AppVersion.current"] = current
    if release_notes_version:
        app_versions[f"{path_label(RELEASE_NOTES_FILE)} top version"] = release_notes_version
    if readme_version:
        app_versions[f"{path_label(README_FILE)} current version"] = readme_version

    version_codes: dict[str, str] = {}
    if app_version_code:
        version_codes[f"{path_label(APP_BUILD_FILE)} versionCode"] = app_version_code
    if release_notes_version_code:
        version_codes[f"{path_label(RELEASE_NOTES_FILE)} top versionCode"] = release_notes_version_code

    add_mismatch_errors(errors, "App version", app_versions)
    add_mismatch_errors(errors, "Android versionCode", version_codes)

    if errors:
        print("App version verification failed:", file=sys.stderr)
        for error in errors:
            print(f"- {error}", file=sys.stderr)
        return 1

    app_version = next(iter(app_versions.values()))
    version_code = next(iter(version_codes.values()))
    print(f"App version OK: {app_version}, Android versionCode {version_code}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
