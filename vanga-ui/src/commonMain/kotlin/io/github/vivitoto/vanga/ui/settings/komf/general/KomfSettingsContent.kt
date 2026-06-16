package io.github.vivitoto.vanga.ui.settings.komf.general

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import io.github.vivitoto.vanga.ui.common.components.DropdownMultiChoiceMenu
import io.github.vivitoto.vanga.ui.common.components.LabeledEntry
import io.github.vivitoto.vanga.ui.platform.cursorForHand
import io.github.vivitoto.vanga.ui.settings.SettingsRow
import io.github.vivitoto.vanga.ui.settings.SettingsSectionCard
import io.github.vivitoto.vanga.ui.settings.SettingsSwitchRow
import io.github.vivitoto.vanga.ui.settings.komf.SavableHttpTextField
import io.github.vivitoto.vanga.ui.settings.komf.SavableTextField
import snd.komf.api.mediaserver.KomfMediaServerLibrary
import snd.komf.api.mediaserver.KomfMediaServerLibraryId

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KomfSettingsContent(
    komfEnabled: Boolean,
    onKomfEnabledChange: suspend (Boolean) -> Unit,
    komfUrl: String,
    onKomfUrlChange: (String) -> Unit,
    integrationToggleEnabled: Boolean,
    komfConnectionError: String?,
    komgaState: KomgaConnectionState?,
    kavitaState: KavitaConnectionState?,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val uriHandler = LocalUriHandler.current
        val coroutineScope = rememberCoroutineScope()
        var komfEnabledConfirmed by remember { mutableStateOf(komfEnabled || !integrationToggleEnabled) }
        if (integrationToggleEnabled) {
            SettingsSectionCard(
                title = "Komf 集成",
            ) {
                SettingsSwitchRow(
                    title = "启用 Komf 集成",
                    checked = komfEnabled,
                    onCheckedChange = {
                        coroutineScope.launch {
                            onKomfEnabledChange(it)
                            komfEnabledConfirmed = true
                        }
                    },
                )

                SettingsRow(
                    title = "项目主页",
                    trailing = {
                        ElevatedButton(
                            onClick = { uriHandler.openUri("https://github.com/Snd-R/komf") },
                        ) {
                            Text("打开")
                        }
                    }
                )
            }
        }

        AnimatedVisibility(komfEnabled || !integrationToggleEnabled) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SettingsSectionCard(
                    title = "Komf 服务连接",
                ) {
                    KomfConnectionDetails(
                        komfUrl = komfUrl,
                        onKomfUrlChange = onKomfUrlChange,
                        komfConnectionError = komfConnectionError
                    )
                }

                AnimatedVisibility(komfConnectionError == null) {
                    SettingsSectionCard(
                        title = "媒体服务器连接",
                    ) {
                        when {
                            komgaState != null && kavitaState != null -> KomgaAndKavitaConnectionSettings(
                                komgaState = komgaState,
                                kavitaState = kavitaState
                            )

                            komgaState != null -> KomgaConnectionDetails(komgaState)
                            kavitaState != null -> KavitaConnectionDetails(kavitaState)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KomgaAndKavitaConnectionSettings(
    komgaState: KomgaConnectionState,
    kavitaState: KavitaConnectionState,
) {
    var selectedTab by remember { mutableStateOf(0) }
    SecondaryTabRow(selectedTabIndex = selectedTab) {
        Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            modifier = Modifier.heightIn(min = 40.dp).cursorForHand(),
        ) {
            Text("Komga")
        }
        Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            modifier = Modifier.heightIn(min = 40.dp).cursorForHand(),
        ) {
            Text("Kavita")
        }
    }

    when (selectedTab) {
        0 -> KomgaConnectionDetails(komgaState)
        1 -> KavitaConnectionDetails(kavitaState)
    }

}

@Composable
private fun KomfConnectionDetails(
    komfUrl: String,
    onKomfUrlChange: (String) -> Unit,
    komfConnectionError: String?,
) {
    SavableHttpTextField(
        label = "Komf URL",
        currentValue = komfUrl,
        onValueSave = onKomfUrlChange,
        confirmationText = "连接",
        isError = komfConnectionError != null,
        supportingText = {
            if (komfConnectionError != null) {
                Text(komfConnectionError)
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("已连接", color = MaterialTheme.colorScheme.secondary)
                    Icon(Icons.Default.Check, null)
                }
            }
        }
    )
}

@Composable
private fun KomgaConnectionDetails(
    state: KomgaConnectionState,
) {
    val baseUrl = state.baseUrl.collectAsState().value
    val onBaseUrlChange = state::onKomgaBaseUrlChange
    val username = state.username.collectAsState().value
    val onUsernameChange = state::onKomgaUsernameChange
    val onPasswordChange = state::onKomgaPasswordUpdate
    val connectionError = state.connectionError.collectAsState().value
    val enableEventListener = state.enableEventListener.collectAsState().value
    val onEnableEventListenerChange = state::onEventListenerEnable
    val metadataLibrariesFilter = state.metadataLibraryFilters.collectAsState().value
    val onMetadataLibraryFilterSelect = state::onMetadataLibraryFilterSelect
    val notificationsFilter = state.notificationsLibraryFilters.collectAsState().value
    val onNotificationsLibraryFilterSelect = state::onNotificationsLibraryFilterSelect
    val libraries = state.libraries.collectAsState(emptyList()).value

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (connectionError != null) {
            Text(connectionError, color = MaterialTheme.colorScheme.error)
        }

        SavableHttpTextField(
            label = "Komga URL",
            confirmationText = "保存",
            currentValue = baseUrl,
            onValueSave = onBaseUrlChange,
        )

        SavableTextField(
            currentValue = username,
            onValueSave = onUsernameChange,
            label = { Text("Komga 用户名") },
        )

        SavableTextField(
            currentValue = "",
            onValueSave = onPasswordChange,
            label = { Text("Komga 密码") },
            useEditButton = true,
            isPassword = true
        )
    }
    MediaServerEventListenerSettings(
        enableEventListener = enableEventListener,
        onEnableEventListenerChange = onEnableEventListenerChange,
        metadataLibrariesFilter = metadataLibrariesFilter,
        onMetadataLibraryFilterSelect = onMetadataLibraryFilterSelect,
        notificationsFilter = notificationsFilter,
        onNotificationsLibraryFilterSelect = onNotificationsLibraryFilterSelect,
        libraries = libraries
    )
}

