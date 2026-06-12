# Vanga full namespace migration

## Decision

Vito confirmed Vanga should be a completely new app identity, not an in-place Vanga-compatible fork identity.

## Target identity

- Product name: `Vanga`
- Android applicationId / namespace: `io.github.vivitoto.vanga`
- Kotlin package root: `io.github.vivitoto.vanga`
- Gradle root project: `Vanga`
- Module directory prefix: `vanga-*`
- Native library prefix: `vanga_*` / `libvanga_*`
- JS bundle: `vanga-app.js`

## Compatibility impact

- Android treats this as a new app, not an update for `io.github.vivitoto.vanga`.
- Existing user data under old app/package paths will not be reused automatically unless a future migration/import feature is added.
- Historical legal/upstream attribution may remain in license/notice docs if required, but product/source identity should not expose Vanga names.

## Verification gates

- `git diff --check`
- no unintended `Vanga`/`vanga`/`VANGA` traces outside explicitly retained legal/history notes
- Gradle project listing and compilation once JDK 17+ is available

## Current environment blocker

This host currently has no Java/JDK configured (`java` not found), so Gradle gates cannot run until JDK 17+ is installed or `JAVA_HOME` is configured.
