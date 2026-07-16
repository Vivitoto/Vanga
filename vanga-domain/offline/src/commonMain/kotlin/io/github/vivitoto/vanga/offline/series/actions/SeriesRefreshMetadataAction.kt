package io.github.vivitoto.vanga.offline.series.actions

import io.github.vivitoto.vanga.offline.action.OfflineAction
import io.github.vivitoto.vanga.offline.offlineUnsupported
import snd.komga.client.series.KomgaSeriesId

class SeriesRefreshMetadataAction(
) : OfflineAction {

    suspend fun run(
        seriesId: KomgaSeriesId,
    ) {
        offlineUnsupported("刷新系列元数据")
    }
}
