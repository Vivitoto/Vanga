package io.github.vivitoto.vanga.ui.reader.image.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReaderImageErrorTextTest {

    @Test
    fun missingLocalFileSuggestsRedownload() {
        val text = readerImageErrorText(IllegalStateException("No such file or directory"))

        assertTrue(text.contains("本地文件缺失"))
        assertTrue(text.contains("重新下载"))
    }

    @Test
    fun offlineUnsupportedExplainsKomgaConnection() {
        val text = readerImageErrorText(IllegalStateException("offline source unsupported"))

        assertEquals("离线模式暂不支持此图片来源，请连接 Komga 后再试。", text)
    }

    @Test
    fun genericErrorUsesSanitizedMessage() {
        val text = readerImageErrorText(IllegalStateException("decoder exploded\nstack trace details"))

        assertEquals("图片加载失败：decoder exploded", text)
    }
}
