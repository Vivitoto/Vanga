package io.github.vivitoto.vanga.offline

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import io.github.vivitoto.vanga.db.TransactionTemplate
import io.github.vivitoto.vanga.offline.action.OfflineActions
import io.github.vivitoto.vanga.offline.api.OfflineActuatorApi
import io.github.vivitoto.vanga.offline.api.OfflineAnnouncementsApi
import io.github.vivitoto.vanga.offline.api.OfflineBookApi
import io.github.vivitoto.vanga.offline.api.OfflineCollectionsApi
import io.github.vivitoto.vanga.offline.api.OfflineFileSystemApi
import io.github.vivitoto.vanga.offline.api.OfflineKomgaApi
import io.github.vivitoto.vanga.offline.api.OfflineLibraryApi
import io.github.vivitoto.vanga.offline.api.OfflineReadListApi
import io.github.vivitoto.vanga.offline.api.OfflineReferentialApi
import io.github.vivitoto.vanga.offline.api.OfflineSeriesApi
import io.github.vivitoto.vanga.offline.api.OfflineSettingsApi
import io.github.vivitoto.vanga.offline.api.OfflineTaskApi
import io.github.vivitoto.vanga.offline.api.OfflineUserApi
import io.github.vivitoto.vanga.offline.api.repository.OfflineBookDtoRepository
import io.github.vivitoto.vanga.offline.api.repository.OfflineReferentialRepository
import io.github.vivitoto.vanga.offline.api.repository.OfflineSeriesDtoRepository
import io.github.vivitoto.vanga.offline.book.actions.BookAnalyzeAction
import io.github.vivitoto.vanga.offline.book.actions.BookDeleteAction
import io.github.vivitoto.vanga.offline.book.actions.BookDeleteFilesAction
import io.github.vivitoto.vanga.offline.book.actions.BookDeleteManyAction
import io.github.vivitoto.vanga.offline.book.actions.BookKomgaImportAction
import io.github.vivitoto.vanga.offline.book.actions.BookMarkRemoteDeletedAction
import io.github.vivitoto.vanga.offline.book.actions.BookMetadataRefreshAction
import io.github.vivitoto.vanga.offline.book.actions.BookMetadataUpdateAction
import io.github.vivitoto.vanga.offline.book.actions.BookThumbnailDeleteAction
import io.github.vivitoto.vanga.offline.book.actions.BookThumbnailSelectAction
import io.github.vivitoto.vanga.offline.book.actions.BookThumbnailUploadAction
import io.github.vivitoto.vanga.offline.book.repository.OfflineBookMetadataAggregationRepository
import io.github.vivitoto.vanga.offline.book.repository.OfflineBookMetadataRepository
import io.github.vivitoto.vanga.offline.book.repository.OfflineBookRepository
import io.github.vivitoto.vanga.offline.book.repository.OfflineThumbnailBookRepository
import io.github.vivitoto.vanga.offline.library.actions.LibraryAddAction
import io.github.vivitoto.vanga.offline.library.actions.LibraryAnalyzeAction
import io.github.vivitoto.vanga.offline.library.actions.LibraryDeleteAction
import io.github.vivitoto.vanga.offline.library.actions.LibraryEmptyTrashAction
import io.github.vivitoto.vanga.offline.library.actions.LibraryKomgaImportAction
import io.github.vivitoto.vanga.offline.library.actions.LibraryPatchAction
import io.github.vivitoto.vanga.offline.library.actions.LibraryRefreshMetadataAction
import io.github.vivitoto.vanga.offline.library.actions.LibraryScanAction
import io.github.vivitoto.vanga.offline.library.repository.OfflineLibraryRepository
import io.github.vivitoto.vanga.offline.media.repository.OfflineMediaRepository
import io.github.vivitoto.vanga.offline.mediacontainer.BookContentExtractors
import io.github.vivitoto.vanga.offline.mediacontainer.DivinaExtractor
import io.github.vivitoto.vanga.offline.mediacontainer.EpubExtractor
import io.github.vivitoto.vanga.offline.readprogress.OfflineReadProgressRepository
import io.github.vivitoto.vanga.offline.readprogress.actions.ProgressCompleteForBookAction
import io.github.vivitoto.vanga.offline.readprogress.actions.ProgressCompleteForSeriesAction
import io.github.vivitoto.vanga.offline.readprogress.actions.ProgressDeleteForBookAction
import io.github.vivitoto.vanga.offline.readprogress.actions.ProgressMarkAction
import io.github.vivitoto.vanga.offline.readprogress.actions.ProgressMarkProgressionAction
import io.github.vivitoto.vanga.offline.series.actions.SeriesAddThumbnailAction
import io.github.vivitoto.vanga.offline.series.actions.SeriesAggregateBookMetadataAction
import io.github.vivitoto.vanga.offline.series.actions.SeriesAnalyzeAction
import io.github.vivitoto.vanga.offline.series.actions.SeriesDeleteAction
import io.github.vivitoto.vanga.offline.series.actions.SeriesDeleteManyAction
import io.github.vivitoto.vanga.offline.series.actions.SeriesDeleteThumbnailAction
import io.github.vivitoto.vanga.offline.series.actions.SeriesKomgaImportAction
import io.github.vivitoto.vanga.offline.series.actions.SeriesRefreshMetadataAction
import io.github.vivitoto.vanga.offline.series.actions.SeriesSelectThumbnailAction
import io.github.vivitoto.vanga.offline.series.actions.SeriesUpdateMetadataAction
import io.github.vivitoto.vanga.offline.series.repository.OfflineSeriesMetadataRepository
import io.github.vivitoto.vanga.offline.series.repository.OfflineSeriesRepository
import io.github.vivitoto.vanga.offline.series.repository.OfflineThumbnailSeriesRepository
import io.github.vivitoto.vanga.offline.server.actions.MediaServerDeleteAction
import io.github.vivitoto.vanga.offline.server.actions.MediaServerSaveAction
import io.github.vivitoto.vanga.offline.server.repository.OfflineMediaServerRepository
import io.github.vivitoto.vanga.offline.settings.OfflineSettingsRepository
import io.github.vivitoto.vanga.offline.sync.BookDownloadService
import io.github.vivitoto.vanga.offline.sync.PlatformDownloadManager
import io.github.vivitoto.vanga.offline.sync.SyncManager
import io.github.vivitoto.vanga.offline.sync.actions.SyncEntrySaveAction
import io.github.vivitoto.vanga.offline.sync.actions.SyncReadProgressAction
import io.github.vivitoto.vanga.offline.sync.model.DownloadEvent
import io.github.vivitoto.vanga.offline.sync.repository.LogJournalRepository
import io.github.vivitoto.vanga.offline.tasks.OfflineTaskEmitter
import io.github.vivitoto.vanga.offline.tasks.TaskHandler
import io.github.vivitoto.vanga.offline.tasks.TaskProcessor
import io.github.vivitoto.vanga.offline.tasks.model.TaskAddedEvent
import io.github.vivitoto.vanga.offline.tasks.repository.OfflineTasksRepository
import io.github.vivitoto.vanga.offline.user.actions.UserDeleteAction
import io.github.vivitoto.vanga.offline.user.actions.UserKomgaImportAction
import io.github.vivitoto.vanga.offline.user.model.OfflineUser
import io.github.vivitoto.vanga.offline.user.repository.OfflineUserRepository
import snd.komga.client.KomgaClientFactory
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.user.KomgaUser
import snd.komga.client.user.KomgaUserId

