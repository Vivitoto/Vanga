package io.github.vivitoto.vanga.offline

class OfflineUnsupportedOperationException(
    operation: String,
) : UnsupportedOperationException("离线模式暂不支持${operation}，请连接 Komga 后再试")

fun offlineUnsupported(operation: String): Nothing = throw OfflineUnsupportedOperationException(operation)
