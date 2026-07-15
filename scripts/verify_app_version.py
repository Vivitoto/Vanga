#!/usr/bin/env python3
"""Verify app version declarations stay consistent."""
from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
VERSIONS_FILE = ROOT / "gradle" / "libs.versions.toml"
APP_VERSION_FILE = ROOT / "vanga-domain" / "core" / "src" / "commonMain" / "kotlin" / "io" / "github" / "vivitoto" / "vanga" / "updates" / "AppVersion.kt"
APP_BUILD_FILE = ROOT / "vanga-app" / "build.gradle.kts"
RELEASE_NOTES_FILE = ROOT / "RELEASE_NOTES.md"
README_FILE = ROOT / "README.md"


def read_text(path: Path) -> str:
    try:
        return path.read_text(encoding="utf-8")
    except OSError as exc:
        raise SystemExit(f"Failed to read {path}: {exc}") from exc


def path_label(path: Path) -> str:
    return path.relative_to(ROOT).as_posix()


def format_version(match: re.Match[str]) -> str:
    return ".".join(match.groups())


def parse_release_notes_top(text: str, errors: list[str]) -> tuple[str | None, str | None]:
    heading = re.match(r"#\s+Vanga\s+(\d+)\.(\d+)\.(\d+)\s*$", text, re.MULTILINE)
    if not heading:
        errors.append(f"Could not find top release notes version in {path_label(RELEASE_NOTES_FILE)}")
        return None, None

    next_heading = re.search(
        r"\n#\s+Vanga\s+\d+\.\d+\.\d+\s*$",
        text[heading.end():],
        re.MULTILINE,
    )
    top_section_end = heading.end() + next_heading.start() if next_heading else len(text)
    top_section = text[:top_section_end]
    version_code = re.search(r"Android\s+versionCode\s+为\s+(\d+)", top_section)
    if not version_code:
        errors.append(f"Could not find top release notes versionCode in {path_label(RELEASE_NOTES_FILE)}")
        return format_version(heading), None

    return format_version(heading), version_code.group(1)


def add_missing_error(errors: list[str], name: str, path: Path) -> None:
    errors.append(f"Could not find {name} in {path_label(path)}")


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

    declared = re.search(r'^app-version\s*=\s*"(\d+)\.(\d+)\.(\d+)"\s*$', versions_text, re.MULTILINE)
    current = re.search(r'val\s+current\s*=\s*AppVersion\((\d+)\s*,\s*(\d+)\s*,\s*(\d+)\)', app_version_text)
    app_version_code = re.search(r"versionCode\s*=\s*(\d+)", app_build_text)
    readme_version = re.search(r"当前版本\s*[:：]\s*`?v?(\d+)\.(\d+)\.(\d+)`?", readme_text)

    errors: list[str] = []
    if not declared:
        add_missing_error(errors, "app-version", VERSIONS_FILE)
    if not current:
        add_missing_error(errors, "AppVersion.current", APP_VERSION_FILE)
    if not app_version_code:
        add_missing_error(errors, "versionCode", APP_BUILD_FILE)
    if not readme_version:
        add_missing_error(errors, "README current version", README_FILE)

    release_notes_version, release_notes_version_code = parse_release_notes_top(release_notes_text, errors)

    app_versions: dict[str, str] = {}
    if declared:
        app_versions[f"{path_label(VERSIONS_FILE)} app-version"] = format_version(declared)
    if current:
        app_versions[f"{path_label(APP_VERSION_FILE)} AppVersion.current"] = format_version(current)
    if release_notes_version:
        app_versions[f"{path_label(RELEASE_NOTES_FILE)} top version"] = release_notes_version
    if readme_version:
        app_versions[f"{path_label(README_FILE)} current version"] = format_version(readme_version)

    version_codes: dict[str, str] = {}
    if app_version_code:
        version_codes[f"{path_label(APP_BUILD_FILE)} versionCode"] = app_version_code.group(1)
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
