package io.github.vivitoto.vanga.ui.book

import io.github.vivitoto.vanga.offline.sync.model.DownloadEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import snd.komga.client.book.KomgaBookId

class DownloadButtonStateTest {

    @Test
    fun buttonIsEnabledWhenThereIsNoActiveDownload() {
        assertEquals(DownloadButtonState.Ready, downloadButtonState(null))
        assertTrue(isDownloadButtonEnabled(null))
        assertEquals("下载", downloadButtonLabel(null))
    }

    @Test
    fun progressDisablesDuplicateDownloadRequests() {
        assertFalse(isDownloadButtonEnabled(DownloadButtonState.Downloading))
        assertEquals("下载中…", downloadButtonLabel(DownloadButtonState.Downloading))
    }

    @Test
    fun errorEnablesRetry() {
        val event = DownloadEvent.BookDownloadError(
            bookId = KomgaBookId("book-id"),
            error = IllegalStateException("boom"),
        )

        assertEquals(DownloadButtonState.Retry, downloadButtonState(event))
        assertTrue(isDownloadButtonEnabled(event))
        assertEquals("重试下载", downloadButtonLabel(event))
    }

    @Test
    fun completedEventReturnsToReadyState() {
        assertTrue(isDownloadButtonEnabled(DownloadButtonState.Ready))
        assertEquals("下载", downloadButtonLabel(DownloadButtonState.Ready))
    }
}
