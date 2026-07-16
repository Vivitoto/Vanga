package io.github.vivitoto.vanga.ui.dialogs.komf.identify

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KomfIdentifyModeTest {

    @Test
    fun seriesTargetCanUseSeriesIdentifyEndpoint() {
        assertTrue(KomfIdentifyTarget.Series.canUseSeriesIdentifyEndpoint())
    }

    @Test
    fun bookTargetNeverUsesSeriesIdentifyEndpoint() {
        assertFalse(KomfIdentifyTarget.Book.canUseSeriesIdentifyEndpoint())
    }
}
