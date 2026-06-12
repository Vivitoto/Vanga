package io.github.vivitoto.vanga.ui.favorites

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import io.github.vivitoto.vanga.ui.LocalOfflineMode
import io.github.vivitoto.vanga.ui.LocalViewModelFactory
import snd.komga.client.book.KomgaBookId
import snd.komga.client.series.KomgaSeriesId

@Composable
fun FavoriteSeriesButton(
    seriesId: KomgaSeriesId,
    modifier: Modifier = Modifier,
    showContainer: Boolean = false,
) {
    FavoriteButton(
        key = "series-${seriesId.value}",
        modifier = modifier,
        showContainer = showContainer,
        contentDescription = "收藏整部作品",
        loadFavorite = { it.isSeriesFavorite(seriesId) },
        toggleFavorite = { it.toggleSeriesFavorite(seriesId) },
    )
}

@Composable
fun FavoriteBookButton(
    bookId: KomgaBookId,
    modifier: Modifier = Modifier,
    showContainer: Boolean = false,
) {
    FavoriteButton(
        key = "book-${bookId.value}",
        modifier = modifier,
        showContainer = showContainer,
        contentDescription = "收藏单本",
        loadFavorite = { it.isBookFavorite(bookId) },
        toggleFavorite = { it.toggleBookFavorite(bookId) },
    )
}

@Composable
private fun FavoriteButton(
    key: String,
    modifier: Modifier = Modifier,
    showContainer: Boolean = false,
    contentDescription: String,
    loadFavorite: suspend (FavoriteToggleViewModel) -> Boolean,
    toggleFavorite: suspend (FavoriteToggleViewModel) -> Boolean,
) {
    val viewModelFactory = LocalViewModelFactory.current
    val vm = remember(key) { viewModelFactory.getFavoriteToggleViewModel() }
    val coroutineScope = rememberCoroutineScope()
    val isOffline = LocalOfflineMode.current.collectAsState().value
    var isFavorite by remember(key) { mutableStateOf(false) }
    var isLoading by remember(key) { mutableStateOf(true) }

    LaunchedEffect(key, isOffline) {
        if (isOffline) {
            isLoading = false
            return@LaunchedEffect
        }
        isLoading = true
        isFavorite = loadFavorite(vm)
        isLoading = false
    }

    IconButton(
        modifier = modifier,
        colors = if (showContainer) {
            IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .86f),
                disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = .64f),
            )
        } else {
            IconButtonDefaults.iconButtonColors()
        },
        enabled = !isLoading && !isOffline,
        onClick = {
            coroutineScope.launch {
                isLoading = true
                isFavorite = toggleFavorite(vm)
                isLoading = false
            }
        }
    ) {
        if (isLoading) {
            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                contentDescription = when {
                    isFavorite -> "取消收藏"
                    isOffline -> "离线模式下无法同步收藏"
                    vm.canWriteFavorites -> contentDescription
                    else -> "收藏需要 Komga 管理员权限"
                },
                tint = when {
                    isFavorite -> MaterialTheme.colorScheme.primary
                    isOffline || !vm.canWriteFavorites -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .35f)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
