package io.github.vivitoto.vanga.ui.reader.image.paged

import kotlin.test.Test
import kotlin.test.assertEquals

class PagedReaderSpreadTest {

    @Test
    fun emptySpreadStaysEmpty() {
        assertEquals(emptyList(), displayablePagedReaderSpreadPages(emptyList<Int>()))
    }

    @Test
    fun singlePageSpreadStaysSinglePage() {
        assertEquals(listOf(1), displayablePagedReaderSpreadPages(listOf(1)))
    }

    @Test
    fun doublePageSpreadStaysDoublePage() {
        assertEquals(listOf(1, 2), displayablePagedReaderSpreadPages(listOf(1, 2)))
    }

    @Test
    fun oversizedSpreadIsClampedToFirstTwoPages() {
        assertEquals(listOf(1, 2), displayablePagedReaderSpreadPages(listOf(1, 2, 3)))
    }
}