data class OfflineRepositories(
    val mediaServerRepository: OfflineMediaServerRepository,
    val mediaRepository: OfflineMediaRepository,
    val bookRepository: OfflineBookRepository,
    val bookMetadataRepository: OfflineBookMetadataRepository,
    val bookMetadataAggregationRepository: OfflineBookMetadataAggregationRepository,
    val libraryRepository: OfflineLibraryRepository,
    val readProgressRepository: OfflineReadProgressRepository,
    val seriesMetadataRepository: OfflineSeriesMetadataRepository,
    val seriesRepository: OfflineSeriesRepository,
    val thumbnailBookRepository: OfflineThumbnailBookRepository,
    val thumbnailSeriesRepository: OfflineThumbnailSeriesRepository,
    val userRepository: OfflineUserRepository,
    val bookDtoRepository: OfflineBookDtoRepository,
    val referentialRepository: OfflineReferentialRepository,
    val seriesDtoRepository: OfflineSeriesDtoRepository,
    val logJournalRepository: LogJournalRepository,
    val transactionTemplate: TransactionTemplate,

    val tasksRepository: OfflineTasksRepository,
    val offlineSettingsRepository: OfflineSettingsRepository,
)

abstract class OfflineModule(
    val repositories: OfflineRepositories,
    val authenticatedUser: StateFlow<KomgaUser?>,
    val onlineServerUrl: StateFlow<String>,
    val isOffline: StateFlow<Boolean>,
    val komgaClientFactory: KomgaClientFactory,
) {
    private val moduleScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun initDependencies(): OfflineDependencies {

        val komgaEvents = MutableSharedFlow<KomgaEvent>(
            replay = 0,
            extraBufferCapacity = Int.MAX_VALUE,
            onBufferOverflow = BufferOverflow.SUSPEND
        )

        val offlineUserId: StateFlow<KomgaUserId> = repositories.offlineSettingsRepository.getUserId()
            .stateIn(moduleScope, SharingStarted.Eagerly, OfflineUser.ROOT)


        val taskAddedEventFlow = MutableSharedFlow<TaskAddedEvent>(0, Int.MAX_VALUE, BufferOverflow.SUSPEND)
        val taskEmitter = OfflineTaskEmitter(
            tasksRepository = repositories.tasksRepository,
            tasksFlow = taskAddedEventFlow
        )
        val actions = createActions(
            isOffline = isOffline,
            downloadsDirectory = repositories.offlineSettingsRepository.getDownloadDirectory(),
            komgaEvents = komgaEvents,
            taskEmitter = taskEmitter,
        )
        val downloadService = BookDownloadService(
            libraryDownloadPath = repositories.offlineSettingsRepository.getDownloadDirectory(),
            bookClient = komgaClientFactory.bookClient(),
            seriesClient = komgaClientFactory.seriesClient(),
            libraryClient = komgaClientFactory.libraryClient(),
            userClient = komgaClientFactory.userClient(),

            saveUserAction = actions.get(),
            saveServerAction = actions.get(),
            libraryImportAction = actions.get(),
            seriesImportAction = actions.get(),
            bookImportAction = actions.get(),
            onlineServerUrl = onlineServerUrl
        )

        val bookDownloadEvents: MutableSharedFlow<DownloadEvent> = MutableSharedFlow(
            replay = 0,
            extraBufferCapacity = 10_000,
            onBufferOverflow = BufferOverflow.SUSPEND
        )
        val downloadManager: PlatformDownloadManager = createPlatformDownloadManager(
            downloadService = downloadService,
            logJournalRepository = repositories.logJournalRepository,
            events = bookDownloadEvents
        )
        val fileService = BookContentExtractors(createDivinaExtractors(), createEpubExtractor())

        val offlineServerFlow = offlineUserId
            .map { repositories.mediaServerRepository.findByUserId(it) }
            .filterNotNull()
            .stateIn(moduleScope, SharingStarted.Eagerly, null)


        val komgaApi = OfflineKomgaApi(
            actuatorApi = OfflineActuatorApi(),
            announcementsApi = OfflineAnnouncementsApi(),
            bookApi = OfflineBookApi(
                mediaRepository = repositories.mediaRepository,
                vangaBookRepository = repositories.bookDtoRepository,
                bookRepository = repositories.bookRepository,
                thumbnailBookRepository = repositories.thumbnailBookRepository,
                readProgressRepository = repositories.readProgressRepository,
                actions = actions,
                fileContentExtractors = fileService,
                offlineUserId = offlineUserId,
            ),
            collectionsApi = OfflineCollectionsApi(),
            fileSystemApi = OfflineFileSystemApi(),
            libraryApi = OfflineLibraryApi(
                libraryRepository = repositories.libraryRepository,
                mediaServer = offlineServerFlow,
                offlineUserId = offlineUserId,
                actions = actions
            ),
            readListApi = OfflineReadListApi(),
            referentialApi = OfflineReferentialApi(
                referentialRepository = repositories.referentialRepository
            ),
            seriesApi = OfflineSeriesApi(
                actions = actions,
                seriesDtoRepository = repositories.seriesDtoRepository,
                seriesThumbnailRepository = repositories.thumbnailSeriesRepository,
                seriesRepository = repositories.seriesRepository,
                libraryRepository = repositories.libraryRepository,
                bookRepository = repositories.bookRepository,
                thumbnailBookRepository = repositories.thumbnailBookRepository,
                offlineUserId = offlineUserId,
            ),
            settingsApi = OfflineSettingsApi(),
            tasksApi = OfflineTaskApi(),
            userApi = OfflineUserApi(
                offlineUserId = offlineUserId,
                userRepository = repositories.userRepository
            ),
            komgaEvents = komgaEvents,
        )

        val taskHandler = TaskHandler(
            actions = actions,
            bookRepository = repositories.bookRepository,
            taskEmitter = taskEmitter,
            downloadManager = downloadManager,
            komgaBookClient = komgaClientFactory.bookClient(),
        )
        val taskProcessor = TaskProcessor(
            tasksRepository = repositories.tasksRepository,
            taskHandler = taskHandler,
            taskAddedEvents = taskAddedEventFlow,
            logJournalRepository = repositories.logJournalRepository,
        )

        val syncManager = SyncManager(
            onlineUser = authenticatedUser,
            bookClient = komgaClientFactory.bookClient(),
            seriesClient = komgaClientFactory.seriesClient(),
            libraryClient = komgaClientFactory.libraryClient(),
            libraryRepository = repositories.libraryRepository,
            seriesRepository = repositories.seriesRepository,
            bookRepository = repositories.bookRepository,
            mediaServerRepository = repositories.mediaServerRepository,
            logJournalRepository = repositories.logJournalRepository,
            settingsRepository = repositories.offlineSettingsRepository,
            userSaveAction = actions.get(),
            libraryImportAction = actions.get(),
            seriesImportAction = actions.get(),
            bookImportAction = actions.get(),
            bookMarkDeletedAction = actions.get(),
            syncReadProgressAction = actions.get(),
        )
        taskProcessor.initialize()

        return OfflineDependencies(
            actions = actions,
            taskEmitter = taskEmitter,
            komgaEvents = komgaEvents,
            bookDownloadEvents = bookDownloadEvents,
            downloadService = downloadService,
            repositories = repositories,
            fileService = fileService,
            komgaApi = komgaApi
        )
    }

    private fun createActions(
        isOffline: StateFlow<Boolean>,
        downloadsDirectory: Flow<PlatformFile>,
        komgaEvents: MutableSharedFlow<KomgaEvent>,
        taskEmitter: OfflineTaskEmitter
    ): OfflineActions {

        val bookDeleteManyAction = BookDeleteManyAction(
            bookRepository = repositories.bookRepository,
            bookMetadataRepository = repositories.bookMetadataRepository,
            thumbnailBookRepository = repositories.thumbnailBookRepository,
            mediaRepository = repositories.mediaRepository,
            readProgressRepository = repositories.readProgressRepository,
            transactionTemplate = repositories.transactionTemplate,
            komgaEvents = komgaEvents,
            taskEmitter = taskEmitter
        )
        val seriesDeleteManyAction = SeriesDeleteManyAction(
            seriesRepository = repositories.seriesRepository,
            seriesMetadataRepository = repositories.seriesMetadataRepository,
            seriesThumbnailSeriesRepository = repositories.thumbnailSeriesRepository,
            bookRepository = repositories.bookRepository,
            bookMetadataAggregationRepository = repositories.bookMetadataAggregationRepository,
            readProgressRepository = repositories.readProgressRepository,
            bookDeleteManyAction = bookDeleteManyAction,
            komgaEvents = komgaEvents,
            transactionTemplate = repositories.transactionTemplate
        )
        val userDeleteAction = UserDeleteAction(
            userRepository = repositories.userRepository,
            readProgressRepository = repositories.readProgressRepository,
            settingsRepository = repositories.offlineSettingsRepository,
            transactionTemplate = repositories.transactionTemplate,
            komgaEvents = komgaEvents
        )
        val libraryDeleteAction = LibraryDeleteAction(
            libraryRepository = repositories.libraryRepository,
            seriesRepository = repositories.seriesRepository,
            seriesDeleteManyAction = seriesDeleteManyAction,
            transactionTemplate = repositories.transactionTemplate,
            komgaEvents = komgaEvents,
        )
        val mediaServerDeleteAction = MediaServerDeleteAction(
            mediaServerRepository = repositories.mediaServerRepository,
            libraryRepository = repositories.libraryRepository,
            userRepository = repositories.userRepository,
            transactionTemplate = repositories.transactionTemplate,
            libraryDeleteAction = libraryDeleteAction,
            userDeleteAction = userDeleteAction,
        )

        return OfflineActions(
            listOf(
                bookDeleteManyAction,
                seriesDeleteManyAction,
                userDeleteAction,
                libraryDeleteAction,
                mediaServerDeleteAction,

                BookAnalyzeAction(),
                BookDeleteAction(
                    bookRepository = repositories.bookRepository,
                    bookMetadataRepository = repositories.bookMetadataRepository,
                    thumbnailBookRepository = repositories.thumbnailBookRepository,
                    readProgressRepository = repositories.readProgressRepository,
                    transactionTemplate = repositories.transactionTemplate,
                    mediaRepository = repositories.mediaRepository,
                    komgaEvents = komgaEvents,
                    taskEmitter = taskEmitter,
                    isOffline = isOffline
                ),
                BookDeleteFilesAction(downloadsDirectory),
                BookMarkRemoteDeletedAction(
                    bookRepository = repositories.bookRepository,
                    transactionTemplate = repositories.transactionTemplate
                ),
                BookMetadataRefreshAction(),
                BookMetadataUpdateAction(),
                BookThumbnailDeleteAction(),
                BookThumbnailSelectAction(),
                BookThumbnailUploadAction(),
                BookKomgaImportAction(
                    bookRepository = repositories.bookRepository,
                    bookMetadataRepository = repositories.bookMetadataRepository,
                    thumbnailBookRepository = repositories.thumbnailBookRepository,
                    readProgressRepository = repositories.readProgressRepository,
                    mediaRepository = repositories.mediaRepository,
                    logJournalRepository = repositories.logJournalRepository,
                    bookClient = komgaClientFactory.bookClient(),
                    taskEmitter = taskEmitter,
                    transactionTemplate = repositories.transactionTemplate,
                    komgaEvents = komgaEvents
                ),

                LibraryAddAction(
                    libraryRepository = repositories.libraryRepository,
                    events = komgaEvents
                ),
                LibraryAnalyzeAction(),
                LibraryEmptyTrashAction(),
                LibraryKomgaImportAction(
                    libraryRepository = repositories.libraryRepository,
                    mediaServerRepository = repositories.mediaServerRepository,
                    logJournalRepository = repositories.logJournalRepository,
                    transactionTemplate = repositories.transactionTemplate
                ),
                LibraryPatchAction(),
                LibraryRefreshMetadataAction(),
                LibraryScanAction(),

                ProgressCompleteForBookAction(
                    mediaRepository = repositories.mediaRepository,
                    readProgressRepository = repositories.readProgressRepository,
                    transactionTemplate = repositories.transactionTemplate,
                    komgaEvents = komgaEvents
                ),
                ProgressCompleteForSeriesAction(
                    readProgressRepository = repositories.readProgressRepository,
                    bookRepository = repositories.bookRepository,
                    mediaRepository = repositories.mediaRepository,
                    userRepository = repositories.userRepository,
                    transactionTemplate = repositories.transactionTemplate,
                    komgaEvents = komgaEvents
                ),
                ProgressDeleteForBookAction(
                    readProgressRepository = repositories.readProgressRepository,
                    transactionTemplate = repositories.transactionTemplate,
                    komgaEvents = komgaEvents
                ),
                ProgressMarkAction(
                    mediaRepository = repositories.mediaRepository,
                    readProgressRepository = repositories.readProgressRepository,
                    transactionTemplate = repositories.transactionTemplate,
                    komgaEvents = komgaEvents
                ),
                ProgressMarkProgressionAction(
                    mediaRepository = repositories.mediaRepository,
                    readProgressRepository = repositories.readProgressRepository,
                    transactionTemplate = repositories.transactionTemplate,
                    komgaEvents = komgaEvents
                ),

                SeriesAddThumbnailAction(),
                SeriesAggregateBookMetadataAction(
                    bookRepository = repositories.bookRepository,
                    bookMetadataRepository = repositories.bookMetadataRepository,
                    bookMetadataAggregationRepository = repositories.bookMetadataAggregationRepository,
                    transactionTemplate = repositories.transactionTemplate
                ),
                SeriesAnalyzeAction(),
                SeriesDeleteAction(
                    seriesRepository = repositories.seriesRepository,
                    seriesMetadataRepository = repositories.seriesMetadataRepository,
                    seriesThumbnailSeriesRepository = repositories.thumbnailSeriesRepository,
                    bookMetadataAggregationRepository = repositories.bookMetadataAggregationRepository,
                    bookRepository = repositories.bookRepository,
                    bookDeleteManyAction = bookDeleteManyAction,
                    transactionTemplate = repositories.transactionTemplate,
                    komgaEvents = komgaEvents,
                    isOffline = isOffline,
                ),
                SeriesDeleteThumbnailAction(),
                SeriesKomgaImportAction(
                    seriesRepository = repositories.seriesRepository,
                    seriesMetadataRepository = repositories.seriesMetadataRepository,
                    thumbnailSeriesRepository = repositories.thumbnailSeriesRepository,
                    bookMetadataAggregationRepository = repositories.bookMetadataAggregationRepository,
                    logJournalRepository = repositories.logJournalRepository,
                    seriesClient = komgaClientFactory.seriesClient(),
                    transactionTemplate = repositories.transactionTemplate
                ),
                SeriesRefreshMetadataAction(),
                SeriesSelectThumbnailAction(),
                SeriesUpdateMetadataAction(),

                SyncEntrySaveAction(repositories.logJournalRepository),
                SyncReadProgressAction(
                    settingsRepository = repositories.offlineSettingsRepository,
                    bookClient = komgaClientFactory.bookClient(),
                    bookMetadataRepository = repositories.bookMetadataRepository,
                    readProgressRepository = repositories.readProgressRepository,
                    mediaServerRepository = repositories.mediaServerRepository,
                    userRepository = repositories.userRepository,
                    logJournalRepository = repositories.logJournalRepository,
                    transactionTemplate = repositories.transactionTemplate
                ),
                MediaServerSaveAction(
                    mediaServerRepository = repositories.mediaServerRepository,
                    transactionTemplate = repositories.transactionTemplate
                ),
                UserKomgaImportAction(
                    userRepository = repositories.userRepository,
                    transactionTemplate = repositories.transactionTemplate
                ),
            )
        )
    }

    protected abstract fun createDivinaExtractors(): List<DivinaExtractor>
    protected abstract fun createEpubExtractor(): EpubExtractor
    protected abstract fun createPlatformDownloadManager(
        downloadService: BookDownloadService,
        logJournalRepository: LogJournalRepository,
        events: MutableSharedFlow<DownloadEvent>,
    ): PlatformDownloadManager
}