package io.github.vivitoto.vanga.ui.dialogs.komf.identify

internal sealed interface KomfBookIdentifySupport {
    data object Supported : KomfBookIdentifySupport
    data class Unsupported(val reason: String = defaultUnsupportedReason) : KomfBookIdentifySupport
}

internal const val defaultUnsupportedReason: String =
    "当前 Komf 版本或 API 不支持单本书籍元数据识别，请升级 Komf 或继续使用系列级识别。"

internal fun KomfBookIdentifySupport.unsupportedReasonOrNull(): String? = when (this) {
    KomfBookIdentifySupport.Supported -> null
    is KomfBookIdentifySupport.Unsupported -> reason
}
