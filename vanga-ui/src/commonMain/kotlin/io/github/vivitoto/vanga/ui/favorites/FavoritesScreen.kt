package io.github.vivitoto.vanga.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.vivitoto.vanga.ui.LoadState
import io.github.vivitoto.vanga.ui.LocalOfflineMode
import io.github.vivitoto.vanga.ui.LocalReloadEvents
import io.github.vivitoto.vanga.ui.LocalViewModelFactory
import io.github.vivitoto.vanga.ui.ReloadableScreen
import io.github.vivitoto.vanga.ui.book.bookScreen
import io.github.vivitoto.vanga.ui.common.components.LoadingMaxSizeIndicator
import io.github.vivitoto.vanga.ui.common.itemlist.BookLazyCardGrid
import io.github.vivitoto.vanga.ui.common.itemlist.SeriesLazyCardGrid
import io.github.vivitoto.vanga.ui.reader.readerScreen
import io.github.vivitoto.vanga.ui.series.seriesScreen

class FavoritesScreen : ReloadableScreen {
    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getFavoritesViewModel() }
        val reloadEvents = LocalReloadEvents.current
        val navigator = LocalNavigator.currentOrThrow
        val isOffline = LocalOfflineMode.current.collectAsState().value

        LaunchedEffect(isOffline) {
            if (isOffline) return@LaunchedEffect
            vm.initialize()
            reloadEvents.collect { vm.reload() }
        }

        FavoritesContent(
            state = vm.state.collectAsState().value,
            canWriteFavorites = vm.canWriteFavorites,
            isOffline = isOffline,
            series = vm.favoriteSeries,
            books = vm.favoriteBooks,
            cardWidth = vm.cardWidth.collectAsState().value,
            onRetry = vm::reload,
            onSeriesClick = { navigator.push(seriesScreen(it)) },
            onBookClick = { book -> navigator.push(bookScreen(book)) },
            onBookReadClick = { book, markProgress -> navigator.push(readerScreen(book, markProgress)) },
        )
    }
}

@Composable
private fun FavoritesContent(
    state: LoadState<Unit>,
    canWriteFavorites: Boolean,
    isOffline: Boolean,
    series: List<snd.komga.client.series.KomgaSeries>,
    books: List<io.github.vivitoto.vanga.komga.api.model.VangaBook>,
    cardWidth: androidx.compose.ui.unit.Dp,
    onRetry: () -> Unit,
    onSeriesClick: (snd.komga.client.series.KomgaSeries) -> Unit,
    onBookClick: (io.github.vivitoto.vanga.komga.api.model.VangaBook) -> Unit,
    onBookReadClick: (io.github.vivitoto.vanga.komga.api.model.VangaBook, Boolean) -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("我的收藏", style = MaterialTheme.typography.headlineMedium)
            Text(
                "收藏保存在本机；启用 WebDAV 后会按服务器和账号同步。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!canWriteFavorites) {
                Text(
                    "用户信息加载中，暂时不能修改收藏。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("整部作品") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("单本") }
            )
        }

        when {
            isOffline -> OfflineState()
            state == LoadState.Uninitialized || state == LoadState.Loading -> LoadingMaxSizeIndicator()
            state is LoadState.Error -> ErrorState(state.exception, onRetry)
            state is LoadState.Success -> when (selectedTab) {
                0 -> FavoriteSeriesGrid(series, cardWidth, onSeriesClick)
                else -> FavoriteBooksGrid(books, cardWidth, onBookClick, onBookReadClick)
            }
        }

    }
}

@Composable
private fun OfflineState() {
    StateMessage(
        icon = Icons.Default.Download,
        title = "离线模式暂不能查看或同步收藏。",
    )
}

@Composable
private fun FavoriteSeriesGrid(
    series: List<snd.komga.client.series.KomgaSeries>,
    cardWidth: androidx.compose.ui.unit.Dp,
    onSeriesClick: (snd.komga.client.series.KomgaSeries) -> Unit,
) {
    if (series.isEmpty()) {
        EmptyState("还没有收藏整部作品。\n打开书库，进入作品详情页，点击星标收藏。")
    } else {
        SeriesLazyCardGrid(
            series = series,
            onSeriesClick = onSeriesClick,
            seriesMenuActions = null,
            topStartContent = { FavoriteSeriesButton(it.id, modifier = Modifier.size(40.dp), showContainer = true) },
            totalPages = 1,
            currentPage = 1,
            onPageChange = {},
            minSize = cardWidth,
        )
    }
}

@Composable
private fun FavoriteBooksGrid(
    books: List<io.github.vivitoto.vanga.komga.api.model.VangaBook>,
    cardWidth: androidx.compose.ui.unit.Dp,
    onBookClick: (io.github.vivitoto.vanga.komga.api.model.VangaBook) -> Unit,
    onBookReadClick: (io.github.vivitoto.vanga.komga.api.model.VangaBook, Boolean) -> Unit,
) {
    if (books.isEmpty()) {
        EmptyState("还没有收藏单本。\n进入单本详情页或阅读器，点击星标收藏。")
    } else {
        BookLazyCardGrid(
            books = books,
            onBookClick = onBookClick,
            onBookReadClick = onBookReadClick,
            bookMenuActions = null,
            topStartContent = { FavoriteBookButton(it.id, modifier = Modifier.size(40.dp), showContainer = true) },
            totalPages = 1,
            currentPage = 1,
            onPageChange = {},
            minSize = cardWidth,
        )
    }
}

@Composable
private fun EmptyState(message: String) {
    StateMessage(
        icon = Icons.Outlined.StarBorder,
        title = message,
    )
}

@Composable
private fun ErrorState(exception: Throwable, onRetry: () -> Unit) {
    val body = buildString {
        append("请检查网络、Komga 地址和账号权限后重试。")
        exception.message?.takeIf { it.isNotBlank() }?.let {
            append("\n")
            append(it)
        }
    }

    StateMessage(
        icon = Icons.Default.Error,
        title = "收藏加载失败",
        body = body,
        action = { Button(onClick = onRetry) { Text("重试") } },
    )
}

@Composable
private fun StateMessage(
    icon: ImageVector,
    title: String,
    body: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().padding(24.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp),
            )
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            if (body != null) {
                Text(
                    body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            action?.invoke()
        }
    }
}
