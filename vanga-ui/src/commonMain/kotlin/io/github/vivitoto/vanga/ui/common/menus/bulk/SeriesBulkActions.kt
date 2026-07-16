package io.github.vivitoto.vanga.ui.common.menus.bulk

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import io.github.vivitoto.vanga.AppNotifications
import io.github.vivitoto.vanga.komga.api.KomgaSeriesApi
import io.github.vivitoto.vanga.offline.tasks.OfflineTaskEmitter
import io.github.vivitoto.vanga.ui.LocalKomfIntegration
import io.github.vivitoto.vanga.ui.LocalKomgaState
import io.github.vivitoto.vanga.ui.LocalOfflineMode
import io.github.vivitoto.vanga.ui.LocalViewModelFactory
import io.github.vivitoto.vanga.ui.dialogs.ConfirmationDialog
import io.github.vivitoto.vanga.ui.dialogs.collectionadd.AddToCollectionDialog
import io.github.vivitoto.vanga.ui.dialogs.permissions.DownloadNotificationRequestDialog
import io.github.vivitoto.vanga.ui.dialogs.series.edit.SeriesEditDialog
import io.github.vivitoto.vanga.ui.dialogs.series.editbulk.SeriesBulkEditDialog
import snd.komf.api.KomfServerLibraryId
import snd.komf.api.KomfServerSeriesId
import snd.komf.client.KomfMetadataClient
import snd.komga.client.series.KomgaSeries


@Composable
fun SeriesBulkActionsContent(
    series: List<KomgaSeries>,
    compact: Boolean
) {
    val state = rememberSeriesBulkActionsState(series)
    BulkActionsButtonsLayout(state.buttons, compact)
    SeriesBulkActionDialogs(state = state)
}

@Composable
fun SeriesBulkActionDialogs(
    state: SeriesBulkActionsState,
) {
    val coroutineScope = rememberCoroutineScope()

    if (state.showAddToCollectionDialog) {
        AddToCollectionDialog(
            series = state.series,
            onDismissRequest = { state.showAddToCollectionDialog = false })
    }
    if (state.showEditDialog) {
        if (state.series.size == 1)
            SeriesEditDialog(series = state.series.first(), onDismissRequest = { state.showEditDialog = false })
        else
            SeriesBulkEditDialog(series = state.series, onDismissRequest = { state.showEditDialog = false })
    }

    if (state.showDeleteDialog) {
        ConfirmationDialog(
            title = "删除漫画系列",
            body = "${state.series.size} 个漫画系列将从服务器删除，相关媒体文件也会被移除。此操作不可撤销。要继续吗？",
            confirmText = "删除 ${state.series.size} 个漫画系列及其文件",
            onDialogConfirm = {
                coroutineScope.launch { state.actions.delete(state.series) }
                state.showDeleteDialog = false
            },
            onDialogDismiss = { state.showDeleteDialog = false },
            buttonConfirmColor = MaterialTheme.colorScheme.errorContainer
        )
    }

    if (state.showDeleteDownloadedDialog) {
        ConfirmationDialog(
            title = "删除本地下载",
            body = "只会删除 ${state.series.size} 个漫画系列在本机的已下载文件，不会删除 Komga 服务器上的漫画系列。",
            onDialogConfirm = {
                coroutineScope.launch { state.actions.deleteDownloaded(state.series) }
                state.showDeleteDownloadedDialog = false
            },
            onDialogDismiss = { state.showDeleteDownloadedDialog = false },
            buttonConfirmColor = MaterialTheme.colorScheme.errorContainer
        )
    }

    if (state.showKomfIdentifyDialog) {
        ConfirmationDialog(
            title = "自动识别元数据",
            body = "将使用 Komf 自动识别 ${state.series.size} 个漫画系列的元数据。",
            onDialogConfirm = {
                coroutineScope.launch { state.actions.komfIdentify(state.series) }
                state.showKomfIdentifyDialog = false
            },
            onDialogDismiss = { state.showKomfIdentifyDialog = false },
        )
    }

    if (state.showDownloadDialog) {
        var permissionRequested by remember { mutableStateOf(false) }
        DownloadNotificationRequestDialog { permissionRequested = true }

        val bodyText = remember(state.series) {
            buildString {
                if (state.series.size == 1) append("下载《${state.series.first().metadata.title}》到本机？")
                else append("下载 ${state.series.size} 个漫画系列到本机？")
            }
        }
        if (permissionRequested) {
            ConfirmationDialog(
                body = bodyText,
                onDialogConfirm = {
                    coroutineScope.launch { state.actions.download(state.series) }
                },
                onDialogDismiss = { state.showDownloadDialog = false }
            )
        }
    }

}

