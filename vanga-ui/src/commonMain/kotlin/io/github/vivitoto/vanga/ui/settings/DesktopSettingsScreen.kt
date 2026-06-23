package io.github.vivitoto.vanga.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.transitions.CrossfadeTransition
import kotlinx.coroutines.flow.SharedFlow
import io.github.vivitoto.vanga.ui.LocalKeyEvents
import io.github.vivitoto.vanga.ui.LocalViewModelFactory
import io.github.vivitoto.vanga.ui.platform.BackPressHandler
import io.github.vivitoto.vanga.ui.platform.PlatformTitleBar
import io.github.vivitoto.vanga.ui.platform.cursorForHand
import io.github.vivitoto.vanga.ui.settings.appearance.AppSettingsScreen
import io.github.vivitoto.vanga.ui.settings.navigation.SettingsNavigationMenu

val settingsDesktopNavMenuWidth = 280.dp
val settingsDesktopContentWidth = 700.dp
val settingsDesktopTopPadding = 50.dp

class SettingsScreen : Screen {
    @Composable
    override fun Content() {
        val currentNavigator = LocalNavigator.currentOrThrow
        val viewModelFactory = LocalViewModelFactory.current
        val keyEvents: SharedFlow<KeyEvent> = LocalKeyEvents.current
        val vm = rememberScreenModel { viewModelFactory.getSettingsNavigationViewModel(currentNavigator) }

        LaunchedEffect(Unit) { vm.initialize() }
        LaunchedEffect(Unit) { keyEvents.collect { if (it.key == Key.Escape) currentNavigator.pop() } }

        Navigator(
            screen = AppSettingsScreen(),
            onBackPressed = null
        ) { navigator ->
            Column {
                PlatformTitleBar()
                SettingsScreenLayout(
                    navMenu = {
                        Row(
                            Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(top = settingsDesktopTopPadding, start = 10.dp, end = 10.dp)
                        ) {
                            Spacer(Modifier.weight(1f))
                            SettingsNavigationMenu(
                                currentScreen = navigator.lastItem,
                                onNavigation = { navigator.replaceAll(it) },
                                hasMediaErrors = vm.hasMediaErrors,
                                komfEnabled = vm.komfEnabledFlow.collectAsState().value,
                                updatesEnabled = vm.updatesEnabled,
                                newVersionIsAvailable = vm.newVersionIsAvailable,
                                onLogout = vm::logout,
                                contentColor = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .widthIn(max = settingsDesktopNavMenuWidth)
                                    .fillMaxWidth(),
                                user = vm.user.collectAsState().value
                            )
                        }
                    },
                    dismissButton = {
                        OutlinedIconButton(
                            onClick = { currentNavigator.pop() },
                            modifier = Modifier.cursorForHand().padding(top = settingsDesktopTopPadding),
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                            content = { Icon(Icons.Default.Close, contentDescription = "关闭设置") }
                        )
                    },
                    content = { CrossfadeTransition(navigator) },
                )

            }
        }
        BackPressHandler { currentNavigator.pop() }

    }

}
