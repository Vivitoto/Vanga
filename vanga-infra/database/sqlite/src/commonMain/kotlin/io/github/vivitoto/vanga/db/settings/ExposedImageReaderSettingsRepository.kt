package io.github.vivitoto.vanga.db.settings

import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import io.github.vivitoto.vanga.db.ExposedRepository
import io.github.vivitoto.vanga.db.ImageReaderSettings
import io.github.vivitoto.vanga.db.defaultBookId
import io.github.vivitoto.vanga.db.tables.ImageReaderSettingsTable
import io.github.vivitoto.vanga.image.ReduceKernel
import io.github.vivitoto.vanga.image.UpsamplingMode
import io.github.vivitoto.vanga.settings.model.ContinuousReadingDirection
import io.github.vivitoto.vanga.settings.model.LayoutScaleType
import io.github.vivitoto.vanga.settings.model.PageDisplayLayout
import io.github.vivitoto.vanga.settings.model.PagedReadingDirection
import io.github.vivitoto.vanga.settings.model.ReaderFlashColor
import io.github.vivitoto.vanga.settings.model.ReaderType

class ExposedImageReaderSettingsRepository(database: Database) : ExposedRepository(database) {

    suspend fun get(): ImageReaderSettings? {
        return transaction {
            ImageReaderSettingsTable.selectAll()
                .where { ImageReaderSettingsTable.bookId.eq(defaultBookId) }
                .firstOrNull()
                ?.let {

                    ImageReaderSettings(
                        readerType = it[ImageReaderSettingsTable.readerType]
                            .takeUnless { raw -> raw == "PANELS" }
                            ?.let { raw -> ReaderType.valueOf(raw) }
                            ?: ReaderType.PAGED,
                        stretchToFit = it[ImageReaderSettingsTable.stretchToFit],
                        pagedScaleType = LayoutScaleType.valueOf(it[ImageReaderSettingsTable.pagedScaleType]),
                        pagedReadingDirection = PagedReadingDirection.valueOf(it[ImageReaderSettingsTable.pagedReadingDirection]),
                        pagedPageLayout = PageDisplayLayout.valueOf(it[ImageReaderSettingsTable.pagedPageLayout]),
                        continuousReadingDirection = ContinuousReadingDirection.valueOf(it[ImageReaderSettingsTable.continuousReadingDirection]),
                        continuousPadding = it[ImageReaderSettingsTable.continuousPadding],
                        continuousPageSpacing = it[ImageReaderSettingsTable.continuousPageSpacing],
                        cropBorders = it[ImageReaderSettingsTable.cropBorders],
                        flashOnPageChange = it[ImageReaderSettingsTable.flashOnPageChange],
                        flashDuration = it[ImageReaderSettingsTable.flashDuration],
                        flashEveryNPages = it[ImageReaderSettingsTable.flashEveryNPages],
                        flashWith = ReaderFlashColor.valueOf(it[ImageReaderSettingsTable.flashWith]),
                        downsamplingKernel = ReduceKernel.valueOf(it[ImageReaderSettingsTable.downsamplingKernel]),
                        linearLightDownsampling = it[ImageReaderSettingsTable.linearLightDownsampling],
                        upsamplingMode = UpsamplingMode.valueOf(it[ImageReaderSettingsTable.upsamplingMode]),
                        loadThumbnailPreviews = it[ImageReaderSettingsTable.loadThumbnailPreviews],
                        volumeKeysNavigation = it[ImageReaderSettingsTable.volumeKeysNavigation],
                    )
                }
        }
    }

    suspend fun save(settings: ImageReaderSettings) {
        transaction {
            ImageReaderSettingsTable.upsert {
                it[bookId] = defaultBookId
                it[readerType] = settings.readerType.name
                it[stretchToFit] = settings.stretchToFit
                it[pagedScaleType] = settings.pagedScaleType.name
                it[pagedReadingDirection] = settings.pagedReadingDirection.name
                it[pagedPageLayout] = settings.pagedPageLayout.name
                it[continuousReadingDirection] = settings.continuousReadingDirection.name
                it[continuousPadding] = settings.continuousPadding
                it[continuousPageSpacing] = settings.continuousPageSpacing
                it[cropBorders] = settings.cropBorders
                it[flashOnPageChange] = settings.flashOnPageChange
                it[flashDuration] = settings.flashDuration
                it[flashEveryNPages] = settings.flashEveryNPages
                it[flashWith] = settings.flashWith.name
                it[downsamplingKernel] = settings.downsamplingKernel.name
                it[linearLightDownsampling] = settings.linearLightDownsampling
                it[loadThumbnailPreviews] = settings.loadThumbnailPreviews
                it[volumeKeysNavigation] = settings.volumeKeysNavigation
                it[upsamplingMode] = settings.upsamplingMode.name
            }
        }
    }
}
