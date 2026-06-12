package io.github.vivitoto.vanga.ui.reader.epub

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import io.github.vivitoto.vanga.komga.api.KomgaBookApi
import snd.komga.client.book.KomgaBookId
import io.github.vivitoto.vanga.webview.ResourceLoadResult


private val bookResourceRegex = ".*/api/v1/books/(?<bookId>.*)/resource/(?<resourceName>.*)".toRegex()
private val bookManifestRegex = ".*/api/v1/books/(?<bookId>.*)/manifest".toRegex()
private val bookPositionsRegex = ".*/api/v1/books/(?<bookId>.*)/positions".toRegex()

suspend fun proxyResourceRequest(
    bookApi: KomgaBookApi,
    urlString: String,
    serverUrl: Flow<String>
): ResourceLoadResult {
    check(urlString.startsWith(serverUrl.first())) { "不允许请求外部主机：$urlString" }
    val resourceMatch = bookResourceRegex.find(urlString)?.groups
    if (resourceMatch != null) {
        val bookId = resourceMatch["bookId"]?.value ?: error("无法找到 bookId：$urlString")
        val resourceName = resourceMatch["resourceName"]?.value ?: error("无法找到资源名称：$urlString")
        return ResourceLoadResult(
            data = bookApi.getBookEpubResource(KomgaBookId(bookId), resourceName),
            contentType = null
        )
    }
    val manifestMatch = bookManifestRegex.find(urlString)?.groups
    if (manifestMatch != null) {
        val bookId = manifestMatch["bookId"]?.value ?: error("无法找到 bookId：$urlString")
        return ResourceLoadResult(
            data = Json.encodeToString(
                bookApi.getWebPubManifest(KomgaBookId(bookId))
            ).encodeToByteArray(),
            contentType = null
        )
    }
    val bookPositionsMatch = bookPositionsRegex.find(urlString)?.groups
    if (bookPositionsMatch != null) {
        val bookId = bookPositionsMatch["bookId"]?.value ?: error("无法找到 bookId：$urlString")

        return ResourceLoadResult(
            data = Json.encodeToString(
                bookApi.getReadiumPositions(KomgaBookId(bookId))
            ).encodeToByteArray(),
            contentType = null
        )
    }

    error("不支持的资源请求：$urlString")
}
