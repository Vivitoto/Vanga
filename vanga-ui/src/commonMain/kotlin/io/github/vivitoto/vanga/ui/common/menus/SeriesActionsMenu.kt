package io.github.vivitoto.vanga.ui.common.menus

import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import io.github.vivitoto.vanga.AppNotification
import io.github.vivitoto.vanga.AppNotifications
import io.github.vivitoto.vanga.komga.api.KomgaSeriesApi
import io.github.vivitoto.vanga.offline.tasks.OfflineTaskEmitter
import io.github.vivitoto.vanga.ui.LocalKomfIntegration
import io.github.vivitoto.vanga.ui.LocalKomgaState
import io.github.vivitoto.vanga.ui.LocalOfflineMode
import io.github.vivitoto.vanga.ui.dialogs.ConfirmationDialog
import io.github.vivitoto.vanga.ui.dialogs.collectionadd.AddToCollectionDialog
import io.github.vivitoto.vanga.ui.dialogs.komf.identify.KomfIdentifyDialog
import io.github.vivitoto.vanga.ui.dialogs.komf.reset.KomfResetSeriesMetadataDialog
import io.github.vivitoto.vanga.ui.dialogs.permissions.DownloadNotificationRequestDialog
import io.github.vivitoto.vanga.ui.dialogs.series.edit.SeriesEditDialog
import snd.komga.client.series.KomgaSeries

@Composable
fun SeriesActionsMenu(
    series: KomgaSeries,
    actions: SeriesMenuActions,
    expanded: Boolean,
    showEditOption: Boolean,
    showDownloadOption: Boolean,
    onDismissRequest: () -> Unit,
) {
    val isAdmin = LocalKomgaState.current.authenticatedUser.collectAsState().value?.roleAdmin() ?: true
    val isOffline = LocalOfflineMode.current.collectAsState().value

    var showDeleteDialog by remember { mutableStateOf(false) }
    if (showDeleteDialog) {
        ConfirmationDialog(
            title = "删除漫画系列",
            body = "《${series.metadata.title}》将从服务器删除，相关媒体文件也会被移除。此操作不可撤销。要继续吗？",
            confirmText = "删除《${series.metadata.title}》",
            onDialogConfirm = {
                actions.delete(series)
                onDismissRequest()

            },
            onDialogDismiss = {
                showDeleteDialog = false
                onDismissRequest()
            },
            buttonConfirmColor = MaterialTheme.colorScheme.errorContainer
        )
    }
    var showDeleteDownloadedDialog by remember { mutableStateOf(false) }
    if (showDeleteDownloadedDialog) {
        ConfirmationDialog(
            title = "删除本地下载",
            body = "只会删除本机已下载文件，不会删除 Komga 服务器上的漫画系列。",
            onDialogConfirm = {
                actions.deleteDownloaded(series)
                onDismissRequest()

            },
            onDialogDismiss = {
                showDeleteDownloadedDialog = false
                onDismissRequest()
            },
            buttonConfirmColor = MaterialTheme.colorScheme.errorContainer
        )
    }

    var showEditDialog by remember { mutableStateOf(false) }
    if (showEditDialog) {
        SeriesEditDialog(series, onDismissRequest = {
            showEditDialog = false
            onDismissRequest()
        })
    }

    var showKomfDialog by remember { mutableStateOf(false) }
    if (showKomfDialog) {
        KomfIdentifyDialog(
            series = series,
            onDismissRequest = {
                showKomfDialog = false
                onDismissRequest()
            }
        )
    }
    var showKomfResetDialog by remember { mutableStateOf(false) }
    if (showKomfResetDialog) {
        KomfResetSeriesMetadataDialog(
            series = series,
            onDismissRequest = {
                showKomfResetDialog = false
                onDismissRequest()
            }
        )
    }

    var showAddToCollectionDialog by remember { mutableStateOf(false) }
    if (showAddToCollectionDialog) {
        AddToCollectionDialog(
            series = listOf(series),
            onDismissRequest = {
                showAddToCollectionDialog = false
                onDismissRequest()
            })
    }
    var showDownloadDialog by remember { mutableStateOf(false) }
    if (showDownloadDialog) {
        var permissionRequested by remember { mutableStateOf(false) }
        DownloadNotificationRequestDialog { permissionRequested = true }

        if (permissionRequested) {
            ConfirmationDialog(
                "下载《${series.metadata.title}》到本机？",
                onDialogConfirm = { actions.download(series) },
                onDialogDismiss = { showDownloadDialog = false }
            )
        }
    }

    val showDropdown = derivedStateOf {
        expanded &&
                !showDeleteDialog &&
                !showKomfDialog &&
                !showKomfResetDialog &&
                !showEditDialog &&
                !showAddToCollectionDialog
    }
    DropdownMenu(
        expanded = showDropdown.value,
        onDismissRequest = onDismissRequest
    ) {
        if (showOnlineAdminActions(isAdmin, isOffline)) {
            DropdownMenuItem(
                text = { Text("分析文件") },
                onClick = {
                    actions.analyze(series)
                    onDismissRequest()
                }
            )

            DropdownMenuItem(
                text = { Text("刷新元数据") },
                onClick = {
                    actions.refreshMetadata(series)
                    onDismissRequest()
                }
            )

            DropdownMenuItem(
                text = { Text("加入合集") },
                onClick = { showAddToCollectionDialog = true },
            )
        }

        val isRead = remember { series.booksReadCount == series.booksCount }
        val isUnread = remember { series.booksUnreadCount == series.booksCount }
        if (!isRead) {
            DropdownMenuItem(
                text = { Text("标记为已读") },
                onClick = {
                    actions.markAsRead(series)
                    onDismissRequest()
                },
            )
        }

        if (!isUnread) {
            DropdownMenuItem(
                text = { Text("标记为未读") },
                onClick = {
                    actions.markAsUnread(series)
                    onDismissRequest()
                },
            )
        }

        if (showOnlineAdminActions(isAdmin, isOffline) && showEditOption) {
            DropdownMenuItem(
                text = { Text("编辑") },
                onClick = { showEditDialog = true },
            )
        }

        if (showOnlineDownloadAction(showDownloadOption, isOffline)) {
            DropdownMenuItem(
                text = { Text("下载") },
                onClick = { showDownloadDialog = true },
            )
        }

        if (showOfflineLocalDeleteAction(isOffline)) {
            val deleteInteractionSource = remember { MutableInteractionSource() }
            val deleteIsHovered = deleteInteractionSource.collectIsHoveredAsState()
            val deleteColor =
                if (deleteIsHovered.value) Modifier.background(MaterialTheme.colorScheme.errorContainer)
                else Modifier
            DropdownMenuItem(
                text = { Text("删除本地下载") },
                onClick = { showDeleteDownloadedDialog = true },
                modifier = Modifier
                    .hoverable(deleteInteractionSource)
                    .then(deleteColor)
            )

        }

        val komfIntegration = LocalKomfIntegration.current.collectAsState(false)
        if (showOnlineKomfActions(komfIntegration.value, isOffline)) {
            DropdownMenuItem(
                text = { Text("自动识别元数据（Komf）") },
                onClick = { showKomfDialog = true },
            )

            DropdownMenuItem(
                text = { Text("重置元数据（Komf）") },
                onClick = { showKomfResetDialog = true },
            )
        }

//        if (isAdmin && !isOffline) {
//            val deleteInteractionSource = remember { MutableInteractionSource() }
//            val deleteIsHovered = deleteInteractionSource.collectIsHoveredAsState()
//            val deleteColor =
//                if (deleteIsHovered.value) Modifier.background(MaterialTheme.colorScheme.errorContainer)
//                else Modifier
//            DropdownMenuItem(
//                text = { Text("Delete from server") },
//                onClick = { showDeleteDialog = true },
//                modifier = Modifier
//                    .hoverable(deleteInteractionSource)
//                    .then(deleteColor)
//            )
//        }
    }
}

