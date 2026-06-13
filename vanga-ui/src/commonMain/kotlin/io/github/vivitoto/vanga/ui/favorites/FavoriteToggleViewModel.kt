package io.github.vivitoto.vanga.ui.favorites

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.vivitoto.vanga.AppNotification
import io.github.vivitoto.vanga.AppNotifications
import io.github.vivitoto.vanga.favorites.FavoriteCollectionService
import io.github.vivitoto.vanga.favorites.FavoriteReadListService
import io.github.vivitoto.vanga.favorites.FavoriteSyncResult
import io.github.vivitoto.vanga.favorites.FavoriteWebDavSyncService
import snd.komga.client.book.KomgaBookId
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.user.KomgaUser

class FavoriteToggleViewModel(
    private val favoriteCollectionService: FavoriteCollectionService,
    private val favoriteReadListService: FavoriteReadListService,
    private val favoriteSyncService: FavoriteWebDavSyncService,
    private val currentUserProvider: () -> KomgaUser?,
    private val appNotifications: AppNotifications,
    private val onFavoritesChanged: () -> Unit,
) : ScreenModel {

    val canWriteFavorites: Boolean
        get() = currentUserProvider() != null

    suspend fun isSeriesFavorite(seriesId: KomgaSeriesId): Boolean =
        appNotifications.runCatchingToNotifications { favoriteCollectionService.isFavorite(seriesId) }
            .getOrDefault(false)

    suspend fun toggleSeriesFavorite(seriesId: KomgaSeriesId): Boolean {
        if (!canWriteFavoritesNow()) return isSeriesFavorite(seriesId)
        return appNotifications.runCatchingToNotifications { favoriteCollectionService.toggleFavorite(seriesId) }
            .onSuccess {
                onFavoritesChanged()
                syncFavoritesInBackground()
            }
            .getOrElse { isSeriesFavorite(seriesId) }
    }

    suspend fun isBookFavorite(bookId: KomgaBookId): Boolean =
        appNotifications.runCatchingToNotifications { favoriteReadListService.isFavorite(bookId) }
            .getOrDefault(false)

    suspend fun toggleBookFavorite(bookId: KomgaBookId): Boolean {
        if (!canWriteFavoritesNow()) return isBookFavorite(bookId)
        return appNotifications.runCatchingToNotifications { favoriteReadListService.toggleFavorite(bookId) }
            .onSuccess {
                onFavoritesChanged()
                syncFavoritesInBackground()
            }
            .getOrElse { isBookFavorite(bookId) }
    }

    private fun canWriteFavoritesNow(): Boolean {
        val user = currentUserProvider()
        return when {
            user == null -> {
                appNotifications.add(AppNotification.Error("用户信息加载中，请稍后再试"))
                false
            }

            else -> true
        }
    }

    private fun syncFavoritesInBackground() {
        appNotifications.runCatchingToNotifications(screenModelScope) {
            when (favoriteSyncService.syncNow()) {
                FavoriteSyncResult.Disabled,
                FavoriteSyncResult.NotConfigured,
                is FavoriteSyncResult.ConnectionOk -> Unit

                is FavoriteSyncResult.Success -> onFavoritesChanged()
            }
        }
    }
}
