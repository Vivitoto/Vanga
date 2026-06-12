package io.github.vivitoto.vanga.ui.settings.epub

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import io.github.vivitoto.vanga.ui.LoadState
import io.github.vivitoto.vanga.ui.LocalViewModelFactory
import io.github.vivitoto.vanga.ui.common.components.ErrorContent
import io.github.vivitoto.vanga.ui.common.components.LoadingMaxSizeIndicator
import io.github.vivitoto.vanga.ui.settings.SettingsScreenContainer

class EpubReaderSettingsScreen : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getEpubReaderSettingsViewModel() }
        LaunchedEffect(Unit) {
            vm.initialize()
        }
        SettingsScreenContainer(title = "EPUB 阅读器") {
            when (val result = vm.state.collectAsState().value) {
                is LoadState.Error -> ErrorContent(result.exception)
                LoadState.Uninitialized, LoadState.Loading -> LoadingMaxSizeIndicator()
                is LoadState.Success<Unit> -> EpubReaderSettingsContent(
                    vm.selectedEpubReader.collectAsState().value,
                    vm::onSelectedTypeChange
                )
            }

        }
    }
}
