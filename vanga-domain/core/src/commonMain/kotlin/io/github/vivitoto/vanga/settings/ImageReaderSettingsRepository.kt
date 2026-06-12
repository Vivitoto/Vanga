package io.github.vivitoto.vanga.settings

import kotlinx.coroutines.flow.Flow
import io.github.vivitoto.vanga.image.ReduceKernel
import io.github.vivitoto.vanga.image.UpsamplingMode
import io.github.vivitoto.vanga.settings.model.ContinuousReadingDirection
import io.github.vivitoto.vanga.settings.model.LayoutScaleType
import io.github.vivitoto.vanga.settings.model.PageDisplayLayout
import io.github.vivitoto.vanga.settings.model.PagedReadingDirection
import io.github.vivitoto.vanga.settings.model.ReaderFlashColor
import io.github.vivitoto.vanga.settings.model.ReaderType

interface ImageReaderSettingsRepository {
    fun getReaderType(): Flow<ReaderType>
    suspend fun putReaderType(type: ReaderType)

    fun getStretchToFit(): Flow<Boolean>
    suspend fun putStretchToFit(stretch: Boolean)

    fun getCropBorders(): Flow<Boolean>
    suspend fun putCropBorders(trim: Boolean)

    fun getPagedReaderScaleType(): Flow<LayoutScaleType>
    suspend fun putPagedReaderScaleType(type: LayoutScaleType)

    fun getPagedReaderReadingDirection(): Flow<PagedReadingDirection>
    suspend fun putPagedReaderReadingDirection(direction: PagedReadingDirection)

    fun getPagedReaderDisplayLayout(): Flow<PageDisplayLayout>
    suspend fun putPagedReaderDisplayLayout(layout: PageDisplayLayout)

    fun getContinuousReaderReadingDirection(): Flow<ContinuousReadingDirection>
    suspend fun putContinuousReaderReadingDirection(direction: ContinuousReadingDirection)

    fun getContinuousReaderPadding(): Flow<Float>
    suspend fun putContinuousReaderPadding(padding: Float)

    fun getContinuousReaderPageSpacing(): Flow<Int>
    suspend fun putContinuousReaderPageSpacing(spacing: Int)

    fun getFlashOnPageChange(): Flow<Boolean>
    suspend fun putFlashOnPageChange(flash: Boolean)

    fun getFlashDuration(): Flow<Long>
    suspend fun putFlashDuration(duration: Long)

    fun getFlashEveryNPages(): Flow<Int>
    suspend fun putFlashEveryNPages(pages: Int)

    fun getFlashWith(): Flow<ReaderFlashColor>
    suspend fun putFlashWith(color: ReaderFlashColor)

    fun getDownsamplingKernel(): Flow<ReduceKernel>
    suspend fun putDownsamplingKernel(kernel: ReduceKernel)

    fun getLinearLightDownsampling(): Flow<Boolean>
    suspend fun putLinearLightDownsampling(linear: Boolean)

    fun getUpsamplingMode(): Flow<UpsamplingMode>
    suspend fun putUpsamplingMode(mode: UpsamplingMode)

    fun getLoadThumbnailPreviews(): Flow<Boolean>
    suspend fun putLoadThumbnailPreviews(load: Boolean)

    fun getVolumeKeysNavigation(): Flow<Boolean>
    suspend fun putVolumeKeysNavigation(enable: Boolean)
}
