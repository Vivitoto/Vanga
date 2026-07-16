package io.github.vivitoto.vanga.ui.dialogs.komf.identify

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class KomfBookIdentifySupportTest {

    @Test
    fun supportedHasNoUnsupportedReason() {
        assertNull(KomfBookIdentifySupport.Supported.unsupportedReasonOrNull())
    }

    @Test
    fun defaultUnsupportedReasonExplainsKomfVersionOrApiLimitation() {
        val support = KomfBookIdentifySupport.Unsupported()
        val reason = support.unsupportedReasonOrNull().orEmpty()

        assertTrue(reason.contains("当前 Komf 版本或 API 不支持单本书籍元数据识别"))
        assertTrue(reason.contains("系列级识别"))
    }

    @Test
    fun customUnsupportedReasonIsPreserved() {
        val reason = "Komf endpoint returned 404"

        assertEquals(reason, KomfBookIdentifySupport.Unsupported(reason).unsupportedReasonOrNull())
    }
}
