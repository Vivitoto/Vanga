package io.github.vivitoto.vanga.ui.topbar

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import io.github.vivitoto.vanga.komga.api.model.VangaBook
import io.github.vivitoto.vanga.ui.LocalKeyEvents
import io.github.vivitoto.vanga.ui.LocalWindowState
import io.github.vivitoto.vanga.ui.LocalWindowWidth
import io.github.vivitoto.vanga.ui.ReloadableScreen
import io.github.vivitoto.vanga.ui.dialogs.ConfirmationDialog
import io.github.vivitoto.vanga.ui.platform.PlatformTitleBar
import io.github.vivitoto.vanga.ui.platform.WindowSizeClass.FULL
import io.github.vivitoto.vanga.ui.search.SearchBar
import io.github.vivitoto.vanga.ui.search.SearchResults
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.series.KomgaSeries

@Composable
fun AppBar(
    onMenuButtonPress: suspend () -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    isLoading: Boolean,
    onSearchAllClick: (String) -> Unit,
    searchResults: SearchResults,
    libraryById: (KomgaLibraryId) -> KomgaLibrary?,
    onBookClick: (VangaBook) -> Unit,
    onSeriesClick: (KomgaSeries) -> Unit,
    onRefreshClick: () -> Unit,
    notificationsState: NotificationsState,
    isOffline: Boolean,
    onOfflineModeChange: () -> Unit
) {
    PlatformTitleBar {
        val coroutineScope = rememberCoroutineScope()

        IconButton(
            modifier = Modifier.align(Alignment.Start),
            onClick = { coroutineScope.launch { onMenuButtonPress() } },
        ) {
            Icon(Icons.Rounded.Menu, null)
        }

        val navigator = LocalNavigator.currentOrThrow
        IconButton(
            modifier = Modifier.align(Alignment.Start),
            onClick = { navigator.pop() },
            enabled = navigator.canPop
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
        }
        val reloadableScreen = remember(navigator.lastItem) { navigator.lastItem as? ReloadableScreen }
        RefreshIndicator(
            onClick = onRefreshClick,
            enabled = reloadableScreen != null,
            modifier = Modifier.align(Alignment.Start),
        )
        DownloadsPopupIcon(notificationsState, Modifier.align(Alignment.Start))

        val searchBarModifier = when (LocalWindowWidth.current) {
            FULL -> Modifier.align(Alignment.CenterHorizontally).width(600.dp)
            else -> Modifier.align(Alignment.Start).width(240.dp)
        }

        SearchBar(
            modifier = searchBarModifier,
            searchResults = searchResults,
            query = query,
            onQueryChange = onQueryChange,
            isLoading = isLoading,
            onSearchAllClick = onSearchAllClick,
            libraryById = libraryById,
            onBookClick = onBookClick,
            onSeriesClick = onSeriesClick
        )


        val windowState = LocalWindowState.current
        val isFullscreen = windowState.isFullscreen.collectAsState(false)
        if (isFullscreen.value) {
            IconButton(
                modifier = Modifier.align(Alignment.End),
                onClick = { coroutineScope.launch { windowState.setFullscreen(false) } },
            ) {
                Icon(Icons.Default.FullscreenExit, null)
            }
        }

        if (isOffline) {
            var showConfirmationDialog by remember { mutableStateOf(false) }
            ElevatedButton(
                onClick = { showConfirmationDialog = true },
                modifier = Modifier.align(Alignment.End).padding(end = 10.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.errorContainer)
            ) {
                Text("离线模式")
            }
            if (showConfirmationDialog) {
                ConfirmationDialog(
                    body = "切换回在线模式？",
                    onDialogConfirm = onOfflineModeChange,
                    onDialogDismiss = { showConfirmationDialog = false }
                )
            }
        }
    }
}


@Composable
private fun RefreshIndicator(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier
) {
    val keyEvents = LocalKeyEvents.current
    var isClicked by remember { mutableStateOf(false) }
    LaunchedEffect(isClicked) {
        if (isClicked) {
            delay(500)
            isClicked = false
        }
    }

    LaunchedEffect(Unit) {
        keyEvents.collect {
            if (it.key == Key.F5 && it.type == KeyEventType.KeyUp) {
                onClick()
                isClicked = true
            }
        }
    }

    IconButton(
        onClick = {
            onClick()
            isClicked = true
        },
        enabled = enabled,
        modifier = modifier
    ) {
        Crossfade(
            targetState = isClicked,
            animationSpec = tween(durationMillis = 200)
        ) { refreshing ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                if (refreshing) {
                    CircularProgressIndicator(
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(17.dp)
                    )
                } else {
                    Icon(Icons.Default.Refresh, null)
                }
            }
        }
    }
}