@Composable
private fun KavitaConnectionDetails(
    state: KavitaConnectionState,
) {
    val baseUrl = state.baseUrl.collectAsState().value
    val onBaseUrlChange = state::onBaseUrlChange
    val onPasswordChange = state::onApiKeyUpdate
    val connectionError = state.connectionError.collectAsState().value
    val enableEventListener = state.enableEventListener.collectAsState().value
    val onEnableEventListenerChange = state::onEventListenerEnable
    val metadataLibrariesFilter = state.metadataLibraryFilters.collectAsState().value
    val onMetadataLibraryFilterSelect = state::onMetadataLibraryFilterSelect
    val notificationsFilter = state.notificationsLibraryFilters.collectAsState().value
    val onNotificationsLibraryFilterSelect = state::onNotificationsLibraryFilterSelect
    val libraries = state.libraries.collectAsState(emptyList()).value

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (connectionError != null) {
            Text(connectionError, color = MaterialTheme.colorScheme.error)
        }

        SavableHttpTextField(
            label = "Kavita URL",
            confirmationText = "保存",
            currentValue = baseUrl,
            onValueSave = onBaseUrlChange,
        )

        SavableTextField(
            currentValue = "",
            onValueSave = onPasswordChange,
            label = { Text("Kavita API 密钥") },
            useEditButton = true,
            isPassword = true
        )
    }

    MediaServerEventListenerSettings(
        enableEventListener = enableEventListener,
        onEnableEventListenerChange = onEnableEventListenerChange,
        metadataLibrariesFilter = metadataLibrariesFilter,
        onMetadataLibraryFilterSelect = onMetadataLibraryFilterSelect,
        notificationsFilter = notificationsFilter,
        onNotificationsLibraryFilterSelect = onNotificationsLibraryFilterSelect,
        libraries = libraries
    )


}

