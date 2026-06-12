package io.github.vivitoto.vanga.ui.dialogs.readlistedit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import io.github.vivitoto.vanga.ui.LocalViewModelFactory
import io.github.vivitoto.vanga.ui.dialogs.tabs.TabDialog
import snd.komga.client.readlist.KomgaReadList

@Composable
fun ReadListEditDialog(
    readList: KomgaReadList,
    onDismissRequest: () -> Unit
) {

    val viewModelFactory = LocalViewModelFactory.current
    val vm = remember { viewModelFactory.getReadListEditDialogViewModel(readList, onDismissRequest) }
    LaunchedEffect(readList) { vm.initialize() }

    val coroutineScope = rememberCoroutineScope()
    TabDialog(
        title = "编辑 ${readList.name}",
        currentTab = vm.currentTab,
        tabs = vm.tabs(),
        confirmationText = "保存更改",
        confirmEnabled = vm.canSave(),
        onConfirm = { coroutineScope.launch { vm.saveChanges() } },
        onTabChange = { vm.currentTab = it },
        onDismissRequest = { onDismissRequest() }
    )
}