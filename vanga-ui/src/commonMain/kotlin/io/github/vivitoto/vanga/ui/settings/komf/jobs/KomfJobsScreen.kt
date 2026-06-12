package io.github.vivitoto.vanga.ui.settings.komf.jobs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.vivitoto.vanga.ui.LoadState
import io.github.vivitoto.vanga.ui.LocalViewModelFactory
import io.github.vivitoto.vanga.ui.MainScreen
import io.github.vivitoto.vanga.ui.error.formatExceptionMessage
import io.github.vivitoto.vanga.ui.series.seriesScreen
import io.github.vivitoto.vanga.ui.settings.SettingsScreenContainer

class KomfJobsScreen(private val enableSeriesResolution: Boolean = true) : Screen {

    @OptIn(InternalVoyagerApi::class)
    @Composable
    override fun Content() {
        val rootNavigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getKomfJobsViewModel() }
        LaunchedEffect(Unit) { vm.initialize() }
        val state = vm.state.collectAsState().value

        SettingsScreenContainer(title = "元数据更新任务") {
            when (state) {
                is LoadState.Error -> Text(formatExceptionMessage(state.exception))
                LoadState.Uninitialized, LoadState.Loading, is LoadState.Success -> KomfJobsContent(
                    jobs = vm.jobs,
                    totalPages = vm.totalPages,
                    currentPage = vm.currentPage,
                    onPageChange = vm::loadPage,
                    selectedStatus = vm.status,
                    onStatusSelect = vm::onStatusSelect,
                    getSeries = if (enableSeriesResolution) vm::getSeries else null,
                    onSeriesClick = {
                        rootNavigator.popUntilRoot()
                        rootNavigator.dispose(rootNavigator.lastItem)
                        rootNavigator.replaceAll(MainScreen(seriesScreen(it)))
                    },
                    onDeleteAll = vm::onDeleteAll,
                    isLoading = state == LoadState.Loading || state == LoadState.Uninitialized
                )
            }
        }
    }
}