package io.github.vivitoto.vanga.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType.Companion.PrimaryEditable
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import io.github.vivitoto.vanga.komga.api.model.VangaBook
import io.github.vivitoto.vanga.ui.VangaShape
import io.github.vivitoto.vanga.ui.common.cards.BookSimpleImageCard
import io.github.vivitoto.vanga.ui.common.cards.SeriesSimpleImageCard
import io.github.vivitoto.vanga.ui.common.components.NoPaddingTextField
import io.github.vivitoto.vanga.ui.platform.cursorForHand
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.series.KomgaSeries

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    searchResults: SearchResults,
    query: String,
    isLoading: Boolean,
    onQueryChange: (String) -> Unit,
    onSearchAllClick: (String) -> Unit,
    libraryById: (KomgaLibraryId) -> KomgaLibrary?,
    onBookClick: (VangaBook) -> Unit,
    onSeriesClick: (KomgaSeries) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    var isFocused by remember { mutableStateOf(false) }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is FocusInteraction.Unfocus -> {
                    isFocused = false
                }

                is FocusInteraction.Focus -> isFocused = true
            }
        }
    }

    val focusManager = LocalFocusManager.current
    val isExpanded = derivedStateOf { isFocused && query.isNotBlank() }
    BoxWithConstraints(modifier) {
        val maxHeight = maxHeight
        val maxWidth = maxWidth
        ExposedDropdownMenuBox(
            modifier = Modifier.fillMaxWidth(),
            expanded = isExpanded.value,
            onExpandedChange = {}
        ) {

            SearchTextField(
                query = query,
                onQueryChange = onQueryChange,
                onDone = onSearchAllClick,
                onDismiss = { onQueryChange("") },
                interactionSource = interactionSource,
                modifier = Modifier.menuAnchor(PrimaryEditable)
            )
            DropdownMenu(
                expanded = isExpanded.value,
                onDismissRequest = {},
                properties = PopupProperties(focusable = false),
                modifier = Modifier
                    .width(maxWidth)
                    .heightIn(max = maxHeight - 150.dp)
                    .padding(5.dp)
            ) {
                SearchResultsDropDownBox(
                    currentQuery = query,
                    searchResults = searchResults,
                    isLoading = isLoading,
                    libraryById = libraryById,
                    onSearchAllClick = onSearchAllClick,
                    onSeriesClick = onSeriesClick,
                    onBookClick = onBookClick,
                    onDismiss = { focusManager.clearFocus() }
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.SearchResultsDropDownBox(
    currentQuery: String,
    searchResults: SearchResults,
    isLoading: Boolean,
    libraryById: (KomgaLibraryId) -> KomgaLibrary?,
    onSearchAllClick: (String) -> Unit,
    onSeriesClick: (KomgaSeries) -> Unit,
    onBookClick: (VangaBook) -> Unit,
    onDismiss: () -> Unit,
) {
    if (currentQuery.isBlank()) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clickable {
                onDismiss()
                onSearchAllClick(currentQuery)
            }
            .background(MaterialTheme.colorScheme.primaryContainer, VangaShape)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            "搜索全部…",
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.labelLarge,
        )
    }
    if (isLoading) LinearProgressIndicator(
        color = MaterialTheme.colorScheme.tertiary,
        modifier = Modifier.fillMaxWidth()
    )


    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val series = searchResults.series
        val books = searchResults.books
        if (!isLoading && series.isEmpty() && books.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "没有找到匹配结果",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        if (series.isNotEmpty()) {
            Text(
                text = "漫画系列",
                modifier = Modifier.padding(start = 4.dp, top = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge,
            )
            series.forEach {
                SeriesSearchEntry(
                    series = it,
                    library = libraryById(it.libraryId),
                    onSeriesClick = {
                        onSeriesClick(it)
                        onDismiss()
                    }
                )
            }
        }
        if (books.isNotEmpty()) {
            Text(
                text = "单本漫画",
                modifier = Modifier.padding(start = 4.dp, top = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge,
            )
            books.forEach {
                BookSearchEntry(
                    book = it,
                    library = libraryById(it.libraryId),
                    onBookClick = {
                        onBookClick(it)
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun EntryContainer(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .cursorForHand(),
        shape = VangaShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .78f),
    ) {
        Row(
            modifier = Modifier
                .height(104.dp)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            content()
        }
    }
}

@Composable
private fun SeriesSearchEntry(
    series: KomgaSeries,
    library: KomgaLibrary?,
    onSeriesClick: () -> Unit,
) {
    EntryContainer(onSeriesClick) {
        SeriesSimpleImageCard(
            series = series,
            onSeriesClick = onSeriesClick,
            modifier = Modifier.width(72.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                series.metadata.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall,
            )
            library?.let {
                Text(
                    "来自 ${library.name}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun BookSearchEntry(
    book: VangaBook,
    library: KomgaLibrary?,
    onBookClick: () -> Unit,
) {
    EntryContainer(onBookClick) {
        BookSimpleImageCard(
            book = book,
            onBookClick = onBookClick,
            modifier = Modifier.width(72.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                book.metadata.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall,
            )
            library?.let {
                Text(
                    "来自 ${library.name}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}


@Composable
fun SearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    onDone: (String) -> Unit,
    onDismiss: () -> Unit = {},
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    NoPaddingTextField(
        text = query,
        placeholder = "搜索漫画、书籍…",
        onTextChange = onQueryChange,
        shape = CircleShape,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedTextColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        interactionSource = interactionSource,
        modifier = modifier
            .height(45.dp)
            .fillMaxWidth()
            .padding(top = 5.dp)
            .onKeyEvent { keyEvent ->
                when {
                    keyEvent.key == Key.Enter && keyEvent.type == KeyEventType.KeyUp -> {
                        focusManager.clearFocus()
                        onDone(query)
                        true
                    }

                    keyEvent.key == Key.Back || keyEvent.key == Key.Escape -> {
                        focusManager.clearFocus()
                        true
                    }

                    else -> false
                }
            },
        trailingIcon = {
            if (query.isNotBlank()) {
                Icon(
                    Icons.Filled.Close, null,
                    modifier = Modifier
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = {
                                focusManager.clearFocus()
                                onDismiss()
                            }
                        ).cursorForHand(),
                    tint = MaterialTheme.colorScheme.secondary
                )
            } else {
                Icon(
                    Icons.Filled.Search, null,
                    modifier = Modifier.cursorForHand()
                )
            }
        },

        keyboardActions = KeyboardActions(
            onDone = { onDone(query) },
        )
    )
}
