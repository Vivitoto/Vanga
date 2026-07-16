package io.github.vivitoto.vanga.offline.series.actions

import io.github.vivitoto.vanga.offline.action.OfflineAction
import io.github.vivitoto.vanga.offline.offlineUnsupported
import snd.komga.client.common.KomgaThumbnailId
import snd.komga.client.series.KomgaSeriesId

class SeriesSelectThumbnailAction(
) : OfflineAction {

    suspend fun run(
        seriesId: KomgaSeriesId,
        thumbnailId: KomgaThumbnailId
    ) {
        offlineUnsupported("选择系列缩略图")
    }
}
