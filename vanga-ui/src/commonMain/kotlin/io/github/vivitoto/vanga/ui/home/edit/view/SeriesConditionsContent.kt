package io.github.vivitoto.vanga.ui.home.edit.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vivitoto.vanga.ui.common.components.DropdownChoiceMenu
import io.github.vivitoto.vanga.ui.common.components.LabeledEntry
import io.github.vivitoto.vanga.ui.home.EqualityOpState
import io.github.vivitoto.vanga.ui.home.NumericNullableOpState
import io.github.vivitoto.vanga.ui.home.edit.AgeRatingConditionState
import io.github.vivitoto.vanga.ui.home.edit.AuthorConditionState
import io.github.vivitoto.vanga.ui.home.edit.CollectionIdConditionState
import io.github.vivitoto.vanga.ui.home.edit.CompleteConditionState
import io.github.vivitoto.vanga.ui.home.edit.DeletedConditionState
import io.github.vivitoto.vanga.ui.home.edit.GenreConditionState
import io.github.vivitoto.vanga.ui.home.edit.LanguageConditionState
import io.github.vivitoto.vanga.ui.home.edit.LibraryConditionState
import io.github.vivitoto.vanga.ui.home.edit.MatchType
import io.github.vivitoto.vanga.ui.home.edit.OneShotConditionState
import io.github.vivitoto.vanga.ui.home.edit.PublisherConditionState
import io.github.vivitoto.vanga.ui.home.edit.ReadStatusConditionState
import io.github.vivitoto.vanga.ui.home.edit.ReleaseDateConditionState
import io.github.vivitoto.vanga.ui.home.edit.SeriesConditionState
import io.github.vivitoto.vanga.ui.home.edit.SeriesCustomFilterState
import io.github.vivitoto.vanga.ui.home.edit.SeriesMatchConditionState
import io.github.vivitoto.vanga.ui.home.edit.SeriesMatchConditionState.SeriesConditionType
import io.github.vivitoto.vanga.ui.home.edit.SeriesSort
import io.github.vivitoto.vanga.ui.home.edit.SeriesStatusConditionState
import io.github.vivitoto.vanga.ui.home.edit.SharingLabelConditionState
import io.github.vivitoto.vanga.ui.home.edit.TagConditionState
import io.github.vivitoto.vanga.ui.home.edit.TitleConditionState
import io.github.vivitoto.vanga.ui.home.edit.TitleSortConditionState
import io.github.vivitoto.vanga.ui.settings.SettingsCard
import snd.komga.client.series.KomgaSeriesStatus

@Composable
fun SeriesConditionContent(
    state: SeriesCustomFilterState,
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        val sort = state.sort.collectAsState().value
        PageSettingsContent(
            pageSize = state.pageSize.collectAsState().value,
            onPageSizeChange = state::onPagSizeChange,
            sort = remember(sort) { LabeledEntry(sort, sort.label()) },
            sortOptions = remember { SeriesSort.entries.map { LabeledEntry(it, it.label()) } },
            onSortChange = state::onSortChange,
            sortDirection = state.sortDirection.collectAsState().value,
            onSortDirectionChange = state::onSortDirectionChange
        )

        ConditionContent(
            condition = state.conditionState.collectAsState().value,
            onConditionAdd = state::addCondition,
            onConditionTypeChange = state::changeConditionType,
            onConditionRemove = state::removeCondition
        )
    }
}

@Composable
fun SeriesMatchConditionContent(
    state: SeriesMatchConditionState,
    onConditionRemove: () -> Unit
) {
    SettingsCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val type = state.matchType.collectAsState().value
            DropdownChoiceMenu(
                selectedOption = LabeledEntry(type, type.label()),
                options = MatchType.entries.map { LabeledEntry(it, it.label()) },
                onOptionChange = { state.setMatchType(it.value) },
                inputFieldModifier = Modifier.widthIn(min = conditionInputMinWidth),
                label = { Text("匹配方式") }
            )
            IconButton(onClick = onConditionRemove) {
                Icon(Icons.Default.Delete, null)
            }
        }
        val conditions = state.conditions.collectAsState().value
        MatchConditionChildContent(conditions, state::onConditionTypeChange, state::removeCondition)

        ConditionAddButton(
            conditions = remember { SeriesConditionType.entries.map { LabeledEntry(it, it.label()) } },
            onConditionAdd = state::addCondition,
        )
    }
}

