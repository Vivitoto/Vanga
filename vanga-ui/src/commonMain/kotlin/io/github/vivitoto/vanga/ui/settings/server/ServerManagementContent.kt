package io.github.vivitoto.vanga.ui.settings.server

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import io.github.vivitoto.vanga.ui.dialogs.ConfirmationDialog
import io.github.vivitoto.vanga.ui.settings.SettingsRow
import io.github.vivitoto.vanga.ui.settings.SettingsSectionCard

@Composable
fun ServerManagementContent(
    onScanAllLibraries: (deep: Boolean) -> Unit,
    onEmptyTrash: () -> Unit,
    onCancelAllTasks: () -> Unit,
    onShutdown: () -> Unit
) {

    var showEmptyTrashDialog by remember { mutableStateOf(false) }
    var showShutdownDialog by remember { mutableStateOf(false) }
    var showDangerActions by remember { mutableStateOf(false) }
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingsSectionCard(
            title = "书库维护",
            description = "扫描书库、处理后台任务，以及执行少量高风险服务器操作。"
        ) {
            Button(
                title = "扫描全部书库",
                description = "检查目录里新增或移除的书籍。适合日常增量更新。",
                buttonText = "扫描",
                level = WarningLevel.NORMAL,
                onClick = { onScanAllLibraries(false) }
            )
            Button(
                title = "深度扫描全部书库",
                description = "强制重新比对文件和数据库，耗时更久。只有普通扫描不准时再用。",
                buttonText = "深度扫描",
                level = WarningLevel.NORMAL,
                onClick = { onScanAllLibraries(true) }
            )
            SettingsRow(
                title = "更多服务器操作",
                supportingText = "显示会影响服务器状态或删除记录的危险操作。",
                trailing = {
                    FilledTonalButton(onClick = { showDangerActions = !showDangerActions }) {
                        Text(if (showDangerActions) "收起" else "显示")
                    }
                }
            )
        }

        if (showDangerActions) {
            SettingsSectionCard(
                title = "危险操作",
                description = "这些操作会影响服务器状态或删除记录，确认后再执行。",
            ) {
                Button(
                    title = "清空全部书库回收站",
                    description = "删除已标记为不可用的媒体记录。确认文件不会恢复后再操作。",
                    buttonText = "清空",
                    level = WarningLevel.NORMAL,
                    onClick = { showEmptyTrashDialog = true }
                )
                Button(
                    title = "取消全部任务",
                    description = "停止当前正在运行的服务器任务。",
                    buttonText = "取消任务",
                    level = WarningLevel.WARNING,
                    onClick = { onCancelAllTasks() }
                )
                Button(
                    title = "关闭服务器",
                    description = "停止服务器服务进程。除非你知道如何重新启动，否则不要操作。",
                    buttonText = "关闭",
                    level = WarningLevel.DANGER,
                    onClick = { showShutdownDialog = true }
                )
            }
        }

        if (showEmptyTrashDialog) {
            ConfirmationDialog(
                title = "清空回收站",
                body = "服务器默认不会立刻删除缺失媒体的信息，以避免硬盘临时断开造成数据丢失。清空后，缺失媒体的记录会被删除。确定继续吗？",
                buttonConfirm = "清空",
                buttonCancel = "取消",
                onDialogConfirm = onEmptyTrash,
                onDialogDismiss = { showEmptyTrashDialog = false }
            )
        }

        if (showShutdownDialog) {
            ConfirmationDialog(
                title = "关闭服务器",
                body = "确定要停止服务器服务吗？",
                buttonConfirm = "停止",
                buttonCancel = "取消",
                buttonConfirmColor = MaterialTheme.colorScheme.errorContainer,
                onDialogConfirm = onShutdown,
                onDialogDismiss = { showShutdownDialog = false }
            )
        }

    }
}

@Composable
private fun Button(
    title: String,
    description: String,
    buttonText: String,
    level: WarningLevel,
    onClick: () -> Unit
) {
    val colors = when (level) {
        WarningLevel.NORMAL -> ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )

        WarningLevel.WARNING -> ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )

        WarningLevel.DANGER -> ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    }

    SettingsRow(
        title = title,
        supportingText = description,
        trailing = {
            FilledTonalButton(onClick = onClick, colors = colors, modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)) {
                Text(buttonText)
            }
        }
    )
}

private enum class WarningLevel { NORMAL, WARNING, DANGER }
