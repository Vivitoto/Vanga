package io.github.vivitoto.vanga.ui.library

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import io.github.vivitoto.vanga.AppNotifications
import io.github.vivitoto.vanga.komga.api.KomgaCollectionsApi
import io.github.vivitoto.vanga.komga.api.KomgaLibraryApi
import io.github.vivitoto.vanga.komga.api.KomgaReadListApi
import io.github.vivitoto.vanga.komga.api.KomgaReferentialApi
import io.github.vivitoto.vanga.komga.api.KomgaSeriesApi
import io.github.vivitoto.vanga.favorites.FavoriteContainerNames
import io.github.vivitoto.vanga.offline.tasks.OfflineTaskEmitter
import io.github.vivitoto.vanga.settings.CommonSettingsRepository
import io.github.vivitoto.vanga.ui.LoadState
import io.github.vivitoto.vanga.ui.LoadState.Error
import io.github.vivitoto.vanga.ui.LoadState.Loading
import io.github.vivitoto.vanga.ui.LoadState.Success
import io.github.vivitoto.vanga.ui.LoadState.Uninitialized
import io.github.vivitoto.vanga.ui.common.cards.defaultCardWidth
import io.github.vivitoto.vanga.ui.common.menus.LibraryMenuActions
import io.github.vivitoto.vanga.ui.library.LibraryTab.COLLECTIONS
import io.github.vivitoto.vanga.ui.library.LibraryTab.READ_LISTS
import io.github.vivitoto.vanga.ui.library.LibraryTab.SERIES
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.sse.KomgaEvent.CollectionAdded
import snd.komga.client.sse.KomgaEvent.CollectionDeleted
import snd.komga.client.sse.KomgaEvent.ReadListAdded
import snd.komga.client.sse.KomgaEvent.ReadListDeleted

class LibraryViewModel(
    private val libraryApi: KomgaLibraryApi,
    private val collectionApi: KomgaCollectionsApi,
    private val readListsApi: KomgaReadListApi,
    private val taskEmitter: OfflineTaskEmitter,
    seriesApi: KomgaSeriesApi,
    referentialApi: KomgaReferentialApi,

    private val appNotifications: AppNotifications,
    private val komgaEvents: SharedFlow<KomgaEvent>,
    libraryFlow: Flow<KomgaLibrary?>,
    settingsRepository: CommonSettingsRepository,
) : StateScreenModel<LoadState<Unit>>(Uninitialized) {
    val library = libraryFlow.stateIn(screenModelScope, SharingStarted.Eagerly, null)
    val cardWidth = settingsRepository.getCardWidth().map { Dp(it.toFloat()) }
        .stateIn(screenModelScope, SharingStarted.Eagerly, defaultCardWidth.dp)

    var currentTab by mutableStateOf(SERIES)
    var collectionsCount by mutableStateOf(0)
        private set
    var readListsCount by mutableStateOf(0)
        private set

    private val reloadEventsEnabled = MutableStateFlow(true)
    private val reloadJobsFlow = MutableSharedFlow<Unit>(1, 0, DROP_OLDEST)

    val seriesTabState = LibrarySeriesTabState(
        seriesApi = seriesApi,
        referentialApi = referentialApi,
        notifications = appNotifications,
        komgaEvents = komgaEvents,
        settingsRepository = settingsRepository,
        library = library,
        cardWidth = cardWidth,
        taskEmitter = taskEmitter,
    )
    val collectionsTabState = LibraryCollectionsTabState(
        collectionApi = collectionApi,
        appNotifications = appNotifications,
        events = komgaEvents,
        library = library,
        cardWidth = cardWidth
    )
    val readListsTabState = LibraryReadListsTabState(
        readListApi = readListsApi,
        appNotifications = appNotifications,
        komgaEvents = komgaEvents,
        library = library,
        cardWidth = cardWidth
    )
    val showToolbar = seriesTabState.isInEditMode.map { !it }
        .stateIn(screenModelScope, SharingStarted.Eagerly, true)

    fun initialize(seriesFilter: SeriesScreenFilter? = null) {
        if (state.value !is Uninitialized) return

        if (seriesFilter != null) toBrowseTab()

        screenModelScope.launch { loadItemCounts() }
        startKomgaEventListener()

        reloadJobsFlow.onEach {
            reloadEventsEnabled.first { it }
            loadItemCounts()
            delay(1000)
        }.launchIn(screenModelScope)
    }

    fun reload() {
        mutableState.value = Loading
        screenModelScope.launch {
            loadItemCounts()
            when (currentTab) {
                SERIES -> seriesTabState.reload()
                COLLECTIONS -> collectionsTabState.reload()
                READ_LISTS -> readListsTabState.reload()
            }
        }
    }

    private suspend fun loadItemCounts() {
        if (state.value is Error) return

        appNotifications.runCatchingToNotifications {
            mutableState.value = Loading
            val pageRequest = KomgaPageRequest(unpaged = true)
            val libraryIds = listOfNotNull(library.value?.id)
            collectionsCount = collectionApi.getAll(libraryIds = libraryIds, pageRequest = pageRequest)
                .content
                .count { !FavoriteContainerNames.isSeriesFavoritesContainer(it.name) }
            readListsCount = readListsApi.getAll(libraryIds = libraryIds, pageRequest = pageRequest)
                .content
                .count { !FavoriteContainerNames.isBookFavoritesContainer(it.name) }

            if (collectionsCount == 0 && currentTab == COLLECTIONS) currentTab = SERIES
            if (readListsCount == 0 && currentTab == READ_LISTS) currentTab = SERIES
            mutableState.value = Success(Unit)
        }.onFailure { mutableState.value = Error(it) }
    }

    fun toBrowseTab() {
        currentTab = SERIES
    }

    fun toCollectionsTab() {
        currentTab = COLLECTIONS
    }

    fun toReadListsTab() {
        currentTab = READ_LISTS
    }

    fun libraryActions() = LibraryMenuActions(libraryApi, appNotifications, taskEmitter, screenModelScope)

    fun stopKomgaEventHandler() {
        reloadEventsEnabled.value = false
    }

    fun startKomgaEventHandler() {
        reloadEventsEnabled.value = true

    }

    private fun startKomgaEventListener() {
        komgaEvents.onEach { event ->
            when (event) {
                is ReadListAdded, is ReadListDeleted -> reloadJobsFlow.tryEmit(Unit)
                is CollectionAdded, is CollectionDeleted -> reloadJobsFlow.tryEmit(Unit)

                else -> {}
            }
        }.launchIn(screenModelScope)
    }
}

enum class LibraryTab {
    SERIES,
    COLLECTIONS,
    READ_LISTS
}
