package io.github.vivitoto.vanga.ui.dialogs.tabs

import kotlin.test.Test
import kotlin.test.assertEquals

class TabDialogTest {

    @Test
    fun dialogWidthUsesWindowFractionBelowMaximum() {
        assertEquals(
            expected = 768,
            actual = calculateTabDialogWidth(
                availableWidth = 800,
                maxWidth = 840,
                fraction = .96f,
            ),
        )
    }

    @Test
    fun dialogWidthCapsFractionalWindowWidthAtMaximum() {
        assertEquals(
            expected = 840,
            actual = calculateTabDialogWidth(
                availableWidth = 2000,
                maxWidth = 840,
                fraction = .96f,
            ),
        )
    }
}
