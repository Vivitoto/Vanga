package io.github.vivitoto.vanga.image.wasm.client

import io.github.vivitoto.vanga.image.VangaImage
import io.github.vivitoto.vanga.image.VangaImageDecoder
import io.github.vivitoto.vanga.image.wasm.jsArray
import io.github.vivitoto.vanga.image.wasm.messages.ImageResponse
import io.github.vivitoto.vanga.image.wasm.messages.decodeAndResizeRequest
import io.github.vivitoto.vanga.image.wasm.messages.decodeRequest
import io.github.vivitoto.vanga.image.wasm.toJsArray

class WorkerImageDecoder : VangaImageDecoder {
    private val worker = ImageWorker()
    suspend fun init() = worker.init()

    override suspend fun decode(encoded: ByteArray, nPages: Int?): VangaImage {
        val jsArray = encoded.toJsArray()
        val message = decodeRequest(worker.getNextId(), jsArray)
        val result = worker.postMessage<ImageResponse>(message, jsArray(jsArray.buffer))
        return WorkerImage(worker, result)
    }

    override suspend fun decodeFromFile(path: String, nPages: Int?): VangaImage {
        error("File operations are not supported")
    }

    override suspend fun decodeAndResize(
        path: String,
        scaleWidth: Int,
        scaleHeight: Int,
        crop: Boolean,
        nPages: Int?
    ): VangaImage {
        error("File operations are not supported")
    }

    override suspend fun decodeAndResize(
        encoded: ByteArray,
        scaleWidth: Int,
        scaleHeight: Int,
        crop: Boolean,
        nPages: Int?
    ): VangaImage {
        val jsArray = encoded.toJsArray()
        val message = decodeAndResizeRequest(
            requestId = worker.getNextId(),
            width = scaleWidth,
            height = scaleHeight,
            crop = crop,
            buffer = jsArray
        )
        val result = worker.postMessage<ImageResponse>(message, jsArray(jsArray.buffer))
        return WorkerImage(worker, result)
    }
}
