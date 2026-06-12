package io.github.vivitoto.vanga.ui.reader

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.vivitoto.vanga.ui.favorites.FavoriteBookButton
import io.github.vivitoto.vanga.ui.platform.TitleBarScope
import snd.komga.client.book.KomgaBookId

@Composable
fun TitleBarScope.TitleBarContent(
    title: String,
    onExit: () -> Unit,
    favoriteBookId: KomgaBookId? = null,
) {
    IconButton(
        onClick = onExit,
        modifier = Modifier
            .align(Alignment.Start)
            .size(48.dp)
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "退出阅读",
        )
    }
    Text(
        text = title,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.heightIn(max = 48.dp)
            .align(Alignment.Start)
            .nonInteractive()
            .padding(start = 58.dp, end = if (favoriteBookId != null) 56.dp else 0.dp)
            .fillMaxWidth()
    )

    if (favoriteBookId != null) {
        FavoriteBookButton(
            bookId = favoriteBookId,
            modifier = Modifier.align(Alignment.End)
                .size(48.dp)
        )
    }

}
