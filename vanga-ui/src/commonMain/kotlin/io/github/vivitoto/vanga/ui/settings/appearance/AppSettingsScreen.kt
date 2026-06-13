package io.github.vivitoto.vanga.ui.settings.appearance

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import io.github.vivitoto.vanga.ui.LoadState
import io.github.vivitoto.vanga.ui.LocalViewModelFactory
import io.github.vivitoto.vanga.ui.common.components.LoadingMaxSizeIndicator
import io.github.vivitoto.vanga.ui.settings.SettingsScreenContainer

class AppSettingsScreen : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getAppearanceViewModel() }
        LaunchedEffect(Unit) { vm.initialize() }
        val state = vm.state.collectAsState()

        SettingsScreenContainer("外观") {
            when (val result = state.value) {
                is LoadState.Error -> Text("${result::class.simpleName}: ${result.exception.message}")
                LoadState.Uninitialized, LoadState.Loading -> LoadingMaxSizeIndicator()
                is LoadState.Success -> AppearanceSettingsContent(
                    cardWidth = vm.cardWidth,
                    onCardWidthChange = vm::onCardWidthChange,
                    currentTheme = vm.currentTheme,
                    onThemeChange = vm::onAppThemeChange,
                    libraryCoversBlurred = vm.libraryCoversBlurred,
                    onLibraryCoversBlurredChange = vm::onLibraryCoversBlurredChange,
                    collectionCoversBlurred = vm.collectionCoversBlurred,
                    onCollectionCoversBlurredChange = vm::onCollectionCoversBlurredChange,
                    bookCoversBlurred = vm.bookCoversBlurred,
                    onBookCoversBlurredChange = vm::onBookCoversBlurredChange,
                )
            }
        }
    }
}
