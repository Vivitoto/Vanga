package io.github.vivitoto.vanga.image.wasm.messages

import org.khronos.webgl.Uint8Array
import io.github.vivitoto.vanga.image.wasm.messages.WorkerMessageType.DECODE_AND_RESIZE
import io.github.vivitoto.vanga.image.wasm.set

external interface DecodeAndResizeRequest : WorkerMessage {
    val width: Int?
    val height: Int?
    val crop: Boolean
    val buffer: Uint8Array
}

internal fun decodeAndResizeRequest(
    requestId: Int,
    width: Int?,
    height: Int?,
    crop: Boolean,
    buffer: Uint8Array,
): DecodeAndResizeRequest {
    val jsObject = workerMessage<DecodeAndResizeRequest>(DECODE_AND_RESIZE, requestId)
    width?.let { jsObject["width"] = it.toJsNumber() }
    height?.let { jsObject["height"] = it.toJsNumber() }
    jsObject["crop"] = crop.toJsBoolean()
    jsObject["buffer"] = buffer
    return jsObject
}
