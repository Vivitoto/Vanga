package io.github.vivitoto.vanga.ui.settings.komf.providers

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.vivitoto.vanga.ui.LoadState
import io.github.vivitoto.vanga.ui.LocalStrings
import io.github.vivitoto.vanga.ui.LocalViewModelFactory
import io.github.vivitoto.vanga.ui.common.components.LoadingMaxSizeIndicator
import io.github.vivitoto.vanga.ui.error.formatExceptionMessage
import io.github.vivitoto.vanga.ui.settings.SettingsScreenContainer
import snd.komf.api.mediaserver.KomfMediaServerLibraryId

class KomfProvidersSettingsScreen : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getKomfProvidersViewModel() }
        val vmState = vm.state.collectAsState().value
        val komfConfigLoadError = vm.komfSharedState.configError.collectAsState().value
        LaunchedEffect(Unit) { vm.initialize() }
        SettingsScreenContainer(title = "元数据源设置") {

            if (komfConfigLoadError != null) {
                Text(formatExceptionMessage(komfConfigLoadError))
                return@SettingsScreenContainer
            }

            when (vmState) {
                is LoadState.Error -> Text(formatExceptionMessage(vmState.exception))
                LoadState.Loading, LoadState.Uninitialized -> LoadingMaxSizeIndicator()
                is LoadState.Success -> KomfProvidersSettingsContent(
                    defaultProcessingState = vm.defaultProvidersConfig,
                    libraryProcessingState = vm.libraryProvidersConfigs,
                    onLibraryConfigAdd = vm::onNewLibraryTabAdd,
                    onLibraryConfigRemove = vm::onLibraryTabRemove,
                    libraries = vm.libraries.collectAsState(emptyList()).value,
                    nameMatchingMode = vm.nameMatchingMode,
                    onNameMatchingModeChange = vm::onNameMatchingModeChange,
                    comicVineClientId = vm.comicVineClientId,
                    onComicVineClientIdSave = vm::onComicVineClientIdChange,
                    malClientId = vm.malClientId,
                    onMalClientIdSave = vm::onMalClientIdChange,
                    mangaBakaDbMetadata = vm.mangaBakaDbMetadata,
                    onMangaBakaUpdate = vm::onMangaBakaDbUpdate
                )
            }

        }
    }
}

class KomfProviderDetailSettingsScreen(
    private val providerKey: String,
    private val libraryId: String? = null,
) : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val navigator = LocalNavigator.currentOrThrow
        val strings = LocalStrings.current.komf.providerSettings
        val vm = rememberScreenModel { viewModelFactory.getKomfProvidersViewModel() }
        val vmState = vm.state.collectAsState().value
        val komfConfigLoadError = vm.komfSharedState.configError.collectAsState().value
        LaunchedEffect(Unit) { vm.initialize() }

        SettingsScreenContainer(title = strings.forProviderKey(providerKey)) {
            TextButton(onClick = { navigator.pop() }) {
                Text("返回数据源列表")
            }

            if (komfConfigLoadError != null) {
                Text(formatExceptionMessage(komfConfigLoadError))
                return@SettingsScreenContainer
            }

            when (vmState) {
                is LoadState.Error -> Text(formatExceptionMessage(vmState.exception))
                LoadState.Loading, LoadState.Uninitialized -> LoadingMaxSizeIndicator()
                is LoadState.Success -> {
                    val providersConfigState = if (libraryId == null) {
                        vm.defaultProvidersConfig
                    } else {
                        vm.libraryProvidersConfigs[KomfMediaServerLibraryId(libraryId)]
                    }

                    if (providersConfigState == null) {
                        Text("未找到媒体库数据源设置")
                        return@SettingsScreenContainer
                    }

                    val providerState = providersConfigState.providerByKey(providerKey)

                    if (providerState == null) {
                        Text("未找到数据源设置")
                    } else {
                        ProviderDetailSettingsContent(providerState)
                    }
                }
            }
        }
    }
}