@Composable
private fun MatchConditionChildContent(
    conditions: List<SeriesConditionState>,
    onChildTypeChange: (SeriesConditionState, SeriesConditionType) -> Unit,
    onChildRemove: (SeriesConditionState) -> Unit
) {
    if (conditions.isEmpty()) return
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (condition in conditions) {
            val removeFunction = { onChildRemove(condition) }
            val changeFunction =
                { type: SeriesConditionType -> onChildTypeChange(condition, type) }
            ConditionContent(
                condition = condition,
                onConditionAdd = {},
                onConditionTypeChange = changeFunction,
                onConditionRemove = removeFunction
            )
        }
    }
}

@Composable
private fun ConditionContent(
    condition: SeriesConditionState?,
    onConditionAdd: (SeriesConditionType) -> Unit,
    onConditionTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit,
) {
    when (condition) {
        is SeriesMatchConditionState -> SeriesMatchConditionContent(condition, onConditionRemove)
        null -> ConditionAddButton(
            conditions = remember { SeriesConditionType.entries.map { LabeledEntry(it, it.label()) } },
            onConditionAdd = onConditionAdd,
        )

        is AgeRatingConditionState -> SeriesAgeRatingConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is AuthorConditionState -> SeriesAuthorConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is CollectionIdConditionState -> CollectionIdConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is CompleteConditionState -> SeriesCompleteConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is DeletedConditionState -> SeriesDeletedConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is GenreConditionState -> SeriesGenreConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is LanguageConditionState -> SeriesLanguageConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is LibraryConditionState -> SeriesLibraryConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is OneShotConditionState -> SeriesOneShotConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is PublisherConditionState -> SeriesPublisherConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is ReadStatusConditionState -> SeriesReadStatusConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is ReleaseDateConditionState -> SeriesReleaseDateConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is SeriesStatusConditionState -> SeriesStatusConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is SharingLabelConditionState -> SeriesSharingLabelConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is TagConditionState -> SeriesTagConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is TitleConditionState -> SeriesTitleConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove
        )

        is TitleSortConditionState -> SeriesTitleSortConditionContent(
            state = condition,
            onConditionTypeChange = onConditionTypeChange,
            onConditionRemove = onConditionRemove

        )
    }
}

@Composable
private fun SeriesConditionLayout(
    type: SeriesConditionType,
    onTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit,
    content: @Composable FlowRowScope.() -> Unit
) {
    SimpleConditionLayout(
        conditionType = remember { LabeledEntry(type, type.label()) },
        options = remember { SeriesConditionType.entries.map { LabeledEntry(it, it.label()) } },
        onConditionTypeChange = onTypeChange,
        onConditionRemove = onConditionRemove
    ) {
        content()
    }
}

@Composable
fun SeriesTagConditionContent(
    state: TagConditionState,
    onConditionTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    SeriesConditionLayout(
        type = SeriesConditionType.Tag,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) { TagConditionContent(state) }
}

@Composable
fun SeriesReadStatusConditionContent(
    state: ReadStatusConditionState,
    onConditionTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    SeriesConditionLayout(
        type = SeriesConditionType.ReadStatus,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) { ReadStatusConditionContent(state) }
}

@Composable
fun SeriesTitleConditionContent(
    state: TitleConditionState,
    onConditionTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    SeriesConditionLayout(
        type = SeriesConditionType.Title,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) { TitleConditionContent(state) }
}

@Composable
fun SeriesTitleSortConditionContent(
    state: TitleSortConditionState,
    onConditionTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    SeriesConditionLayout(
        type = SeriesConditionType.TitleSort,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) {
        StringOpContent(
            operator = state.operator.collectAsState().value,
            onOperatorChange = state::setOp,
            value = state.value.collectAsState().value,
            onValueChange = state::setValue
        )
    }
}

@Composable
fun SeriesLibraryConditionContent(
    state: LibraryConditionState,
    onConditionTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    SeriesConditionLayout(
        type = SeriesConditionType.Library,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) { LibraryConditionContent(state) }
}

@Composable
private fun SeriesAuthorConditionContent(
    state: AuthorConditionState,
    onConditionTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    SeriesConditionLayout(
        type = SeriesConditionType.Author,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) { AuthorConditionContent(state) }
}

@Composable
private fun SeriesReleaseDateConditionContent(
    state: ReleaseDateConditionState,
    onConditionTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    SeriesConditionLayout(
        type = SeriesConditionType.ReleaseDate,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) { ReleaseDateConditionContent(state) }
}

@Composable
private fun SeriesDeletedConditionContent(
    state: DeletedConditionState,
    onConditionTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    SeriesConditionLayout(
        type = SeriesConditionType.Deleted,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) { DeletedConditionContent(state) }
}

