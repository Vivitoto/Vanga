package io.github.vivitoto.vanga.ui.settings.authactivity

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import io.github.vivitoto.vanga.ui.LoadState.Error
import io.github.vivitoto.vanga.ui.LoadState.Loading
import io.github.vivitoto.vanga.ui.LoadState.Success
import io.github.vivitoto.vanga.ui.LoadState.Uninitialized
import io.github.vivitoto.vanga.ui.LocalViewModelFactory
import io.github.vivitoto.vanga.ui.common.components.LoadingMaxSizeIndicator
import io.github.vivitoto.vanga.ui.settings.SettingsScreenContainer

class AuthenticationActivityScreen(val forMe: Boolean) : Screen {
    override val key: ScreenKey = "SettingsAuthActivityScreen$forMe"

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel(forMe.toString()) { viewModelFactory.getAuthenticationActivityViewModel(forMe) }
        LaunchedEffect(forMe) { vm.initialize() }

        SettingsScreenContainer(if (forMe) "我的登录记录" else "服务器登录记录") {
            when (val state = vm.state.collectAsState().value) {
                Uninitialized, Loading -> LoadingMaxSizeIndicator()
                is Error -> Text(state.exception.message ?: "加载失败")
                is Success -> AuthenticationActivityContent(
                    activity = vm.activity,
                    forMe = forMe,
                    totalPages = vm.totalPages,
                    currentPage = vm.currentPage,
                    onPageChange = vm::loadPage
                )
            }
        }
    }
}
