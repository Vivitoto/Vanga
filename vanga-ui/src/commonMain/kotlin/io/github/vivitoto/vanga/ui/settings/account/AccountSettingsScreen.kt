package io.github.vivitoto.vanga.ui.settings.account

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import io.github.vivitoto.vanga.ui.LocalViewModelFactory
import io.github.vivitoto.vanga.ui.settings.SettingsScreenContainer

class AccountSettingsScreen : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getAccountViewModel() }
        SettingsScreenContainer(title = "我的账号") {
            AccountSettingsContent(user = vm.user)
        }
    }
}
