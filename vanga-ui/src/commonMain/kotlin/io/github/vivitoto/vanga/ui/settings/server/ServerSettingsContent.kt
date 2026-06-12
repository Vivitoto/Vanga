package io.github.vivitoto.vanga.ui.settings.server

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import io.github.vivitoto.vanga.ui.LocalStrings
import io.github.vivitoto.vanga.ui.OptionsStateHolder
import io.github.vivitoto.vanga.ui.StateHolder
import io.github.vivitoto.vanga.ui.common.components.CheckboxWithLabel
import io.github.vivitoto.vanga.ui.common.components.DropdownChoiceMenu
import io.github.vivitoto.vanga.ui.common.components.LabeledEntry
import io.github.vivitoto.vanga.ui.common.components.withTextFieldNavigation
import io.github.vivitoto.vanga.ui.dialogs.ConfirmationDialog
import snd.komga.client.settings.KomgaThumbnailSize

@Composable
fun ServerSettingsContent(
    deleteEmptyCollections: StateHolder<Boolean>,
    deleteEmptyReadLists: StateHolder<Boolean>,
    taskPoolSize: StateHolder<Int?>,
    rememberMeDurationDays: StateHolder<Int?>,
    renewRememberMeKey: StateHolder<Boolean>,
    serverPort: StateHolder<Int?>,
    configServerPort: Int,
    serverContextPath: StateHolder<String?>,
    thumbnailSize: OptionsStateHolder<KomgaThumbnailSize>,
    thumbnailSizeChanged: Boolean,
    onThumbnailRegenerate: (forBiggerResultOnly: Boolean) -> Unit,
    generalSettingsChanged: Boolean,
    onGeneralSettingsSave: () -> Unit,
    onGeneralSettingsDiscard: () -> Unit,

    onScanAllLibraries: (deep: Boolean) -> Unit,
    onEmptyTrash: () -> Unit,
    onCancelAllTasks: () -> Unit,
    onShutdown: () -> Unit
) {
    GeneralSettingsContent(
        deleteEmptyCollections = deleteEmptyCollections,
        deleteEmptyReadLists = deleteEmptyReadLists,
        taskPoolSize = taskPoolSize,

        rememberMeDurationDays = rememberMeDurationDays,
        renewRememberMeKey = renewRememberMeKey,
        serverPort = serverPort,
        configServerPort = configServerPort,
        serverContextPath = serverContextPath,
        thumbnailSize = thumbnailSize,
    )

    ChangesConfirmationButton(
        thumbnailSizeChanged = thumbnailSizeChanged,
        onThumbnailRegenerate = onThumbnailRegenerate,

        isChanged = generalSettingsChanged,
        onSave = onGeneralSettingsSave,
        onDiscard = onGeneralSettingsDiscard,
    )


    ServerManagementContent(
        onScanAllLibraries = onScanAllLibraries,
        onEmptyTrash = onEmptyTrash,
        onCancelAllTasks = onCancelAllTasks,
        onShutdown = onShutdown
    )

    Spacer(Modifier.height(100.dp))
}

@Composable
fun GeneralSettingsContent(
    deleteEmptyCollections: StateHolder<Boolean>,
    deleteEmptyReadLists: StateHolder<Boolean>,
    taskPoolSize: StateHolder<Int?>,
    rememberMeDurationDays: StateHolder<Int?>,
    renewRememberMeKey: StateHolder<Boolean>,
    serverPort: StateHolder<Int?>,
    configServerPort: Int,
    serverContextPath: StateHolder<String?>,
    thumbnailSize: OptionsStateHolder<KomgaThumbnailSize>,
) {
    val strings = LocalStrings.current.settings
    var showAdvancedServerSettings by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("封面与缩略图", style = MaterialTheme.typography.titleMedium)
            Text("影响书库列表和详情页的封面清晰度。普通用户通常只需要改这里。", style = MaterialTheme.typography.bodyMedium)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(40.dp)) {
            DropdownChoiceMenu(
                selectedOption = LabeledEntry(thumbnailSize.value, strings.forThumbnailSize(thumbnailSize.value)),
                options = thumbnailSize.options.map { LabeledEntry(it, strings.forThumbnailSize(it)) },
                onOptionChange = { thumbnailSize.onValueChange(it.value) },
                label = { Text(strings.thumbnailSize) }
            )
        }

        FilledTonalButton(onClick = { showAdvancedServerSettings = !showAdvancedServerSettings }) {
            Text(if (showAdvancedServerSettings) "收起高级服务器配置" else "高级服务器配置")
        }

        if (showAdvancedServerSettings) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("高级服务器配置", style = MaterialTheme.typography.titleMedium)
                Text("这些设置会影响服务器行为，部分修改需要重启 Komga 后生效。", style = MaterialTheme.typography.bodyMedium)
            }

            Column {
                CheckboxWithLabel(
                    checked = deleteEmptyCollections.value,
                    onCheckedChange = deleteEmptyCollections.setValue,
                    label = { Text(strings.deleteEmptyCollections) },
                )

                CheckboxWithLabel(
                    checked = deleteEmptyReadLists.value,
                    onCheckedChange = deleteEmptyReadLists.setValue,
                    label = { Text(strings.deleteEmptyReadLists) },
                )
            }

            TextField(
                value = taskPoolSize.value?.toString() ?: "",
                onValueChange = { newValue ->
                    if (newValue.isBlank()) taskPoolSize.setValue(null)
                    else newValue.toIntOrNull()?.let { taskPoolSize.setValue(it) }
                },
                label = { Text(strings.taskPoolSize) },
                supportingText = {
                    val error = taskPoolSize.errorMessage
                    if (error != null)
                        Text(text = error, color = MaterialTheme.colorScheme.error)
                },
                modifier = Modifier.fillMaxWidth().withTextFieldNavigation(),
            )

            TextField(
                value = rememberMeDurationDays.value?.toString() ?: "",
                onValueChange = { newValue ->
                    if (newValue.isBlank()) rememberMeDurationDays.setValue(null)
                    else newValue.toIntOrNull()?.let { rememberMeDurationDays.setValue(it) }

                },
                label = { Text(strings.rememberMeDurationDays) },
                supportingText = {
                    val error = rememberMeDurationDays.errorMessage
                    if (error != null)
                        Text(text = error, color = MaterialTheme.colorScheme.error)
                    else Text(strings.requiresRestart)
                },
                modifier = Modifier.fillMaxWidth().withTextFieldNavigation(),
            )

            CheckboxWithLabel(
                checked = renewRememberMeKey.value,
                onCheckedChange = renewRememberMeKey.setValue,
                label = { Text(strings.renewRememberMeKey) },
            )

            TextField(
                value = serverPort.value?.toString() ?: "",
                onValueChange = { newValue ->
                    if (newValue.isBlank()) serverPort.setValue(null)
                    else newValue.toIntOrNull()?.let { serverPort.setValue(it) }

                },
                placeholder = { Text(configServerPort.toString()) },
                label = { Text(strings.serverPort) },
                supportingText = { Text(strings.requiresRestart) },
                modifier = Modifier.fillMaxWidth().withTextFieldNavigation(),
            )

            TextField(
                value = serverContextPath.value ?: "",
                onValueChange = { serverContextPath.setValue(it) },
                label = { Text(strings.serverContextPath) },
                supportingText = { Text(strings.requiresRestart) },
                modifier = Modifier.fillMaxWidth().withTextFieldNavigation(),
            )
        }

    }
}


