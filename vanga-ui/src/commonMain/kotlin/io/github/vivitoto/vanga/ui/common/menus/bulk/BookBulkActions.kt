package io.github.vivitoto.vanga.ui.common.menus.bulk

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
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
import io.github.vivitoto.vanga.komga.api.KomgaBookApi
import io.github.vivitoto.vanga.komga.api.model.VangaBook
import io.github.vivitoto.vanga.offline.tasks.OfflineTaskEmitter
import io.github.vivitoto.vanga.ui.LocalKomgaState
import io.github.vivitoto.vanga.ui.LocalOfflineMode
import io.github.vivitoto.vanga.ui.LocalViewModelFactory
import io.github.vivitoto.vanga.ui.dialogs.ConfirmationDialog
import io.github.vivitoto.vanga.ui.dialogs.book.edit.BookEditDialog
import io.github.vivitoto.vanga.ui.dialogs.book.editbulk.BookBulkEditDialog
import io.github.vivitoto.vanga.ui.dialogs.permissions.DownloadNotificationRequestDialog
import io.github.vivitoto.vanga.ui.dialogs.readlistadd.AddToReadListDialog
import snd.komga.client.book.KomgaBookReadProgressUpdateRequest

@Composable
fun BooksBulkActionsContent(
    books: List<VangaBook>,
    actions: BookBulkActions,
    compact: Boolean,
) {
    val state = rememberBookBulkActionsState(books, actions)
    BulkActionsButtonsLayout(state.buttons, compact)
    BookBulkActionDialogs(state)
}

@Composable
fun BookBulkActionDialogs(state: BookBulkActionsState) {
    val coroutineScope = rememberCoroutineScope()

    if (state.showAddToReadListDialog) {
        AddToReadListDialog(
            books = state.books,
            onDismissRequest = { state.showAddToReadListDialog = false })
    }
    if (state.showEditDialog) {
        if (state.books.size == 1)
            BookEditDialog(book = state.books.first(), onDismissRequest = { state.showEditDialog = false })
        else
            BookBulkEditDialog(books = state.books, onDismissRequest = { state.showEditDialog = false })
    }

    if (state.showDeleteDownloadedDialog) {
        val booksToDelete = remember(state.books) { state.books.filter { it.downloaded } }
        val textBody = remember(booksToDelete.size) {
            buildString {
                if (booksToDelete.size == 1) {
                    append("《${booksToDelete.first().metadata.title}》将从本机删除")
                } else {
                    append("${booksToDelete.size} 本漫画及其本地文件将从本机删除")
                }
            }
        }

        ConfirmationDialog(
            title = "删除本地下载",
            body = textBody,
            onDialogConfirm = {
                coroutineScope.launch {
                    state.actions.deleteDownloaded(booksToDelete)
                    state.showDeleteDownloadedDialog = false
                }
            },
            onDialogDismiss = {
                state.showDeleteDownloadedDialog = false
            }
        )
    }
    if (state.showDownloadDialog) {
        var permissionRequested by remember { mutableStateOf(false) }
        DownloadNotificationRequestDialog { permissionRequested = true }

        val bodyText = remember(state.books) {
            buildString {
                if (state.books.size == 1) append("下载《${state.books.first().metadata.title}》到本机？")
                else append("下载 ${state.books.size} 本漫画到本机？")
            }
        }
        if (permissionRequested) {
            ConfirmationDialog(
                body = bodyText,
                onDialogConfirm = {
                    coroutineScope.launch {
                        state.actions.download(state.books)
                    }
                },
                onDialogDismiss = { state.showDownloadDialog = false }
            )
        }
    }

    if (state.showDeleteDialog) {
        val textBody = remember(state.books.size) {
            buildString {
                if (state.books.size == 1) {
                    append("这本漫画")
                } else {
                    append("${state.books.size} 本漫画")
                }
                append("将从服务器删除，相关媒体文件也会被移除。此操作不可撤销。要继续吗？")
            }
        }

        val confirmationText = remember(state.books.size) {
            buildString {
                if (state.books.size == 1) {
                    append("删除这本漫画及其文件")
                } else {
                    append("删除 ${state.books.size} 本漫画及其文件")
                }
            }
        }
        ConfirmationDialog(
            title = "删除漫画",
            body = textBody,
            confirmText = confirmationText,
            onDialogConfirm = {
                coroutineScope.launch { state.actions.delete(state.books) }
                state.showDeleteDialog = false
            },
            onDialogDismiss = { state.showDeleteDialog = false },
            buttonConfirmColor = MaterialTheme.colorScheme.errorContainer
        )
    }

}

