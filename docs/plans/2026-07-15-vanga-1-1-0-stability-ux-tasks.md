# Vanga 1.1.0 Stability UX Implementation Plan

> **For implementer:** Use TDD throughout. Write failing test first. Watch it fail. Then implement. If local Gradle cannot run because Java/JDK is unavailable, still write tests and run all available Python/static checks; final Gradle/build verification must happen in GitHub Actions after confirmed API push.

**Goal:** Deliver Vanga 1.1.0 as one large release improving offline downloaded reading, reader resilience, Komf book metadata behavior, book/series list UX, and release pipeline safety.

**Architecture:** Keep the existing module boundaries. Offline download/read behavior stays in `vanga-domain/offline` plus UI state/view layers. Komf book-level behavior must be implemented as a separate capability probe/wrapper and must not call series identify as a fake fallback. Version/release hardening lives under `scripts/` and CI-facing metadata files.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, Komga client APIs, Komf client/API, Python release scripts, GitHub Actions.

---

## Global Constraints

- Do not push, release, upload, or create tags without Vito's explicit confirmation.
- Vanga pushes must use `scripts/push-via-api.py`; do not rely on `git push` / `git fetch` from this workstation.
- Unsupported offline admin actions should be hidden/removed from UI. Domain/API layers still need explicit unsupported protection so old paths cannot crash via `TODO()`.
- Komf single-book metadata identify must not masquerade as series identify. If the Komf server/client does not support book-level identify, show a clear unsupported message.
- Final app version is `1.1.0`, Android `versionCode = 23`, but bump version only after feature work is complete.

---

## Phase 0: Baseline and Pipeline Hygiene

### Task 0.1: Commit design amendment and task plan

**Files:**
- Modify: `docs/plans/2026-07-15-vanga-1-1-0-stability-ux-design.md`
- Create: `docs/plans/2026-07-15-vanga-1-1-0-stability-ux-tasks.md`

**Step 1: Verify docs formatting**
Command:
```bash
git diff --check -- docs/plans/2026-07-15-vanga-1-1-0-stability-ux-design.md docs/plans/2026-07-15-vanga-1-1-0-stability-ux-tasks.md
```
Expected: no output, exit 0.

**Step 2: Commit**
Command:
```bash
git add docs/plans/2026-07-15-vanga-1-1-0-stability-ux-design.md docs/plans/2026-07-15-vanga-1-1-0-stability-ux-tasks.md && \
git commit -m "docs: plan Vanga 1.1.0 stability UX release"
```

### Task 0.2: Keep API push workflow separate

**Files:**
- Already local: `scripts/push-via-api.py`
- Already local: `docs/github-api-push.md`

**Step 1: Verify API push helper syntax**
Command:
```bash
PYTHONPYCACHEPREFIX=/tmp/vanga-pycache-check python3 -m py_compile scripts/push-via-api.py
rm -rf /tmp/vanga-pycache-check
```
Expected: exit 0.

**Step 2: Verify dry-run**
Command:
```bash
scripts/push-via-api.py --dry-run
```
Expected: prints repo/branch/local/remote information and either `nothing to push` or a replay list; must not mutate remote.

**Step 3: Do not push yet**
Keep this as a local committed workflow change until Vito confirms a push/release batch.

---

## Phase 1: Offline Unsupported Operation Safety

### Task 1.1: Add explicit offline unsupported exception/helper

**Files:**
- Create: `vanga-domain/offline/src/commonMain/kotlin/io/github/vivitoto/vanga/offline/OfflineUnsupportedOperation.kt`
- Create test if feasible: `vanga-domain/offline/src/commonTest/kotlin/io/github/vivitoto/vanga/offline/OfflineUnsupportedOperationTest.kt`

**Step 1: Write failing test**
Test intent:
- `offlineUnsupported("刷新元数据")` throws `OfflineUnsupportedOperationException`.
- Exception message contains `离线模式暂不支持` and the operation name.

**Step 2: Run focused test**
Command:
```bash
./gradlew :vanga-domain:offline:commonTest --tests '*OfflineUnsupportedOperationTest*'
```
Expected initially: fail because helper does not exist. If Java is unavailable, record blocker.

