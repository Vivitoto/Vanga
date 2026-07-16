package io.github.vivitoto.vanga.offline

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class OfflineUnsupportedOperationTest {

    @Test
    fun exceptionMessageExplainsOfflineUnsupportedOperation() {
        val exception = OfflineUnsupportedOperationException("刷新元数据")

        val message = exception.message.orEmpty()
        assertTrue(message.contains("离线模式暂不支持"), message)
        assertTrue(message.contains("刷新元数据"), message)
        assertTrue(message.contains("Komga"), message)
    }

    @Test
    fun helperThrowsOfflineUnsupportedOperationException() {
        val exception = assertFailsWith<OfflineUnsupportedOperationException> {
            offlineUnsupported("刷新元数据")
        }

        assertTrue(exception.message.orEmpty().contains("刷新元数据"))
    }
}
