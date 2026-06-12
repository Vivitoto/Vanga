package io.github.vivitoto.vanga.image.wasm.messages

import io.github.vivitoto.vanga.image.wasm.messages.WorkerMessageType.MAKE_HISTOGRAM
import io.github.vivitoto.vanga.image.wasm.set

external interface MakeHistogramRequest : WorkerMessage {
    val imageId: Int
}

internal fun makeHistogramRequest(
    requestId: Int,
    imageId: Int
): MakeHistogramRequest {
    val jsObject = workerMessage<MakeHistogramRequest>(MAKE_HISTOGRAM, requestId)
    jsObject["imageId"] = imageId.toJsNumber()
    return jsObject
}