**Step 3: Implement minimal helper**
Implementation shape:
```kotlin
package io.github.vivitoto.vanga.offline

class OfflineUnsupportedOperationException(
    operation: String,
) : UnsupportedOperationException("离线模式暂不支持${operation}，请连接 Komga 后再试")

fun offlineUnsupported(operation: String): Nothing = throw OfflineUnsupportedOperationException(operation)
```

**Step 4: Run test again**
Expected: pass when Gradle is available.

**Step 5: Commit**
```bash
git add vanga-domain/offline/src/commonMain/kotlin/io/github/vivitoto/vanga/offline/OfflineUnsupportedOperation.kt vanga-domain/offline/src/commonTest/kotlin/io/github/vivitoto/vanga/offline/OfflineUnsupportedOperationTest.kt && \
git commit -m "fix: add explicit offline unsupported errors"
```

### Task 1.2: Replace high-risk offline TODO crashes

**Files:**
- Modify: `vanga-domain/offline/src/commonMain/kotlin/io/github/vivitoto/vanga/offline/book/actions/BookAnalyzeAction.kt`
- Modify: `vanga-domain/offline/src/commonMain/kotlin/io/github/vivitoto/vanga/offline/book/actions/BookMetadataRefreshAction.kt`
- Modify: `vanga-domain/offline/src/commonMain/kotlin/io/github/vivitoto/vanga/offline/book/actions/BookMetadataUpdateAction.kt`
- Modify: `vanga-domain/offline/src/commonMain/kotlin/io/github/vivitoto/vanga/offline/book/actions/BookThumbnailUploadAction.kt`
- Modify: `vanga-domain/offline/src/commonMain/kotlin/io/github/vivitoto/vanga/offline/book/actions/BookThumbnailSelectAction.kt`
- Modify: `vanga-domain/offline/src/commonMain/kotlin/io/github/vivitoto/vanga/offline/book/actions/BookThumbnailDeleteAction.kt`
- Modify: `vanga-domain/offline/src/commonMain/kotlin/io/github/vivitoto/vanga/offline/series/actions/SeriesAnalyzeAction.kt`
- Modify: `vanga-domain/offline/src/commonMain/kotlin/io/github/vivitoto/vanga/offline/series/actions/SeriesRefreshMetadataAction.kt`
- Modify: `vanga-domain/offline/src/commonMain/kotlin/io/github/vivitoto/vanga/offline/series/actions/SeriesAddThumbnailAction.kt`
- Modify: `vanga-domain/offline/src/commonMain/kotlin/io/github/vivitoto/vanga/offline/series/actions/SeriesSelectThumbnailAction.kt`
- Modify: `vanga-domain/offline/src/commonMain/kotlin/io/github/vivitoto/vanga/offline/series/actions/SeriesDeleteThumbnailAction.kt`
- Modify: `vanga-domain/offline/src/commonMain/kotlin/io/github/vivitoto/vanga/offline/library/actions/LibraryAddAction.kt`
- Modify: `vanga-domain/offline/src/commonMain/kotlin/io/github/vivitoto/vanga/offline/library/actions/LibraryPatchAction.kt`
- Modify: `vanga-domain/offline/src/commonMain/kotlin/io/github/vivitoto/vanga/offline/library/actions/LibraryScanAction.kt`
- Modify: `vanga-domain/offline/src/commonMain/kotlin/io/github/vivitoto/vanga/offline/library/actions/LibraryAnalyzeAction.kt`
- Modify: `vanga-domain/offline/src/commonMain/kotlin/io/github/vivitoto/vanga/offline/library/actions/LibraryRefreshMetadataAction.kt`
- Modify: `vanga-domain/offline/src/commonMain/kotlin/io/github/vivitoto/vanga/offline/library/actions/LibraryEmptyTrashAction.kt`
- Modify: `vanga-domain/offline/src/commonMain/kotlin/io/github/vivitoto/vanga/offline/mediacontainer/BookContentExtractors.kt`
- Modify: `vanga-domain/offline/src/androidMain/kotlin/io/github/vivitoto/vanga/offline/mediacontainer/SafSeekableReadByteChannel.kt`

