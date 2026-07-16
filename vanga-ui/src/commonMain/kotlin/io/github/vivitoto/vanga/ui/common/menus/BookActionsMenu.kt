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
import io.github.vivitoto.vanga.komga.api.KomgaBookApi
import io.github.vivitoto.vanga.komga.api.model.VangaBook
import io.github.vivitoto.vanga.offline.tasks.OfflineTaskEmitter
import io.github.vivitoto.vanga.ui.LocalKomfIntegration
import io.github.vivitoto.vanga.ui.LocalKomgaState
import io.github.vivitoto.vanga.ui.LocalOfflineMode
import io.github.vivitoto.vanga.ui.dialogs.ConfirmationDialog
import io.github.vivitoto.vanga.ui.dialogs.book.edit.BookEditDialog
import io.github.vivitoto.vanga.ui.dialogs.komf.identify.KomfBookIdentifyDialog
import io.github.vivitoto.vanga.ui.dialogs.permissions.DownloadNotificationRequestDialog
import io.github.vivitoto.vanga.ui.dialogs.readlistadd.AddToReadListDialog
import snd.komga.client.book.KomgaBookReadProgressUpdateRequest

@Composable
fun BookActionsMenu(
    book: VangaBook,
    actions: BookMenuActions,
    expanded: Boolean,
    showEditOption: Boolean,
    showDownloadOption: Boolean,
    onDismissRequest: () -> Unit,
) {
    val isAdmin = LocalKomgaState.current.authenticatedUser.collectAsState().value?.roleAdmin() ?: true
    val isOffline = LocalOfflineMode.current.collectAsState().value
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDeleteDownloadedDialog by remember { mutableStateOf(false) }
    if (showDeleteDialog) {
        ConfirmationDialog(
            title = "删除单本漫画",
            body = "《${book.metadata.title}》将从服务器删除，相关媒体文件也会被移除。此操作不可撤销。要继续吗？",
            confirmText = "删除《${book.metadata.title}》",
            onDialogConfirm = {
                actions.delete(book)
                onDismissRequest()

            },
            onDialogDismiss = {
                showDeleteDialog = false
                onDismissRequest()
            },
            buttonConfirmColor = MaterialTheme.colorScheme.errorContainer
        )
    }

    if (showDeleteDownloadedDialog) {
        ConfirmationDialog(
            title = "删除本地下载",
            body = "只会删除本机已下载文件，不会删除 Komga 服务器上的漫画。",
            onDialogConfirm = {
                actions.deleteDownloaded(book)
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
        BookEditDialog(book, onDismissRequest = {
            showEditDialog = false
            onDismissRequest()
        })
    }

    var showAddToReadListDialog by remember { mutableStateOf(false) }
    if (showAddToReadListDialog) {
        AddToReadListDialog(
            books = listOf(book),
            onDismissRequest = {
                showAddToReadListDialog = false
                onDismissRequest()
            })
    }
    var showKomfBookIdentifyDialog by remember { mutableStateOf(false) }
    if (showKomfBookIdentifyDialog) {
        KomfBookIdentifyDialog(
            book = book,
            onDismissRequest = {
                showKomfBookIdentifyDialog = false
                onDismissRequest()
            }
        )
    }
    var showDownloadDialog by remember { mutableStateOf(false) }
    if (showDownloadDialog) {
        var permissionRequested by remember { mutableStateOf(false) }
        DownloadNotificationRequestDialog { permissionRequested = true }

        if (permissionRequested) {
            ConfirmationDialog(
                "下载《${book.metadata.title}》到本机？",
                onDialogConfirm = { actions.download(book) },
                onDialogDismiss = { showDownloadDialog = false }
            )
        }
    }

    val showDropdown = derivedStateOf {
        expanded &&
                !showDeleteDialog &&
                !showEditDialog &&
                !showAddToReadListDialog &&
                !showKomfBookIdentifyDialog
    }
    DropdownMenu(
        expanded = showDropdown.value,
        onDismissRequest = onDismissRequest
    ) {
        if (showOnlineAdminActions(isAdmin, isOffline)) {
            DropdownMenuItem(
                text = { Text("分析文件") },
                onClick = {
                    actions.analyze(book)
                    onDismissRequest()
                }
            )

            DropdownMenuItem(
                text = { Text("刷新元数据") },
                onClick = {
                    actions.refreshMetadata(book)
                    onDismissRequest()
                }
            )

            DropdownMenuItem(
                text = { Text("加入阅读清单") },
                onClick = { showAddToReadListDialog = true },
            )
        }

        val komfIntegration = LocalKomfIntegration.current.collectAsState(false)
        if (showKomfBookIdentifyAction(
                komfEnabled = komfIntegration.value,
                isOffline = isOffline,
                hasBookContext = true,
            )
        ) {
            DropdownMenuItem(
                text = { Text("自动识别单本元数据（Komf）") },
                onClick = { showKomfBookIdentifyDialog = true },
            )
        }

        val isRead = remember { book.readProgress?.completed ?: false }
        val isUnread = remember { book.readProgress == null }

        if (!isRead) {
            DropdownMenuItem(
                text = { Text("标记为已读") },
                onClick = {
                    actions.markAsRead(book)
                    onDismissRequest()
                },
            )
        }

        if (!isUnread) {
            DropdownMenuItem(
                text = { Text("标记为未读") },
                onClick = {
                    actions.markAsUnread(book)
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

        if (book.downloaded) {
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

data class BookMenuActions(
    val analyze: (VangaBook) -> Unit,
    val refreshMetadata: (VangaBook) -> Unit,
    val markAsRead: (VangaBook) -> Unit,
    val markAsUnread: (VangaBook) -> Unit,
    val delete: (VangaBook) -> Unit,
    val download: (VangaBook) -> Unit,
    val deleteDownloaded: (VangaBook) -> Unit,
) {
    constructor(
        bookApi: KomgaBookApi,
        notifications: AppNotifications,
        scope: CoroutineScope,
        taskEmitter: OfflineTaskEmitter
    ) : this(
        analyze = {
            notifications.runCatchingToNotifications(scope) {
                bookApi.analyze(it.id)
                notifications.add(AppNotification.Normal("已开始分析单本漫画"))
            }
        },
        refreshMetadata = {
            notifications.runCatchingToNotifications(scope) {
                bookApi.refreshMetadata(it.id)
                notifications.add(AppNotification.Normal("已开始刷新单本漫画元数据"))
            }
        },
        markAsRead = { book ->
            notifications.runCatchingToNotifications(scope) {
                bookApi.markReadProgress(
                    book.id,
                    KomgaBookReadProgressUpdateRequest(completed = true)
                )
            }
        },
        markAsUnread = {
            notifications.runCatchingToNotifications(scope) { bookApi.deleteReadProgress(it.id) }
        },
        delete = {
            notifications.runCatchingToNotifications(scope) { bookApi.deleteBook(it.id) }
        },
        download = { scope.launch { taskEmitter.downloadBook(it.id) } },
        deleteDownloaded = { scope.launch { taskEmitter.deleteBook(it.id) } }
    )
}
