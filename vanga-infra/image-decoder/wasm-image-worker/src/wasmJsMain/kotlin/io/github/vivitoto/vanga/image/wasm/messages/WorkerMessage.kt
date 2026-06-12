package io.github.vivitoto.vanga.image.wasm.messages

import io.github.vivitoto.vanga.image.wasm.makeJsObject
import io.github.vivitoto.vanga.image.wasm.set

external interface WorkerMessage : JsAny {
    val type: String
    val requestId: Int
}

internal fun WorkerMessage.getType(): WorkerMessageType = WorkerMessageType.valueOf(type)

internal fun <T : WorkerMessage> workerMessage(
    type: WorkerMessageType,
    requestId: Int
): T {
    val jsObject = makeJsObject<T>()
    jsObject["type"] = type.name.toJsString()
    jsObject["requestId"] = requestId.toJsNumber()
    return jsObject
}

internal enum class WorkerMessageType {
    INIT,
    DECODE_AND_RESIZE,
    DECODE,
    EXTRACT_AREA,
    RESIZE,
    SHRINK,
    FIND_TRIM,
    MAKE_HISTOGRAM,
    MAP_LOOKUP_TABLE,
    GET_BYTES,
    IMAGE,
    ERROR,
    CLOSE,
}
