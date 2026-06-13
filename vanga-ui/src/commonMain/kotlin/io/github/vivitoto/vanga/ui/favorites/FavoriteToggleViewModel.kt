package io.github.vivitoto.vanga.ui.favorites

import cafe.adriel.voyager.core.model.ScreenModel
import io.github.vivitoto.vanga.AppNotification
import io.github.vivitoto.vanga.AppNotifications
import io.github.vivitoto.vanga.favorites.FavoriteCollectionService
import io.github.vivitoto.vanga.favorites.FavoriteReadListService
import snd.komga.client.book.KomgaBookId
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.user.KomgaUser

class FavoriteToggleViewModel(
    private val favoriteCollectionService: FavoriteCollectionService,
    private val favoriteReadListService: FavoriteReadListService,
    private val currentUserProvider: () -> KomgaUser?,
    private val appNotifications: AppNotifications,
    private val onFavoritesChanged: () -> Unit,
) : ScreenModel {

    val canWriteFavorites: Boolean
        get() = currentUserProvider()?.roleAdmin() ?: true

    suspend fun isSeriesFavorite(seriesId: KomgaSeriesId): Boolean =
        appNotifications.runCatchingToNotifications { favoriteCollectionService.isFavorite(seriesId) }
            .getOrDefault(false)

    suspend fun toggleSeriesFavorite(seriesId: KomgaSeriesId): Boolean {
        if (!canWriteFavoritesNow()) return isSeriesFavorite(seriesId)
        return appNotifications.runCatchingToNotifications { favoriteCollectionService.toggleFavorite(seriesId) }
            .onSuccess { onFavoritesChanged() }
            .getOrElse { isSeriesFavorite(seriesId) }
    }

    suspend fun isBookFavorite(bookId: KomgaBookId): Boolean =
        appNotifications.runCatchingToNotifications { favoriteReadListService.isFavorite(bookId) }
            .getOrDefault(false)

    suspend fun toggleBookFavorite(bookId: KomgaBookId): Boolean {
        if (!canWriteFavoritesNow()) return isBookFavorite(bookId)
        return appNotifications.runCatchingToNotifications { favoriteReadListService.toggleFavorite(bookId) }
            .onSuccess { onFavoritesChanged() }
            .getOrElse { isBookFavorite(bookId) }
    }

    private fun canWriteFavoritesNow(): Boolean {
        val user = currentUserProvider()
        return when {
            user == null -> {
                appNotifications.add(AppNotification.Error("用户信息加载中，请稍后再试"))
                false
            }

            !user.roleAdmin() -> {
                appNotifications.add(AppNotification.Error("收藏同步需要 Komga 管理员权限"))
                false
            }

            else -> true
        }
    }
}
