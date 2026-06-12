package io.github.vivitoto.vanga.ui.reader.image

import androidx.compose.ui.unit.IntSize
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import io.github.vivitoto.vanga.AppNotifications
import io.github.vivitoto.vanga.image.BookImageLoader
import io.github.vivitoto.vanga.image.ReaderImageFactory
import io.github.vivitoto.vanga.komga.api.KomgaBookApi
import io.github.vivitoto.vanga.komga.api.KomgaReadListApi
import io.github.vivitoto.vanga.komga.api.KomgaSeriesApi
import io.github.vivitoto.vanga.settings.ImageReaderSettingsRepository
import io.github.vivitoto.vanga.settings.model.ReaderType.CONTINUOUS
import io.github.vivitoto.vanga.settings.model.ReaderType.PAGED
import io.github.vivitoto.vanga.ui.BookSiblingsContext
import io.github.vivitoto.vanga.ui.LoadState
import io.github.vivitoto.vanga.ui.reader.image.continuous.ContinuousReaderState
import io.github.vivitoto.vanga.ui.reader.image.paged.PagedReaderState
import io.github.vivitoto.vanga.ui.strings.AppStrings
import snd.komga.client.book.KomgaBookId

private val cleanupScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
class ReaderViewModel(
    bookApi: KomgaBookApi,
    seriesApi: KomgaSeriesApi,
    readListApi: KomgaReadListApi,
    navigator: Navigator,
    appNotifications: AppNotifications,
    readerSettingsRepository: ImageReaderSettingsRepository,
    imageLoader: BookImageLoader,
    appStrings: Flow<AppStrings>,
    readerImageFactory: ReaderImageFactory,
    markReadProgress: Boolean,
    currentBookId: MutableStateFlow<KomgaBookId?>,
    bookSiblingsContext: BookSiblingsContext,
) : ScreenModel {
    val screenScaleState = ScreenScaleState()
    private val pageChangeFlow = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val readerState: ReaderState = ReaderState(
        bookApi = bookApi,
        seriesApi = seriesApi,
        readListApi = readListApi,
        navigator = navigator,
        appNotifications = appNotifications,
        readerSettingsRepository = readerSettingsRepository,
        currentBookId = currentBookId,
        markReadProgress = markReadProgress,
        stateScope = screenModelScope,
        bookSiblingsContext = bookSiblingsContext,
        pageChangeFlow = pageChangeFlow,
    )

    val pagedReaderState = PagedReaderState(
        cleanupScope = cleanupScope,
        readerState = readerState,
        appNotifications = appNotifications,
        settingsRepository = readerSettingsRepository,
        imageLoader = imageLoader,
        appStrings = appStrings,
        pageChangeFlow = pageChangeFlow,
        screenScaleState = screenScaleState,
    )
    val continuousReaderState = ContinuousReaderState(
        cleanupScope = cleanupScope,
        readerState = readerState,
        imageLoader = imageLoader,
        settingsRepository = readerSettingsRepository,
        notifications = appNotifications,
        appStrings = appStrings,
        readerImageFactory = readerImageFactory,
        pageChangeFlow = pageChangeFlow,
        screenScaleState = screenScaleState,
    )

    suspend fun initialize(bookId: KomgaBookId) {
        val currentState = readerState.state.value
        if (currentState is LoadState.Success || currentState == LoadState.Loading) return

        readerState.initialize(bookId)
        screenScaleState.areaSize.takeWhile { it == IntSize.Zero }.collect()

        readerState.readerType.onEach {
            stopAllReaderModeStates()
            when (it) {
                PAGED -> pagedReaderState.initialize()
                CONTINUOUS -> continuousReaderState.initialize()
            }
        }.launchIn(screenModelScope)
    }

    private fun stopAllReaderModeStates() {
        pagedReaderState.stop()
        continuousReaderState.stop()

    }

    override fun onDispose() {
        stopAllReaderModeStates()
        readerState.onDispose()
    }
}
