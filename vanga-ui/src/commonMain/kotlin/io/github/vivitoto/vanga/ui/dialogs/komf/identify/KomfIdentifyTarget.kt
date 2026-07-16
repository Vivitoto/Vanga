package io.github.vivitoto.vanga.ui.dialogs.komf.identify

internal enum class KomfIdentifyTarget {
    Series,
    Book,
}

internal fun KomfIdentifyTarget.canUseSeriesIdentifyEndpoint(): Boolean = when (this) {
    KomfIdentifyTarget.Series -> true
    KomfIdentifyTarget.Book -> false
}
