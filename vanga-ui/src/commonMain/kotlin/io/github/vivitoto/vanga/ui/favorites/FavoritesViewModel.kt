package io.github.vivitoto.vanga.ui.favorites

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import io.github.vivitoto.vanga.AppNotifications
import io.github.vivitoto.vanga.favorites.FavoriteCollectionService
import io.github.vivitoto.vanga.favorites.FavoriteReadListService
import io.github.vivitoto.vanga.favorites.FavoriteWebDavSyncService
import io.github.vivitoto.vanga.komga.api.KomgaBookApi
import io.github.vivitoto.vanga.komga.api.KomgaSeriesApi
import io.github.vivitoto.vanga.komga.api.model.VangaBook
import io.github.vivitoto.vanga.ui.LoadState
import io.github.vivitoto.vanga.ui.common.cards.defaultCardWidth
import snd.komga.client.series.KomgaSeries
import snd.komga.client.user.KomgaUser

class FavoritesViewModel(
    private val favoriteCollectionService: FavoriteCollectionService,
    private val favoriteReadListService: FavoriteReadListService,
    private val favoriteSyncService: FavoriteWebDavSyncService,
    private val seriesApi: KomgaSeriesApi,
    private val bookApi: KomgaBookApi,
    private val currentUser: StateFlow<KomgaUser?>,
    private val appNotifications: AppNotifications,
    cardWidthFlow: Flow<androidx.compose.ui.unit.Dp>,
) : StateScreenModel<LoadState<Unit>>(LoadState.Uninitialized) {

    val cardWidth = cardWidthFlow.stateIn(screenModelScope, SharingStarted.Eagerly, defaultCardWidth.dp)

    var favoriteSeries by mutableStateOf<List<KomgaSeries>>(emptyList())
        private set
    var favoriteBooks by mutableStateOf<List<VangaBook>>(emptyList())
        private set

    val canWriteFavorites: Boolean
        get() = currentUser.value != null

    fun initialize() {
        if (state.value != LoadState.Uninitialized) return
        screenModelScope.launch { reload() }
    }

    fun reload() {
        screenModelScope.launch {
            mutableState.value = LoadState.Loading
            appNotifications.runCatchingToNotifications {
                loadFavorites()
            }.onSuccess {
                mutableState.value = LoadState.Success(Unit)
            }.onFailure {
                mutableState.value = LoadState.Error(it)
            }
        }
    }

    private suspend fun loadFavorites() {
        runCatching { favoriteSyncService.syncNow() }
        val favoriteSeriesIds = favoriteCollectionService.getFavoriteSeriesIds()
        val favoriteBookIds = favoriteReadListService.getFavoriteBookIds()

        favoriteSeries = favoriteSeriesIds
            .mapNotNull { id -> runCatching { seriesApi.getOneSeries(id) }.getOrNull() }

        favoriteBooks = favoriteBookIds
            .mapNotNull { id -> runCatching { bookApi.getOne(id) }.getOrNull() }
    }
}
