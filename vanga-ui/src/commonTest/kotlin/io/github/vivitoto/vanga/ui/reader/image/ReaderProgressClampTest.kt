package io.github.vivitoto.vanga.ui.reader.image

import kotlin.test.Test
import kotlin.test.assertEquals

class ReaderProgressClampTest {

    @Test
    fun pageBelowOneClampsToFirstPage() {
        assertEquals(1, clampReaderPage(page = 0, pageCount = 10))
        assertEquals(1, clampReaderPage(page = -5, pageCount = 10))
    }

    @Test
    fun pageAbovePageCountClampsToLastPage() {
        assertEquals(10, clampReaderPage(page = 42, pageCount = 10))
    }

    @Test
    fun emptyPageCountFallsBackToFirstPage() {
        assertEquals(1, clampReaderPage(page = 3, pageCount = 0))
        assertEquals(1, clampReaderPage(page = 3, pageCount = -1))
    }

    @Test
    fun pageWithinBoundsIsUnchanged() {
        assertEquals(5, clampReaderPage(page = 5, pageCount = 10))
    }
}
