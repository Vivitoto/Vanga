package io.github.vivitoto.vanga.ui.book

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.vivitoto.vanga.komga.api.model.VangaBook
import io.github.vivitoto.vanga.ui.BookSiblingsContext
import io.github.vivitoto.vanga.ui.LocalReloadEvents
import io.github.vivitoto.vanga.ui.LocalViewModelFactory
import io.github.vivitoto.vanga.ui.ReloadableScreen
import io.github.vivitoto.vanga.ui.library.LibraryScreen
import io.github.vivitoto.vanga.ui.oneshot.OneshotScreen
import io.github.vivitoto.vanga.ui.platform.BackPressHandler
import io.github.vivitoto.vanga.ui.platform.ScreenPullToRefreshBox
import io.github.vivitoto.vanga.ui.reader.ImageReaderScreen
import io.github.vivitoto.vanga.ui.reader.readerScreen
import io.github.vivitoto.vanga.ui.readlist.ReadListScreen
import io.github.vivitoto.vanga.ui.series.SeriesScreen
import snd.komga.client.book.KomgaBookId
import snd.komga.client.series.KomgaSeriesId
import kotlin.jvm.Transient

fun bookScreen(
    book: VangaBook,
    bookSiblingsContext: BookSiblingsContext? = null
): Screen {
    val context = bookSiblingsContext ?: BookSiblingsContext.Series
    return if (book.oneshot) OneshotScreen(book, context)
    else BookScreen(
        book = book,
        bookSiblingsContext = context
    )
}

class BookScreen(
    val bookId: KomgaBookId,
    private val bookSiblingsContext: BookSiblingsContext,
    @Transient
    val book: VangaBook? = null,
) : ReloadableScreen {
    constructor(book: VangaBook, bookSiblingsContext: BookSiblingsContext) : this(book.id, bookSiblingsContext, book)

    override val key: ScreenKey = bookId.toString()

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel(bookId.value) { viewModelFactory.getBookViewModel(bookId, book) }
        val navigator = LocalNavigator.currentOrThrow
        val reloadEvents = LocalReloadEvents.current

        LaunchedEffect(Unit) {
            vm.initialize()
            vm.book.value?.let { if (it.oneshot) navigator.replace(OneshotScreen(it, bookSiblingsContext)) }
            reloadEvents.collect { vm.reload() }
        }
        DisposableEffect(Unit) {
            vm.startKomgaEventsHandler()
            onDispose { vm.stopKomgaEventHandler() }
        }

        val book = vm.book.collectAsState().value

        ScreenPullToRefreshBox(
            screenState = vm.state,
            onRefresh = vm::reload
        ) {
            BookScreenContent(
                library = vm.library,
                book = book,
                bookMenuActions = vm.bookMenuActions,
                onBookReadPress = { markReadProgress ->
                    navigator.parent?.push(
                        if (book != null) readerScreen(
                            book = book,
                            bookSiblingsContext = bookSiblingsContext,
                            markReadProgress = markReadProgress
                        )
                        else ImageReaderScreen(
                            bookId = bookId,
                            bookSiblingsContext = bookSiblingsContext,
                            markReadProgress = markReadProgress
                        )
                    )
                },
                onBookDownload = vm::onBookDownload,
                onBookDownloadDelete = vm::onBookDownloadDelete,

                readLists = vm.readListsState.readLists,
                onReadListClick = { navigator.push(ReadListScreen(it.id)) },
                onReadListBookPress = { book, readList ->
                    if (book.id != bookId) navigator.push(
                        bookScreen(
                            book = book,
                            bookSiblingsContext = BookSiblingsContext.ReadList(readList.id)
                        )
                    )
                },
                onParentSeriesPress = { book?.seriesId?.let { seriesId -> navigator.push(SeriesScreen(seriesId)) } },
                onFilterClick = { filter ->
                    navigator.push(LibraryScreen(requireNotNull(book?.libraryId), filter))
                },
                cardWidth = vm.cardWidth.collectAsState().value,
            )

            BackPressHandler { onBackPress(navigator, book?.seriesId) }
        }
    }

    private fun onBackPress(navigator: Navigator, seriesId: KomgaSeriesId?) {
        if (navigator.canPop) {
            navigator.pop()
        } else {
            when (val context = bookSiblingsContext) {
                is BookSiblingsContext.ReadList -> navigator.replace(ReadListScreen(context.id))
                BookSiblingsContext.Series -> seriesId?.let { navigator.replace(SeriesScreen(it)) }
            }

        }
    }

}