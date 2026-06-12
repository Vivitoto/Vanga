package io.github.vivitoto.vanga.image.wasm.messages

import org.khronos.webgl.Uint8Array
import io.github.vivitoto.vanga.image.wasm.messages.WorkerMessageType.MAP_LOOKUP_TABLE
import io.github.vivitoto.vanga.image.wasm.set

external interface MapLookupTableRequest : WorkerMessage {
    val imageId: Int
    val table: Uint8Array
}

internal fun mapLookupTableRequest(
    requestId: Int,
    imageId: Int,
    table: Uint8Array
): MapLookupTableRequest {
    val jsObject = workerMessage<MapLookupTableRequest>(MAP_LOOKUP_TABLE, requestId)
    jsObject["imageId"] = imageId.toJsNumber()
    jsObject["table"] = table
    return jsObject
}
