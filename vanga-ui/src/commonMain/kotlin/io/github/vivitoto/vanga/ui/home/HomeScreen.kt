package io.github.vivitoto.vanga.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.vivitoto.vanga.ui.LoadState
import io.github.vivitoto.vanga.ui.LocalKomgaState
import io.github.vivitoto.vanga.ui.LocalOfflineMode
import io.github.vivitoto.vanga.ui.LocalReloadEvents
import io.github.vivitoto.vanga.ui.LocalViewModelFactory
import io.github.vivitoto.vanga.ui.ReloadableScreen
import io.github.vivitoto.vanga.ui.book.bookScreen
import io.github.vivitoto.vanga.ui.common.components.ErrorContent
import io.github.vivitoto.vanga.ui.home.edit.FilterEditScreen
import io.github.vivitoto.vanga.ui.platform.ScreenPullToRefreshBox
import io.github.vivitoto.vanga.ui.series.seriesScreen
import snd.komga.client.library.KomgaLibraryId

class HomeScreen(private val libraryId: KomgaLibraryId? = null) : ReloadableScreen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val isOffline = LocalOfflineMode.current.value
        val serverUrl = LocalKomgaState.current.serverUrl.value

        val vmKey = remember(libraryId, isOffline, serverUrl) {
            buildString {
                libraryId?.let { append(it.value) }
                append(serverUrl)
                append(isOffline.toString())
            }
        }
        val vm = rememberScreenModel(vmKey) { viewModelFactory.getHomeViewModel() }
        val navigator = LocalNavigator.currentOrThrow
        val reloadEvents = LocalReloadEvents.current

        LaunchedEffect(Unit) {
            vm.initialize()
            reloadEvents.collect { vm.reload() }
        }

        DisposableEffect(Unit) {
            vm.startKomgaEventsHandler()
            onDispose { vm.stopKomgaEventsHandler() }
        }

        ScreenPullToRefreshBox(screenState = vm.state, onRefresh = vm::reload) {
            when (val state = vm.state.collectAsState().value) {
                is LoadState.Error -> ErrorContent(
                    message = state.exception.message ?: "未知错误",
                    onReload = vm::reload
                )

                else ->
                    HomeContent(
                        filters = vm.currentFilters.collectAsState().value,
                        activeFilterNumber = vm.activeFilterNumber.collectAsState().value,
                        onFilterChange = vm::onFilterChange,
                        onEditStart = { navigator.replaceAll(FilterEditScreen(vm.currentFilters.value)) },
                        selectionMode = vm.selectionMode.collectAsState().value,
                        selectedItems = vm.selectedItems.collectAsState().value,
                        onSelectionModeChange = vm::setSelectionMode,
                        onSelectedItemSelect = vm::onSelectedItemSelect,

                        cardWidth = vm.cardWidth.collectAsState().value,
                        onSeriesClick = { navigator push seriesScreen(it) },
                        onBookClick = { navigator push bookScreen(it) },
                    )

            }
        }
    }
}
