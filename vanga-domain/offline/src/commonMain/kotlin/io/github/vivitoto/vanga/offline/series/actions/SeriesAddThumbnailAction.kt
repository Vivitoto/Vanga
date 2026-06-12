package io.github.vivitoto.vanga.offline.series.actions

import io.github.vivitoto.vanga.offline.action.OfflineAction
import io.github.vivitoto.vanga.offline.series.model.OfflineThumbnailSeries
import snd.komga.client.series.KomgaSeriesId

class SeriesAddThumbnailAction(
) : OfflineAction {

    suspend fun run(
        seriesId: KomgaSeriesId,
        file: ByteArray,
        selected: Boolean
    ): OfflineThumbnailSeries {
        TODO("Not yet implemented")
    }
}