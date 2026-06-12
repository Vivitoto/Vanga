package io.github.vivitoto.vanga.image.wasm.messages

import io.github.vivitoto.vanga.image.wasm.messages.WorkerMessageType.ERROR
import io.github.vivitoto.vanga.image.wasm.set

external interface ErrorResponse : WorkerMessage {
    val message: String
}

internal fun errorResponse(
    requestId: Int,
    message: String,
): ErrorResponse {
    val jsObject = workerMessage<ErrorResponse>(ERROR, requestId)
    jsObject["message"] = message.toJsString()
    return jsObject
}
