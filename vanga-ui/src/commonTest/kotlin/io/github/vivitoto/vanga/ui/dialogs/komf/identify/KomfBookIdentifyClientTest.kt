package io.github.vivitoto.vanga.ui.dialogs.komf.identify

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import snd.komf.api.KomfServerLibraryId
import snd.komf.api.KomfServerSeriesId
import snd.komga.client.book.KomgaBookId

class KomfBookIdentifyClientTest {

    private val request = KomfBookIdentifyRequest(
        libraryId = KomfServerLibraryId("library-id"),
        seriesId = KomfServerSeriesId("series-id"),
        bookId = KomgaBookId("book-id"),
        bookTitle = "Book Title",
    )

    @Test
    fun probeReportsUnsupportedWhenKomfHasNoBookEndpoint() = runTest {
        val client = UnsupportedKomfBookIdentifyClient()

        val support = client.probeSupport(request)

        val unsupported = assertIs<KomfBookIdentifySupport.Unsupported>(support)
        assertEquals(noKomfBookIdentifyEndpointReason, unsupported.reason)
        assertTrue(unsupported.reason.contains("不会把单本识别伪装成系列级识别"))
    }

    @Test
    fun identifyBookReturnsUnsupportedInsteadOfStartingSeriesIdentify() = runTest {
        val client = UnsupportedKomfBookIdentifyClient()

        val result = client.identifyBook(request)

        val unsupported = assertIs<KomfBookIdentifyResult.Unsupported>(result)
        assertEquals(noKomfBookIdentifyEndpointReason, unsupported.reason)
    }
}
