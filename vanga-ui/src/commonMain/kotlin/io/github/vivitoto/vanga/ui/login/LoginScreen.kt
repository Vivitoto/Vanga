package io.github.vivitoto.vanga.ui.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.vivitoto.vanga.ui.LoadState.Error
import io.github.vivitoto.vanga.ui.LoadState.Loading
import io.github.vivitoto.vanga.ui.LoadState.Success
import io.github.vivitoto.vanga.ui.LoadState.Uninitialized
import io.github.vivitoto.vanga.ui.LocalOfflineMode
import io.github.vivitoto.vanga.ui.LocalPlatform
import io.github.vivitoto.vanga.ui.LocalViewModelFactory
import io.github.vivitoto.vanga.ui.MainScreen
import io.github.vivitoto.vanga.ui.login.offline.OfflineLoginScreen
import io.github.vivitoto.vanga.ui.platform.PlatformTitleBar
import io.github.vivitoto.vanga.ui.platform.PlatformType.DESKTOP
import io.github.vivitoto.vanga.ui.platform.PlatformType.MOBILE
import io.github.vivitoto.vanga.ui.platform.PlatformType.WEB_KOMF
import io.github.vivitoto.vanga.ui.settings.SettingsScreenContainer

class LoginScreen : Screen {

    @Composable
    override fun Content() {
        val rootNavigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow
        val platform = LocalPlatform.current
        val viewModelFactory = LocalViewModelFactory.current
        val isOffline = LocalOfflineMode.current
        val vm = rememberScreenModel(isOffline.value.toString()) { viewModelFactory.getLoginViewModel() }

        LaunchedEffect(Unit) { vm.initialize() }
        Column {
            PlatformTitleBar { }
            when (platform) {
                MOBILE, DESKTOP ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { ScreenContent(vm, rootNavigator) }

                WEB_KOMF -> SettingsScreenContainer(title = "登录 Vanga") {
                    ScreenContent(vm, rootNavigator)
                }
            }
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
        }
    }

    @Composable
    private fun ScreenContent(
        viewModel: LoginViewModel,
        rootNavigator: Navigator
    ) {
        val state = viewModel.state.collectAsState()

        when (state.value) {
            Loading, Uninitialized -> LoginLoadingContent(viewModel::cancel)

            is Error -> LoginContent(
                url = viewModel.url,
                onUrlChange = viewModel::url::set,
                user = viewModel.user,
                onUserChange = { viewModel.user = it },
                password = viewModel.password,
                onPasswordChange = { viewModel.password = it },
                userLoginError = viewModel.userLoginError,
                autoLoginError = viewModel.autoLoginError,
                onAutoLoginRetry = viewModel::retryAutoLogin,
                onLogin = viewModel::loginWithCredentials,
                offlineIsAvailable = viewModel.offlineIsAvailable.collectAsState().value,
                onOfflineSelect = { rootNavigator.replaceAll(OfflineLoginScreen()) },
                canGoOfflineAsCurrentUser = viewModel.canGoOfflineAsCurrentUser.collectAsState(false).value,
                goOfflineAsCurrentUser = viewModel::offlineLogin
            )

            is Success -> rootNavigator.replaceAll(MainScreen())
        }

    }
}
