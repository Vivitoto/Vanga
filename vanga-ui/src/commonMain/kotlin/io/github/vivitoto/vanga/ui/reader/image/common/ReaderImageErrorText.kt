package io.github.vivitoto.vanga.ui.reader.image.common

private const val MaxReaderImageErrorMessageLength = 160

internal fun readerImageErrorText(error: Throwable): String {
    val className = error::class.simpleName.orEmpty()
    val message = error.message.orEmpty()
    val searchable = "$className $message".lowercase()

    return when {
        searchable.contains("not found") ||
                searchable.contains("no such file") ||
                searchable.contains("missing file") ||
                searchable.contains("file missing") ||
                searchable.contains("does not exist") ||
                searchable.contains("404") ->
            "图片文件不存在，可能是本地文件缺失；请联网重新下载后再试。"

        searchable.contains("offline") && searchable.contains("unsupported") ->
            "离线模式暂不支持此图片来源，请连接 Komga 后再试。"

        searchable.contains("unsupported") ->
            "当前图片格式或来源暂不支持，请尝试连接 Komga 后重试。"

        else -> "图片加载失败：${sanitizeReaderImageErrorMessage(message.ifBlank { className })}"
    }
}

private fun sanitizeReaderImageErrorMessage(message: String): String = message
    .lineSequence()
    .map { it.trim() }
    .firstOrNull { it.isNotEmpty() }
    ?.take(MaxReaderImageErrorMessageLength)
    ?: "请重试或重新下载。"
