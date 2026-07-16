package io.github.vivitoto.vanga.ui.dialogs.komf.identify

import snd.komf.api.KomfServerLibraryId
import snd.komf.api.KomfServerSeriesId
import snd.komf.api.job.KomfMetadataJobId
import snd.komga.client.book.KomgaBookId

internal data class KomfBookIdentifyRequest(
    val libraryId: KomfServerLibraryId,
    val seriesId: KomfServerSeriesId,
    val bookId: KomgaBookId,
    val bookTitle: String,
)

internal sealed interface KomfBookIdentifyResult {
    data class Started(val jobId: KomfMetadataJobId) : KomfBookIdentifyResult
    data class Unsupported(val reason: String = defaultUnsupportedReason) : KomfBookIdentifyResult
}

internal interface KomfBookIdentifyClient {
    suspend fun probeSupport(request: KomfBookIdentifyRequest): KomfBookIdentifySupport
    suspend fun identifyBook(request: KomfBookIdentifyRequest): KomfBookIdentifyResult
}

internal class UnsupportedKomfBookIdentifyClient(
    private val reason: String = noKomfBookIdentifyEndpointReason,
) : KomfBookIdentifyClient {
    override suspend fun probeSupport(request: KomfBookIdentifyRequest): KomfBookIdentifySupport =
        KomfBookIdentifySupport.Unsupported(reason)

    override suspend fun identifyBook(request: KomfBookIdentifyRequest): KomfBookIdentifyResult =
        KomfBookIdentifyResult.Unsupported(reason)
}

internal const val noKomfBookIdentifyEndpointReason: String =
    "当前 Komf metadata API 未提供单本书籍元数据识别端点；Vanga 不会把单本识别伪装成系列级识别。"