@Composable
private fun MediaServerEventListenerSettings(
    enableEventListener: Boolean,
    onEnableEventListenerChange: (Boolean) -> Unit,
    metadataLibrariesFilter: List<KomfMediaServerLibraryId>,
    onMetadataLibraryFilterSelect: (KomfMediaServerLibraryId) -> Unit,
    notificationsFilter: List<KomfMediaServerLibraryId>,
    onNotificationsLibraryFilterSelect: (KomfMediaServerLibraryId) -> Unit,
    libraries: List<KomfMediaServerLibrary>
) {
    SettingsSectionCard(
        title = "事件监听",
        description = "新增作品或单本时启动处理任务。",
    ) {
        SettingsSwitchRow(
            title = "启用事件监听",
            checked = enableEventListener,
            onCheckedChange = onEnableEventListenerChange,
        )

        AnimatedVisibility(enableEventListener) {
            EventListenerContent(
                metadataLibrariesFilter = metadataLibrariesFilter,
                onMetadataLibraryFilterSelect = onMetadataLibraryFilterSelect,
                notificationsFilter = notificationsFilter,
                onNotificationsLibraryFilterSelect = onNotificationsLibraryFilterSelect,
                libraries = libraries
            )
        }
    }
}

@Composable
private fun EventListenerContent(
    metadataLibrariesFilter: List<KomfMediaServerLibraryId>,
    onMetadataLibraryFilterSelect: (KomfMediaServerLibraryId) -> Unit,
    notificationsFilter: List<KomfMediaServerLibraryId>,
    onNotificationsLibraryFilterSelect: (KomfMediaServerLibraryId) -> Unit,
    libraries: List<KomfMediaServerLibrary>,
) {
    val libraryOptions = remember(libraries) {
        val ids = libraries.map { it.id }
        val unknown = metadataLibrariesFilter.filter { it !in ids }
            .map { LabeledEntry(it, "未知书库：${it.value}") }
        libraries.map { LabeledEntry(it.id, it.name) }.plus(unknown)
    }
    val metadataSelectedOptions = remember(metadataLibrariesFilter, libraries) {
        metadataLibrariesFilter.map { libraryId ->
            LabeledEntry(
                value = libraryId,
                label = libraries.find { it.id == libraryId }?.name
                    ?: "未知书库：${libraryId.value}"
            )
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        DropdownMultiChoiceMenu(
            selectedOptions = metadataSelectedOptions,
            options = libraryOptions,
            onOptionSelect = { onMetadataLibraryFilterSelect(it.value) },
            label = { Text("为书库启用元数据更新任务") },
            inputFieldModifier = Modifier.fillMaxWidth(),
            inputFieldColor = MaterialTheme.colorScheme.surfaceVariant
        )

        val notificationsSelectedOptions = remember(notificationsFilter, libraries) {
            notificationsFilter.map { libraryId ->
                LabeledEntry(
                    value = libraryId,
                    label = libraries.find { it.id == libraryId }?.name
                        ?: "未知书库：id(${libraryId.value})"
                )
            }
        }
        DropdownMultiChoiceMenu(
            selectedOptions = notificationsSelectedOptions,
            options = libraryOptions,
            onOptionSelect = { onNotificationsLibraryFilterSelect(it.value) },
            label = { Text("为书库启用通知任务") },
            inputFieldModifier = Modifier.fillMaxWidth(),
            inputFieldColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
