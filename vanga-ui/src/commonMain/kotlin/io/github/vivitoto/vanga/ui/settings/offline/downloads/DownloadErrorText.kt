package io.github.vivitoto.vanga.ui.settings.offline.downloads

import kotlin.coroutines.cancellation.CancellationException

internal fun downloadErrorText(error: Throwable): String {
    if (error is CancellationException) return "已取消"

    val message = error.message.orEmpty()
    val normalized = message.lowercase()
    return when {
        normalized.contains("permission") ||
                normalized.contains("denied") ||
                message.contains("权限") -> "无法写入下载目录，请检查存储权限。"

        normalized.contains("can't create") ||
                normalized.contains("can't write") ||
                normalized.contains("create file") ||
                normalized.contains("write to file") ||
                normalized.contains("directory") ||
                message.contains("目录") -> "无法写入下载目录，请检查下载位置是否可用。"

        normalized.contains("timeout") ||
                normalized.contains("timed out") ||
                normalized.contains("connect") ||
                normalized.contains("network") ||
                message.contains("网络") ||
                message.contains("连接") -> "下载失败，请检查网络或 Komga 连接。"

        message.isBlank() -> "下载失败：${error::class.simpleName ?: "未知错误"}"
        else -> "下载失败：$message"
    }
}
