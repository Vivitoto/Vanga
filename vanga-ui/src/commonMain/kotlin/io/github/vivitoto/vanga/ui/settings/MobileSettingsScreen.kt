package io.github.vivitoto.vanga.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.vivitoto.vanga.ui.LocalViewModelFactory
import io.github.vivitoto.vanga.ui.MobileTopContentPadding
import io.github.vivitoto.vanga.ui.settings.navigation.SettingsNavigationMenu

private val MobileSettingsBottomPadding = 24.dp

class MobileSettingsScreen : Screen {
    @Composable
    override fun Content() {
        val currentNavigator = LocalNavigator.currentOrThrow
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getSettingsNavigationViewModel(currentNavigator) }
        LaunchedEffect(Unit) { vm.initialize() }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp)
                    .padding(top = MobileTopContentPadding),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SettingsNavigationMenu(
                    currentScreen = currentNavigator.lastItem,
                    onNavigation = { currentNavigator.push(it) },
                    hasMediaErrors = vm.hasMediaErrors,
                    komfEnabled = vm.komfEnabledFlow.collectAsState().value,
                    updatesEnabled = vm.updatesEnabled,
                    newVersionIsAvailable = vm.newVersionIsAvailable,
                    onLogout = vm::logout,
                    user = vm.user.collectAsState().value,
                    contentColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f, false),
                    contentPadding = PaddingValues(bottom = MobileSettingsBottomPadding),
                )
            }
        }
    }
}
