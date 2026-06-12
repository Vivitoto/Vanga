package io.github.vivitoto.vanga.webview.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import io.github.vivitoto.vanga.webview.VangaWebview

@Composable
actual fun Webview(onCreated: (VangaWebview) -> Unit) {
    val webview = remember { VangaWebview() }
    LaunchedEffect(webview) {
        onCreated(webview)
    }
}