data class SeriesMenuActions(
    val analyze: (KomgaSeries) -> Unit,
    val refreshMetadata: (KomgaSeries) -> Unit,
    val addToCollection: (KomgaSeries) -> Unit,
    val markAsRead: (KomgaSeries) -> Unit,
    val markAsUnread: (KomgaSeries) -> Unit,
    val delete: (KomgaSeries) -> Unit,
    val download: (KomgaSeries) -> Unit,
    val deleteDownloaded: (KomgaSeries) -> Unit,
) {
    constructor(
        seriesApi: KomgaSeriesApi,
        notifications: AppNotifications,
        taskEmitter: OfflineTaskEmitter,
        scope: CoroutineScope,
    ) : this(
        analyze = {
            notifications.runCatchingToNotifications(scope) {
                seriesApi.analyze(it.id)
                notifications.add(AppNotification.Normal("已开始分析漫画系列"))
            }
        },
        refreshMetadata = {
            notifications.runCatchingToNotifications(scope) {
                seriesApi.refreshMetadata(it.id)
                notifications.add(AppNotification.Normal("已开始刷新漫画系列元数据"))
            }
        },
        addToCollection = { },
        markAsRead = {
            notifications.runCatchingToNotifications(scope) { seriesApi.markAsRead(it.id) }
        },
        markAsUnread = {
            notifications.runCatchingToNotifications(scope) { seriesApi.markAsUnread(it.id) }
        },
        delete = {
            notifications.runCatchingToNotifications(scope) { seriesApi.delete(it.id) }
        },
        download = { scope.launch { taskEmitter.downloadSeries(it.id) } },
        deleteDownloaded = { scope.launch { taskEmitter.deleteSeries(it.id) } }
    )
}
