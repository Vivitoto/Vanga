package io.github.vivitoto.vanga.image.wasm.messages

import org.khronos.webgl.Uint8Array
import io.github.vivitoto.vanga.image.wasm.messages.WorkerMessageType.GET_BYTES
import io.github.vivitoto.vanga.image.wasm.set

external interface GetBytesRequest : WorkerMessage {
    val imageId: Int
}

external interface GetBytesResponse : WorkerMessage {
    val bytes: Uint8Array
}

internal fun getBytesRequest(
    requestId: Int,
    imageId: Int
): GetBytesRequest {
    val jsObject = workerMessage<GetBytesRequest>(GET_BYTES, requestId)
    jsObject["imageId"] = imageId.toJsNumber()
    return jsObject
}

internal fun getBytesResponse(
    requestId: Int,
    bytes: Uint8Array
): GetBytesResponse {
    val jsObject = workerMessage<GetBytesResponse>(GET_BYTES, requestId)
    jsObject["bytes"] = bytes
    return jsObject
}
