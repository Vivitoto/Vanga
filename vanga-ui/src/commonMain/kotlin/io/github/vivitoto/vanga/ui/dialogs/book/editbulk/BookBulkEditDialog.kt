package io.github.vivitoto.vanga.ui.dialogs.book.editbulk

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import io.github.vivitoto.vanga.komga.api.model.VangaBook
import io.github.vivitoto.vanga.ui.LocalViewModelFactory
import io.github.vivitoto.vanga.ui.dialogs.tabs.TabDialog


@Composable
fun BookBulkEditDialog(
    books: List<VangaBook>,
    onDismissRequest: () -> Unit,
) {
    val viewModelFactory = LocalViewModelFactory.current
    val coroutineScope = rememberCoroutineScope()
    val vm = remember { viewModelFactory.getBookBulkEditDialogViewModel(books, onDismissRequest) }
    LaunchedEffect(books) { vm.initialize() }

    TabDialog(
        title = "编辑 ${books.size} 本书",
        currentTab = vm.currentTab,
        tabs = vm.tabs(),
        confirmationText = "保存更改",
        onConfirm = { coroutineScope.launch { vm.saveChanges() } },
        onTabChange = { vm.currentTab = it },
        onDismissRequest = onDismissRequest
    )
}