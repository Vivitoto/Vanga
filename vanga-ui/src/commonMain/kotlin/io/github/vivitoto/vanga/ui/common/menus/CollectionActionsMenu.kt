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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.vivitoto.vanga.ui.dialogs.ConfirmationDialog
import io.github.vivitoto.vanga.ui.dialogs.collectionedit.CollectionEditDialog
import snd.komga.client.collection.KomgaCollection

@Composable
fun CollectionActionsMenu(
    collection: KomgaCollection,
    onCollectionDelete: () -> Unit,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    if (showDeleteDialog) {
        ConfirmationDialog(
            title = "删除合集",
            body = "合集「${collection.name}」将从服务器删除，媒体文件本身不会受影响。此操作不可撤销。要继续吗？",
            confirmText = "删除合集「${collection.name}」",
            onDialogConfirm = {
                onCollectionDelete()
                onDismissRequest()

            },
            onDialogDismiss = {
                showDeleteDialog = false
                onDismissRequest()
            },
            buttonConfirmColor = MaterialTheme.colorScheme.errorContainer
        )
    }

    var showEditDialog by remember { mutableStateOf(false) }
    if (showEditDialog) {
        CollectionEditDialog(collection = collection, onDismissRequest = {
            showEditDialog = false
            onDismissRequest()
        })
    }

    val showDropdown = derivedStateOf { expanded && !showDeleteDialog && !showEditDialog }
    DropdownMenu(
        expanded = showDropdown.value,
        onDismissRequest = onDismissRequest
    ) {
        val deleteInteractionSource = remember { MutableInteractionSource() }
        val deleteIsHovered = deleteInteractionSource.collectIsHoveredAsState()
        DropdownMenuItem(
            text = { Text("编辑") },
            onClick = { showEditDialog = true },
        )

        DropdownMenuItem(
            text = { Text("删除") },
            onClick = { showDeleteDialog = true },
            modifier = Modifier
                .hoverable(deleteInteractionSource)
                .then(
                    if (deleteIsHovered.value) Modifier.background(MaterialTheme.colorScheme.errorContainer)
                    else Modifier
                )
        )

    }
}
