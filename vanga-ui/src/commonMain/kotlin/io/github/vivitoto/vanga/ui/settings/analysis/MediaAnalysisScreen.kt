package io.github.vivitoto.vanga.ui.settings.analysis

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.vivitoto.vanga.ui.LoadState.Error
import io.github.vivitoto.vanga.ui.LoadState.Loading
import io.github.vivitoto.vanga.ui.LoadState.Success
import io.github.vivitoto.vanga.ui.LoadState.Uninitialized
import io.github.vivitoto.vanga.ui.LocalViewModelFactory
import io.github.vivitoto.vanga.ui.MainScreen
import io.github.vivitoto.vanga.ui.book.bookScreen
import io.github.vivitoto.vanga.ui.common.components.LoadingMaxSizeIndicator
import io.github.vivitoto.vanga.ui.settings.SettingsScreenContainer

class MediaAnalysisScreen : Screen {
    @Composable
    @OptIn(InternalVoyagerApi::class)
    override fun Content() {
        val rootNavigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getMediaAnalysisViewModel() }
        LaunchedEffect(Unit) { vm.initialize() }

        SettingsScreenContainer("媒体管理") {
            when (val state = vm.state.collectAsState().value) {
                Uninitialized, Loading -> LoadingMaxSizeIndicator()
                is Error -> Text(state.exception.message ?: "Error")
                is Success -> MediaAnalysisContent(
                    books = vm.books,
                    onBookClick = {
                        rootNavigator.pop()
                        rootNavigator.dispose(rootNavigator.lastItem)
                        rootNavigator.replaceAll(MainScreen(bookScreen(it)))
                    },
                    currentPage = vm.currentPage,
                    totalPages = vm.totalPages,
                    onPageChange = vm::loadPage
                )
            }
        }
    }
}
