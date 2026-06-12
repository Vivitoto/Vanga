package io.github.vivitoto.vanga.image.wasm.messages

import io.github.vivitoto.vanga.image.wasm.messages.WorkerMessageType.SHRINK
import io.github.vivitoto.vanga.image.wasm.set

external interface ShrinkRequest : WorkerMessage {
    val imageId: Int
    val factor: Double
}

internal fun shrinkRequest(
    requestId: Int,
    factor: Double,
    imageId: Int
): ShrinkRequest {
    val jsObject = workerMessage<ShrinkRequest>(SHRINK, requestId)
    jsObject["imageId"] = imageId.toJsNumber()
    jsObject["factor"] = factor.toJsNumber()
    return jsObject
}
