package io.github.vivitoto.vanga.ui.common.menus.bulk

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LayersClear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import io.github.vivitoto.vanga.AppNotifications
import io.github.vivitoto.vanga.komga.api.KomgaReadListApi
import io.github.vivitoto.vanga.komga.api.model.VangaBook
import io.github.vivitoto.vanga.ui.LocalKomgaState
import io.github.vivitoto.vanga.ui.LocalOfflineMode
import io.github.vivitoto.vanga.ui.LocalViewModelFactory
import io.github.vivitoto.vanga.ui.dialogs.ConfirmationDialog
import snd.komga.client.common.PatchValue
import snd.komga.client.readlist.KomgaReadList
import snd.komga.client.readlist.KomgaReadListUpdateRequest

@Composable
fun ReadListBulkActionsContent(
    readList: KomgaReadList,
    books: List<VangaBook>,
    compact: Boolean,
) {
    val readListState = rememberReadListBulkActionsState(readList, books)
    val bookState = rememberBookBulkActionsState(books)
    val buttons = remember(readListState, bookState) { readListState.buttons + bookState.buttons }
    BulkActionsButtonsLayout(buttons, compact)
    ReadListBulkActionsDialogs(readListState)
    BookBulkActionDialogs(bookState)
}

@Composable
fun ReadListBulkActionsDialogs(state: ReadListBulkActinsState) {
    val coroutineScope = rememberCoroutineScope()
    if (state.showDeleteDialog) {
        ConfirmationDialog(
            body = "从此阅读清单中移除选中的书籍？",
            onDialogConfirm = {
                coroutineScope.launch { state.actions.removeFromReadList(state.readList, state.books) }
                state.showDeleteDialog = false
            },
            onDialogDismiss = { state.showDeleteDialog = false },
        )
    }
}

@Composable
fun rememberReadListBulkActionsState(
    readList: KomgaReadList,
    books: List<VangaBook>,
): ReadListBulkActinsState {
    val factory = LocalViewModelFactory.current
    val isOffline = LocalOfflineMode.current.collectAsState().value
    val isAdmin = LocalKomgaState.current.authenticatedUser.collectAsState().value?.roleAdmin() ?: true
    return remember(readList, books, isOffline, isAdmin) {
        val actions = factory.getReadListBulkActions()
        ReadListBulkActinsState(
            readList = readList,
            books = books,
            actions = actions,
            isOffline = isOffline,
            isAdmin = isAdmin
        )
    }
}

data class ReadListBulkActions(
    val removeFromReadList: suspend (KomgaReadList, List<VangaBook>) -> Unit
) {
    constructor(
        readListApi: KomgaReadListApi,
        notifications: AppNotifications,
    ) : this(
        removeFromReadList = { readList, books ->
            notifications.runCatchingToNotifications {

                val selectedIds = books.map { it.id }
                readListApi.updateOne(
                    readList.id,
                    KomgaReadListUpdateRequest(
                        bookIds = PatchValue.Some(readList.bookIds.filter { it !in selectedIds })
                    )
                )

            }
        },
    )

}


data class ReadListBulkActinsState(
    val readList: KomgaReadList,
    val books: List<VangaBook>,
    val actions: ReadListBulkActions,
    private val isOffline: Boolean,
    private val isAdmin: Boolean,
) {
    var showDeleteDialog by mutableStateOf(false)

    val buttons = buildList {
        if (!isOffline && isAdmin) {
            add(
                BulkActionButtonData(
                    description = "从阅读清单中移除",
                    icon = Icons.Default.LayersClear,
                    onClick = { showDeleteDialog = true }
                )
            )
        }
    }
}
