package io.github.vivitoto.vanga.ui.dialogs.oneshot

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import io.github.vivitoto.vanga.komga.api.model.VangaBook
import io.github.vivitoto.vanga.ui.LoadState.Error
import io.github.vivitoto.vanga.ui.LoadState.Loading
import io.github.vivitoto.vanga.ui.LoadState.Success
import io.github.vivitoto.vanga.ui.LoadState.Uninitialized
import io.github.vivitoto.vanga.ui.LocalViewModelFactory
import io.github.vivitoto.vanga.ui.dialogs.DialogLoadIndicator
import io.github.vivitoto.vanga.ui.dialogs.tabs.TabDialog
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesId

@Composable
fun OneshotEditDialog(
    seriesId: KomgaSeriesId,
    series: KomgaSeries?,
    book: VangaBook?,
    onDismissRequest: () -> Unit,
) {
    val viewModelFactory = LocalViewModelFactory.current
    val coroutineScope = rememberCoroutineScope()
    val vm = remember {
        viewModelFactory.getOneshotEditDialogViewModel(seriesId, series, book, onDismissRequest)
    }
    LaunchedEffect(book) { vm.initialize() }
    when (val loadState = vm.loadState.collectAsState().value) {
        Uninitialized, Loading -> DialogLoadIndicator(onDismissRequest)
        is Success -> TabDialog(
            title = "编辑 ${loadState.value.seriesMetadataState.series.metadata.title}",
            currentTab = loadState.value.currentTab,
            tabs = loadState.value.tabs,
            confirmationText = "Save",
            onConfirm = { coroutineScope.launch { vm.saveChanges() } },
            onTabChange = { loadState.value.currentTab = it },
            onDismissRequest = onDismissRequest
        )

        is Error -> onDismissRequest()
    }

}
