package io.github.vivitoto.vanga.ui.dialogs.komf.reset

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import io.github.vivitoto.vanga.ui.LocalViewModelFactory
import io.github.vivitoto.vanga.ui.common.components.SwitchWithLabel
import io.github.vivitoto.vanga.ui.dialogs.AppDialog
import io.github.vivitoto.vanga.ui.dialogs.DialogConfirmCancelButtons
import io.github.vivitoto.vanga.ui.dialogs.DialogSimpleHeader
import snd.komf.api.KomfServerLibraryId
import snd.komf.api.KomfServerSeriesId
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.series.KomgaSeries

val resetSeriesText = """
    将重置此作品的全部元数据，包括字段锁定和由 Komf 上传的封面。
    不会修改本地文件。是否继续？
""".trimIndent()
val resetLibraryText = """
    将重置此书库内全部作品的元数据，包括字段锁定和由 Komf 上传的封面。
    不会修改本地文件。是否继续？
""".trimIndent()

@Composable
fun KomfResetSeriesMetadataDialog(
    series: KomgaSeries,
    onDismissRequest: () -> Unit,
) {
    KomfResetSeriesMetadataDialog(
        seriesId = KomfServerSeriesId(value = series.id.value),
        libraryId = KomfServerLibraryId(series.libraryId.value),
        onDismissRequest = onDismissRequest
    )
}

@Composable
fun KomfResetSeriesMetadataDialog(
    seriesId: KomfServerSeriesId,
    libraryId: KomfServerLibraryId,
    onDismissRequest: () -> Unit,
) {
    val viewModelFactory = LocalViewModelFactory.current
    val vm = remember { viewModelFactory.getKomfResetMetadataDialogViewModel(onDismissRequest) }
    ResetDialog(
        dialogText = resetSeriesText,
        removeComicInfo = vm.removeComicInfo,
        onRemoveComicInfoChange = vm::removeComicInfo::set,
        onConfirm = { vm.onSeriesReset(seriesId, libraryId) },
        onDismissRequest = onDismissRequest
    )
}

@Composable
fun KomfResetLibraryMetadataDialog(
    library: KomgaLibrary,
    onDismissRequest: () -> Unit,
) {
    KomfResetLibraryMetadataDialog(KomfServerLibraryId(library.id.value), onDismissRequest)
}

@Composable
fun KomfResetLibraryMetadataDialog(
    libraryId: KomfServerLibraryId,
    onDismissRequest: () -> Unit,
) {
    val viewModelFactory = LocalViewModelFactory.current
    val vm = remember { viewModelFactory.getKomfResetMetadataDialogViewModel(onDismissRequest) }
    ResetDialog(
        dialogText = resetLibraryText,
        removeComicInfo = vm.removeComicInfo,
        onRemoveComicInfoChange = vm::removeComicInfo::set,
        onConfirm = { vm.onLibraryReset(libraryId) },
        onDismissRequest = onDismissRequest
    )
}

@Composable
fun ResetDialog(
    dialogText: String,
    removeComicInfo: Boolean,
    onRemoveComicInfoChange: (Boolean) -> Unit,
    onConfirm: suspend () -> Unit,
    onDismissRequest: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    AppDialog(
        contentPadding = PaddingValues(20.dp),
        modifier = Modifier.widthIn(max = 650.dp),
        header = { DialogSimpleHeader("重置书库元数据") },
        content = { DialogContent(dialogText, removeComicInfo, onRemoveComicInfoChange) },
        controlButtons = {
            DialogConfirmCancelButtons(
                onConfirm = {
                    coroutineScope.launch {
                        isLoading = true
                        onConfirm()
                        onDismissRequest()
                    }
                },
                onCancel = onDismissRequest,
                isLoading = isLoading
            )
        },
        onDismissRequest = onDismissRequest
    )
}

@Composable
private fun DialogContent(
    dialogText: String,
    removeComicInfo: Boolean,
    onRemoveComicInfoChange: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier.heightIn(min = 200.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(text = dialogText)
        SwitchWithLabel(
            checked = removeComicInfo,
            onCheckedChange = onRemoveComicInfoChange,
            label = { Text("移除 ComicInfo.xml？") },
            supportingText = { Text("需要文件写入权限") }
        )
    }
}
