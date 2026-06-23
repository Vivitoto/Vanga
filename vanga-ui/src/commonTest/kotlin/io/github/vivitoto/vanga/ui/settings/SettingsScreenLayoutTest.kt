package io.github.vivitoto.vanga.ui.settings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsScreenLayoutTest {

    @Test
    fun twoColumnLayoutKeepsContentAtLeastMinimumAtBreakpoint() {
        for (availableWidth in 540..553) {
            val metrics = calculateSettingsScreenLayoutMetrics(
                availableWidth = availableWidth,
                preferredNavWidth = 280,
                preferredContentWidth = 700,
                minNavWidth = 180,
                minContentWidth = 360,
            )

            assertFalse(metrics.useSingleColumn, "availableWidth=$availableWidth")
            assertTrue(
                actual = metrics.contentWidth >= 360,
                message = "availableWidth=$availableWidth contentWidth=${metrics.contentWidth}",
            )
        }
    }

    @Test
    fun singleColumnLayoutIsUsedWhenMinimumWidthsDoNotFit() {
        val metrics = calculateSettingsScreenLayoutMetrics(
            availableWidth = 539,
            preferredNavWidth = 280,
            preferredContentWidth = 700,
            minNavWidth = 180,
            minContentWidth = 360,
        )

        assertTrue(metrics.useSingleColumn)
        assertEquals(539, metrics.contentWidth)
    }
}
