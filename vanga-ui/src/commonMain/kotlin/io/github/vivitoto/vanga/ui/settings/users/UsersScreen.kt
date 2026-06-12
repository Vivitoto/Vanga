package io.github.vivitoto.vanga.ui.settings.users

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import io.github.vivitoto.vanga.ui.LoadState.Error
import io.github.vivitoto.vanga.ui.LoadState.Loading
import io.github.vivitoto.vanga.ui.LoadState.Success
import io.github.vivitoto.vanga.ui.LoadState.Uninitialized
import io.github.vivitoto.vanga.ui.LocalViewModelFactory
import io.github.vivitoto.vanga.ui.common.components.LoadingMaxSizeIndicator
import io.github.vivitoto.vanga.ui.settings.SettingsScreenContainer

class UsersScreen : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getUsersViewModel() }
        LaunchedEffect(Unit) { vm.initialize() }

        SettingsScreenContainer("用户管理") {
            when (vm.state.collectAsState().value) {
            is Error -> Text("加载失败")
                Uninitialized, Loading -> LoadingMaxSizeIndicator()

                is Success -> UsersContent(
                    currentUser = vm.currentUser,
                    users = vm.users,
                    onUserReloadRequest = vm::loadUserList,
                    onUserDelete = vm::onUserDelete
                )
            }
        }
    }
}
