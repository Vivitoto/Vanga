package io.github.vivitoto.vanga.ui.error

fun formatExceptionMessage(exception: Throwable): String {
    // wasmJs ktor returns error on fetch failure and most of the time without any useful information
    if (exception is Error && exception.message == "获取失败") {
        var jsCause = exception.cause
        while (jsCause != null) {
            val message = jsCause.message
            if (message != null && message.startsWith("JavaScript 错误")) {
                break
            }
            jsCause = jsCause.cause
        }
        if (jsCause != null) return buildErrorMessage(BrowserConnectionError(null, jsCause))
    }

    return buildErrorMessage(exception)
}

private fun buildErrorMessage(exception: Throwable): String {
    return buildString {
        exception::class.simpleName?.let { append("$it: ") }
        exception.message?.let { append("$it; ") }
        var cause = exception.cause
        while (cause != null) {
            cause.message?.let { append("$it;") }
            cause = cause.cause
        }
    }
}

private class BrowserConnectionError(
    override val message: String?,
    override val cause: Throwable?,
) : RuntimeException(message, cause)