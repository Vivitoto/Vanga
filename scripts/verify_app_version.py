#!/usr/bin/env python3
"""Verify Gradle app-version matches AppVersion.current."""
from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
VERSIONS_FILE = ROOT / "gradle" / "libs.versions.toml"
APP_VERSION_FILE = ROOT / "vanga-domain" / "core" / "src" / "commonMain" / "kotlin" / "io" / "github" / "vivitoto" / "vanga" / "updates" / "AppVersion.kt"


def read_text(path: Path) -> str:
    try:
        return path.read_text(encoding="utf-8")
    except OSError as exc:
        raise SystemExit(f"Failed to read {path}: {exc}") from exc


def main() -> int:
    versions_text = read_text(VERSIONS_FILE)
    app_version_text = read_text(APP_VERSION_FILE)

    declared = re.search(r'^app-version\s*=\s*"(\d+)\.(\d+)\.(\d+)"\s*$', versions_text, re.MULTILINE)
    current = re.search(r'val\s+current\s*=\s*AppVersion\((\d+)\s*,\s*(\d+)\s*,\s*(\d+)\)', app_version_text)

    if not declared:
        print(f"Could not find app-version in {VERSIONS_FILE}", file=sys.stderr)
        return 1
    if not current:
        print(f"Could not find AppVersion.current in {APP_VERSION_FILE}", file=sys.stderr)
        return 1

    declared_version = ".".join(declared.groups())
    current_version = ".".join(current.groups())
    if declared_version != current_version:
        print(
            "Version mismatch: "
            f"gradle/libs.versions.toml app-version={declared_version}, "
            f"AppVersion.current={current_version}",
            file=sys.stderr,
        )
        return 1

    print(f"App version OK: {declared_version}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
