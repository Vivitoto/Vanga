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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import io.github.vivitoto.vanga.AppNotification
import io.github.vivitoto.vanga.AppNotifications
import io.github.vivitoto.vanga.komga.api.KomgaLibraryApi
import io.github.vivitoto.vanga.offline.tasks.OfflineTaskEmitter
import io.github.vivitoto.vanga.ui.LocalKomfIntegration
import io.github.vivitoto.vanga.ui.LocalKomgaState
import io.github.vivitoto.vanga.ui.LocalOfflineMode
import io.github.vivitoto.vanga.ui.LocalViewModelFactory
import io.github.vivitoto.vanga.ui.dialogs.ConfirmationDialog
import io.github.vivitoto.vanga.ui.dialogs.komf.reset.KomfResetLibraryMetadataDialog
import io.github.vivitoto.vanga.ui.dialogs.libraryedit.LibraryEditDialogs
import snd.komga.client.library.KomgaLibrary

@Composable
fun LibraryActionsMenu(
    library: KomgaLibrary,
    actions: LibraryMenuActions,
    expanded: Boolean,
    onDismissRequest: () -> Unit
) {
    var showLibraryEditDialog by remember { mutableStateOf(false) }
    if (showLibraryEditDialog) {
        LibraryEditDialogs(
            library = library,
            onDismissRequest = { showLibraryEditDialog = false }
        )
    }

    var showAnalyzeDialog by remember { mutableStateOf(false) }
    if (showAnalyzeDialog)
        ConfirmationDialog(
            title = "分析书库",
            body = "分析书库中的媒体文件并提取媒体信息。书库较大时可能需要较长时间。",
            onDialogConfirm = { actions.analyze(library) },
            onDialogDismiss = { showAnalyzeDialog = false }
        )

    var refreshMetadataDialog by remember { mutableStateOf(false) }
    if (refreshMetadataDialog)
        ConfirmationDialog(
            title = "刷新书库元数据",
            body = "刷新书库中所有媒体文件的元数据。书库较大时可能需要较长时间。",
            onDialogConfirm = { actions.refresh(library) },
            onDialogDismiss = { refreshMetadataDialog = false }
        )

    var emptyTrashDialog by remember { mutableStateOf(false) }
    if (emptyTrashDialog)
        ConfirmationDialog(
            title = "清空书库回收站",
            body = """
                    Komga 默认不会立刻删除缺失媒体的信息，以避免硬盘临时断开造成数据丢失。
                    清空回收站后，缺失媒体的记录会被删除。""".trimIndent(),
            onDialogConfirm = { actions.emptyTrash(library) },
            onDialogDismiss = { emptyTrashDialog = false }
        )

    var deleteLibraryDialog by remember { mutableStateOf(false) }
    if (deleteLibraryDialog)
        ConfirmationDialog(
            title = "删除书库",
            body = "书库「${library.name}」将从服务器删除，媒体文件本身不会受影响。此操作不可撤销。要继续吗？",
            confirmText = "删除书库「${library.name}」",
            onDialogConfirm = { actions.delete(library) },
            onDialogDismiss = { deleteLibraryDialog = false },
            buttonConfirmColor = MaterialTheme.colorScheme.errorContainer
        )
    var deleteOfflineLibraryDialog by remember { mutableStateOf(false) }
    if (deleteOfflineLibraryDialog)
        ConfirmationDialog(
            title = "删除本地下载",
            body = "书库「${library.name}」的离线内容将只从本机删除。",
            onDialogConfirm = { actions.deleteOffline(library) },
            onDialogDismiss = { deleteOfflineLibraryDialog = false },
            buttonConfirmColor = MaterialTheme.colorScheme.errorContainer
        )

    var showKomfResetDialog by remember { mutableStateOf(false) }
    if (showKomfResetDialog) {
        KomfResetLibraryMetadataDialog(
            library = library,
            onDismissRequest = {
                showKomfResetDialog = false
                onDismissRequest()
            }
        )
    }

    val isAdmin = LocalKomgaState.current.authenticatedUser.collectAsState().value?.roleAdmin() ?: true
    val isOffline = LocalOfflineMode.current.collectAsState().value
    DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest) {
        if (isAdmin && !isOffline) {
            DropdownMenuItem(
                text = { Text("扫描书库文件") },
                onClick = {
                    actions.scan(library)
                    onDismissRequest()
                }
            )

            val deepScanInteractionSource = remember { MutableInteractionSource() }
            val deepScanIsHovered = deepScanInteractionSource.collectIsHoveredAsState()
            val deepScanColor =
                if (deepScanIsHovered.value) Modifier.background(MaterialTheme.colorScheme.tertiaryContainer)
                else Modifier

            DropdownMenuItem(
                text = { Text("深度扫描书库文件") },
                onClick = {
                    actions.deepScan(library)
                    onDismissRequest()
                },
                modifier = Modifier
                    .hoverable(deepScanInteractionSource)
                    .then(deepScanColor)
            )
            DropdownMenuItem(
                text = { Text("分析文件") },
                onClick = {
                    showAnalyzeDialog = true
                    onDismissRequest()
                }
            )
            DropdownMenuItem(
                text = { Text("刷新元数据") },
                onClick = {
                    refreshMetadataDialog = true
                    onDismissRequest()
                }
            )
            DropdownMenuItem(
                text = { Text("清空回收站") },
                onClick = {
                    emptyTrashDialog = true
                    onDismissRequest()
                }
            )
            DropdownMenuItem(
                text = { Text("编辑") },
                onClick = {
                    showLibraryEditDialog = true
                    onDismissRequest()
                }
            )
        }

        val komfIntegration = LocalKomfIntegration.current.collectAsState(false)
        if (komfIntegration.value) {
            val vmFactory = LocalViewModelFactory.current
            val autoIdentifyVm = remember(library) {
                vmFactory.getKomfLibraryIdentifyViewModel(library)
            }
            DropdownMenuItem(
                text = { Text("自动识别元数据（Komf）") },
                onClick = {
                    autoIdentifyVm.autoIdentify()
                    onDismissRequest()
                },
            )

            DropdownMenuItem(
                text = { Text("重置元数据（Komf）") },
                onClick = { showKomfResetDialog = true },
            )
        }

        val deleteScanInteractionSource = remember { MutableInteractionSource() }
        val deleteScanIsHovered = deleteScanInteractionSource.collectIsHoveredAsState()
        val deleteScanColor =
            if (deleteScanIsHovered.value) Modifier.background(MaterialTheme.colorScheme.errorContainer)
            else Modifier

        if (!isOffline && isAdmin) {
            DropdownMenuItem(
                text = { Text("删除") },
                onClick = {
                    deleteLibraryDialog = true
                    onDismissRequest()
                },
                modifier = Modifier
                    .hoverable(deleteScanInteractionSource)
                    .then(deleteScanColor)
            )
        }
        if (isOffline) {
            DropdownMenuItem(
                text = { Text("删除本地下载") },
                onClick = {
                    deleteOfflineLibraryDialog = true
                    onDismissRequest()
                },
                modifier = Modifier
                    .hoverable(deleteScanInteractionSource)
                    .then(deleteScanColor)
            )

        }
    }
}

