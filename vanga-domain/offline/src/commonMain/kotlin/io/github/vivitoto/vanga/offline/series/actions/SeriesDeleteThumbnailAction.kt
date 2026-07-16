package io.github.vivitoto.vanga.offline.series.actions

import io.github.vivitoto.vanga.offline.action.OfflineAction
import io.github.vivitoto.vanga.offline.offlineUnsupported
import snd.komga.client.common.KomgaThumbnailId
import snd.komga.client.series.KomgaSeriesId

class SeriesDeleteThumbnailAction(
) : OfflineAction {

    suspend fun run(
        seriesId: KomgaSeriesId,
        thumbnailId: KomgaThumbnailId
    ) {
        offlineUnsupported("删除系列缩略图")
    }
}
