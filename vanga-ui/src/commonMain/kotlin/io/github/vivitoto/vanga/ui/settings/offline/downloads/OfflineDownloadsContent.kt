package io.github.vivitoto.vanga.ui.settings.offline.downloads

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.PlatformFile
import io.github.vivitoto.vanga.formatDecimal
import io.github.vivitoto.vanga.offline.sync.model.DownloadEvent
import io.github.vivitoto.vanga.ui.common.components.EmptyState
import io.github.vivitoto.vanga.ui.dialogs.permissions.StoragePermissionRequestDialog
import io.github.vivitoto.vanga.ui.settings.SettingsCard
import io.github.vivitoto.vanga.ui.settings.SettingsRow
import io.github.vivitoto.vanga.ui.settings.SettingsSectionCard
import io.github.vivitoto.vanga.ui.settings.SettingsSectionHeader
import io.github.vivitoto.vanga.ui.settings.SettingsValueRow
import snd.komga.client.book.KomgaBookId
import kotlin.coroutines.cancellation.CancellationException

@Composable
fun OfflineDownloadsContent(
    storageLocation: PlatformFile?,

    onStorageLocationChange: (PlatformFile) -> Unit,
    onStorageLocationReset: () -> Unit,
    downloads: Collection<DownloadEvent>,
    onDownloadCancel: (KomgaBookId) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingsSectionCard(
            title = "下载与存储",
        ) {
            SettingsValueRow(
                title = "下载位置",
                value = storageLocation?.let { rememberStorageLabel(it) } ?: "内部存储",
            )

            var showDirectoryPickerDialog by remember { mutableStateOf(false) }
            if (showDirectoryPickerDialog) {
                StoragePermissionRequestDialog { directory ->
                    if (directory != null) {
                        onStorageLocationChange(directory)
                    }
                    showDirectoryPickerDialog = false
                }
            }
            SettingsRow(
                title = "存储位置",
                stackTrailing = true,
                trailing = {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { showDirectoryPickerDialog = true }) { Text("更改") }
                        Button(onClick = onStorageLocationReset) { Text("恢复") }
                    }
                }
            )
        }

        SettingsSectionHeader("当前下载")
        if (downloads.isEmpty()) {
            EmptyState("暂无下载任务", body = "离线下载任务会显示在这里。")
        }
        for (event in downloads) {
            SettingsCard {
                when (event) {
                    is DownloadEvent.BookDownloadProgress -> DownloadProgress(event, onDownloadCancel)
                    is DownloadEvent.BookDownloadCompleted -> DownloadCompleted(event)
                    is DownloadEvent.BookDownloadError -> DownloadError(event)
                }
            }
        }
    }
}

@Composable
private fun DownloadProgress(
    event: DownloadEvent.BookDownloadProgress,
    onCancel: (KomgaBookId) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        DownloadProgressIndicator(event)
        IconButton(onClick = { onCancel(event.book.id) }) { Icon(Icons.Default.Cancel, null) }
    }
}

@Composable
private fun RowScope.DownloadProgressIndicator(
    event: DownloadEvent.BookDownloadProgress,
) {
    Column(modifier = Modifier.weight(1f)) {
        Text(event.book.metadata.title)
        if (event.total == 0L) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        } else {
            LinearProgressIndicator(
                progress = { event.completed / event.total.toFloat() },
                modifier = Modifier.fillMaxWidth()
            )

            val totalMiB = remember(event.total) {
                (event.total.toFloat() / 1024 / 1024).formatDecimal(2)
            }
            val completedMiB = remember(event.completed) {
                (event.completed.toFloat() / 1024 / 1024).formatDecimal(2)
            }
            Text("${completedMiB}MiB / ${totalMiB}MiB")
        }
    }
}

@Composable
private fun DownloadCompleted(event: DownloadEvent.BookDownloadCompleted) {
    Column {
        Text(event.book.metadata.title)
        Text("下载完成")
    }
}

@Composable
private fun DownloadError(event: DownloadEvent.BookDownloadError) {
    Column {
        Text(event.book?.metadata?.title ?: event.bookId.value)
        val errorMessage = remember {
            if (event.error is CancellationException) "已取消"
            else "${event.error::class.simpleName}: ${event.error.message}"
        }
        Text(errorMessage, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
internal expect fun rememberStorageLabel(file: PlatformFile): String
