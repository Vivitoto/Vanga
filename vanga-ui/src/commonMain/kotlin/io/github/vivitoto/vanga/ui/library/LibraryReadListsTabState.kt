package io.github.vivitoto.vanga.ui.library

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import io.github.vivitoto.vanga.AppNotifications
import io.github.vivitoto.vanga.komga.api.KomgaReadListApi
import io.github.vivitoto.vanga.ui.LoadState
import io.github.vivitoto.vanga.ui.LoadState.Loading
import io.github.vivitoto.vanga.ui.LoadState.Uninitialized
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.readlist.KomgaReadList
import snd.komga.client.readlist.KomgaReadListId
import snd.komga.client.sse.KomgaEvent

class LibraryReadListsTabState(
    private val readListApi: KomgaReadListApi,
    private val appNotifications: AppNotifications,
    private val komgaEvents: SharedFlow<KomgaEvent>,
    val library: StateFlow<KomgaLibrary?>?,
    val cardWidth: StateFlow<Dp>,
) : StateScreenModel<LoadState<Unit>>(Uninitialized) {

    var readLists: List<KomgaReadList> by mutableStateOf(emptyList())
        private set
    var totalPages by mutableStateOf(1)
        private set
    var totalReadLists by mutableStateOf(0)
        private set
    var currentPage by mutableStateOf(1)
        private set
    var pageSize by mutableStateOf(50)
        private set

    private val reloadEventsEnabled = MutableStateFlow(true)
    private val pendingKomgaReload = MutableStateFlow(false)
    private val readListsReloadJobsFlow = MutableSharedFlow<Unit>(1, 0, BufferOverflow.DROP_OLDEST)

    fun initialize() {
        if (state.value !is Uninitialized) return
        screenModelScope.launch { loadReadLists(1) }
        startKomgaEventListener()

        readListsReloadJobsFlow.onEach {
            if (processKomgaReload()) delay(1000)
        }.launchIn(screenModelScope)
    }

    fun reload() {
        screenModelScope.launch { loadReadLists(1, showLoading = true) }
    }

    fun onReadListDelete(readListId: KomgaReadListId) {
        appNotifications.runCatchingToNotifications(screenModelScope) {
            readListApi.deleteOne(readListId)
        }
    }

    fun onPageChange(pageNumber: Int) {
        screenModelScope.launch { loadReadLists(pageNumber) }
    }

    fun onPageSizeChange(pageSize: Int) {
        this.pageSize = pageSize
        screenModelScope.launch { loadReadLists(1) }
    }

    private suspend fun processKomgaReload(): Boolean {
        if (!reloadEventsEnabled.value) {
            pendingKomgaReload.value = true
            return false
        }

        pendingKomgaReload.value = false
        loadReadLists(currentPage, showLoading = false)
        return true
    }

    private suspend fun loadReadLists(page: Int, showLoading: Boolean = true) {
        appNotifications.runCatchingToNotifications {

            if (showLoading && totalReadLists > pageSize) mutableState.value = Loading

            val library = this.library?.value
            val libraryIds = if (library != null) listOf(library.id) else emptyList()
            val pageRequest = KomgaPageRequest(unpaged = true)
            val visibleReadLists = readListApi.getAll(libraryIds = libraryIds, pageRequest = pageRequest).content
            val visibleTotalPages = ((visibleReadLists.size + pageSize - 1) / pageSize).coerceAtLeast(1)
            val visiblePage = page.coerceIn(1, visibleTotalPages)

            currentPage = visiblePage
            totalPages = visibleTotalPages
            totalReadLists = visibleReadLists.size
            readLists = visibleReadLists.drop((visiblePage - 1) * pageSize).take(pageSize)
            mutableState.value = LoadState.Success(Unit)

        }.onFailure { mutableState.value = LoadState.Error(it) }
    }

    fun stopKomgaEventHandler() {
        reloadEventsEnabled.value = false
    }

    fun startKomgaEventHandler() {
        reloadEventsEnabled.value = true
        if (pendingKomgaReload.value) readListsReloadJobsFlow.tryEmit(Unit)
    }

    private fun startKomgaEventListener() {
        komgaEvents.onEach {
            when (it) {
                is KomgaEvent.ReadListEvent -> readListsReloadJobsFlow.tryEmit(Unit)
                else -> {}
            }
        }.launchIn(screenModelScope)
    }
}