**Step 1: Write tests where practical**
Create focused common tests for representative unsupported actions:
- Book metadata refresh unsupported.
- Series thumbnail upload unsupported.
- Library scan unsupported.
- PDF page extraction unsupported.

Suggested file:
`vanga-domain/offline/src/commonTest/kotlin/io/github/vivitoto/vanga/offline/OfflineUnsupportedActionsTest.kt`

**Step 2: Replace TODOs**
- Use `offlineUnsupported("刷新单本元数据")`, `offlineUnsupported("上传系列缩略图")`, etc.
- For `BookContentExtractors` `MediaProfile.PDF`, throw unsupported with message like `离线 PDF 页内容抽取`.
- For `SafSeekableReadByteChannel.write`, throw `java.nio.channels.NonWritableChannelException()` instead of TODO.

**Step 3: Check no high-risk offline TODO remains**
Command:
```bash
grep -RIn "TODO(\|Not yet implemented" vanga-domain/offline/src | sed -n '1,120p'
```
Expected: no common/android high-risk TODOs for user-facing offline actions. wasm platform TODOs may remain only if explicitly hidden/unsupported and documented.

**Step 4: Commit**
```bash
git add vanga-domain/offline/src && git commit -m "fix: prevent unsupported offline actions from crashing"
```

### Task 1.3: Hide unsupported offline management actions in UI

**Files:**
- Modify: `vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/common/menus/LibraryActionsMenu.kt`
- Modify: `vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/common/menus/BookActionsMenu.kt`
- Modify: `vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/common/menus/SeriesActionsMenu.kt`
- Modify: `vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/common/menus/OneshotActionsMenu.kt`
- Modify if needed: `vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/common/menus/bulk/BookBulkActions.kt`
- Modify if needed: `vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/common/menus/bulk/SeriesBulkActions.kt`

**Step 1: Write/extend UI state tests where possible**
Prefer extracting pure predicates, e.g. `shouldShowOnlineMetadataActions(isOffline, isAdmin, komfEnabled)`, then test them in commonTest.

Suggested new file:
`vanga-ui/src/commonTest/kotlin/io/github/vivitoto/vanga/ui/common/menus/OfflineMenuVisibilityTest.kt`

**Step 2: Implement**
- In offline mode, hide refresh metadata, analyze, scan, Komf identify/reset, thumbnail upload/select/delete, user/server/library management actions that require online Komga/Komf.
- Keep local-only actions visible where valid: read, delete local downloaded file, local offline settings.
- Do not leave disabled dead buttons for unsupported offline admin actions.

**Step 3: Verify**
Command when Gradle available:
```bash
./gradlew :vanga-ui:commonTest --tests '*OfflineMenuVisibilityTest*'
```

**Step 4: Commit**
```bash
git add vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/common/menus vanga-ui/src/commonTest/kotlin/io/github/vivitoto/vanga/ui/common/menus && \
git commit -m "fix: hide unsupported offline management actions"
```

---

## Phase 2: Offline Download UX

### Task 2.1: Add download error text mapper

**Files:**
- Create: `vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/settings/offline/downloads/DownloadErrorText.kt`
- Create test: `vanga-ui/src/commonTest/kotlin/io/github/vivitoto/vanga/ui/settings/offline/downloads/DownloadErrorTextTest.kt`
- Modify: `vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/settings/offline/downloads/OfflineDownloadsContent.kt`
- Modify: `vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/book/BookScreenContent.kt`
- Modify: `vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/oneshot/OneshotScreenContent.kt`

**Step 1: Test mapping**
Cases:
- Storage permission / file create errors -> `无法写入下载目录，请检查目录权限`.
- Network/client errors -> `下载失败，请检查网络或 Komga 连接`.
- Unknown errors -> include sanitized message.

**Step 2: Implement mapper**
Keep it pure and platform-independent.

**Step 3: Wire UI**
Use the mapper wherever `DownloadEvent.BookDownloadError` is displayed.

