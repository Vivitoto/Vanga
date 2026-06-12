package io.github.vivitoto.vanga.ui.reader

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.vivitoto.vanga.komga.api.model.VangaBook
import io.github.vivitoto.vanga.ui.BookSiblingsContext
import io.github.vivitoto.vanga.ui.LoadState
import io.github.vivitoto.vanga.ui.LocalViewModelFactory
import io.github.vivitoto.vanga.ui.LocalWindowState
import io.github.vivitoto.vanga.ui.MainScreen
import io.github.vivitoto.vanga.ui.book.BookScreen
import io.github.vivitoto.vanga.ui.book.bookScreen
import io.github.vivitoto.vanga.ui.common.components.ErrorContent
import io.github.vivitoto.vanga.ui.common.components.LoadingMaxSizeIndicator
import io.github.vivitoto.vanga.ui.platform.PlatformTitleBar
import io.github.vivitoto.vanga.ui.platform.canIntegrateWithSystemBar
import io.github.vivitoto.vanga.ui.reader.epub.EpubContent
import snd.komga.client.book.KomgaBookId
import snd.komga.client.book.MediaProfile
import kotlin.jvm.Transient

class EpubScreen(
    private val bookId: KomgaBookId,
    private val bookSiblingsContext: BookSiblingsContext,
    private val markReadProgress: Boolean = true,
    @Transient
    private val book: VangaBook? = null,
) : Screen {

    override val key: ScreenKey = bookId.value

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel(bookId.value) {
            viewModelFactory.getEpubReaderViewModel(
                bookId = bookId,
                bookSiblingsContext = bookSiblingsContext,
                book = book,
                markReadProgress = markReadProgress
            )
        }
        LaunchedEffect(bookId) {
            vm.initialize(navigator)
            val state = vm.state.value
            if (state is LoadState.Success) {
                val book = state.value.book.value
                if (book != null && book.media.mediaProfile != MediaProfile.EPUB) {
                    navigator.replace(readerScreen(book, markReadProgress))
                }
            }
        }

        val state = vm.state.collectAsState().value
        Column {
            PlatformTitleBar(applyInsets = false) {
                if (canIntegrateWithSystemBar()) {
                    val isFullscreen = LocalWindowState.current.isFullscreen.collectAsState(false)
                    if (state is LoadState.Success && !isFullscreen.value) {
                        val book = state.value.book.collectAsState().value
                        TitleBarContent(
                            title = book?.metadata?.title ?: "",
                            onExit = { state.value.closeWebview() },
                            favoriteBookId = book?.id,
                        )
                    }
                }
            }
            when (state) {
                LoadState.Loading, LoadState.Uninitialized -> LoadingMaxSizeIndicator()
                is LoadState.Error -> ErrorContent(
                    message = state.exception.message ?: state.exception.stackTraceToString(),
                    onExit = {
                        val screen = book?.let { bookScreen(book = it, bookSiblingsContext = bookSiblingsContext) }
                            ?: BookScreen(bookId = bookId, bookSiblingsContext = bookSiblingsContext)

                        navigator.replaceAll(MainScreen(screen))
                    }
                )

                is LoadState.Success -> EpubContent(
                    onWebviewCreated = { state.value.onWebviewCreated(it) },
                    onBackButtonPress = state.value::onBackButtonPress
                )
            }
        }
    }

}