@Composable
private fun SeriesOneShotConditionContent(
    state: OneShotConditionState,
    onConditionTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    SeriesConditionLayout(
        type = SeriesConditionType.Oneshot,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) { OneShotConditionContent(state) }
}

@Composable
private fun SeriesSharingLabelConditionContent(
    state: SharingLabelConditionState,
    onConditionTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    SeriesConditionLayout(
        type = SeriesConditionType.SharingLabel,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) {
        EqualityNullableOpDropdownSearchContent(
            state = state,
            options = state.sharingLabels.collectAsState(emptyList()).value,
            label = "共享标签"
        )
    }
}

@Composable
private fun SeriesCompleteConditionContent(
    state: CompleteConditionState,
    onConditionTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    SeriesConditionLayout(
        type = SeriesConditionType.Complete,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) {
        BooleanOpContent(
            operator = state.operator.collectAsState().value,
            onOperatorChange = state::setOp
        )
    }
}

@Composable
private fun SeriesGenreConditionContent(
    state: GenreConditionState,
    onConditionTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    SeriesConditionLayout(
        type = SeriesConditionType.Genre,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) {
        EqualityNullableOpDropdownSearchContent(
            state = state,
            options = state.genres.collectAsState(emptyList()).value,
            label = "类型"
        )
    }
}

@Composable
private fun SeriesLanguageConditionContent(
    state: LanguageConditionState,
    onConditionTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    SeriesConditionLayout(
        type = SeriesConditionType.Language,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) {
        EqualityOpDropdownSearchContent(
            state = state,
            options = state.languages.collectAsState(emptyList()).value,
            label = "语言"
        )
    }
}

@Composable
private fun SeriesPublisherConditionContent(
    state: PublisherConditionState,
    onConditionTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    SeriesConditionLayout(
        type = SeriesConditionType.Publisher,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) {
        EqualityOpDropdownSearchContent(
            state = state,
            options = state.publishers.collectAsState(emptyList()).value,
            label = "出版社"
        )
    }
}

@Composable
fun SeriesStatusConditionContent(
    state: SeriesStatusConditionState,
    onConditionTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    SeriesConditionLayout(
        type = SeriesConditionType.Status,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) {
        val value = state.value.collectAsState().value
        EqualityOpDropDownContent(
            operator = state.operator.collectAsState().value,
            onOpChange = state::setOp,
            selectedValue = remember(value) { value?.let { LabeledEntry(it, it.label()) } },
            valueOptions = remember { KomgaSeriesStatus.entries.map { LabeledEntry(it, it.label()) } },
            onValueChange = state::setValue
        )
    }
}


@Composable
fun SeriesAgeRatingConditionContent(
    state: AgeRatingConditionState,
    onConditionTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    SeriesConditionLayout(
        type = SeriesConditionType.AgeRating,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) {
        val operator = state.operator.collectAsState().value
        DropdownChoiceMenu(
            selectedOption = LabeledEntry(operator, operator.label()),
            options = NumericNullableOpState.Op.entries.map { LabeledEntry(it, it.label()) },
            onOptionChange = { state.setOp(it.value) },
            inputFieldModifier = Modifier.widthIn(min = conditionInputMinWidth),
            label = { Text("操作") }
        )
        if (operator != NumericNullableOpState.Op.IsNull && operator != NumericNullableOpState.Op.IsNotNull)
            IntTextField(
                value = state.value.collectAsState().value,
                onValueChange = state::setValue,
                label = "年龄",
            )
    }
}

@Composable
fun CollectionIdConditionContent(
    state: CollectionIdConditionState,
    onConditionTypeChange: (SeriesConditionType) -> Unit,
    onConditionRemove: () -> Unit
) {
    SeriesConditionLayout(
        type = SeriesConditionType.Collection,
        onTypeChange = onConditionTypeChange,
        onConditionRemove = onConditionRemove
    ) {
        val options = state.collectionsSuggestions.collectAsState(emptyList()).value
        val operator = state.operator.collectAsState().value
        DropdownChoiceMenu(
            selectedOption = LabeledEntry(operator, operator.label()),
            options = EqualityOpState.Op.entries.map { LabeledEntry(it, it.label()) },
            onOptionChange = { state.setOp(it.value) },
            inputFieldModifier = Modifier.widthIn(min = conditionInputMinWidth),
            label = { Text("操作") }
        )
        SearchableOptionSelectionField(
            searchText = state.searchText.collectAsState().value,
            onSearchTextChange = state::onSearchTextChange,
            options = remember(options) { options.map { LabeledEntry(it, it.name) } },
            onValueChange = state::onCollectionSelect,
            label = "合集"
        )
    }
}
