# Vanga Design Document

**Date:** 2026-06-12  
**Author:** Claw (with Vito)  
**Status:** Draft — Pending Approval

---

## 1. Overview

**Vanga** is an Android comic reader app for self-hosted Komga servers, forked from Vanga, adding dual-layer favorites (Series + Books) with server-side sync.

- **Project name:** Vanga
- **Subtitle:** Komga Manga Reader
- **Chinese name:** Vanga 漫画阅读器
- **Target platform:** Android only (first version)
- **Base:** Fork of [Vivitoto/Vanga](https://github.com/Vivitoto/Vanga) (Apache-2.0)
- **Base commit:** `58c97bc`

### 1.1 Name Rationale

Following Vito's naming pattern: short V-prefixed product names.

**Vanga** = V + Manga — short, two syllables, immediately suggests manga/comics.

---

## 2. Goals

### 2.1 MVP Goals

1. **Server connection & auth**: Connect to Komga server, authenticate with username/password or API key
2. **Browse**: Libraries → Series → Books, with search
3. **Reader**: Single-page image reader, swipe navigation, zoom, reading direction
4. **Favorites (Series)**: Star/unstar series; synced to Komga Collection
5. **Favorites (Books)**: Star/unstar individual books; synced to Komga ReadList
6. **Favorites screen**: View all favorites with Series/Books tabs
7. **Reading progress sync**: Auto-sync reading progress to Komga
8. **Cover image caching**: Efficient image caching for thumbnails and pages

### 2.2 Non-Goals (MVP)

- Offline download
- EPUB/PDF reading
- Metadata editing
- Multi-user management
- AI upscale/image processing
- OPDS client
- Desktop/iOS platforms

---

## 3. Favorites Architecture

### 3.1 Core Principle

Komga does not have native "favorites" or "starred" functionality. We simulate favorites using existing Komga objects:

| Favorites Type | Komga Object | API | Permission |
|---|---:|---|
| Favorite Series | Collection | `/api/v1/collections` | ADMIN for write; read for all authenticated |
| Favorite Books | ReadList | `/api/v1/readlists` | ADMIN for write; read for all authenticated |

### 3.2 Naming Convention

```
Favorite Series:  "Favorites - <username>"
Favorite Books:   "Favorite Books - <username>"
```

Example:
```
Favorites - vito
Favorite Books - vito
```

These appear in Komga Web UI as regular collections/readlists, which is acceptable for self-hosted use.

### 3.3 Server-Side Sync Flow

```
ADD FAVORITE:
  1. Fetch all Collections/ReadLists
  2. Find the user's favorites container by name
  3. If not exists:
       POST /api/v1/collections  { name, ordered:false, seriesIds:[id] }
       or POST /api/v1/readlists  { name, ordered:true, bookIds:[id] }
  4. If exists:
       Read latest seriesIds/bookIds
       Merge target ID
       PATCH with full merged list

REMOVE FAVORITE:
  1. Fetch the container
  2. Remove target ID from list
  3. If list is empty:
       DELETE the container
     Else:
       PATCH with remaining IDs

TOGGLE FAVORITE:
  - Check if already favorited (read from container)
  - Call ADD or REMOVE accordingly
```

### 3.4 Concurrency Handling

Since PATCH replaces the entire `seriesIds`/`bookIds` list (not incremental add):

1. Always **fetch latest** before PATCH
2. If PATCH fails (race condition), retry once with fresh fetch
3. Accept eventual consistency for multi-device scenarios

### 3.5 Empty Favorites Handling

Komga requires non-empty `seriesIds`/`bookIds` on creation and update. Strategy:

- **Favorites container exists** = has favorites
- **Favorites container doesn't exist** = empty favorites
- **Last item removed** → DELETE the container

### 3.6 Permission Handling

Since Collection/ReadList write requires ADMIN:

1. On login, detect user roles from `/api/v2/users/me`
2. If user lacks ADMIN role:
   - Favorites are **read-only** (can view if container exists, cannot add/remove)
   - Show toast: "Favorites require admin access"
3. For self-hosted single-user Komga (Vito's use case), this is not an issue

### 3.7 Local Cache

Even with server sync, maintain local SQLite cache for:

- Fast favorites state lookup (star icon on cards)
- Offline favorites list display
- Reduced API calls

Cache structure:
```
favorite_series_cache:
  - server_url
  - user_id
  - collection_id
  - collection_name
  - series_ids (JSON)
  - last_synced_at

favorite_books_cache:
  - server_url
  - user_id
  - readlist_id
  - readlist_name
  - book_ids (JSON)
  - last_synced_at
```

Cache is **derived**, not authoritative. Server is the source of truth.

---

## 4. Module Structure

### 4.1 New Files

```
vanga-domain/core/src/commonMain/kotlin/snd/vanga/favorites/
├── FavoriteCollectionService.kt       # Series favorites → Komga Collection
├── FavoriteReadListService.kt         # Book favorites → Komga ReadList
├── FavoriteState.kt                   # Shared favorites state
├── FavoriteSyncError.kt               # Error types

vanga-infra/database/sqlite/src/commonMain/kotlin/snd/vanga/db/favorites/
├── FavoriteSeriesCacheTable.kt        # Exposed table definition
├── FavoriteBooksCacheTable.kt         # Exposed table definition
├── ExposedFavoriteCacheRepository.kt  # SQLite implementation

vanga-ui/src/commonMain/kotlin/snd/vanga/ui/favorites/
├── FavoritesScreen.kt                 # Main favorites screen
├── FavoritesViewModel.kt              # ViewModel
├── FavoritesContent.kt                # Tabbed content (Series/Books)
├── FavoritesSeriesTab.kt              # Series favorites grid
├── FavoritesBooksTab.kt               # Books favorites list
├── FavoriteButton.kt                  # Reusable star button component
```

### 4.2 Modified Files

```
vanga-ui/.../home/HomeScreen.kt              # Add favorites section
vanga-ui/.../home/HomeViewModel.kt           # Wire favorites data
vanga-ui/.../series/list/SeriesCard*.kt      # Add star icon
vanga-ui/.../series/view/SeriesScreen*.kt    # Add star button
vanga-ui/.../book/BookScreenContent.kt       # Add star button
vanga-ui/.../reader/TitleBarContent.kt       # Add star button in reader
vanga-ui/.../MainScreen.kt                   # Add Favorites to navigation
vanga-ui/.../topbar/NavigationMenuContent.kt # Add Favorites nav item
vanga-ui/.../DependencyContainer.kt          # Wire favorites dependencies
vanga-ui/.../ViewModelFactory.kt             # Create favorites ViewModels
vanga-app/.../strings.xml                    # Rename to Vanga
```

---

## 5. UI Design

### 5.1 Favorites Screen

```
┌──────────────────────────┐
│  Favorites               │
│                          │
│  [Series] [Books]        │  ← Tabs
│                          │
│  ┌──────┐ ┌──────┐      │
│  │Cover │ │Cover │      │
│  │Title │ │Title │      │
│  │ ★    │ │ ★    │      │  ← Series grid (starred icon)
│  └──────┘ └──────┘      │
│  ┌──────┐ ┌──────┐      │
│  │ ...  │ │ ...  │      │
│  └──────┘ └──────┘      │
│                          │
└──────────────────────────┘
```

### 5.2 Star Button Placement

- **Series card** (list/grid): Star icon top-right corner
- **Series detail page**: Star button in toolbar
- **Book detail page**: Star button in toolbar
- **Reader toolbar**: Star button for current series + current book
- **Favorites screen**: Already-starred items shown; tap to unstar

### 5.3 Navigation Entry

**Desktop/Mobile layout:** Add "Favorites" to sidebar/bottom navigation bar.

Bottom bar (mobile):
```
[Libraries] [Home] [Search] [Favorites] [Settings]
```

Sidebar (desktop/tablet):
```
Home
Favorites    ← NEW
Libraries
Settings
```

### 5.4 Home Screen Favorites Section

Add a "Favorites" section on the home screen showing recently favorited series (top 6–8), with a "View All" link.

---

## 6. Data Flow

### 6.1 Star Toggle Flow

```
User taps star on Series card
  → FavoriteCollectionService.toggleFavorite(seriesId)
  → Fetch latest Collection "Favorites - <user>"
  → If favorited: remove from seriesIds, PATCH
  → If not favorited: add to seriesIds, PATCH
  → Update local cache
  → Update UI state (filled/empty star)
```

### 6.2 Favorites Screen Load Flow

```
User opens Favorites screen
  → FavoritesViewModel.initialize()
  → FavoriteCollectionService.getFavoriteSeries()
  → FavoriteReadListService.getFavoriteBooks()
  → Fetch latest metadata from Komga Series/Book APIs
  → Display with loading state
  → Handle: network error, permission denied, empty favorites
```

### 6.3 Card Star State Flow

```
SeriesCard renders
  → Observe FavoriteState.isFavorite(seriesId)
  → Shows filled star ★ or empty star ☆
  → On click: toggleFavorite → UI updates reactively
```

---

## 7. Rebranding

### 7.1 Android App

- App name: `Vanga`
- Package: keep `io.github.vivitoto.vanga` (to not break things in v1)
- strings.xml: `app_name` → `Vanga`

### 7.2 Gradle Module Names

Keep existing module names (`vanga-*`) in v1 to avoid refactoring overhead. The app display name is `Vanga`; internal module names stay `vanga-*`.

---

## 8. Testing Strategy

### 8.1 Unit Tests

- `FavoriteCollectionService` logic (find container, add, remove, empty handling)
- `FavoriteReadListService` logic
- Cache repository CRUD
- Name generation for favorites containers

### 8.2 Integration Tests

- Komga API integration with test server
- Collection create/read/update/delete cycle
- ReadList create/read/update/delete cycle
- Permission handling (admin vs non-admin)

### 8.3 Manual Testing

- Connect to real Komga server
- Star/unstar series and books
- Verify sync across devices
- Verify Komga Web UI shows favorites containers
- Verify empty favorites behavior
- Verify non-admin user behavior

---

## 9. Risks & Mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Komga admin required | Non-admin users can't favorite | Detect role early, show clear message |
| Collection/ReadList name collision | Multiple users clash | Use `- <username>` suffix |
| Race condition on concurrent edits | Lost favorites | Fetch-before-write pattern |
| Komga removes empty containers | Favorites disappear | Re-create on next add (acceptable) |
| Large favorites list performance | Slow PATCH/GET | Pagination for UI; batch metadata fetch |
| Breaking changes in upstream Vanga | Merge conflicts | Keep fork minimal; only add favorites layer |

---

## 10. Open Questions

1. **Should we rename the Android package?**  
   Recommendation: Keep `io.github.vivitoto.vanga` for v1 stability; rename in v2 if needed.

2. **Should favorites sync on app start or on-demand?**  
   Recommendation: Sync on favorites screen open + on toggle; do not sync on app start to avoid startup delay.

3. **Should we support multiple favorites lists?**  
   Not in v1. Single "Favorites" per type per user.

---

## 11. Approval Checklist

- [ ] Project name: Vanga
- [ ] Fork Vanga as base (not from scratch)
- [ ] Android only, first version
- [ ] Server-synced favorites via Komga Collection/ReadList
- [ ] Local cache for fast UI
- [ ] Admin-only write, read-only for non-admin
- [ ] Series favorites + Book favorites (two separate containers)
- [ ] Star button on Series card, Series detail, Book detail, Reader toolbar
- [ ] Favorites screen with Series/Books tabs
- [ ] Navigation entry for Favorites
- [ ] Home screen favorites section

---

*Next: Implementation plan → `docs/plans/2026-06-12-vanga-favorites-plan.md`*
