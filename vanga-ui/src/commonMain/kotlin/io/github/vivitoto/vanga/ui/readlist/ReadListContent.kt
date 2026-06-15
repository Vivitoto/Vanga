package io.github.vivitoto.vanga.ui.readlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.vivitoto.vanga.komga.api.model.VangaBook
import io.github.vivitoto.vanga.ui.LocalKomgaState
import io.github.vivitoto.vanga.ui.LocalWindowWidth
import io.github.vivitoto.vanga.ui.common.components.PageSizeSelectionDropdown
import io.github.vivitoto.vanga.ui.common.itemlist.BookLazyCardGrid
import io.github.vivitoto.vanga.ui.common.menus.BookMenuActions
import io.github.vivitoto.vanga.ui.common.menus.ReadListActionsMenu
import io.github.vivitoto.vanga.ui.common.menus.bulk.BottomPopupBulkActionsPanel
import io.github.vivitoto.vanga.ui.common.menus.bulk.BulkActionsContainer
import io.github.vivitoto.vanga.ui.common.menus.bulk.ReadListBulkActionsContent
import io.github.vivitoto.vanga.ui.platform.WindowSizeClass
import snd.komga.client.readlist.KomgaReadList

@Composable
fun ReadListContent(
    readList: KomgaReadList,
    onReadListDelete: () -> Unit,

    books: List<VangaBook>,
    bookMenuActions: BookMenuActions,
    onBookClick: (VangaBook) -> Unit,
    onBookReadClick: (VangaBook, Boolean) -> Unit,

    selectedBooks: List<VangaBook>,
    onBookSelect: (VangaBook) -> Unit,

    editMode: Boolean,
    onEditModeChange: (Boolean) -> Unit,
    onReorder: (fromIndex: Int, toIndex: Int) -> Unit,
    onReorderDragStateChange: (dragging: Boolean) -> Unit = {},

    totalPages: Int,
    currentPage: Int,
    pageSize: Int,
    onPageChange: (Int) -> Unit,
    onPageSizeChange: (Int) -> Unit,

    cardMinSize: Dp,
) {
    val isAdmin = LocalKomgaState.current.authenticatedUser.collectAsState().value?.roleAdmin() ?: true

    Column {
        if (editMode)
            BulkActionsToolbar(
                onCancel = { onEditModeChange(false) },
                readList = readList,
                books = books,
                selectedBooks = selectedBooks,
                onBookSelect = onBookSelect,
                isAdmin = isAdmin,
            )
        else {
            ReadListToolbar(
                readList = readList,
                onReadListDelete = onReadListDelete,
                onEditModeEnable = { onEditModeChange(true) },
                isAdmin = isAdmin,

                pageSize = pageSize,
                onPageSizeChange = onPageSizeChange,
            )
        }

        if (readList.summary.isNotBlank()) {
            Text(
                text = readList.summary,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(5.dp))
            HorizontalDivider()
        }
        BookLazyCardGrid(
            books = books,
            onBookClick = if (editMode) onBookSelect else onBookClick,
            onBookReadClick = if (editMode) null else onBookReadClick,
            bookMenuActions = if (editMode) null else bookMenuActions,

            selectedBooks = selectedBooks,
            onBookSelect = onBookSelect,
            showSelectionControls = editMode,

            reorderable = readList.ordered && editMode && isAdmin,
            onReorder = onReorder,
            onReorderDragStateChange = onReorderDragStateChange,

            totalPages = totalPages,
            currentPage = currentPage,
            onPageChange = onPageChange,

            minSize = cardMinSize,
        )

        val width = LocalWindowWidth.current
        if ((width == WindowSizeClass.COMPACT || width == WindowSizeClass.MEDIUM) && selectedBooks.isNotEmpty()) {
            BottomPopupBulkActionsPanel(onCancel = { onEditModeChange(false) }) {
                ReadListBulkActionsContent(readList, selectedBooks, true)
            }
        }
    }
}

@Composable
private fun ReadListToolbar(
    readList: KomgaReadList,
    onReadListDelete: () -> Unit,
    onEditModeEnable: () -> Unit,
    isAdmin: Boolean,
    pageSize: Int,
    onPageSizeChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                readList.name,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
            )
            Text(
                "阅读清单 · ${readList.bookIds.size} 本书",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (isAdmin) {
            Box {
                var expandActions by remember { mutableStateOf(false) }
                IconButton(onClick = { expandActions = true }) {
                    Icon(Icons.Rounded.MoreVert, null)
                }

                ReadListActionsMenu(
                    readList = readList,
                    onReadListDelete = onReadListDelete,
                    expanded = expandActions,
                    onDismissRequest = { expandActions = false }
                )
            }
        }
        IconButton(onClick = onEditModeEnable) { Icon(Icons.Default.EditNote, null) }
        PageSizeSelectionDropdown(pageSize, onPageSizeChange)
    }
}

@Composable
private fun BulkActionsToolbar(
    onCancel: () -> Unit,
    readList: KomgaReadList,
    books: List<VangaBook>,
    selectedBooks: List<VangaBook>,
    onBookSelect: (VangaBook) -> Unit,
    isAdmin: Boolean,
) {
    BulkActionsContainer(
        onCancel = onCancel,
        selectedCount = selectedBooks.size,
        allSelected = books.size == selectedBooks.size,
        onSelectAll = {
            if (books.size == selectedBooks.size) books.forEach { onBookSelect(it) }
            else books.filter { it !in selectedBooks }.forEach { onBookSelect(it) }
        }
    ) {
        when (LocalWindowWidth.current) {
            WindowSizeClass.FULL -> {
                if (readList.ordered && isAdmin) Text("编辑模式：点击选择，拖动调整顺序")
                else Text("选择模式：点击条目选择或取消选择")
                if (selectedBooks.isNotEmpty()) {
                    Spacer(Modifier.weight(1f))

                    ReadListBulkActionsContent(readList, selectedBooks, false)
                }
            }

            WindowSizeClass.EXPANDED -> {
                if (selectedBooks.isEmpty()) {
                    if (readList.ordered && isAdmin) Text("编辑模式：点击选择，拖动调整顺序")
                    else Text("选择模式：点击条目选择或取消选择")
                } else {
                    Spacer(Modifier.weight(1f))
                    ReadListBulkActionsContent(readList, selectedBooks, false)
                }
            }

            WindowSizeClass.COMPACT, WindowSizeClass.MEDIUM -> {}
        }
    }
}
