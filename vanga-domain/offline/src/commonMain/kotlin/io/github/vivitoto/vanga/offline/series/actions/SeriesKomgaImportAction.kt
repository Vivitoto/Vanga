package io.github.vivitoto.vanga.offline.series.actions

import io.github.vivitoto.vanga.db.TransactionTemplate
import io.github.vivitoto.vanga.offline.action.OfflineAction
import io.github.vivitoto.vanga.offline.book.repository.OfflineBookMetadataAggregationRepository
import io.github.vivitoto.vanga.offline.series.model.OfflineBookMetadataAggregation
import io.github.vivitoto.vanga.offline.series.model.OfflineSeries
import io.github.vivitoto.vanga.offline.series.model.OfflineSeriesMetadata
import io.github.vivitoto.vanga.offline.series.model.toOfflineThumbnailSeries
import io.github.vivitoto.vanga.offline.series.repository.OfflineSeriesMetadataRepository
import io.github.vivitoto.vanga.offline.series.repository.OfflineSeriesRepository
import io.github.vivitoto.vanga.offline.series.repository.OfflineThumbnailSeriesRepository
import io.github.vivitoto.vanga.offline.sync.model.OfflineLogEntry.Companion.logError
import io.github.vivitoto.vanga.offline.sync.model.OfflineLogEntry.Companion.logInfo
import io.github.vivitoto.vanga.offline.sync.repository.LogJournalRepository
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesClient
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.series.KomgaSeriesMetadata

class SeriesKomgaImportAction(
    private val seriesRepository: OfflineSeriesRepository,
    private val seriesMetadataRepository: OfflineSeriesMetadataRepository,
    private val thumbnailSeriesRepository: OfflineThumbnailSeriesRepository,
    private val bookMetadataAggregationRepository: OfflineBookMetadataAggregationRepository,
    private val logJournalRepository: LogJournalRepository,
    private val seriesClient: KomgaSeriesClient,
    private val transactionTemplate: TransactionTemplate,
) : OfflineAction {

    suspend fun execute(series: KomgaSeries) {
        try {
            transactionTemplate.execute {
                doImport(series)
                logJournalRepository.logInfo { "Series updated '${series.metadata.title}'" }
            }
        } catch (e: Exception) {
            logJournalRepository.logError(e) { "Series update error '${series.metadata.title}'" }
            throw e
        }

    }

    private suspend fun doImport(series: KomgaSeries) {
        val offlineSeries = series.toOfflineSeries()
        val offlineSeriesMetadata = series.metadata.toOfflineMetadata(series.id)

        val offlineSeriesThumbnail = seriesClient.getThumbnails(series.id)
            .firstOrNull { it.selected }
            ?.let { thumb ->
                val thumbnailBytes = seriesClient.getThumbnail(thumb.seriesId, thumb.id)
                thumb.toOfflineThumbnailSeries(thumbnailBytes)
            }
        seriesRepository.save(offlineSeries)
        seriesMetadataRepository.save(offlineSeriesMetadata)
        bookMetadataAggregationRepository.save(OfflineBookMetadataAggregation(seriesId = offlineSeries.id))
        offlineSeriesThumbnail?.let { thumbnailSeriesRepository.save(it) }
    }

    private fun KomgaSeries.toOfflineSeries() =
        OfflineSeries(
            id = this.id,
            libraryId = this.libraryId,
            name = this.name,
            url = this.url,
            oneshot = this.oneshot,

            bookCount = this.booksCount,
            deleted = this.deleted,
            created = this.created,
            lastModified = this.lastModified,
            fileLastModified = this.fileLastModified,
        )

    fun KomgaSeriesMetadata.toOfflineMetadata(seriesId: KomgaSeriesId) =
        OfflineSeriesMetadata(
            seriesId = seriesId,
            status = this.status,
            statusLock = this.statusLock,
            title = this.title,
            alternateTitles = this.alternateTitles,
            alternateTitlesLock = this.alternateTitlesLock,
            titleLock = this.titleLock,
            titleSort = this.titleSort,
            titleSortLock = this.titleSortLock,
            summary = this.summary,
            summaryLock = this.summaryLock,
            readingDirection = this.readingDirection,
            readingDirectionLock = this.readingDirectionLock,
            publisher = this.publisher,
            publisherLock = this.publisherLock,
            ageRating = this.ageRating,
            ageRatingLock = this.ageRatingLock,
            language = this.language,
            languageLock = this.languageLock,
            genres = this.genres,
            genresLock = this.genresLock,
            tags = this.tags,
            tagsLock = this.tagsLock,
            totalBookCount = this.totalBookCount,
            totalBookCountLock = this.totalBookCountLock,
            sharingLabels = this.sharingLabels,
            sharingLabelsLock = this.sharingLabelsLock,
            links = this.links,
            linksLock = this.linksLock
        )
}