**Step 4: Commit**
```bash
git add vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/settings/offline/downloads vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/book/BookScreenContent.kt vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/oneshot/OneshotScreenContent.kt vanga-ui/src/commonTest/kotlin/io/github/vivitoto/vanga/ui/settings/offline/downloads && \
git commit -m "feat: clarify offline download failures"
```

### Task 2.2: Prevent duplicate single-book download triggers

**Files:**
- Modify: `vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/book/BookScreenContent.kt`
- Modify: `vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/oneshot/OneshotScreenContent.kt`
- Modify if needed: `vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/book/BookViewModel.kt`
- Modify if needed: `vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/oneshot/OneshotViewModel.kt`

**Step 1: Test pure state if extracted**
Extract a small helper `isDownloadButtonEnabled(downloadEvent, book)` and test:
- enabled when no active event and not downloaded/outdated.
- disabled while progress event exists.
- re-enabled after error/completion.

**Step 2: Implement**
- Disable click while a download event for the same book is active.
- Show current progress rather than allowing repeated enqueue.

**Step 3: Commit**
```bash
git add vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/book vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/oneshot vanga-ui/src/commonTest/kotlin/io/github/vivitoto/vanga/ui/book && \
git commit -m "fix: avoid duplicate book download requests"
```

### Task 2.3: Improve local delete wording

**Files:**
- Modify: `vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/book/BookScreenContent.kt`
- Modify: `vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/oneshot/OneshotScreenContent.kt`
- Modify: `vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/series/view/SeriesContent.kt`

**Step 1: Update text**
Use wording like:
- Button: `删除本地下载`
- Dialog body: `只会删除本机已下载文件，不会删除 Komga 服务器上的漫画。`

**Step 2: Verify no remote-delete ambiguity**
Command:
```bash
grep -RIn "删除已下载\|删除本地" vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui | sed -n '1,120p'
```

**Step 3: Commit**
```bash
git add vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/book/BookScreenContent.kt vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/oneshot/OneshotScreenContent.kt vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/series/view/SeriesContent.kt && \
git commit -m "chore: clarify local download deletion wording"
```

---

## Phase 3: Reader Resilience

### Task 3.1: Add reader error text mapper and retry affordance

**Files:**
- Create: `vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/reader/image/common/ReaderImageErrorText.kt`
- Create test: `vanga-ui/src/commonTest/kotlin/io/github/vivitoto/vanga/ui/reader/image/common/ReaderImageErrorTextTest.kt`
- Modify: `vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/reader/image/common/ReaderImageContent.kt`
- Modify if needed: `vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/reader/image/common/ReaderContent.kt`

**Step 1: Test error text**
Cases:
- not found/file missing -> local file may be missing; reconnect and re-download.
- unsupported/offline unsupported -> clear unsupported text.
- generic error -> sanitized fallback.

**Step 2: Implement mapper**
Keep pure, commonMain.

**Step 3: UI**
- Replace raw exception class dump with human-readable text.
- If a retry callback is available in parent content, expose a button. If not available without broad refactor, keep text mapper now and create a follow-up task for retry wiring.

**Step 4: Commit**
```bash
git add vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/reader/image/common vanga-ui/src/commonTest/kotlin/io/github/vivitoto/vanga/ui/reader/image/common && \
git commit -m "feat: clarify reader image load failures"
```

### Task 3.2: Clamp reader progress/page boundaries

**Files:**
- Modify: `vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/reader/image/ReaderState.kt`
- Create test: `vanga-ui/src/commonTest/kotlin/io/github/vivitoto/vanga/ui/reader/image/ReaderProgressClampTest.kt`

**Step 1: Extract pure helper**
Example helper:
```kotlin
internal fun clampReaderPage(page: Int, pageCount: Int): Int
```

**Step 2: Test**
- page below 1 -> 1.
- page above pageCount -> pageCount.
- pageCount <= 0 -> 1.

**Step 3: Implement and use**
Use when restoring progress and when changing progress if page count is known.

**Step 4: Commit**
```bash
git add vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/reader/image/ReaderState.kt vanga-ui/src/commonTest/kotlin/io/github/vivitoto/vanga/ui/reader/image/ReaderProgressClampTest.kt && \
git commit -m "fix: clamp reader progress pages"
```

