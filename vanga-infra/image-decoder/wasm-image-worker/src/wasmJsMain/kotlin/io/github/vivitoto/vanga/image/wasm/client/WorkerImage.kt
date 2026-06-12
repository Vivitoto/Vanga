package io.github.vivitoto.vanga.image.wasm.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import io.github.vivitoto.vanga.image.ImageFormat
import io.github.vivitoto.vanga.image.ImageRect
import io.github.vivitoto.vanga.image.VangaImage
import io.github.vivitoto.vanga.image.ReduceKernel
import io.github.vivitoto.vanga.image.wasm.asByteArray
import io.github.vivitoto.vanga.image.wasm.jsArray
import io.github.vivitoto.vanga.image.wasm.messages.CloseImageResponse
import io.github.vivitoto.vanga.image.wasm.messages.FindTrimResponse
import io.github.vivitoto.vanga.image.wasm.messages.GetBytesResponse
import io.github.vivitoto.vanga.image.wasm.messages.ImageResponse
import io.github.vivitoto.vanga.image.wasm.messages.closeImageRequest
import io.github.vivitoto.vanga.image.wasm.messages.extractAreaRequest
import io.github.vivitoto.vanga.image.wasm.messages.findTrimRequest
import io.github.vivitoto.vanga.image.wasm.messages.getBytesRequest
import io.github.vivitoto.vanga.image.wasm.messages.makeHistogramRequest
import io.github.vivitoto.vanga.image.wasm.messages.mapLookupTableRequest
import io.github.vivitoto.vanga.image.wasm.messages.resizeRequest
import io.github.vivitoto.vanga.image.wasm.messages.shrinkRequest
import io.github.vivitoto.vanga.image.wasm.toJsArray

class WorkerImage(
    private val worker: ImageWorker,
    private val imageId: Int,
    override val width: Int,
    override val height: Int,
    override val bands: Int,
    override val type: ImageFormat
) : VangaImage {
    override val pagesLoaded: Int = 1
    override val pagesTotal: Int = 1
    override val pageHeight: Int = height
    override val pageDelays: IntArray? = null

    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    constructor(worker: ImageWorker, response: ImageResponse) : this(
        worker = worker,
        imageId = response.imageId,
        width = response.width,
        height = response.height,
        bands = response.bands,
        type = ImageFormat.valueOf(response.format)
    )

    override suspend fun extractArea(rect: ImageRect): VangaImage {
        val message = extractAreaRequest(worker.getNextId(), rect, imageId)
        val result = worker.postMessage<ImageResponse>(message)
        return WorkerImage(worker, result)
    }

    override suspend fun resize(
        scaleWidth: Int,
        scaleHeight: Int,
        linear: Boolean,
        kernel: ReduceKernel,
    ): VangaImage {
        val message = resizeRequest(worker.getNextId(), scaleWidth, scaleHeight, false, imageId)
        val result = worker.postMessage<ImageResponse>(message)
        return WorkerImage(worker, result)
    }

    override suspend fun shrink(factor: Double): VangaImage {
        val message = shrinkRequest(worker.getNextId(), factor, imageId)
        val result = worker.postMessage<ImageResponse>(message)
        return WorkerImage(worker, result)
    }

    override suspend fun findTrim(): ImageRect {
        val message = findTrimRequest(worker.getNextId(), imageId)
        val result = worker.postMessage<FindTrimResponse>(message)
        return ImageRect(
            left = result.left,
            top = result.top,
            right = result.right,
            bottom = result.bottom
        )
    }

    override suspend fun makeHistogram(): VangaImage {
        val message = makeHistogramRequest(worker.getNextId(), imageId)
        val result = worker.postMessage<ImageResponse>(message)
        return WorkerImage(worker, result)
    }

    override suspend fun mapLookupTable(table: ByteArray): VangaImage {
        val tableJsArray = table.toJsArray()
        val message = mapLookupTableRequest(
            requestId = worker.getNextId(),
            imageId = imageId,
            table = tableJsArray
        )
        val result = worker.postMessage<ImageResponse>(message, jsArray(tableJsArray.buffer))
        return WorkerImage(worker, result)
    }

    override suspend fun getBytes(): ByteArray {
        val message = getBytesRequest(worker.getNextId(), imageId)
        val result = worker.postMessage<GetBytesResponse>(message)
        return result.bytes.asByteArray()
    }

    override fun close() {
        coroutineScope.launch {
            val message = closeImageRequest(worker.getNextId(), imageId)
            worker.postMessage<CloseImageResponse>(message)
        }
    }
}

