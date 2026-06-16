package io.github.vivitoto.vanga.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.vivitoto.vanga.ui.LocalViewModelFactory
import io.github.vivitoto.vanga.ui.platform.BackPressHandler
import io.github.vivitoto.vanga.ui.platform.PlatformTitleBar
import io.github.vivitoto.vanga.ui.settings.navigation.SettingsNavigationMenu

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
                modifier = Modifier.padding(horizontal = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PlatformTitleBar()
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentNavigator.pop() }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, null)
                    }
                    Text("设置", style = MaterialTheme.typography.titleLarge)
                }

                SettingsCard(
                    modifier = Modifier.weight(1f, false),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
                    contentPadding = PaddingValues(8.dp),
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
                    )
                }

                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
            }
        }
        BackPressHandler { currentNavigator.pop() }
    }
}
