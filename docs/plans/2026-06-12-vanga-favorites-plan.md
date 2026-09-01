# Vanga Favorites Implementation Plan

**Date:** 2026-06-12  
**Design:** `docs/plans/2026-06-12-vanga-favorites-design.md`  
**Status:** Draft / Ready for local execution

---

## Current Baseline

- Repo: `/home/vito/.hermes/workspace/Vanga`
- Base: `Vivitoto/Vanga` at `58c97bc`
- Branch: `vanga/favorites`
- Target: Android app only
- Verification blocker found: no local `java`/`JAVA_HOME`; Gradle cannot run until JDK is installed/configured.

---

## Execution Rules

- No GitHub push, release, APK publishing, Docker, or R2 upload without Vito confirmation.
- Keep upstream Vanga structure intact; add Vanga behavior with minimal invasive edits.
- Server sync source of truth: Komga Collection/ReadList.
- TDD where practical: service logic gets unit tests before implementation.
- For UI-only wiring where test harness is absent, use compile/build checks and targeted inspection.

---

## Task 0 — Restore Local Build Capability

**Goal:** Gradle commands can run locally.

1. Inspect whether JDK/Android SDK exists.
2. If absent, ask Vito before installing/configuring JDK.
3. Re-run:
   ```bash
   ./gradlew tasks --all --console=plain
   ```
4. Record available Android build/test tasks.

**Verify:** Gradle task listing succeeds.

---

## Task 1 — Rebrand App Display Name to Vanga

**Goal:** Android launcher/app name shows Vanga.

Files:
- `vanga-app/src/androidMain/res/values/strings.xml`

Steps:
1. Change `app_name` from `Vanga` to `Vanga`.
2. Search for Android-facing `Vanga` labels that should become `Vanga`.
3. Do not rename modules/packages yet.

**Verify:** grep confirms app name string is Vanga; Gradle resource check/build when available.

---

## Task 2 — Add Favorites Domain Contracts and Name Strategy

**Goal:** Introduce pure domain layer for Vanga favorites, independent of UI.

Files:
- `vanga-domain/core/src/commonMain/kotlin/snd/vanga/favorites/FavoriteContainerNames.kt`
- `vanga-domain/core/src/commonMain/kotlin/snd/vanga/favorites/FavoriteSyncError.kt`
- `vanga-domain/core/src/commonMain/kotlin/snd/vanga/favorites/FavoriteState.kt`

Steps:
1. Write tests for name generation:
   - user email `vito@example.com` → `Favorites - vito@example.com` or normalized policy chosen below
   - blank/missing user → fallback `Favorites`
   - Book list name → `Favorite Books - <user>`
2. Implement name helpers.
3. Define sealed error/state types.

Decision: use user email from Komga `UserDto.email` because it is exposed and human-readable. If unavailable, fallback to user id.

**Verify:** Unit tests pass once Gradle is available; otherwise static inspection.

---

## Task 3 — Implement FavoriteCollectionService for Series Favorites

**Goal:** Series favorites map to Komga Collection.

Files:
- `vanga-domain/core/src/commonMain/kotlin/snd/vanga/favorites/FavoriteCollectionService.kt`
- tests under matching commonTest path if test source set is available

Behavior:
- `getFavoriteCollection()` finds collection by exact generated name.
- `getFavoriteSeriesIds()` returns empty set if container absent.
- `isFavorite(seriesId)` checks set membership.
- `addFavorite(seriesId)` creates Collection if absent; otherwise fetch-latest + merge + PATCH.
- `removeFavorite(seriesId)` fetches latest; removes; if empty deletes container.
- `toggleFavorite(seriesId)` delegates add/remove.
- Retry once on failed PATCH after a fresh fetch.

**Verify:** Fake API tests cover absent, create, update, delete-last, no-op duplicate add, no-op remove missing.

---

## Task 4 — Implement FavoriteReadListService for Book Favorites

**Goal:** Book favorites map to Komga ReadList.

Files:
- `vanga-domain/core/src/commonMain/kotlin/snd/vanga/favorites/FavoriteReadListService.kt`

Behavior mirrors Task 3:
- Container name: `Favorite Books - <user>`
- `bookIds` instead of `seriesIds`
- Create with `ordered=true`
- Delete when last book removed

**Verify:** Fake API tests mirror Series favorites tests.

---

## Task 5 — Add Local Cache Repository

**Goal:** Fast local favorites state and weak/offline display.

Files:
- `vanga-domain/core/src/commonMain/kotlin/snd/vanga/favorites/FavoriteCacheRepository.kt`
- `vanga-infra/database/sqlite/.../favorites/*`

