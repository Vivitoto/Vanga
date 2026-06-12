package io.github.vivitoto.vanga.ui.reader.epub

import androidx.compose.runtime.Composable
import io.github.vivitoto.vanga.ui.platform.BackPressHandler
import io.github.vivitoto.vanga.webview.VangaWebview
import io.github.vivitoto.vanga.webview.compose.Webview

@Composable
fun EpubContent(
    onWebviewCreated: (VangaWebview) -> Unit,
    onBackButtonPress: () -> Unit,
) {
    Webview(onWebviewCreated)
    BackPressHandler(onBackButtonPress)
}