package io.github.vivitoto.vanga.ui.settings.offline.downloads

import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DownloadErrorTextTest {

    @Test
    fun cancellationIsShownAsCanceled() {
        assertEquals("已取消", downloadErrorText(CancellationException("user canceled")))
    }

    @Test
    fun permissionErrorsSuggestCheckingStoragePermission() {
        val text = downloadErrorText(IllegalStateException("Permission denied"))

        assertTrue(text.contains("存储权限"), text)
    }

    @Test
    fun fileCreationErrorsSuggestCheckingDownloadLocation() {
        val text = downloadErrorText(IllegalStateException("Can't create file in directory"))

        assertTrue(text.contains("下载位置"), text)
    }

    @Test
    fun connectionErrorsSuggestCheckingNetworkOrKomga() {
        val text = downloadErrorText(IllegalStateException("Connection timed out"))

        assertTrue(text.contains("网络"), text)
        assertTrue(text.contains("Komga"), text)
    }

    @Test
    fun genericErrorsKeepUsefulMessage() {
        val text = downloadErrorText(IllegalStateException("server returned 500"))

        assertTrue(text.contains("server returned 500"), text)
    }
}
