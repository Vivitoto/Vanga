package io.github.vivitoto.vanga.ui.home.edit.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import io.github.vivitoto.vanga.ui.VangaShape
import io.github.vivitoto.vanga.ui.LocalPlatform
import io.github.vivitoto.vanga.ui.common.cards.BookImageCard
import io.github.vivitoto.vanga.ui.common.cards.SeriesImageCard
import io.github.vivitoto.vanga.ui.common.components.DropdownChoiceMenu
import io.github.vivitoto.vanga.ui.common.components.LabeledEntry
import io.github.vivitoto.vanga.ui.dialogs.ConfirmationDialog
import io.github.vivitoto.vanga.ui.home.edit.BookCustomFilterState
import io.github.vivitoto.vanga.ui.home.edit.BookFilterEditState
import io.github.vivitoto.vanga.ui.home.edit.BookOnDeckFilterState
import io.github.vivitoto.vanga.ui.home.edit.FilterEditState
import io.github.vivitoto.vanga.ui.home.edit.FilterEditViewModel
import io.github.vivitoto.vanga.ui.home.edit.SeriesCustomFilterState
import io.github.vivitoto.vanga.ui.home.edit.SeriesFilterEditState
import io.github.vivitoto.vanga.ui.home.edit.SeriesRecentlyAddedFilterState
import io.github.vivitoto.vanga.ui.home.edit.SeriesRecentlyUpdatedFilterState
import io.github.vivitoto.vanga.ui.platform.PlatformType.MOBILE
import io.github.vivitoto.vanga.ui.platform.cursorForHand
import io.github.vivitoto.vanga.ui.platform.cursorForMove

@Composable
fun FilterEditContent(
    filters: List<FilterEditState>,
    onFilterMove: (Int, Int) -> Unit,
    onEditEnd: () -> Unit,
    onFilterAdd: (FilterEditViewModel.FilterType) -> Unit,
    onFilterRemove: (FilterEditState) -> Unit,
    onFiltersReset: () -> Unit,
) {
    Column {
        Toolbar(onEditEnd, onFiltersReset)
        EditContent(
            filters = filters,
            onFilterAdd = onFilterAdd,
            onFilterRemove = onFilterRemove,
            onFilterMove = onFilterMove,
        )
    }
}

@Composable
private fun Toolbar(
    onEditEnd: () -> Unit,
    onReset: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = VangaShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            FilterChip(
                onClick = {},
                selected = true,
                label = {
                    Icon(Icons.Default.Tune, null)
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                border = null,
            )

            ElevatedButton(
                onClick = { onEditEnd() },
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
            ) {
                Text("完成")
                Icon(Icons.Default.Check, null)
            }

            var showResetDialog by remember { mutableStateOf(false) }
            ElevatedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
            ) {
                Text("重置为默认", maxLines = 1)
                Icon(Icons.Default.Restore, null)
            }
            if (showResetDialog) {
                ConfirmationDialog(
                    body = "将首页筛选器重置为默认？",
                    onDialogConfirm = onReset,
                    onDialogDismiss = { showResetDialog = false }
                )
            }
        }
    }
}