### Task 3.3: Guard paged reader spread size errors

**Files:**
- Modify: `vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/reader/image/paged/PagedReaderState.kt`
- Create test if helper can be extracted: `vanga-ui/src/commonTest/kotlin/io/github/vivitoto/vanga/ui/reader/image/paged/PagedReaderSpreadTest.kt`

**Step 1: Extract helper if feasible**
Replace hard `else -> throw IllegalStateException("can't display more than 2 images")` with a safe clamp/helper that renders the first two images and logs/ignores extra pages, or turns it into an error state.

**Step 2: Test helper**
Input 0/1/2/3 pages -> safe output <= 2 pages.

**Step 3: Commit**
```bash
git add vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/reader/image/paged/PagedReaderState.kt vanga-ui/src/commonTest/kotlin/io/github/vivitoto/vanga/ui/reader/image/paged && \
git commit -m "fix: guard paged reader spread bounds"
```

---

## Phase 4: Komf Single-Book Metadata Behavior

### Task 4.1: Add Komf book identify capability probe

**Files:**
- Create: `vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/dialogs/komf/identify/KomfBookIdentifySupport.kt`
- Create test: `vanga-ui/src/commonTest/kotlin/io/github/vivitoto/vanga/ui/dialogs/komf/identify/KomfBookIdentifySupportTest.kt`

**Step 1: Define support model**
Use a sealed result:
```kotlin
sealed interface KomfBookIdentifySupport {
    data object Supported : KomfBookIdentifySupport
    data class Unsupported(val reason: String) : KomfBookIdentifySupport
}
```

**Step 2: Test unsupported message**
Ensure unsupported text says current Komf version/API does not support single-book metadata identify.

**Step 3: Implement pure model only**
No network yet.

**Step 4: Commit**
```bash
git add vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/dialogs/komf/identify/KomfBookIdentifySupport.kt vanga-ui/src/commonTest/kotlin/io/github/vivitoto/vanga/ui/dialogs/komf/identify/KomfBookIdentifySupportTest.kt && \
git commit -m "feat: model Komf book identify support"
```

### Task 4.2: Implement Komf book endpoint probe/wrapper without guessing series fallback

**Files:**
- Create: `vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/dialogs/komf/identify/KomfBookIdentifyClient.kt`
- Create test with fake engine if feasible: `vanga-ui/src/commonTest/kotlin/io/github/vivitoto/vanga/ui/dialogs/komf/identify/KomfBookIdentifyClientTest.kt`
- Modify if needed: `vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/ViewModelFactory.kt`

**Step 1: Research exact endpoints before implementation**
Do not guess. Check Komf server docs/source for book-level endpoints. If no evidence exists for Komf 1.6.1/1.7.1, wrapper should only probe and return Unsupported on 404/NotFound.

**Step 2: Test unsupported behavior**
Fake HTTP 404/NotFound -> `Unsupported` result and no series identify call.

**Step 3: Implement minimal wrapper**
- Uses Komf base URL / existing HTTP client if accessible.
- Never calls `identifySeries`, `matchSeries`, or `matchLibrary` for a book action.
- Treats missing endpoint as unsupported.

**Step 4: Commit**
```bash
git add vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/dialogs/komf/identify vanga-ui/src/commonTest/kotlin/io/github/vivitoto/vanga/ui/dialogs/komf/identify vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/ViewModelFactory.kt && \
git commit -m "feat: probe Komf single-book identify support"
```

### Task 4.3: Add single-book Komf UI entry and unsupported dialog

**Files:**
- Modify: `vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/common/menus/BookActionsMenu.kt`
- Modify: `vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/common/menus/OneshotActionsMenu.kt`
- Create/Modify: `vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/dialogs/komf/identify/KomfBookIdentifyDialog.kt`
- Modify: `vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/dialogs/komf/identify/KomfIdentifyDialog.kt` only if sharing UI safely.

**Step 1: Extract visibility predicate and test**
Predicate conditions:
- Komf enabled.
- Not offline.
- Book has library/series/book context.

