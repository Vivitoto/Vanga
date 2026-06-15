package io.github.vivitoto.vanga.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import io.github.vivitoto.vanga.favorites.FavoriteCollectionService
import io.github.vivitoto.vanga.favorites.FavoriteReadListService
import io.github.vivitoto.vanga.favorites.FavoriteWebDavSyncService
import io.github.vivitoto.vanga.komga.api.model.VangaBook
import io.github.vivitoto.vanga.ui.book.BookViewModel
import io.github.vivitoto.vanga.ui.collection.CollectionViewModel
import io.github.vivitoto.vanga.ui.common.menus.bulk.BookBulkActions
import io.github.vivitoto.vanga.ui.common.menus.bulk.CollectionBulkActions
import io.github.vivitoto.vanga.ui.common.menus.bulk.FavoriteBulkActions
import io.github.vivitoto.vanga.ui.common.menus.bulk.ReadListBulkActions
import io.github.vivitoto.vanga.ui.common.menus.bulk.SeriesBulkActions
import io.github.vivitoto.vanga.ui.dialogs.book.edit.BookEditDialogViewModel
import io.github.vivitoto.vanga.ui.dialogs.book.editbulk.BookBulkEditDialogViewModel
import io.github.vivitoto.vanga.ui.dialogs.collectionadd.AddToCollectionDialogViewModel
import io.github.vivitoto.vanga.ui.dialogs.collectionedit.CollectionEditDialogViewModel
import io.github.vivitoto.vanga.ui.dialogs.filebrowser.FileBrowserDialogViewModel
import io.github.vivitoto.vanga.ui.dialogs.komf.identify.KomfIdentifyDialogViewModel
import io.github.vivitoto.vanga.ui.dialogs.komf.identify.KomfLibraryIdentifyViewmodel
import io.github.vivitoto.vanga.ui.dialogs.komf.reset.KomfResetMetadataDialogViewModel
import io.github.vivitoto.vanga.ui.dialogs.libraryedit.LibraryEditDialogViewModel
import io.github.vivitoto.vanga.ui.dialogs.oneshot.OneshotEditDialogViewModel
import io.github.vivitoto.vanga.ui.dialogs.readlistadd.AddToReadListDialogViewModel
import io.github.vivitoto.vanga.ui.dialogs.readlistedit.ReadListEditDialogViewModel
import io.github.vivitoto.vanga.ui.dialogs.series.edit.SeriesEditDialogViewModel
import io.github.vivitoto.vanga.ui.dialogs.series.editbulk.SeriesBulkEditDialogViewModel
import io.github.vivitoto.vanga.ui.dialogs.user.PasswordChangeDialogViewModel
import io.github.vivitoto.vanga.ui.dialogs.user.UserAddDialogViewModel
import io.github.vivitoto.vanga.ui.dialogs.user.UserEditDialogViewModel
import io.github.vivitoto.vanga.ui.favorites.FavoriteToggleViewModel
import io.github.vivitoto.vanga.ui.favorites.FavoritesViewModel
import io.github.vivitoto.vanga.ui.home.HomeFilterData
import io.github.vivitoto.vanga.ui.home.HomeViewModel
import io.github.vivitoto.vanga.ui.home.edit.FilterEditViewModel
import io.github.vivitoto.vanga.ui.library.LibraryViewModel
import io.github.vivitoto.vanga.ui.login.LoginViewModel
import io.github.vivitoto.vanga.ui.login.offline.OfflineLoginViewModel
import io.github.vivitoto.vanga.ui.oneshot.OneshotViewModel
import io.github.vivitoto.vanga.ui.platform.PlatformType
import io.github.vivitoto.vanga.ui.reader.epub.EpubReaderViewModel
import io.github.vivitoto.vanga.ui.reader.image.ReaderViewModel
import io.github.vivitoto.vanga.ui.readlist.ReadListViewModel
import io.github.vivitoto.vanga.ui.search.SearchViewModel
import io.github.vivitoto.vanga.ui.series.SeriesViewModel
import io.github.vivitoto.vanga.ui.series.SeriesViewModel.SeriesTab
import io.github.vivitoto.vanga.ui.settings.account.AccountSettingsViewModel
import io.github.vivitoto.vanga.ui.settings.analysis.MediaAnalysisViewModel
import io.github.vivitoto.vanga.ui.settings.announcements.AnnouncementsViewModel
import io.github.vivitoto.vanga.ui.settings.appearance.AppSettingsViewModel
import io.github.vivitoto.vanga.ui.settings.authactivity.AuthenticationActivityViewModel
import io.github.vivitoto.vanga.ui.settings.epub.EpubReaderSettingsViewModel
import io.github.vivitoto.vanga.ui.settings.favoritesync.FavoriteSyncSettingsViewModel
import io.github.vivitoto.vanga.ui.settings.imagereader.ImageReaderSettingsViewModel
import io.github.vivitoto.vanga.ui.settings.komf.KomfSharedState
import io.github.vivitoto.vanga.ui.settings.komf.general.KomfSettingsViewModel
import io.github.vivitoto.vanga.ui.settings.komf.jobs.KomfJobsViewModel
import io.github.vivitoto.vanga.ui.settings.komf.notifications.KomfNotificationSettingsViewModel
import io.github.vivitoto.vanga.ui.settings.komf.processing.KomfProcessingSettingsViewModel
import io.github.vivitoto.vanga.ui.settings.komf.providers.KomfProvidersSettingsViewModel
import io.github.vivitoto.vanga.ui.settings.navigation.SettingsNavigationViewModel
import io.github.vivitoto.vanga.ui.settings.offline.OfflineSettingsViewModel
import io.github.vivitoto.vanga.ui.settings.server.ServerSettingsViewModel
import io.github.vivitoto.vanga.ui.settings.updates.AppUpdatesViewModel
import io.github.vivitoto.vanga.ui.settings.users.UsersViewModel
import io.github.vivitoto.vanga.ui.topbar.NotificationsState
import io.github.vivitoto.vanga.ui.topbar.SearchBarState
import io.github.vivitoto.vanga.updates.AppRelease
import io.github.vivitoto.vanga.updates.StartupUpdateChecker
import snd.komf.api.KomfServerLibraryId
import snd.komf.api.KomfServerSeriesId
import snd.komf.api.MediaServer
import snd.komf.api.MediaServer.KAVITA
import snd.komf.api.MediaServer.KOMGA
import snd.komga.client.book.KomgaBookId
import snd.komga.client.collection.KomgaCollection
import snd.komga.client.collection.KomgaCollectionId
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.readlist.KomgaReadList
import snd.komga.client.readlist.KomgaReadListId
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.user.KomgaUser

