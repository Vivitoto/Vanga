package io.github.vivitoto.vanga.ui.settings.favoritesync

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

class FavoriteSyncSettingsScreen : Screen {
    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getFavoriteSyncSettingsViewModel() }
        LaunchedEffect(Unit) { vm.initialize() }
        val state = vm.state.collectAsState().value

        SettingsScreenContainer("收藏同步") {
            when (state) {
                is LoadState.Error -> Text("加载失败：${state.exception.message}")
                LoadState.Uninitialized, LoadState.Loading -> LoadingMaxSizeIndicator()
                is LoadState.Success -> FavoriteSyncSettingsContent(
                    enabled = vm.enabled.collectAsState().value,
                    webDavUrl = vm.webDavUrl.collectAsState().value,
                    username = vm.username.collectAsState().value,
                    password = vm.password.collectAsState().value,
                    remotePath = vm.remotePath.collectAsState().value,
                    lastSyncedAt = vm.lastSyncedAt.collectAsState().value,
                    busy = vm.busy.collectAsState().value,
                    onEnabledChange = vm::onEnabledChange,
                    onWebDavUrlChange = vm::onWebDavUrlChange,
                    onUsernameChange = vm::onUsernameChange,
                    onPasswordChange = vm::onPasswordChange,
                    onRemotePathChange = vm::onRemotePathChange,
                    onTestConnection = vm::testConnection,
                    onSyncToLocal = vm::syncToLocal,
                    onSyncToCloud = vm::syncToCloud,
                )
            }
        }
    }
}
