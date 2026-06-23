package io.github.vivitoto.vanga.ui.dialogs

import kotlin.test.Test
import kotlin.test.assertEquals

class AppDialogTest {

    @Test
    fun compactDialogWidthUsesWindowFraction() {
        assertEquals(
            expected = 331,
            actual = calculateAppDialogWidth(
                availableWidth = 360,
                compactBreakpointWidth = 600,
                compactMinTotalMargin = 24,
            ),
        )
    }

    @Test
    fun compactDialogWidthKeepsMinimumTotalMargin() {
        assertEquals(
            expected = 256,
            actual = calculateAppDialogWidth(
                availableWidth = 280,
                compactBreakpointWidth = 600,
                compactMinTotalMargin = 24,
            ),
        )
    }

    @Test
    fun dialogWidthIsUnchangedAtCompactBreakpoint() {
        assertEquals(
            expected = 600,
            actual = calculateAppDialogWidth(
                availableWidth = 600,
                compactBreakpointWidth = 600,
                compactMinTotalMargin = 24,
            ),
        )
    }

    @Test
    fun dialogWidthIsUnchangedAboveCompactBreakpoint() {
        assertEquals(
            expected = 840,
            actual = calculateAppDialogWidth(
                availableWidth = 840,
                compactBreakpointWidth = 600,
                compactMinTotalMargin = 24,
            ),
        )
    }
}