data class LibraryMenuActions(
    val scan: (KomgaLibrary) -> Unit,
    val deepScan: (KomgaLibrary) -> Unit,
    val analyze: (KomgaLibrary) -> Unit,
    val refresh: (KomgaLibrary) -> Unit,
    val emptyTrash: (KomgaLibrary) -> Unit,
    val delete: (KomgaLibrary) -> Unit,
    val deleteOffline: (KomgaLibrary) -> Unit
) {
    constructor(
        libraryApi: KomgaLibraryApi,
        notifications: AppNotifications,
        taskEmitter: OfflineTaskEmitter,
        scope: CoroutineScope
    ) : this(
        scan = {
            notifications.runCatchingToNotifications(scope) {
                libraryApi.scan(it.id)
                notifications.add(AppNotification.Normal("已开始扫描书库"))
            }
        },
        deepScan = {
            notifications.runCatchingToNotifications(scope) {
                libraryApi.scan(it.id, true)
                notifications.add(AppNotification.Normal("已开始深度扫描书库"))
            }
        },
        analyze = {
            notifications.runCatchingToNotifications(scope) {
                libraryApi.analyze(it.id)
                notifications.add(AppNotification.Normal("已开始分析书库"))
            }
        },
        refresh = {
            notifications.runCatchingToNotifications(scope) {
                libraryApi.refreshMetadata(it.id)
                notifications.add(AppNotification.Normal("已开始刷新书库元数据"))
            }
        },
        emptyTrash = {
            notifications.runCatchingToNotifications(scope) {
                libraryApi.emptyTrash(it.id)
                notifications.add(AppNotification.Normal("已开始清空书库回收站"))
            }
        },
        delete = {
            notifications.runCatchingToNotifications(scope) { libraryApi.deleteOne(it.id) }
        },
        deleteOffline = {
            scope.launch { taskEmitter.deleteLibrary(it.id) }
        }
    )
}
