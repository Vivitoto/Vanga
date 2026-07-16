package io.github.vivitoto.vanga.ui.book

import io.github.vivitoto.vanga.offline.sync.model.DownloadEvent

internal enum class DownloadButtonState {
    Ready,
    Downloading,
    Retry,
}

internal fun downloadButtonState(downloadEvent: DownloadEvent?): DownloadButtonState = when (downloadEvent) {
    is DownloadEvent.BookDownloadProgress -> DownloadButtonState.Downloading
    is DownloadEvent.BookDownloadError -> DownloadButtonState.Retry
    else -> DownloadButtonState.Ready
}

internal fun isDownloadButtonEnabled(downloadEvent: DownloadEvent?): Boolean =
    isDownloadButtonEnabled(downloadButtonState(downloadEvent))

internal fun isDownloadButtonEnabled(state: DownloadButtonState): Boolean = when (state) {
    DownloadButtonState.Ready,
    DownloadButtonState.Retry -> true

    DownloadButtonState.Downloading -> false
}

internal fun downloadButtonLabel(downloadEvent: DownloadEvent?): String =
    downloadButtonLabel(downloadButtonState(downloadEvent))

internal fun downloadButtonLabel(state: DownloadButtonState): String = when (state) {
    DownloadButtonState.Ready -> "下载"
    DownloadButtonState.Downloading -> "下载中…"
    DownloadButtonState.Retry -> "重试下载"
}