@Composable
fun ChangesConfirmationButton(
    thumbnailSizeChanged: Boolean,
    onThumbnailRegenerate: (forBiggerResultOnly: Boolean) -> Unit,

    isChanged: Boolean,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
) {
    val strings = LocalStrings.current.settings

    var showThumbnailRegenerateDialog by remember { mutableStateOf(false) }
    if (showThumbnailRegenerateDialog) {
        ThumbRegenerationDialog(
            onThumbnailRegenerate = onThumbnailRegenerate,
            onDismiss = { showThumbnailRegenerateDialog = false }
        )
    }

    Row(
        modifier = Modifier.padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Spacer(Modifier.weight(1f))

        ElevatedButton(
            onClick = onDiscard,
            enabled = isChanged,
            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
        ) {
            Text(strings.serverSettingsDiscard)
        }
        Spacer(Modifier.width(20.dp))

        FilledTonalButton(
            onClick = {
                if (thumbnailSizeChanged) showThumbnailRegenerateDialog = true
                onSave()
            },
            enabled = isChanged,
            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
        ) {
            Text(strings.serverSettingsSave)
        }
    }

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChangesConfirmationPopup(
    thumbnailSizeChanged: Boolean,
    onThumbnailRegenerate: (forBiggerResultOnly: Boolean) -> Unit,

    isChanged: Boolean,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
) {

    var showThumbnailRegenerateDialog by remember { mutableStateOf(false) }
    if (showThumbnailRegenerateDialog) {
        ThumbRegenerationDialog(
            onThumbnailRegenerate = onThumbnailRegenerate,
            onDismiss = { showThumbnailRegenerateDialog = false }
        )
    }

    if (isChanged) {
        Popup(
            alignment = Alignment.BottomCenter,
        ) {
            Surface(
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(5.dp),
                modifier = Modifier
                    .fillMaxWidth(.92f)
                    .widthIn(max = 600.dp)
                    .padding(20.dp)
            ) {
                FlowRow(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("有未保存的修改")
                    Spacer(Modifier.weight(1f))

                    TextButton(onClick = onDiscard) {
                        Text("放弃")
                    }
                    Spacer(Modifier.width(20.dp))

                    FilledTonalButton(
                        onClick = {
                            if (thumbnailSizeChanged) showThumbnailRegenerateDialog = true
                            onSave()
                        },
                    ) {
                        Text("保存修改")

                    }
                }
            }
        }
    }

}

@Composable
private fun ThumbRegenerationDialog(
    onThumbnailRegenerate: (forBiggerResultOnly: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val strings = LocalStrings.current.settings
    ConfirmationDialog(
        title = strings.thumbnailRegenTitle,
        body = strings.thumbnailRegenBody,
        buttonConfirm = strings.thumbnailRegenIfBigger,
        buttonAlternate = strings.thumbnailRegenAllBooks,
        buttonCancel = strings.thumbnailRegenNo,
        onDialogConfirm = { onThumbnailRegenerate(true) },
        onDialogConfirmAlternate = { onThumbnailRegenerate(false) },
        onDialogDismiss = onDismiss
    )

}
