package io.github.vivitoto.vanga.offline.tasks

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.vivitoto.vanga.offline.action.OfflineActions
import io.github.vivitoto.vanga.offline.book.actions.BookDeleteAction
import io.github.vivitoto.vanga.offline.book.actions.BookDeleteFilesAction
import io.github.vivitoto.vanga.offline.book.actions.BookMetadataRefreshAction
import io.github.vivitoto.vanga.offline.book.repository.OfflineBookRepository
import io.github.vivitoto.vanga.offline.library.actions.LibraryDeleteAction
import io.github.vivitoto.vanga.offline.library.actions.LibraryEmptyTrashAction
import io.github.vivitoto.vanga.offline.series.actions.SeriesAggregateBookMetadataAction
import io.github.vivitoto.vanga.offline.series.actions.SeriesDeleteAction
import io.github.vivitoto.vanga.offline.series.actions.SeriesRefreshMetadataAction
import io.github.vivitoto.vanga.offline.sync.PlatformDownloadManager
import io.github.vivitoto.vanga.offline.tasks.model.TaskData
import io.github.vivitoto.vanga.offline.tasks.model.TaskData.AggregateSeriesMetadata
import io.github.vivitoto.vanga.offline.tasks.model.TaskData.DeleteBook
import io.github.vivitoto.vanga.offline.tasks.model.TaskData.DeleteLibrary
import io.github.vivitoto.vanga.offline.tasks.model.TaskData.DeleteSeries
import io.github.vivitoto.vanga.offline.tasks.model.TaskData.DownloadBook
import io.github.vivitoto.vanga.offline.tasks.model.TaskData.DownloadSeries
import io.github.vivitoto.vanga.offline.tasks.model.TaskData.EmptyTrash
import io.github.vivitoto.vanga.offline.tasks.model.TaskData.RefreshBookMetadata
import io.github.vivitoto.vanga.offline.tasks.model.TaskData.RefreshSeriesMetadata
import io.github.vivitoto.vanga.offline.tasks.model.TaskData.ScanLibrary
import io.github.vivitoto.vanga.offline.tasks.model.TaskEntry
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.search.anyOfBooks

private val logger = KotlinLogging.logger { }

class TaskHandler(
    private val actions: OfflineActions,
    private val bookRepository: OfflineBookRepository,
    private val taskEmitter: OfflineTaskEmitter,
    private val downloadManager: PlatformDownloadManager,
    private val komgaBookClient: KomgaBookClient,
) {
    suspend fun handleTask(entry: TaskEntry) {
        logger.info { "handling task ${entry.task}" }
        when (val task = entry.task) {
            is AggregateSeriesMetadata -> actions.get<SeriesAggregateBookMetadataAction>().execute(task.seriesId)
            is DeleteBook -> {
                bookRepository.find(task.bookId)?.let { book ->
                    if (book.oneshot) {
                        actions.get<SeriesDeleteAction>().execute(book.seriesId)
                    } else {
                        actions.get<BookDeleteAction>().execute(book.id)
                    }
                }
            }

            is DeleteSeries -> {
                actions.get<SeriesDeleteAction>().execute(task.seriesId)
            }

            is DeleteLibrary -> {
                actions.get<LibraryDeleteAction>().execute(task.libraryId)
            }

            is EmptyTrash -> actions.get<LibraryEmptyTrashAction>().execute(task.libraryId)
            is RefreshBookMetadata -> {
                bookRepository.find(task.bookId)?.let { book ->
                    actions.get<BookMetadataRefreshAction>().run(task.bookId)
                    taskEmitter.refreshSeriesMetadata(book.seriesId)
                }
            }

            is RefreshSeriesMetadata -> {
                actions.get<SeriesRefreshMetadataAction>().run(task.seriesId)
                taskEmitter.aggregateSeriesMetadata(task.seriesId)
            }

            is ScanLibrary -> {}

            is DownloadBook -> {
                downloadManager.launchBookDownload(task.bookId)
            }

            is DownloadSeries -> {
                val books = komgaBookClient.getBookList(
                    conditionBuilder = anyOfBooks { seriesId { isEqualTo(task.seriesId) } },
                    pageRequest = KomgaPageRequest(unpaged = true)
                ).content

                books.forEach { taskEmitter.downloadBook(it.id) }
            }

            is TaskData.DownloadBookCancel -> downloadManager.cancelBookDownload(task.bookId)
            is TaskData.DeleteBookFiles -> actions.get<BookDeleteFilesAction>().execute(task.file)
        }
    }
}