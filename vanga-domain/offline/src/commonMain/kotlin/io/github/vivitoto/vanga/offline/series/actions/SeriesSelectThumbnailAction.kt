package io.github.vivitoto.vanga.offline.series.actions

import io.github.vivitoto.vanga.offline.action.OfflineAction
import snd.komga.client.common.KomgaThumbnailId
import snd.komga.client.series.KomgaSeriesId

class SeriesSelectThumbnailAction(
) : OfflineAction {

    suspend fun run(
        seriesId: KomgaSeriesId,
        thumbnailId: KomgaThumbnailId
    ) {
        TODO("Not yet implemented")
    }
}