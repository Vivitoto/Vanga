package io.github.vivitoto.vanga.ui.common.itemlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import io.github.vivitoto.vanga.ui.common.cards.ReadListImageCard
import io.github.vivitoto.vanga.ui.common.components.Pagination
import io.github.vivitoto.vanga.ui.platform.VerticalScrollbar
import snd.komga.client.readlist.KomgaReadList
import snd.komga.client.readlist.KomgaReadListId

@Composable
fun ReadListLazyCardGrid(
    readLists: List<KomgaReadList>,
    onReadListClick: (KomgaReadListId) -> Unit,
    onReadListDelete: (KomgaReadListId) -> Unit,
    totalPages: Int,
    currentPage: Int,
    onPageChange: (Int) -> Unit,
    minSize: Dp = 200.dp,
    scrollState: LazyGridState = rememberLazyGridState(),
    contentPadding: PaddingValues = PaddingValues(bottom = CardGridBottomPadding),
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    BoxWithConstraints(modifier) {
        val gridMinSize = adaptiveCardGridMinSize(minSize, maxWidth)
        LazyVerticalGrid(
            columns = GridCells.Adaptive(gridMinSize),
            state = scrollState,
            horizontalArrangement = Arrangement.spacedBy(CardGridItemSpacing),
            verticalArrangement = Arrangement.spacedBy(CardGridItemSpacing),
            contentPadding = contentPadding,
            modifier = Modifier.padding(horizontal = CardGridHorizontalPadding)
        ) {
            items(readLists) {
                ReadListImageCard(
                    readLists = it,
                    onCollectionClick = { onReadListClick(it.id) },
                    onCollectionDelete = { onReadListDelete(it.id) },
                    modifier = Modifier.fillMaxSize().padding(5.dp),
                )
            }
            if (totalPages > 1) {
                item(
                    span = { GridItemSpan(maxLineSpan) },
                ) {
                    Pagination(
                        totalPages = totalPages,
                        currentPage = currentPage,
                        onPageChange = {
                            coroutineScope.launch {
                                onPageChange(it)
                                scrollState.scrollToItem(0)
                            }
                        }
                    )
                }
            }

        }

        VerticalScrollbar(scrollState, Modifier.align(Alignment.TopEnd))
    }
}