@Composable
fun rememberBookBulkActionsState(
    books: List<VangaBook>,
    actions: BookBulkActions? = null
): BookBulkActionsState {
    val coroutineScope = rememberCoroutineScope()
    val factory = LocalViewModelFactory.current
    val isOffline = LocalOfflineMode.current.collectAsState().value
    val isAdmin = LocalKomgaState.current.authenticatedUser.collectAsState().value?.roleAdmin() ?: true

    return remember(books, actions, isOffline) {
        BookBulkActionsState(
            books = books,
            actions = actions ?: factory.getBookBulkActions(),
            favoriteActions = factory.getFavoriteBulkActions(),
            isOffline = isOffline,
            isAdmin = isAdmin,
            coroutineScope = coroutineScope
        )
    }
}

data class BookBulkActionsState(
    val books: List<VangaBook>,
    val actions: BookBulkActions,
    private val favoriteActions: FavoriteBulkActions,
    private val isOffline: Boolean,
    private val isAdmin: Boolean,
    private val coroutineScope: CoroutineScope,
) {

    var showAddToReadListDialog by mutableStateOf(false)
    var showEditDialog by mutableStateOf(false)
    var showDownloadDialog by mutableStateOf(false)
    var showDeleteDownloadedDialog by mutableStateOf(false)
    var showDeleteDialog by mutableStateOf(false)

    val buttons = buildList {
        add(
            BulkActionButtonData(
                description = "标记已读",
                icon = Icons.Default.BookmarkAdd,
                onClick = { coroutineScope.launch { actions.markAsRead(books) } }
            ))

        add(
            BulkActionButtonData(
                description = "标记未读",
                icon = Icons.Default.BookmarkRemove,
                onClick = { coroutineScope.launch { actions.markAsUnread(books) } }
            ))
        add(
            BulkActionButtonData(
                description = "加入收藏",
                icon = Icons.Default.Star,
                onClick = { coroutineScope.launch { favoriteActions.addBooksToLocalFavorites(books) } }
            )
        )
        if (!isOffline && isAdmin) add(
            BulkActionButtonData(
                    description = "编辑",
                icon = Icons.Default.Edit,
                onClick = { showEditDialog = true }
            ))
        if (!isOffline && isAdmin)
            add(
                BulkActionButtonData(
                    description = "加入阅读清单",
                    icon = Icons.AutoMirrored.Default.PlaylistAdd,
                    onClick = { showAddToReadListDialog = true }
                ))
        if (books.any { it.downloaded })
            add(
                BulkActionButtonData(
                    description = "删除本地下载",
                    icon = Icons.Default.AutoDelete,
                    onClick = { showDeleteDownloadedDialog = true }
                ))
        if (!isOffline && books.any { !it.downloaded })
            add(
                BulkActionButtonData(
                    description = "下载",
                    icon = Icons.Default.Download,
                    onClick = { showDownloadDialog = true }
                ))

//        if (!isOffline && isAdmin) {
//            add(
//                BulkActionButtonData(
//                    description = "Delete from server",
//                    icon = Icons.Default.Delete,
//                    onClick =
//                        { showDeleteDialog = true }
//                ))
//        }
    }
}

data class BookBulkActions(
    val markAsRead: suspend (List<VangaBook>) -> Unit,
    val markAsUnread: suspend (List<VangaBook>) -> Unit,
    val delete: suspend (List<VangaBook>) -> Unit,
    val download: suspend (List<VangaBook>) -> Unit,
    val deleteDownloaded: suspend (List<VangaBook>) -> Unit,
) {

    constructor(
        bookApi: KomgaBookApi,
        taskEmitter: OfflineTaskEmitter,
        notifications: AppNotifications,
    ) : this(
        markAsRead = { books ->
            notifications.runCatchingToNotifications {
                books.forEach {
                    bookApi.markReadProgress(it.id, KomgaBookReadProgressUpdateRequest(completed = true))
                }
            }
        },
        markAsUnread = { books ->
            notifications.runCatchingToNotifications {
                books.forEach { bookApi.deleteReadProgress(it.id) }
            }
        },
        delete = { books ->
            notifications.runCatchingToNotifications {
                books.forEach { bookApi.deleteBook(it.id) }
            }
        },
        download = { books ->
            books.forEach { book ->
                taskEmitter.downloadBook(book.id)
            }
        },
        deleteDownloaded = { books ->
            books.forEach { book ->
                taskEmitter.deleteBook(book.id)
            }
        }
    )
}