@Composable
private fun EditContent(
    filters: List<FilterEditState>,
    onFilterAdd: (FilterEditViewModel.FilterType) -> Unit,
    onFilterRemove: (FilterEditState) -> Unit,
    onFilterMove: (Int, Int) -> Unit,
) {
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        onFilterMove(from.index, to.index)
    }

    LazyColumn(
        state = lazyListState,
        contentPadding = PaddingValues(bottom = 50.dp),
        modifier = Modifier.imePadding()
    ) {
        items(filters, key = { it.hashCode() }) { data ->
            ReorderableItem(reorderableLazyListState, key = data.hashCode()) { isDragging ->
                FilterContent(
                    filterState = data,
                    isDragging = isDragging,
                    onFilterRemove = { onFilterRemove(data) }
                )
            }
        }
        item {
            AddConditionButton(onFilterAdd, modifier = Modifier.padding(start = 15.dp).animateItem())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddConditionButton(
    onConditionAdd: (FilterEditViewModel.FilterType) -> Unit,
    modifier: Modifier = Modifier
) {
    var dropDownExpanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = dropDownExpanded,
        onExpandedChange = { dropDownExpanded = it },
        modifier = modifier
    ) {
        FilledTonalButton(
            onClick = { dropDownExpanded = true },
            modifier = Modifier
                .cursorForHand()
                .menuAnchor(PrimaryNotEditable)
        ) {
            Text("添加筛选器")
        }

        ExposedDropdownMenu(
            expanded = dropDownExpanded,
            onDismissRequest = { dropDownExpanded = false },
            modifier = Modifier.widthIn(min = 200.dp)
        ) {
            FilterEditViewModel.FilterType.entries.forEach {
                DropdownMenuItem(
                    text = { Text(it.label()) },
                    onClick = {
                        dropDownExpanded = false
                        onConditionAdd(it)
                    },
                    modifier = Modifier.cursorForHand()
                )
            }
        }
    }
}

@Composable
private fun ReorderableCollectionItemScope.FilterContent(
    filterState: FilterEditState,
    isDragging: Boolean,
    onFilterRemove: () -> Unit,
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }
    val label = filterState.label.collectAsState().value
    var labelText by remember { mutableStateOf(label) }
    Card(
        modifier = Modifier
            .padding(vertical = 5.dp)
            .fillMaxWidth(),
        shape = VangaShape,
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging) {
                MaterialTheme.colorScheme.surfaceBright
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = if (isDragging) BorderStroke(2.dp, MaterialTheme.colorScheme.secondary) else null,
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()

            ) {
                val platform = LocalPlatform.current
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.heightIn(min = 46.dp).then(
                        if (platform != MOBILE) Modifier.draggableHandle().cursorForMove()
                        else Modifier
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = null,
                        modifier = Modifier.padding(start = 15.dp).size(32.dp)
                            .then(if (platform == MOBILE) Modifier.draggableHandle() else Modifier)
                    )
                    if (showEdit) {
                        OutlinedTextField(
                            value = labelText,
                            label = { Text("名称") },
                            onValueChange = {
                                labelText = it
                                filterState.label.value = it
                            },
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )
                    } else {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier
                                .padding(horizontal = 14.dp)
                                .widthIn(min = 180.dp, max = 360.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                ElevatedButton(
                    onClick = { showEdit = !showEdit },
                    modifier = Modifier.cursorForHand()
                ) {
                    Text(if (showEdit) "收起" else "编辑")
                    Icon(
                        imageVector = if (showEdit) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = null,
                    )
                }

                ElevatedButton(
                    onClick = {
                        showDeleteConfirmation = true
                    },
                    modifier = Modifier.cursorForHand()
                ) {
                    Text("删除")
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                    )
                }
            }

            AnimatedVisibility(showEdit, modifier = Modifier.padding(5.dp)) {
                when (filterState) {
                    is BookFilterEditState -> BookFilterEditContent(filterState)
                    is SeriesFilterEditState -> SeriesFilterEditContent(filterState)
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        ConfirmationDialog(
            body = "删除 ${label}？",
            onDialogConfirm = onFilterRemove,
            onDialogDismiss = { showDeleteConfirmation = false })
    }
}

@Composable
private fun BookFilterEditContent(state: BookFilterEditState) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        val filter = state.filter.collectAsState().value
        val type = state.type.collectAsState().value
        DropdownChoiceMenu(
            selectedOption = LabeledEntry(type, type.label()),
            options = remember { BookFilterEditState.FilterType.entries.map { LabeledEntry(it, it.label()) } },
            onOptionChange = { state.onTypeChange(it.value) },
        )

        when (filter) {
            is BookCustomFilterState -> BookConditionContent(filter)
            is BookOnDeckFilterState -> PageSizeSettingsContent(
                pageSize = filter.pageSize.collectAsState().value,
                onPageSizeChange = filter::onPageSizeChange
            )
        }

        val books = state.books.collectAsState().value
        val cardWidth = state.cardWidth.collectAsState().value
        LazyRow(
            contentPadding = PaddingValues(15.dp),
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            items(books) {
                BookImageCard(book = it, modifier = Modifier.width(cardWidth))
            }
        }
    }
}

@Composable
private fun SeriesFilterEditContent(state: SeriesFilterEditState) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        val filter = state.filter.collectAsState().value
        val type = state.type.collectAsState().value
        DropdownChoiceMenu(
            selectedOption = LabeledEntry(type, type.label()),
            options = remember { SeriesFilterEditState.FilterType.entries.map { LabeledEntry(it, it.label()) } },
            onOptionChange = { state.onTypeChange(it.value) },
        )

        when (filter) {
            is SeriesCustomFilterState -> SeriesConditionContent(filter)
            is SeriesRecentlyAddedFilterState -> PageSizeSettingsContent(
                pageSize = filter.pageSize.collectAsState().value,
                onPageSizeChange = filter::onPageSizeChange
            )

            is SeriesRecentlyUpdatedFilterState -> PageSizeSettingsContent(
                pageSize = filter.pageSize.collectAsState().value,
                onPageSizeChange = filter::onPageSizeChange
            )
        }

        val books = state.series.collectAsState().value
        val cardWidth = state.cardWidth.collectAsState().value
        LazyRow(
            contentPadding = PaddingValues(15.dp),
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            items(books) {
                SeriesImageCard(series = it, modifier = Modifier.width(cardWidth))
            }
        }
    }
}
