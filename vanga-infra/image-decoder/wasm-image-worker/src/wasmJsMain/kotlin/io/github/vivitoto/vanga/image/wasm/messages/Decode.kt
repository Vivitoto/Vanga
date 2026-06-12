package io.github.vivitoto.vanga.image.wasm.messages

import org.khronos.webgl.Uint8Array
import io.github.vivitoto.vanga.image.wasm.messages.WorkerMessageType.DECODE
import io.github.vivitoto.vanga.image.wasm.set

external interface DecodeRequest : WorkerMessage {
    val buffer: Uint8Array
}

internal fun decodeRequest(
    requestId: Int,
    buffer: Uint8Array,
): DecodeAndResizeRequest {
    val jsObject = workerMessage<DecodeAndResizeRequest>(DECODE, requestId)
    jsObject["buffer"] = buffer
    return jsObject
}
