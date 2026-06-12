package io.github.vivitoto.vanga.webview.compose

import androidx.compose.runtime.Composable
import io.github.vivitoto.vanga.webview.VangaWebview

@Composable
expect fun Webview(
    onCreated: (VangaWebview) -> Unit,
)
