package io.github.vivitoto.vanga.ui.common

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Process-level cache of lazy-list scroll positions, keyed by caller-supplied
 * string. Survives screen replacements (Voyager replace/replaceAll) within the
 * same process; does not survive app restart.
 */
private val scrollStateCache = mutableMapOf<String, ScrollPosition>()

private data class ScrollPosition(val index: Int, val offset: Int)

/**
 * Remember a [LazyGridState] whose first-visible position is saved under
 * [key]. When a new composable with the same key mounts later (e.g. after a
 * Voyager `replaceAll` that recreates the screen), the previous scroll
 * position is restored.
 */
@Composable
fun rememberRestorableLazyGridState(key: String): LazyGridState {
    val saved = scrollStateCache[key]
    val state = rememberLazyGridState(
        initialFirstVisibleItemIndex = saved?.index ?: 0,
        initialFirstVisibleItemScrollOffset = saved?.offset ?: 0,
    )

    LaunchedEffect(state) {
        snapshotFlow { ScrollPosition(state.firstVisibleItemIndex, state.firstVisibleItemScrollOffset) }
            .distinctUntilChanged()
            .collect { scrollStateCache[key] = it }
    }

    return state
}

/**
 * Remember a [LazyListState] whose first-visible position is saved under
 * [key]. See [rememberRestorableLazyGridState].
 */
@Composable
fun rememberRestorableLazyListState(key: String): LazyListState {
    val saved = scrollStateCache[key]
    val state = rememberLazyListState(
        initialFirstVisibleItemIndex = saved?.index ?: 0,
        initialFirstVisibleItemScrollOffset = saved?.offset ?: 0,
    )

    LaunchedEffect(state) {
        snapshotFlow { ScrollPosition(state.firstVisibleItemIndex, state.firstVisibleItemScrollOffset) }
            .distinctUntilChanged()
            .collect { scrollStateCache[key] = it }
    }

    return state
}