package io.github.vivitoto.vanga.ui.settings.announcements

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import io.github.vivitoto.vanga.ui.LoadState
import io.github.vivitoto.vanga.ui.LoadState.Error
import io.github.vivitoto.vanga.ui.LoadState.Loading
import io.github.vivitoto.vanga.ui.LoadState.Success
import io.github.vivitoto.vanga.ui.LocalViewModelFactory
import io.github.vivitoto.vanga.ui.common.components.LoadingMaxSizeIndicator
import io.github.vivitoto.vanga.ui.settings.SettingsScreenContainer

class AnnouncementsScreen : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getAnnouncementsViewModel() }
        val state = vm.state.collectAsState()

        SettingsScreenContainer("公告") {
            when (val result = state.value) {
                is Success -> AnnouncementsContent(result.value.items)
                LoadState.Uninitialized, Loading -> LoadingMaxSizeIndicator()

                is Error -> Text("加载失败")
            }
        }

    }
}
