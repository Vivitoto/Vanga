package io.github.vivitoto.vanga.image.wasm.messages

import io.github.vivitoto.vanga.image.wasm.messages.WorkerMessageType.INIT

external interface InitMessage : WorkerMessage

fun initMessage(): InitMessage {
    return workerMessage(INIT, 0)
}