**Step 2: Add menu item**
Text:
`自动识别单本元数据（Komf）`

**Step 3: Dialog behavior**
- If supported: execute book-level flow only.
- If unsupported: show clear unsupported message and no destructive action.

**Step 4: Commit**
```bash
git add vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/common/menus vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/dialogs/komf/identify vanga-ui/src/commonTest/kotlin/io/github/vivitoto/vanga/ui/dialogs/komf/identify && \
git commit -m "feat: add Komf single-book identify entry"
```

### Task 4.4: Preserve existing Komf series/library behavior

**Files:**
- Modify only if required: `vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/dialogs/komf/identify/KomfIdentifyDialogViewModel.kt`
- Test: `vanga-ui/src/commonTest/kotlin/io/github/vivitoto/vanga/ui/dialogs/komf/identify/KomfIdentifyModeTest.kt`

**Step 1: Test separation**
- Series identify path calls series mode.
- Book identify path never calls series mode.

**Step 2: Implement any refactor**
If generic dialog is needed, introduce explicit mode enum:
```kotlin
enum class KomfIdentifyTarget { SERIES, BOOK }
```
Do not overload by nullable IDs in a way that can accidentally pick series.

**Step 3: Commit**
```bash
git add vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/dialogs/komf/identify vanga-ui/src/commonTest/kotlin/io/github/vivitoto/vanga/ui/dialogs/komf/identify && \
git commit -m "test: separate Komf book and series identify flows"
```

---

## Phase 5: Series Book List UX

### Task 5.1: Add pure series book filter state helpers

**Files:**
- Modify/Create: `vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/series/SeriesBookListFilters.kt`
- Test: `vanga-ui/src/commonTest/kotlin/io/github/vivitoto/vanga/ui/series/SeriesBookListFiltersTest.kt`

**Step 1: Define filter model**
Fields:
- `query: String`
- `readStatus: All/Unread/InProgress/Read`
- `downloadStatus: All/Downloaded/NotDownloaded/Outdated`
- optional `favoritesOnly: Boolean`

**Step 2: Test pure filtering**
Use small fake book model or extracted predicate over `VangaBook` if constructors are practical.

**Step 3: Implement**
Keep API-compatible with existing server-side `BooksFilterState`; use client-side refinement only where server-side condition does not exist.

**Step 4: Commit**
```bash
git add vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/series/SeriesBookListFilters.kt vanga-ui/src/commonTest/kotlin/io/github/vivitoto/vanga/ui/series/SeriesBookListFiltersTest.kt && \
git commit -m "feat: add series book list filters"
```

### Task 5.2: Wire search/filter UI into series books toolbar

**Files:**
- Modify: `vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/series/SeriesBooksState.kt`
- Modify: `vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/series/view/BooksContent.kt`

**Step 1: Add state fields**
Extend `BooksData` or keep separate `MutableStateFlow` for list filters.

**Step 2: Add UI**
- Search field in `BooksToolBar`.
- Filter dropdowns for read/download/favorite.
- Keep existing filter dialog and layout controls.

**Step 3: Verify behavior**
- Changing query resets page to 1.
- Selection mode still works.
- Pagination still works.
- Scroll restore key remains stable.

**Step 4: Commit**
```bash
git add vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/series/SeriesBooksState.kt vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/series/view/BooksContent.kt && \
git commit -m "feat: add series book search and filters"
```

### Task 5.3: Make sort entry clearer

**Files:**
- Modify: `vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/series/view/BooksContent.kt`
- Modify if needed: `vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/book/BookFilterState.kt`

**Step 1: Inspect existing `BooksFilterState.BooksSort` labels**
Ensure labels are clear in Chinese.

**Step 2: Update labels/UI placement**
Make sort visible in toolbar/dropdown, not hidden behind ambiguous filter icon on larger screens.

**Step 3: Commit**
```bash
git add vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/series/view/BooksContent.kt vanga-ui/src/commonMain/kotlin/io/github/vivitoto/vanga/ui/book/BookFilterState.kt && \
git commit -m "chore: clarify series book sorting controls"
```

---

## Phase 6: Release Pipeline Hardening

