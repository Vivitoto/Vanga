package io.github.vivitoto.vanga.ui.dialogs.series.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import io.github.vivitoto.vanga.ui.LocalViewModelFactory
import io.github.vivitoto.vanga.ui.dialogs.oneshot.OneshotEditDialog
import io.github.vivitoto.vanga.ui.dialogs.tabs.TabDialog
import snd.komga.client.series.KomgaSeries

@Composable
fun SeriesEditDialog(
    series: KomgaSeries,
    onDismissRequest: () -> Unit
) {
    val viewModelFactory = LocalViewModelFactory.current
    val vm = remember { viewModelFactory.getSeriesEditDialogViewModel(series, onDismissRequest) }
    LaunchedEffect(series) { vm.initialize() }

    val coroutineScope = rememberCoroutineScope()
    if (series.oneshot) {
        OneshotEditDialog(series.id, series, null, onDismissRequest)
    } else {
        TabDialog(
            title = "编辑 ${series.metadata.title}",
            currentTab = vm.currentTab,
            tabs = vm.tabs,
            confirmationText = "保存",
            onConfirm = { coroutineScope.launch { vm.saveChanges() } },
            onTabChange = { vm.currentTab = it },
            onDismissRequest = { onDismissRequest() }
        )
    }
}
