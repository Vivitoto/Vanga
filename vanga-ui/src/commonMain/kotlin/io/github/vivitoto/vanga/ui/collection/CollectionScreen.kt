package io.github.vivitoto.vanga.ui.collection

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.vivitoto.vanga.ui.LoadState.Error
import io.github.vivitoto.vanga.ui.LoadState.Loading
import io.github.vivitoto.vanga.ui.LoadState.Success
import io.github.vivitoto.vanga.ui.LoadState.Uninitialized
import io.github.vivitoto.vanga.ui.LocalReloadEvents
import io.github.vivitoto.vanga.ui.LocalViewModelFactory
import io.github.vivitoto.vanga.ui.ReloadableScreen
import io.github.vivitoto.vanga.ui.common.components.LoadingMaxSizeIndicator
import io.github.vivitoto.vanga.ui.platform.BackPressHandler
import io.github.vivitoto.vanga.ui.series.seriesScreen
import snd.komga.client.collection.KomgaCollectionId

class CollectionScreen(val collectionId: KomgaCollectionId) : ReloadableScreen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel(collectionId.value) { viewModelFactory.getCollectionViewModel(collectionId) }
        val reloadEvents = LocalReloadEvents.current
        LaunchedEffect(collectionId) {
            vm.initialize()
            reloadEvents.collect { vm.reload() }
        }
        DisposableEffect(Unit) {
            vm.startKomgaEventsHandler()
            onDispose { vm.stopKomgaEventHandler() }
        }

        val navigator = LocalNavigator.currentOrThrow

        when (vm.state.collectAsState().value) {
            Uninitialized -> LoadingMaxSizeIndicator()
            is Success, Loading -> {
                val collection = vm.collection
                if (collection == null) {
                    LoadingMaxSizeIndicator()
                } else
                    CollectionContent(
                        collection = collection,
                        onCollectionDelete = vm::onCollectionDelete,

                        series = vm.series,
                        seriesActions = vm.seriesMenuActions(),
                        onSeriesClick = { navigator push seriesScreen(it) },

                        selectedSeries = vm.selectedSeries,
                        onSeriesSelect = vm::onSeriesSelect,

                        editMode = vm.isInEditMode,
                        onEditModeChange = vm::setEditMode,
                        onReorder = vm::onSeriesReorder,
                        onReorderDragStateChange = vm::onSeriesReorderDragStateChange,

                        totalSeriesCount = vm.totalSeriesCount,
                        totalPages = vm.totalSeriesPages,
                        currentPage = vm.currentSeriesPage,
                        pageSize = vm.pageLoadSize,
                        onPageChange = vm::onPageChange,
                        onPageSizeChange = vm::onPageSizeChange,

                        cardMinSize = vm.cardWidth.collectAsState().value,
                    )
            }

            is Error -> Text("Error")
        }

        BackPressHandler { navigator.pop() }

    }
}