### Task 6.1: Strengthen version verification

**Files:**
- Modify: `scripts/verify_app_version.py`
- Test: create lightweight Python self-check if practical, e.g. `scripts/test_verify_app_version.py` or keep script-only if repo has no Python test harness.

**Step 1: Extend checks**
Verify:
- `gradle/libs.versions.toml` `app-version`
- `vanga-domain/core/.../AppVersion.kt` `AppVersion.current`
- `vanga-app/build.gradle.kts` `versionCode`
- `README.md` current version
- `RELEASE_NOTES.md` top version and top `versionCode`
- optional: release notes top heading starts with `# Vanga X.Y.Z`

**Step 2: Run**
```bash
python3 scripts/verify_app_version.py
```
Expected currently: `App version OK: 1.0.12, Android versionCode 22`.

**Step 3: Commit**
```bash
git add scripts/verify_app_version.py && git commit -m "test: harden Vanga version verification"
```

### Task 6.2: Add release doctor script

**Files:**
- Create: `scripts/doctor.py`

**Step 1: Implement checks**
- Runs `scripts/verify_app_version.py`.
- Checks `scripts/push-via-api.py --dry-run` unless `--skip-network` is supplied.
- Checks no high-risk `TODO()` remains in offline common/android user-facing paths.
- Checks `.github/workflows/android-build.yml` exists.
- Checks Java availability and prints skip/blocker clearly rather than failing unexpectedly.

**Step 2: Run**
```bash
python3 scripts/doctor.py --skip-network
```
Expected: pass static checks; Java check may warn if no JDK.

**Step 3: Commit**
```bash
git add scripts/doctor.py && git commit -m "chore: add Vanga release doctor"
```

### Task 6.3: Bump to 1.1.0 after features are complete

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `vanga-app/build.gradle.kts`
- Modify: `vanga-domain/core/src/commonMain/kotlin/io/github/vivitoto/vanga/updates/AppVersion.kt`
- Modify: `README.md`
- Modify: `RELEASE_NOTES.md`

**Step 1: Update versions**
- `app-version = "1.1.0"`
- `versionCode = 23`
- `AppVersion.current = AppVersion(1, 1, 0)`
- README current version `v1.1.0`
- RELEASE_NOTES top section `# Vanga 1.1.0`

**Step 2: Verify**
```bash
python3 scripts/verify_app_version.py
python3 scripts/doctor.py --skip-network
git diff --check
```

**Step 3: Commit**
```bash
git add gradle/libs.versions.toml vanga-app/build.gradle.kts vanga-domain/core/src/commonMain/kotlin/io/github/vivitoto/vanga/updates/AppVersion.kt README.md RELEASE_NOTES.md && \
git commit -m "release: prepare Vanga 1.1.0"
```

---

## Final Verification Before Any Push

Run locally:
```bash
python3 scripts/verify_app_version.py
python3 scripts/doctor.py --skip-network
PYTHONPYCACHEPREFIX=/tmp/vanga-pycache-check python3 -m py_compile scripts/*.py
rm -rf /tmp/vanga-pycache-check
git diff --check
```

If Java/JDK is available:
```bash
./gradlew :vanga-ui:commonTest :vanga-domain:offline:commonTest --stacktrace
```

If Java/JDK is not available locally, state blocker clearly and rely on GitHub Actions after confirmed API push.

## Final Push/Release Gate

Before push, present Vito with:

- Project/repo: `Vivitoto/Vanga`
- Branch: `main`
- Commit list
- Changed files summary
- Version/tag: `1.1.0` / `v1.1.0`
- Expected impact: GitHub Actions builds APK, publishes historical `v1.1.0` release and updates `latest`

Only after confirmation:
```bash
cd /home/vito/.hermes/workspace/Vanga
scripts/push-via-api.py
```

Verify with API, not `git fetch`:
```bash
gh api repos/Vivitoto/Vanga/branches/main --jq .commit.sha
gh run list --repo Vivitoto/Vanga --branch main --limit 5
gh release view v1.1.0 --repo Vivitoto/Vanga --json tagName,name,assets,url
```
