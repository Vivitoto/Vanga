package io.github.vivitoto.vanga.ui.collection

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.rounded.MoreVert
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
import io.github.vivitoto.vanga.ui.LocalKomgaState
import io.github.vivitoto.vanga.ui.LocalWindowWidth
import io.github.vivitoto.vanga.ui.common.components.PageSizeSelectionDropdown
import io.github.vivitoto.vanga.ui.common.itemlist.CardGridBottomPadding
import io.github.vivitoto.vanga.ui.common.itemlist.SeriesLazyCardGrid
import io.github.vivitoto.vanga.ui.common.menus.CollectionActionsMenu
import io.github.vivitoto.vanga.ui.common.menus.SeriesMenuActions
import io.github.vivitoto.vanga.ui.common.menus.bulk.BottomPopupBulkActionsPanel
import io.github.vivitoto.vanga.ui.common.menus.bulk.BulkActionsContainer
import io.github.vivitoto.vanga.ui.common.menus.bulk.CollectionBulkActionsContent
import io.github.vivitoto.vanga.ui.platform.WindowSizeClass.COMPACT
import io.github.vivitoto.vanga.ui.platform.WindowSizeClass.EXPANDED
import io.github.vivitoto.vanga.ui.platform.WindowSizeClass.FULL
import io.github.vivitoto.vanga.ui.platform.WindowSizeClass.MEDIUM
import io.github.vivitoto.vanga.ui.rememberMeasuredBottomOverlayPadding
import snd.komga.client.collection.KomgaCollection
import snd.komga.client.series.KomgaSeries

@Composable
fun CollectionContent(
    collection: KomgaCollection,
    onCollectionDelete: () -> Unit,

    series: List<KomgaSeries>,
    totalSeriesCount: Int,

    editMode: Boolean,
    onEditModeChange: (Boolean) -> Unit,
    onSeriesClick: (KomgaSeries) -> Unit,
    seriesActions: SeriesMenuActions,
    onReorder: (fromIndex: Int, toIndex: Int) -> Unit,
    onReorderDragStateChange: (dragging: Boolean) -> Unit = {},

    selectedSeries: List<KomgaSeries>,
    onSeriesSelect: (KomgaSeries) -> Unit,

    totalPages: Int,
    currentPage: Int,
    pageSize: Int,
    onPageChange: (Int) -> Unit,
    onPageSizeChange: (Int) -> Unit,

    cardMinSize: Dp,
) {
    Column {
        if (editMode) {
            BulkActionsToolbar(
                onCancel = { onEditModeChange(false) },
                collection = collection,
                series = series,
                selectedSeries = selectedSeries,
                onSeriesSelect = onSeriesSelect
            )
        } else CollectionToolbar(
            collection = collection,
            onCollectionDelete = onCollectionDelete,
            onEditModeEnable = { onEditModeChange(true) },

            totalSeriesCount = totalSeriesCount,
            pageSize = pageSize,
            onPageSizeChange = onPageSizeChange,

            )

        val width = LocalWindowWidth.current
        val bottomOverlayVisible = (width == COMPACT || width == MEDIUM) && selectedSeries.isNotEmpty()
        val bottomOverlayPadding = rememberMeasuredBottomOverlayPadding(
            visible = bottomOverlayVisible,
            basePadding = CardGridBottomPadding,
        )

        SeriesLazyCardGrid(
            series = series,
            onSeriesClick = if (editMode) onSeriesSelect else onSeriesClick,
            seriesMenuActions = if (editMode) null else seriesActions,

            selectedSeries = selectedSeries,
            onSeriesSelect = onSeriesSelect,
            showSelectionControls = editMode,

            reorderable = collection.ordered && editMode,
            onReorder = onReorder,
            onReorderDragStateChange = onReorderDragStateChange,

            totalPages = totalPages,
            currentPage = currentPage,
            onPageChange = onPageChange,

            minSize = cardMinSize,
            contentPadding = PaddingValues(bottom = bottomOverlayPadding.bottomPadding),
            modifier = Modifier.weight(1f)
        )

        if (bottomOverlayVisible) {
            BottomPopupBulkActionsPanel(
                onCancel = { onEditModeChange(false) },
                onSizeChanged = bottomOverlayPadding.onOverlaySizeChanged,
            ) {
                CollectionBulkActionsContent(collection, selectedSeries, true)
            }
        }
    }
}


@Composable
private fun CollectionToolbar(
    collection: KomgaCollection,
    onCollectionDelete: () -> Unit,
    onEditModeEnable: () -> Unit,

    totalSeriesCount: Int,
    pageSize: Int,
    onPageSizeChange: (Int) -> Unit,

) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                collection.name,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
            )
            Text(
                "合集 · $totalSeriesCount 部作品",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        val isAdmin = LocalKomgaState.current.authenticatedUser.collectAsState().value?.roleAdmin() ?: true
        if (isAdmin) {
            Box {
                var expandActions by remember { mutableStateOf(false) }
                IconButton(onClick = { expandActions = true }) {
                    Icon(Icons.Rounded.MoreVert, null)
                }

                CollectionActionsMenu(
                    collection = collection,
                    onCollectionDelete = onCollectionDelete,
                    expanded = expandActions,
                    onDismissRequest = { expandActions = false }
                )
            }

            IconButton(onClick = onEditModeEnable) { Icon(Icons.Default.EditNote, null) }
        }

        PageSizeSelectionDropdown(pageSize, onPageSizeChange)
    }
}

@Composable
private fun BulkActionsToolbar(
    onCancel: () -> Unit,
    collection: KomgaCollection,
    series: List<KomgaSeries>,
    selectedSeries: List<KomgaSeries>,
    onSeriesSelect: (KomgaSeries) -> Unit,
) {
    BulkActionsContainer(
        onCancel = onCancel,
        selectedCount = selectedSeries.size,
        allSelected = series.size == selectedSeries.size,
        onSelectAll = {
            if (series.size == selectedSeries.size) series.forEach { onSeriesSelect(it) }
            else series.filter { it !in selectedSeries }.forEach { onSeriesSelect(it) }
        }
    ) {
        when (LocalWindowWidth.current) {
            FULL -> {
                if (collection.ordered) Text("点击选择，拖动调整顺序")
                else Text("点击条目可选择或取消选择")
                if (selectedSeries.isNotEmpty()) {
                    Spacer(Modifier.weight(1f))

                    CollectionBulkActionsContent(collection, selectedSeries, false)
                }
            }

            EXPANDED -> {
                if (selectedSeries.isEmpty()) {
                    if (collection.ordered) Text("点击选择，拖动调整顺序")
                    else Text("点击条目可选择或取消选择")
                } else {
                    Spacer(Modifier.weight(1f))
                    CollectionBulkActionsContent(collection, selectedSeries, false)
                }
            }

            COMPACT, MEDIUM -> {}
        }
    }
}
