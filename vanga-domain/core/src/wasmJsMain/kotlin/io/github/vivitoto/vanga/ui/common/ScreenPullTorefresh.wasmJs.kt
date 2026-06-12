package io.github.vivitoto.vanga.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import io.github.vivitoto.vanga.ui.LoadState
import kotlinx.coroutines.flow.Flow

@Composable
actual fun ScreenPullToRefreshBox(
    screenState: Flow<LoadState<*>>,
    onRefresh: () -> Unit,
    minLoadDuration: Long,
    content: @Composable BoxScope.() -> Unit,
) = Box(content = content)
