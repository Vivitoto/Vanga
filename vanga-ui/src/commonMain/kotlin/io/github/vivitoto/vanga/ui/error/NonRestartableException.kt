package io.github.vivitoto.vanga.ui.error

class NonRestartableException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}