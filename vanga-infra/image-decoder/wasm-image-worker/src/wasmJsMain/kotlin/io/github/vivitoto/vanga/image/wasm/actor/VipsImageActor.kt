package io.github.vivitoto.vanga.image.wasm.actor

import org.w3c.dom.DedicatedWorkerGlobalScope
import io.github.vivitoto.vanga.image.ImageFormat
import io.github.vivitoto.vanga.image.wasm.jsArray
import io.github.vivitoto.vanga.image.wasm.messages.CloseImageRequest
import io.github.vivitoto.vanga.image.wasm.messages.DecodeAndResizeRequest
import io.github.vivitoto.vanga.image.wasm.messages.DecodeRequest
import io.github.vivitoto.vanga.image.wasm.messages.ExtractAreaRequest
import io.github.vivitoto.vanga.image.wasm.messages.FindTrimRequest
import io.github.vivitoto.vanga.image.wasm.messages.GetBytesRequest
import io.github.vivitoto.vanga.image.wasm.messages.ImageResponse
import io.github.vivitoto.vanga.image.wasm.messages.MakeHistogramRequest
import io.github.vivitoto.vanga.image.wasm.messages.MapLookupTableRequest
import io.github.vivitoto.vanga.image.wasm.messages.ResizeRequest
import io.github.vivitoto.vanga.image.wasm.messages.ShrinkRequest
import io.github.vivitoto.vanga.image.wasm.messages.closeImageResponse
import io.github.vivitoto.vanga.image.wasm.messages.findTrimResponse
import io.github.vivitoto.vanga.image.wasm.messages.getBytesResponse
import io.github.vivitoto.vanga.image.wasm.messages.imageResponse
import io.github.vivitoto.vanga.image.wasm.toBlob

internal class VipsImageActor(workerScope: DedicatedWorkerGlobalScope) : AbstractImageActor<VipsImage>(workerScope) {
    override suspend fun beforeInit() {
        Vips.init()
    }

    override suspend fun decodeAndResize(message: DecodeAndResizeRequest) {
        val decodedImage = if (message.width == null && message.height == null) {
            Vips.newImageFromBuffer(message.buffer)
        } else {
            val dstWidth = (message.width ?: vipsMaxSize).coerceAtMost(vipsMaxSize)
            val dstHeight = (message.height ?: vipsMaxSize).coerceAtMost(vipsMaxSize)
            Vips.thumbnail(message.buffer, dstWidth, dstHeight, message.crop)
        }
        val converted = convertToSupportedFormat(decodedImage)
        decodedImage.delete()
        val imageId = saveImage(converted)
        workerScope.postMessage(converted.toImageResponse(message.requestId, imageId))
    }

    override suspend fun decode(message: DecodeRequest) {
        val decodedImage = Vips.newImageFromBuffer(message.buffer)
        val converted = convertToSupportedFormat(decodedImage)
        decodedImage.delete()
        val imageId = saveImage(converted)
        workerScope.postMessage(converted.toImageResponse(message.requestId, imageId))
    }

    override suspend fun resize(message: ResizeRequest) {
        val image = requireNotNull(images[message.imageId])
        val resized = image.thumbnailImage(
            width = message.scaleWidth.coerceAtMost(vipsMaxSize),
            options = thumbnailOptions(height = message.scaleHeight, crop = message.crop)
        )
        val imageId = saveImage(resized)
        workerScope.postMessage(resized.toImageResponse(message.requestId, imageId))
    }

    override suspend fun getBytes(message: GetBytesRequest) {
        val image = requireNotNull(images[message.imageId])
        val bytes = image.writeToMemory()

        workerScope.postMessage(
            getBytesResponse(
                requestId = message.requestId,
                bytes = bytes
            ),
            jsArray(bytes.buffer)
        )
    }

    override suspend fun extractArea(message: ExtractAreaRequest) {
        val image = requireNotNull(images[message.imageId])
        val area = image.extractArea(
            left = message.left,
            top = message.top,
            width = message.right - message.left,
            height = message.bottom - message.top
        )

        val imageId = saveImage(area)
        workerScope.postMessage(area.toImageResponse(message.requestId, imageId))
    }

    override suspend fun shrink(message: ShrinkRequest) {
        val image = requireNotNull(images[message.imageId])
        val resized = image.shrink(
            hshrink = message.factor,
            vshrink = message.factor,
            options = null,
        )
        val imageId = saveImage(resized)
        workerScope.postMessage(resized.toImageResponse(message.requestId, imageId))
    }

    override suspend fun findTrim(message: FindTrimRequest) {
        val image = requireNotNull(images[message.imageId])
        val trim = image.findTrim(options = findTrimOptions(50.0, false))
        workerScope.postMessage(
            findTrimResponse(
                requestId = message.requestId,
                left = trim.left,
                top = trim.top,
                right = trim.width + trim.left,
                bottom = trim.height + trim.top
            )
        )
    }

    override suspend fun makeHistogram(message: MakeHistogramRequest) {
        val image = requireNotNull(images[message.imageId])
        val histogram = image.histFind(histFindOptions(bands = -1))
        val normalized = histogram.histNorm()
        histogram.delete()
        val imageId = saveImage(normalized)
        workerScope.postMessage(normalized.toImageResponse(message.requestId, imageId))
    }

    override suspend fun mapLookupTable(message: MapLookupTableRequest) {
        val image = requireNotNull(images[message.imageId])
        val lutImage = Vips.newImageFromMemory(
            memory = toBlob(message.table),
            width = 256,
            height = 1,
            bands = image.bands,
            format = BandFormat.UCHAR
        )
        val mapped = image.mapLut(lutImage, null)
        lutImage.delete()

        val imageId = saveImage(mapped)
        workerScope.postMessage(mapped.toImageResponse(message.requestId, imageId))
    }

    private fun convertToSupportedFormat(image: VipsImage): VipsImage {
        var result: VipsImage = image.clone()
        if (result.interpretation == Interpretation.B_W && result.bands != 1 ||
            result.interpretation != Interpretation.sRGB && result.interpretation != Interpretation.B_W
        ) {
            val old = result
            result = old.colourspace(Interpretation.sRGB)
            old.delete()
        }
        if (result.interpretation == Interpretation.sRGB && result.bands == 3) {
            val old = result
            result = old.bandjoin(255, null)
            old.delete()
        }

        return result
    }

    override suspend fun closeImage(message: CloseImageRequest) {
        val image = requireNotNull(images.remove(message.imageId))
        image.delete()
        workerScope.postMessage(closeImageResponse(requestId = message.requestId))
    }

    private fun VipsImage.toImageResponse(requestId: Int, imageId: Int): ImageResponse {
        return imageResponse(
            requestId = requestId,
            imageId = imageId,
            width = width,
            height = height,
            bands = bands,
            format = when (interpretation) {
                Interpretation.sRGB -> ImageFormat.RGBA_8888.name
                Interpretation.B_W -> ImageFormat.GRAYSCALE_8.name
                Interpretation.HISTOGRAM -> ImageFormat.HISTOGRAM.name
                else -> error("Unsupported image format")
            }
        )
    }
}