Behavior:
- Store server URL + user id + container id/name + id list + last synced timestamp.
- Cache is derived; all writes follow server success.
- On server fetch failure, UI may use stale cache with warning.

**Verify:** Repository CRUD tests if database test harness exists; otherwise compile check + code review.

---

## Task 6 — Wire Dependency Injection

**Goal:** App can construct favorites services/ViewModels.

Files:
- `vanga-ui/src/commonMain/kotlin/snd/vanga/ui/DependencyContainer.kt`
- `vanga-ui/src/commonMain/kotlin/snd/vanga/ui/ViewModelFactory.kt`
- app DI/bootstrap files as needed

Steps:
1. Add favorites services to container or construct in factory from existing `komgaApi` + auth state.
2. Ensure offline mode gives read-only/stale cache behavior.
3. Preserve existing Vanga offline dependencies.

**Verify:** compile/build.

---

## Task 7 — Add Favorites Screen

**Goal:** User can view all favorites.

Files:
- `vanga-ui/src/commonMain/kotlin/snd/vanga/ui/favorites/FavoritesScreen.kt`
- `FavoritesViewModel.kt`
- `FavoritesContent.kt`
- `FavoritesSeriesTab.kt`
- `FavoritesBooksTab.kt`

UI:
- Title: `Favorites`
- Tabs: `Series` and `Books`
- Empty state: `No favorite series yet` / `No favorite books yet`
- Permission state: `Favorites sync requires Komga admin access`
- Error state: show stale cache if available, otherwise retry button.

**Verify:** compile/build; manual navigation after APK install.

---

## Task 8 — Add Navigation Entry

**Goal:** Favorites screen is reachable.

Files:
- `MainScreen.kt`
- `topbar/NavigationMenuContent.kt`

Steps:
1. Add bottom nav item on Android mobile: Favorites.
2. Add drawer/sidebar item on larger layouts.
3. Highlight selected state when current screen is `FavoritesScreen`.

**Verify:** compile/build; manual UI check.

---

## Task 9 — Add Reusable FavoriteButton

**Goal:** Shared star UI component.

Files:
- `ui/favorites/FavoriteButton.kt`

Behavior:
- Empty star when not favorite.
- Filled star when favorite.
- Loading indicator or disabled state while toggling.
- Permission denied toast/error when non-admin.

**Verify:** compile/build; static preview if feasible.

---

## Task 10 — Add Series Favorite Entry Points

**Goal:** Series can be favorited/unfavorited.

Files:
- Series cards/list content
- `SeriesScreen` / detail content

Steps:
1. Add star icon to Series grid/list cards.
2. Add star button to Series detail toolbar/header.
3. Refresh favorites state after toggle.

**Verify:** compile/build; manual test with Komga server.

---

## Task 11 — Add Book Favorite Entry Points

**Goal:** Individual books can be favorited/unfavorited.

Files:
- Book cards/list content
- `BookScreenContent.kt`
- Reader title bar where current book is known

Steps:
1. Add star icon to book detail/header.
2. Add optional star on book cards where space allows.
3. Add reader toolbar action: favorite current book.

**Verify:** compile/build; manual test with Komga server.

---

## Task 12 — Home Screen Favorites Section

**Goal:** Fast access from home.

Files:
- `home/HomeViewModel.kt`
- `home/HomeContent.kt`

Behavior:
- Show recent favorite series (top 6–8) from cache/server.
- `View All` opens Favorites screen.
- If empty, no section or friendly empty prompt.

**Verify:** compile/build; manual UI check.

---

## Task 13 — Manual Komga Integration Test

**Goal:** Validate actual server sync semantics.

Prereqs:
- Vito provides/authorizes test Komga server credentials or uses local test server.
- Admin account preferred.

Test cases:
1. Login.
2. Favorite a series.
3. Verify Komga Web UI has `Favorites - <user>` Collection.
4. Favorite a book.
5. Verify Komga Web UI has `Favorite Books - <user>` ReadList.
6. Restart app / clear cache; verify favorites reload from server.
7. Remove last favorite; verify container deletion.
8. Non-admin account shows clear write-denied message.

**Verify:** documented manual test results.

---

## Build / Verification Commands

Once JDK is available:

```bash
cd /home/vito/.hermes/workspace/Vanga
./gradlew tasks --all --console=plain
./gradlew :vanga-domain:core:allTests --console=plain
./gradlew :vanga-ui:compileKotlinAndroid --console=plain
./gradlew :vanga-app:assembleDebug --console=plain
```

Task names may differ; Task 0 records exact available tasks.

---

## Stop Points

Stop and ask Vito before:
- Installing JDK/Android SDK system packages
- Renaming application package id
- Pushing to GitHub
- Creating release/APK distribution
- Modifying Komga server
- Adding external sync service