@Composable
fun rememberSeriesBulkActionsState(
    series: List<KomgaSeries>,
): SeriesBulkActionsState {
    val coroutineScope = rememberCoroutineScope()
    val factory = LocalViewModelFactory.current
    val isOffline = LocalOfflineMode.current.collectAsState().value
    val isAdmin = LocalKomgaState.current.authenticatedUser.collectAsState().value?.roleAdmin() ?: true
    val isKomfEnabled = LocalKomfIntegration.current.collectAsState(false).value

    return remember(series, coroutineScope, isOffline, isAdmin, isKomfEnabled) {
        SeriesBulkActionsState(
            series = series,
            actions = factory.getSeriesBulkActions(),
            favoriteActions = factory.getFavoriteBulkActions(),
            coroutineScope = coroutineScope,
            isOffline = isOffline,
            isAdmin = isAdmin,
            isKomfEnabled = isKomfEnabled
        )
    }
}

data class SeriesBulkActionsState(
    val series: List<KomgaSeries>,
    val actions: SeriesBulkActions,
    private val favoriteActions: FavoriteBulkActions,
    private val coroutineScope: CoroutineScope,
    private val isOffline: Boolean,
    private val isKomfEnabled: Boolean,
    private val isAdmin: Boolean,
) {
    var showAddToCollectionDialog by mutableStateOf(false)
    var showEditDialog by mutableStateOf(false)
    var showDeleteDialog by mutableStateOf(false)
    var showDeleteDownloadedDialog by mutableStateOf(false)
    var showKomfIdentifyDialog by mutableStateOf(false)
    var showDownloadDialog by mutableStateOf(false)

    val buttons = buildList {
        add(
            BulkActionButtonData(
                description = "标记已读",
                icon = Icons.Default.BookmarkAdd,
                onClick = { coroutineScope.launch { actions.markAsRead(series) } }
            )
        )
        add(
            BulkActionButtonData(
                description = "标记未读",
                icon = Icons.Default.BookmarkRemove,
                onClick = { coroutineScope.launch { actions.markAsUnread(series) } }
            )
        )
        add(
            BulkActionButtonData(
                description = "加入收藏",
                icon = Icons.Default.Star,
                onClick = { coroutineScope.launch { favoriteActions.addSeriesToLocalFavorites(series) } }
            )
        )
        if (!isOffline && isAdmin) {
            add(
                BulkActionButtonData(
                    description = "编辑",
                    icon = Icons.Default.Edit,
                    onClick = { showEditDialog = true }
                )
            )
            add(
                BulkActionButtonData(
                    description = "加入合集",
                    icon = Icons.AutoMirrored.Default.PlaylistAdd,
                    onClick = { showAddToCollectionDialog = true }
                )
            )
        }

        if (!isOffline) {
            add(
                BulkActionButtonData(
                    description = "下载",
                    icon = Icons.Default.Download,
                    onClick = { showDownloadDialog = true }
                )
            )
        }

        if (isOffline) {
            add(
                BulkActionButtonData(
                    description = "删除本地下载",
                    icon = Icons.Default.Delete,
                    onClick = { showDeleteDownloadedDialog = true }
                )
            )
        }
        if (isKomfEnabled) {
            add(
                BulkActionButtonData(
                    description = "自动识别",
                    icon = Icons.Default.Extension,
                    onClick = { showKomfIdentifyDialog = true }
                )
            )
        }

//        if (!isOffline && isAdmin) {
//            add(
//                BulkActionButtonData(
//                    description = "Delete from server",
//                    icon = Icons.Default.Delete,
//                    onClick = { showDeleteDialog = true }
//                )
//            )
//        }
    }
}

data class SeriesBulkActions(
    val markAsRead: suspend (List<KomgaSeries>) -> Unit,
    val markAsUnread: suspend (List<KomgaSeries>) -> Unit,
    val delete: suspend (List<KomgaSeries>) -> Unit,
    val download: suspend (List<KomgaSeries>) -> Unit,
    val deleteDownloaded: suspend (List<KomgaSeries>) -> Unit,
    val komfIdentify: suspend (List<KomgaSeries>) -> Unit,
) {

    constructor(
        seriesApi: KomgaSeriesApi,
        komfClient: KomfMetadataClient,
        taskEmitter: OfflineTaskEmitter,
        notifications: AppNotifications,
    ) : this(
        markAsRead = { series ->
            notifications.runCatchingToNotifications {
                series.forEach { seriesApi.markAsRead(it.id) }
            }

        },
        markAsUnread = { series ->
            notifications.runCatchingToNotifications {
                series.forEach { seriesApi.markAsUnread(it.id) }
            }
        },
        delete = { series ->
            notifications.runCatchingToNotifications {
                series.forEach { seriesApi.delete(it.id) }
            }
        },
        download = { series ->
            series.forEach { taskEmitter.downloadSeries(it.id) }
        },
        deleteDownloaded = { series ->
            series.forEach { taskEmitter.deleteSeries(it.id) }
        },
        komfIdentify = { series ->
            series.forEach {
                komfClient.matchSeries(
                    KomfServerLibraryId(it.libraryId.value),
                    KomfServerSeriesId(it.id.value),
                )
            }
        }
    )
}
