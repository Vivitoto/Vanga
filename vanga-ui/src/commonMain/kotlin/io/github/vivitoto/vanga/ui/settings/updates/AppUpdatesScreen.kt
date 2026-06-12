package io.github.vivitoto.vanga.ui.settings.updates

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import io.github.vivitoto.vanga.ui.LoadState
import io.github.vivitoto.vanga.ui.LocalViewModelFactory
import io.github.vivitoto.vanga.ui.settings.SettingsScreenContainer
import io.github.vivitoto.vanga.updates.AppVersion

class AppUpdatesScreen : Screen {
    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getSettingsUpdatesViewModel() }
        LaunchedEffect(Unit) { vm.initialize() }

        val state = vm.state.collectAsState().value
        SettingsScreenContainer("版本更新") {
            when (state) {
                is LoadState.Error -> Text("加载失败：${state.exception.message}")
                LoadState.Loading, LoadState.Uninitialized, is LoadState.Success -> AppUpdatesContent(
                    checkForUpdates = vm.checkForUpdatesOnStartup.collectAsState().value,
                    onCheckForUpdatesChange = vm::onCheckForUpdatesOnStartupChange,
                    currentVersion = AppVersion.current,
                    releases = vm.releases.collectAsState().value,

                    latestVersion = vm.latestVersion.collectAsState().value,
                    lastChecked = vm.lastUpdateCheck.collectAsState().value,
                    onCheckForUpdates = vm::checkForUpdates,
                    versionCheckInProgress = state == LoadState.Loading,
                    onUpdate = vm::onUpdate,
                    onUpdateCancel = vm::onUpdateCancel,
                    downloadProgress = vm.downloadProgress.collectAsState().value
                )
            }

        }
    }
}