class ViewModelFactory(
    private val dependencies: DependencyContainer,
    private val platformType: PlatformType,
) {
    private val appRepositories = dependencies.appRepositories
    private val komgaApi
        get() = dependencies.komgaApi.value

    private val releases = MutableStateFlow<List<AppRelease>>(emptyList())
    private val imageReaderCurrentBook = MutableStateFlow<KomgaBookId?>(null)

    private val komfSharedState = KomfSharedState(
        komfConfigClient = dependencies.komfClientFactory.configClient(),
        komgaServerClient = dependencies.komfClientFactory.mediaServerClient(KOMGA),
        kavitaServerClient = dependencies.komfClientFactory.mediaServerClient(KAVITA),
        notifications = dependencies.appNotifications,
    )

    private val startupUpdateChecker = dependencies.appUpdater?.let { updater ->
        StartupUpdateChecker(
            updater,
            appRepositories.settingsRepository,
            releases
        )
    }
    val screenReloadEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = DROP_OLDEST)

    fun getLibraryViewModel(
        libraryId: KomgaLibraryId?,
    ): LibraryViewModel {
        return LibraryViewModel(
            libraryApi = komgaApi.libraryApi,
            collectionApi = komgaApi.collectionsApi,
            readListsApi = komgaApi.readListApi,
            seriesApi = komgaApi.seriesApi,
            referentialApi = komgaApi.referentialApi,

            appNotifications = dependencies.appNotifications,
            komgaEvents = dependencies.komgaEvents.events,
            libraryFlow = getLibraryFlow(libraryId),
            settingsRepository = appRepositories.settingsRepository,
            taskEmitter = dependencies.offlineDependencies.taskEmitter,
        )
    }

    fun getHomeViewModel(): HomeViewModel {
        return HomeViewModel(
            seriesApi = komgaApi.seriesApi,
            bookApi = komgaApi.bookApi,
            appNotifications = dependencies.appNotifications,
            komgaEvents = dependencies.komgaEvents.events,
            filterRepository = appRepositories.homeScreenFilterRepository,
            taskEmitter = dependencies.offlineDependencies.taskEmitter,
            cardWidthFlow = getGridCardWidth(),
        )
    }

    fun getFavoriteToggleViewModel(): FavoriteToggleViewModel {
        val ownerLabelProvider = { dependencies.komgaSharedState.authenticatedUser.value?.email }
        val serverUrlProvider = { dependencies.komgaSharedState.serverUrl.value }
        return FavoriteToggleViewModel(
            favoriteCollectionService = FavoriteCollectionService(
                localFavoritesRepository = appRepositories.localFavoritesRepository,
                ownerLabelProvider = ownerLabelProvider,
                serverUrlProvider = serverUrlProvider,
            ),
            favoriteReadListService = FavoriteReadListService(
                localFavoritesRepository = appRepositories.localFavoritesRepository,
                ownerLabelProvider = ownerLabelProvider,
                serverUrlProvider = serverUrlProvider,
            ),
            favoriteSyncService = createFavoriteSyncService(ownerLabelProvider, serverUrlProvider),
            currentUserProvider = { dependencies.komgaSharedState.authenticatedUser.value },
            appNotifications = dependencies.appNotifications,
            onFavoritesChanged = { screenReloadEvents.tryEmit(Unit) },
        )
    }

    fun getFavoritesViewModel(): FavoritesViewModel {
        val ownerLabelProvider = { dependencies.komgaSharedState.authenticatedUser.value?.email }
        val serverUrlProvider = { dependencies.komgaSharedState.serverUrl.value }
        return FavoritesViewModel(
            favoriteCollectionService = FavoriteCollectionService(
                localFavoritesRepository = appRepositories.localFavoritesRepository,
                ownerLabelProvider = ownerLabelProvider,
                serverUrlProvider = serverUrlProvider,
            ),
            favoriteReadListService = FavoriteReadListService(
                localFavoritesRepository = appRepositories.localFavoritesRepository,
                ownerLabelProvider = ownerLabelProvider,
                serverUrlProvider = serverUrlProvider,
            ),
            favoriteSyncService = createFavoriteSyncService(ownerLabelProvider, serverUrlProvider),
            seriesApi = komgaApi.seriesApi,
            bookApi = komgaApi.bookApi,
            currentUser = dependencies.komgaSharedState.authenticatedUser,
            appNotifications = dependencies.appNotifications,
            cardWidthFlow = getGridCardWidth(),
        )
    }

    private fun createFavoriteSyncService(
        ownerLabelProvider: () -> String?,
        serverUrlProvider: () -> String?,
    ): FavoriteWebDavSyncService = FavoriteWebDavSyncService(
        settingsRepository = appRepositories.favoriteSyncSettingsRepository,
        localFavoritesRepository = appRepositories.localFavoritesRepository,
        httpClient = dependencies.webDavHttpClient,
        serverUrlProvider = serverUrlProvider,
        ownerLabelProvider = ownerLabelProvider,
    )

    fun getFilterEditViewModel(homeFilters: List<HomeFilterData>?): FilterEditViewModel {
        return FilterEditViewModel(
            initialFilters = homeFilters,
            appNotifications = dependencies.appNotifications,
            seriesApi = komgaApi.seriesApi,
            bookApi = komgaApi.bookApi,
            readListApi = komgaApi.readListApi,
            collectionApi = komgaApi.collectionsApi,
            referentialApi = komgaApi.referentialApi,
            filterRepository = appRepositories.homeScreenFilterRepository,
            libraries = getLibraries(),
            cardWidthFlow = getGridCardWidth(),
        )
    }

    fun getNavigationViewModel(): MainScreenViewModel {
        return MainScreenViewModel(
            libraryApi = komgaApi.libraryApi,
            appNotifications = dependencies.appNotifications,
            komgaEvents = dependencies.komgaEvents.events,
            screenReloadFlow = screenReloadEvents,
            searchBarState = SearchBarState(
                seriesApi = komgaApi.seriesApi,
                bookApi = komgaApi.bookApi,
                appNotifications = dependencies.appNotifications,
                libraries = dependencies.komgaSharedState.libraries
            ),
            notificationsState = NotificationsState(
                komgaEvents = dependencies.komgaEvents.events,
                bookDownloadEvents = dependencies.offlineDependencies.bookDownloadEvents
            ),
            libraries = dependencies.komgaSharedState.libraries,
            offlineSettingsRepository = dependencies.offlineDependencies.repositories.offlineSettingsRepository,
            taskEmitter = dependencies.offlineDependencies.taskEmitter,
        )
    }

    fun getSeriesViewModel(
        seriesId: KomgaSeriesId,
        series: KomgaSeries? = null,
        defaultTab: SeriesTab? = null,
    ) = SeriesViewModel(
        seriesId = seriesId,
        series = series,
        libraries = dependencies.komgaSharedState.libraries,
        seriesApi = komgaApi.seriesApi,
        taskEmitter = dependencies.offlineDependencies.taskEmitter,
        bookApi = komgaApi.bookApi,
        collectionApi = komgaApi.collectionsApi,
        notifications = dependencies.appNotifications,
        events = dependencies.komgaEvents.events,
        settingsRepository = appRepositories.settingsRepository,
        referentialApi = komgaApi.referentialApi,
        defaultTab = defaultTab ?: SeriesTab.BOOKS,
    )

    fun getBookViewModel(bookId: KomgaBookId, book: VangaBook?): BookViewModel {
        return BookViewModel(
            book = book,
            bookId = bookId,
            bookApi = komgaApi.bookApi,
            notifications = dependencies.appNotifications,
            komgaEvents = dependencies.komgaEvents.events,
            libraries = dependencies.komgaSharedState.libraries,
            settingsRepository = appRepositories.settingsRepository,
            readListApi = komgaApi.readListApi,
            taskEmitter = dependencies.offlineDependencies.taskEmitter,
        )
    }

    fun getOneshotViewModel(
        seriesId: KomgaSeriesId,
        series: KomgaSeries? = null,
        book: VangaBook? = null,
    ) = OneshotViewModel(
        series = series,
        book = book,
        seriesId = seriesId,
        seriesApi = komgaApi.seriesApi,
        bookApi = komgaApi.bookApi,
        events = dependencies.komgaEvents.events,
        notifications = dependencies.appNotifications,
        libraries = dependencies.komgaSharedState.libraries,
        taskEmitter = dependencies.offlineDependencies.taskEmitter,
        settingsRepository = appRepositories.settingsRepository,
        readListApi = komgaApi.readListApi,
        collectionApi = komgaApi.collectionsApi,
    )

    fun getBookReaderViewModel(
        navigator: Navigator,
        markReadProgress: Boolean,
        bookSiblingsContext: BookSiblingsContext
    ): ReaderViewModel {
        return ReaderViewModel(
            bookApi = komgaApi.bookApi,
            seriesApi = komgaApi.seriesApi,
            readListApi = komgaApi.readListApi,
            navigator = navigator,
            appNotifications = dependencies.appNotifications,
            readerSettingsRepository = appRepositories.imageReaderSettingsRepository,
            imageLoader = dependencies.bookImageLoader,
            appStrings = dependencies.appStrings,
            readerImageFactory = dependencies.readerImageFactory,
            currentBookId = imageReaderCurrentBook,
            bookSiblingsContext = bookSiblingsContext,
            markReadProgress = markReadProgress,
        )
    }

    fun getLoginViewModel(): LoginViewModel {
        return LoginViewModel(
            settingsRepository = appRepositories.settingsRepository,
            secretsRepository = appRepositories.secretsRepository,
            komgaUserApi = dependencies.komgaApi.map { it.userApi },
            komgaLibraryApi = dependencies.komgaApi.map { it.libraryApi },
            komgaAuthState = dependencies.komgaSharedState,
            notifications = dependencies.appNotifications,
            platform = platformType,
            offlineUserRepository = dependencies.offlineDependencies.repositories.userRepository,
            offlineServerRepository = dependencies.offlineDependencies.repositories.mediaServerRepository,
            offlineSettingsRepository = dependencies.offlineDependencies.repositories.offlineSettingsRepository,
            offlineLibraryApi = dependencies.offlineDependencies.komgaApi.libraryApi,
        )
    }

    fun getLibraryEditDialogViewModel(library: KomgaLibrary?, onDismissRequest: () -> Unit) =
        LibraryEditDialogViewModel(
            library = library,
            onDialogDismiss = onDismissRequest,
            libraryApi = komgaApi.libraryApi,
            appNotifications = dependencies.appNotifications,
        )

    fun getSeriesEditDialogViewModel(series: KomgaSeries, onDismissRequest: () -> Unit) =
        SeriesEditDialogViewModel(
            series = series,
            onDialogDismiss = onDismissRequest,
            seriesApi = komgaApi.seriesApi,
            referentialApi = komgaApi.referentialApi,
            notifications = dependencies.appNotifications,
            cardWidth = getGridCardWidth(),
        )

    fun getSeriesBulkEditDialogViewModel(series: List<KomgaSeries>, onDismissRequest: () -> Unit) =
        SeriesBulkEditDialogViewModel(
            series = series,
            onDialogDismiss = onDismissRequest,
            seriesApi = komgaApi.seriesApi,
            referentialApi = komgaApi.referentialApi,
            notifications = dependencies.appNotifications,
        )

    fun getBookEditDialogViewModel(book: VangaBook, onDismissRequest: () -> Unit) =
        BookEditDialogViewModel(
            book = book,
            onDialogDismiss = onDismissRequest,
            bookApi = komgaApi.bookApi,
            referentialApi = komgaApi.referentialApi,
            notifications = dependencies.appNotifications,
            cardWidth = getGridCardWidth(),
        )

    fun getOneshotEditDialogViewModel(
        seriesId: KomgaSeriesId,
        series: KomgaSeries?,
        book: VangaBook?,
        onDismissRequest: () -> Unit
    ) = OneshotEditDialogViewModel(
        seriesId = seriesId,
        series = series,
        book = book,
        onDialogDismiss = onDismissRequest,
        bookApi = komgaApi.bookApi,
        seriesApi = komgaApi.seriesApi,
        referentialApi = komgaApi.referentialApi,
        notifications = dependencies.appNotifications,
        cardWidth = getGridCardWidth(),
    )

    fun getBookBulkEditDialogViewModel(books: List<VangaBook>, onDismissRequest: () -> Unit) =
        BookBulkEditDialogViewModel(
            books = books,
            onDialogDismiss = onDismissRequest,
            bookApi = komgaApi.bookApi,
            referentialApi = komgaApi.referentialApi,
            notifications = dependencies.appNotifications,
        )

    fun getCollectionEditDialogViewModel(
        collection: KomgaCollection,
        onDismissRequest: () -> Unit
    ) = CollectionEditDialogViewModel(
        collection = collection,
        onDialogDismiss = onDismissRequest,
        collectionApi = komgaApi.collectionsApi,
        notifications = dependencies.appNotifications,
        cardWidth = getGridCardWidth(),
    )

    fun getReadListEditDialogViewModel(readList: KomgaReadList, onDismissRequest: () -> Unit) =
        ReadListEditDialogViewModel(
            readList = readList,
            onDialogDismiss = onDismissRequest,
            readListApi = komgaApi.readListApi,
            notifications = dependencies.appNotifications,
            cardWidth = getGridCardWidth(),
        )

    fun getAddToCollectionDialogViewModel(series: List<KomgaSeries>, onDismissRequest: () -> Unit) =
        AddToCollectionDialogViewModel(
            series = series,
            onDismissRequest = onDismissRequest,
            collectionApi = komgaApi.collectionsApi,
            appNotifications = dependencies.appNotifications,
        )

    fun getAddToReadListDialogViewModel(books: List<VangaBook>, onDismissRequest: () -> Unit) =
        AddToReadListDialogViewModel(
            books = books,
            onDismissRequest = onDismissRequest,
            readListApi = komgaApi.readListApi,
            appNotifications = dependencies.appNotifications,
        )

    fun getFileBrowserDialogViewModel() =
        FileBrowserDialogViewModel(komgaApi.fileSystemApi, dependencies.appNotifications)


    fun getSearchViewModel() = SearchViewModel(
        seriesApi = komgaApi.seriesApi,
        bookApi = komgaApi.bookApi,
        appNotifications = dependencies.appNotifications,
        libraries = dependencies.komgaSharedState.libraries,
    )


    fun getAccountViewModel(): AccountSettingsViewModel {
        val user = requireNotNull(dependencies.komgaSharedState.authenticatedUser.value)
        return AccountSettingsViewModel(user)
    }

    fun getAuthenticationActivityViewModel(forMe: Boolean): AuthenticationActivityViewModel {
        return AuthenticationActivityViewModel(
            forMe,
            komgaApi.userApi,
            dependencies.appNotifications
        )
    }

    fun getUsersViewModel(): UsersViewModel {
        val user = requireNotNull(dependencies.komgaSharedState.authenticatedUser.value)
        return UsersViewModel(dependencies.appNotifications, komgaApi.userApi, user)
    }

    fun getPasswordChangeDialogViewModel(user: KomgaUser?) = PasswordChangeDialogViewModel(
        dependencies.appNotifications,
        komgaApi.userApi,
        user
    )

    fun getUserAddDialogViewModel(): UserAddDialogViewModel {
        return UserAddDialogViewModel(
            appNotifications = dependencies.appNotifications,
            userApi = komgaApi.userApi
        )
    }

    fun getUserEditDialogViewModel(user: KomgaUser): UserEditDialogViewModel {
        val libraries = requireNotNull(dependencies.komgaSharedState.libraries.value)
        return UserEditDialogViewModel(
            dependencies.appNotifications,
            user,
            libraries,
            komgaApi.userApi
        )
    }

    fun getServerSettingsViewModel(): ServerSettingsViewModel {
        return ServerSettingsViewModel(
            appNotifications = dependencies.appNotifications,
            settingsApi = komgaApi.settingsApi,
            bookApi = komgaApi.bookApi,
            libraryApi = komgaApi.libraryApi,
            libraries = dependencies.komgaSharedState.libraries,
            taskApi = komgaApi.tasksApi,
            actuatorApi = komgaApi.actuatorApi
        )
    }

    fun getAnnouncementsViewModel(): AnnouncementsViewModel {
        return AnnouncementsViewModel(dependencies.appNotifications, komgaApi.announcementsApi)
    }

    fun getSettingsNavigationViewModel(rootNavigator: Navigator): SettingsNavigationViewModel {
        return SettingsNavigationViewModel(
            rootNavigator = rootNavigator,
            appNotifications = dependencies.appNotifications,
            userApi = komgaApi.userApi,
            komgaSharedState = dependencies.komgaSharedState,
            secretsRepository = appRepositories.secretsRepository,
            offlineSettingsRepository = dependencies.offlineDependencies.repositories.offlineSettingsRepository,
            isOffline = dependencies.isOffline,
            currentServerUrl = appRepositories.settingsRepository.getServerUrl(),
            bookApi = komgaApi.bookApi,
            latestVersion = appRepositories.settingsRepository.getLastCheckedReleaseVersion(),
            komfEnabled = appRepositories.komfSettingsRepository.getKomfEnabled(),
            platformType = platformType,
            updatesEnabled = dependencies.appUpdater != null,
            user = dependencies.komgaSharedState.authenticatedUser,
        )
    }

    fun getAppearanceViewModel(): AppSettingsViewModel {
        return AppSettingsViewModel(appRepositories.settingsRepository)
    }

    fun getSettingsUpdatesViewModel(): AppUpdatesViewModel {
        return AppUpdatesViewModel(
            releases = releases,
            updater = dependencies.appUpdater,
            settings = appRepositories.settingsRepository,
            notifications = dependencies.appNotifications,
        )
    }

    fun getFavoriteSyncSettingsViewModel(): FavoriteSyncSettingsViewModel {
        val ownerLabelProvider = { dependencies.komgaSharedState.authenticatedUser.value?.email }
        val serverUrlProvider = { dependencies.komgaSharedState.serverUrl.value }
        return FavoriteSyncSettingsViewModel(
            settingsRepository = appRepositories.favoriteSyncSettingsRepository,
            syncService = createFavoriteSyncService(ownerLabelProvider, serverUrlProvider),
            notifications = dependencies.appNotifications,
            serverUrlProvider = serverUrlProvider,
            ownerLabelProvider = ownerLabelProvider,
        )
    }

    fun getCollectionViewModel(collectionId: KomgaCollectionId): CollectionViewModel {
        return CollectionViewModel(
            collectionId = collectionId,
            collectionApi = komgaApi.collectionsApi,
            notifications = dependencies.appNotifications,
            seriesApi = komgaApi.seriesApi,
            komgaEvents = dependencies.komgaEvents.events,
            cardWidthFlow = getGridCardWidth(),
            taskEmitter = dependencies.offlineDependencies.taskEmitter
        )
    }

    fun getReadListViewModel(readListId: KomgaReadListId): ReadListViewModel {
        return ReadListViewModel(
            readListId = readListId,
            readListApi = komgaApi.readListApi,
            bookApi = komgaApi.bookApi,
            taskEmitter = dependencies.offlineDependencies.taskEmitter,
            notifications = dependencies.appNotifications,
            komgaEvents = dependencies.komgaEvents.events,
            cardWidthFlow = getGridCardWidth()
        )
    }

    fun getMediaAnalysisViewModel(): MediaAnalysisViewModel {
        return MediaAnalysisViewModel(
            bookApi = komgaApi.bookApi,
            appNotifications = dependencies.appNotifications,
        )
    }

    fun getKomfSettingsViewModel(
        enableKavita: Boolean,
        integrationToggleEnabled: Boolean,
    ): KomfSettingsViewModel {
        return KomfSettingsViewModel(
            komfConfigClient = dependencies.komfClientFactory.configClient(),
            komgaMediaServerClient = dependencies.komfClientFactory.mediaServerClient(KOMGA),
            kavitaMediaServerClient = if (enableKavita) dependencies.komfClientFactory.mediaServerClient(KAVITA) else null,
            appNotifications = dependencies.appNotifications,
            settingsRepository = appRepositories.komfSettingsRepository,
            integrationToggleEnabled = integrationToggleEnabled,
            komfSharedState = komfSharedState,
        )
    }

    fun getKomfNotificationViewModel(): KomfNotificationSettingsViewModel {
        return KomfNotificationSettingsViewModel(
            komfConfigClient = dependencies.komfClientFactory.configClient(),
            komfNotificationClient = dependencies.komfClientFactory.notificationClient(),
            appNotifications = dependencies.appNotifications,
            komfConfig = komfSharedState
        )
    }

    fun getKomfProcessingViewModel(serverType: MediaServer): KomfProcessingSettingsViewModel {
        return KomfProcessingSettingsViewModel(
            komfConfigClient = dependencies.komfClientFactory.configClient(),
            appNotifications = dependencies.appNotifications,
            serverType = serverType,
            komfSharedState = komfSharedState
        )
    }

    fun getKomfProvidersViewModel(): KomfProvidersSettingsViewModel {
        return KomfProvidersSettingsViewModel(
            komfConfigClient = dependencies.komfClientFactory.configClient(),
            appNotifications = dependencies.appNotifications,
            komfSharedState = komfSharedState
        )
    }

    fun getKomfJobsViewModel(): KomfJobsViewModel {
        return KomfJobsViewModel(
            jobClient = dependencies.komfClientFactory.jobClient(),
            seriesApi = komgaApi.seriesApi,
            appNotifications = dependencies.appNotifications
        )
    }

    fun getKomfIdentifyDialogViewModel(
        series: KomgaSeries,
        onDismissRequest: () -> Unit
    ): KomfIdentifyDialogViewModel {
        return KomfIdentifyDialogViewModel(
            seriesId = KomfServerSeriesId(series.id.value),
            libraryId = KomfServerLibraryId(series.libraryId.value),
            seriesName = series.metadata.title,
            komfConfig = komfSharedState,
            komfMetadataClient = dependencies.komfClientFactory.metadataClient(KOMGA),
            komfJobClient = dependencies.komfClientFactory.jobClient(),
            appNotifications = dependencies.appNotifications,
            onDismiss = onDismissRequest,
        )
    }

    fun getKomfResetMetadataDialogViewModel(
        onDismissRequest: () -> Unit
    ): KomfResetMetadataDialogViewModel {
        return KomfResetMetadataDialogViewModel(
            komfMetadataClient = dependencies.komfClientFactory.metadataClient(KOMGA),
            appNotifications = dependencies.appNotifications,
            onDismiss = onDismissRequest,
        )
    }

    fun getKomfLibraryIdentifyViewModel(
        library: KomgaLibrary
    ): KomfLibraryIdentifyViewmodel {
        return KomfLibraryIdentifyViewmodel(
            libraryId = KomfServerLibraryId(library.id.value),
            komfMetadataClient = dependencies.komfClientFactory.metadataClient(KOMGA),
            appNotifications = dependencies.appNotifications,
        )
    }

    fun getEpubReaderViewModel(
        bookId: KomgaBookId,
        bookSiblingsContext: BookSiblingsContext,
        book: VangaBook? = null,
        markReadProgress: Boolean = true
    ): EpubReaderViewModel {
        return EpubReaderViewModel(
            bookId = bookId,
            book = book,
            markReadProgress = markReadProgress,
            bookApi = komgaApi.bookApi,
            seriesApi = komgaApi.seriesApi,
            readListApi = komgaApi.readListApi,
            settingsRepository = appRepositories.settingsRepository,
            epubSettingsRepository = appRepositories.epubReaderSettingsRepository,
            fontsRepository = appRepositories.fontsRepository,
            notifications = dependencies.appNotifications,
            windowState = dependencies.windowState,
            platformType = platformType,
            bookSiblingsContext = bookSiblingsContext,
        )
    }

    fun getEpubReaderSettingsViewModel(): EpubReaderSettingsViewModel {
        return EpubReaderSettingsViewModel(appRepositories.epubReaderSettingsRepository)
    }

    fun getSeriesBulkActions() = SeriesBulkActions(
        seriesApi = komgaApi.seriesApi,
        komfClient = dependencies.komfClientFactory.metadataClient(KOMGA),
        taskEmitter = dependencies.offlineDependencies.taskEmitter,
        notifications = dependencies.appNotifications,
    )

    fun getCollectionBulkActions() = CollectionBulkActions(
        komgaApi.collectionsApi,
        dependencies.appNotifications,
    )

    fun getBookBulkActions() = BookBulkActions(
        bookApi = komgaApi.bookApi,
        taskEmitter = dependencies.offlineDependencies.taskEmitter,
        notifications = dependencies.appNotifications
    )

    fun getReadListBulkActions() = ReadListBulkActions(
        komgaApi.readListApi,
        dependencies.appNotifications,
    )

    fun getFavoriteBulkActions(): FavoriteBulkActions {
        val ownerLabelProvider = { dependencies.komgaSharedState.authenticatedUser.value?.email }
        val serverUrlProvider = { dependencies.komgaSharedState.serverUrl.value }
        return FavoriteBulkActions(
            favoriteCollectionService = FavoriteCollectionService(
                localFavoritesRepository = appRepositories.localFavoritesRepository,
                ownerLabelProvider = ownerLabelProvider,
                serverUrlProvider = serverUrlProvider,
            ),
            favoriteReadListService = FavoriteReadListService(
                localFavoritesRepository = appRepositories.localFavoritesRepository,
                ownerLabelProvider = ownerLabelProvider,
                serverUrlProvider = serverUrlProvider,
            ),
            favoriteSyncService = createFavoriteSyncService(ownerLabelProvider, serverUrlProvider),
            notifications = dependencies.appNotifications,
            onFavoritesChanged = { screenReloadEvents.tryEmit(Unit) },
        )
    }

    fun getImageReaderSettingsViewModel(): ImageReaderSettingsViewModel {
        return ImageReaderSettingsViewModel(
            settingsRepository = appRepositories.imageReaderSettingsRepository,
            appNotifications = dependencies.appNotifications,

            coilMemoryCache = dependencies.coilImageLoader.memoryCache,
            coilDiskCache = dependencies.coilImageLoader.diskCache,
            readerDiskCache = dependencies.bookImageLoader.diskCache,
        )
    }

    fun getOfflineModeSettingsViewModel(): OfflineSettingsViewModel {
        return OfflineSettingsViewModel(
            authState = dependencies.komgaSharedState,
            appNotifications = dependencies.appNotifications,
            offlineSettingsRepository = dependencies.offlineDependencies.repositories.offlineSettingsRepository,
            userRepository = dependencies.offlineDependencies.repositories.userRepository,
            serverRepository = dependencies.offlineDependencies.repositories.mediaServerRepository,
            logJournalRepository = dependencies.offlineDependencies.repositories.logJournalRepository,
            serverDeleteAction = dependencies.offlineDependencies.actions.get(),
            userDeleteAction = dependencies.offlineDependencies.actions.get(),
            platformContext = dependencies.coilContext,

            taskEmitter = dependencies.offlineDependencies.taskEmitter,
            downloadEvents = dependencies.offlineDependencies.bookDownloadEvents
        )
    }

    fun getOfflineLoginViewModel(): OfflineLoginViewModel {
        return OfflineLoginViewModel(
            appNotifications = dependencies.appNotifications,
            offlineSettingsRepository = dependencies.offlineDependencies.repositories.offlineSettingsRepository,
            userRepository = dependencies.offlineDependencies.repositories.userRepository,
            serverRepository = dependencies.offlineDependencies.repositories.mediaServerRepository,
            komgaAuthState = dependencies.komgaSharedState,
            offlineLibraryApi = dependencies.offlineDependencies.komgaApi.libraryApi,
            serverDeleteAction = dependencies.offlineDependencies.actions.get(),
            userDeleteAction = dependencies.offlineDependencies.actions.get(),
        )
    }

    fun getStartupUpdateChecker() = startupUpdateChecker

    fun getLibraries(): StateFlow<List<KomgaLibrary>> = dependencies.komgaSharedState.libraries

    private fun getLibraryFlow(id: KomgaLibraryId?): Flow<KomgaLibrary?> {
        if (id == null) return flowOf(null)
        return dependencies.komgaSharedState.libraries.map { libraries -> libraries.firstOrNull { it.id == id } }
    }

    private fun getGridCardWidth(): Flow<Dp> {
        return appRepositories.settingsRepository.getCardWidth().map { it.dp }
    }
}
