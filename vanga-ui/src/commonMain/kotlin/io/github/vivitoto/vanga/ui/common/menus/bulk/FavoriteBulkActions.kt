package io.github.vivitoto.vanga.ui.common.menus.bulk

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import io.github.vivitoto.vanga.AppNotification
import io.github.vivitoto.vanga.AppNotifications
import io.github.vivitoto.vanga.favorites.FavoriteCollectionService
import io.github.vivitoto.vanga.favorites.FavoriteReadListService
import io.github.vivitoto.vanga.favorites.FavoriteSyncResult
import io.github.vivitoto.vanga.favorites.FavoriteWebDavSyncService
import io.github.vivitoto.vanga.komga.api.model.VangaBook
import io.github.vivitoto.vanga.ui.LocalViewModelFactory
import snd.komga.client.series.KomgaSeries

@Composable
fun MixedBulkActionsContent(
    items: List<SelectedItem>,
    compact: Boolean,
) {
    val factory = LocalViewModelFactory.current
    val actions = remember(factory) { factory.getFavoriteBulkActions() }
    val coroutineScope = rememberCoroutineScope()
    val buttons = remember(items, actions) {
        listOf(
            BulkActionButtonData(
                description = "加入收藏",
                icon = Icons.Default.Star,
                onClick = { coroutineScope.launch { actions.addItemsToLocalFavorites(items) } }
            )
        )
    }
    BulkActionsButtonsLayout(buttons, compact)
}

data class FavoriteBulkActions(
    val addBooksToLocalFavorites: suspend (List<VangaBook>) -> Unit,
    val addSeriesToLocalFavorites: suspend (List<KomgaSeries>) -> Unit,
    val addItemsToLocalFavorites: suspend (List<SelectedItem>) -> Unit,
) {
    constructor(
        favoriteCollectionService: FavoriteCollectionService,
        favoriteReadListService: FavoriteReadListService,
        favoriteSyncService: FavoriteWebDavSyncService,
        notifications: AppNotifications,
        onFavoritesChanged: () -> Unit,
    ) : this(
        addBooksToLocalFavorites = { books ->
            addToLocalFavorites(
                books = books,
                series = emptyList(),
                favoriteCollectionService = favoriteCollectionService,
                favoriteReadListService = favoriteReadListService,
                favoriteSyncService = favoriteSyncService,
                notifications = notifications,
                onFavoritesChanged = onFavoritesChanged,
            )
        },
        addSeriesToLocalFavorites = { series ->
            addToLocalFavorites(
                books = emptyList(),
                series = series,
                favoriteCollectionService = favoriteCollectionService,
                favoriteReadListService = favoriteReadListService,
                favoriteSyncService = favoriteSyncService,
                notifications = notifications,
                onFavoritesChanged = onFavoritesChanged,
            )
        },
        addItemsToLocalFavorites = { items ->
            addToLocalFavorites(
                books = items.selectedBooks(),
                series = items.selectedSeries(),
                favoriteCollectionService = favoriteCollectionService,
                favoriteReadListService = favoriteReadListService,
                favoriteSyncService = favoriteSyncService,
                notifications = notifications,
                onFavoritesChanged = onFavoritesChanged,
            )
        }
    )
}

private suspend fun addToLocalFavorites(
    books: List<VangaBook>,
    series: List<KomgaSeries>,
    favoriteCollectionService: FavoriteCollectionService,
    favoriteReadListService: FavoriteReadListService,
    favoriteSyncService: FavoriteWebDavSyncService,
    notifications: AppNotifications,
    onFavoritesChanged: () -> Unit,
) {
    val uniqueBooks = books.distinctBy { it.id }
    val uniqueSeries = series.distinctBy { it.id }
    if (uniqueBooks.isEmpty() && uniqueSeries.isEmpty()) return

    notifications.runCatchingToNotifications {
        uniqueBooks.forEach { favoriteReadListService.addFavorite(it.id) }
        uniqueSeries.forEach { favoriteCollectionService.addFavorite(it.id) }
    }.onSuccess {
        notifications.add(AppNotification.Success("已加入收藏：${favoriteSummary(uniqueBooks, uniqueSeries)}"))
        onFavoritesChanged()
        runCatching { favoriteSyncService.syncNow() }
            .onSuccess { result ->
                if (result is FavoriteSyncResult.Success) onFavoritesChanged()
            }
            .onFailure { exception ->
                notifications.add(
                    AppNotification.Error(
                        "收藏已保存，但 WebDAV 同步失败：${exception.message ?: "未知错误"}"
                    )
                )
            }
    }
}

private fun favoriteSummary(
    books: List<VangaBook>,
    series: List<KomgaSeries>,
): String = buildList {
    if (series.isNotEmpty()) add("${series.size} 个漫画系列")
    if (books.isNotEmpty()) add("${books.size} 本书")
}.joinToString("、")
