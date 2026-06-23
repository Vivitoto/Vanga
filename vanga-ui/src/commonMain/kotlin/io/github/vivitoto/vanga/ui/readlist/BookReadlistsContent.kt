package io.github.vivitoto.vanga.ui.readlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.vivitoto.vanga.komga.api.model.VangaBook
import io.github.vivitoto.vanga.ui.VangaShape
import io.github.vivitoto.vanga.ui.common.cards.BookImageCard
import io.github.vivitoto.vanga.ui.common.itemlist.ItemCardsSlider
import io.github.vivitoto.vanga.ui.common.layout.ResponsiveCardWidth
import io.github.vivitoto.vanga.ui.platform.cursorForHand
import snd.komga.client.readlist.KomgaReadList

@Composable
fun BookReadListsContent(
    readLists: Map<KomgaReadList, List<VangaBook>>,
    onReadListClick: (KomgaReadList) -> Unit,
    onBookClick: (VangaBook, KomgaReadList) -> Unit,
    cardWidth: Dp
) {
    if (readLists.isEmpty()) return

    var show by rememberSaveable { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(VangaShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { show = !show }
                .cursorForHand()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("阅读清单")
            if (show) Icon(Icons.Default.ExpandLess, null)
            else Icon(Icons.Default.ExpandMore, null)
        }

        AnimatedVisibility(show) {
            ResponsiveCardWidth(cardWidth = cardWidth) { itemWidth ->
                Column(
                    modifier = Modifier.padding(bottom = 30.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    readLists.forEach { (readList, books) ->
                        ItemCardsSlider(
                            onClick = { onReadListClick(readList) },
                            label = { ReadListLabel(readList) },
                        ) {
                            items(books) { book ->
                                BookImageCard(
                                    book = book,
                                    onBookClick = { onBookClick(book, readList) },
                                    modifier = Modifier.width(itemWidth)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun ReadListLabel(readList: KomgaReadList) {
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                append("阅读清单")
            }
            append(readList.name)
        },
        style = MaterialTheme.typography.titleMedium,
        textDecoration = TextDecoration.Underline
    )
}
