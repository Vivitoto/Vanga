package io.github.vivitoto.vanga.ui.dialogs.libraryedit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import io.github.vivitoto.vanga.ui.LocalViewModelFactory
import io.github.vivitoto.vanga.ui.dialogs.tabs.TabDialog
import snd.komga.client.library.KomgaLibrary

@Composable
fun LibraryEditDialogs(
    library: KomgaLibrary?,
    onDismissRequest: () -> Unit
) {
    val viewModelFactory = LocalViewModelFactory.current
    val vm = remember { viewModelFactory.getLibraryEditDialogViewModel(library, onDismissRequest) }
    val coroutineScope = rememberCoroutineScope()

    val title = if (library != null) "编辑书库" else "添加书库"
    val confirmationText = remember(library, vm.currentTab) {
        when {
            library != null -> "Edit"
            vm.currentTab is MetadataTab -> "Add"
            else -> "Next"
        }
    }

    TabDialog(
        title = title,
        currentTab = vm.currentTab,
        tabs = vm.tabs(),
        onTabChange = { vm.currentTab = it },
        onConfirm = { coroutineScope.launch { vm.onNextTabSwitch() } },
        confirmationText = confirmationText,
        onDismissRequest = onDismissRequest,
    )

}
