package io.github.vivitoto.vanga.webview.compose

import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.github.vivitoto.vanga.webview.VangaWebview

@Composable
actual fun Webview(
    onCreated: (VangaWebview) -> Unit,
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { WebView(it).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        } },
        update = { onCreated(VangaWebview(it)) })
}