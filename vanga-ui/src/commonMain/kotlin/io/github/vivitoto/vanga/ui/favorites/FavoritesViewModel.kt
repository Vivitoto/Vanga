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
import io.github.vivitoto.vanga.komga.api.KomgaCollectionsApi
import io.github.vivitoto.vanga.komga.api.KomgaReadListApi
import io.github.vivitoto.vanga.komga.api.model.VangaBook
import io.github.vivitoto.vanga.ui.LoadState
import io.github.vivitoto.vanga.ui.common.cards.defaultCardWidth
import snd.komga.client.collection.KomgaCollection
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.readlist.KomgaReadList
import snd.komga.client.series.KomgaSeries
import snd.komga.client.user.KomgaUser

class FavoritesViewModel(
    private val favoriteCollectionService: FavoriteCollectionService,
    private val favoriteReadListService: FavoriteReadListService,
    private val collectionsApi: KomgaCollectionsApi,
    private val readListApi: KomgaReadListApi,
    private val currentUser: StateFlow<KomgaUser?>,
    private val appNotifications: AppNotifications,
    cardWidthFlow: Flow<androidx.compose.ui.unit.Dp>,
) : StateScreenModel<LoadState<Unit>>(LoadState.Uninitialized) {

    val cardWidth = cardWidthFlow.stateIn(screenModelScope, SharingStarted.Eagerly, defaultCardWidth.dp)

    var favoriteSeriesCollection by mutableStateOf<KomgaCollection?>(null)
        private set
    var favoriteBooksReadList by mutableStateOf<KomgaReadList?>(null)
        private set
    var favoriteSeries by mutableStateOf<List<KomgaSeries>>(emptyList())
        private set
    var favoriteBooks by mutableStateOf<List<VangaBook>>(emptyList())
        private set

    val canWriteFavorites: Boolean
        get() = currentUser.value?.roleAdmin() ?: true

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
        val collection = favoriteCollectionService.getFavoriteCollection(forceRefresh = true)
        val readList = favoriteReadListService.getFavoriteReadList(forceRefresh = true)

        favoriteSeriesCollection = collection
        favoriteBooksReadList = readList

        favoriteSeries = collection?.let {
            collectionsApi.getSeriesForCollection(
                id = it.id,
                pageRequest = KomgaPageRequest(unpaged = true)
            ).content
        } ?: emptyList()

        favoriteBooks = readList?.let {
            readListApi.getBooksForReadList(
                id = it.id,
                pageRequest = KomgaPageRequest(unpaged = true)
            ).content
        } ?: emptyList()
    }
}
