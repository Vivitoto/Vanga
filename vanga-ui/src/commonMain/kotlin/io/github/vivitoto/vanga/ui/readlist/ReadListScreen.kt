package io.github.vivitoto.vanga.ui.readlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.vivitoto.vanga.ui.BookSiblingsContext
import io.github.vivitoto.vanga.ui.LoadState
import io.github.vivitoto.vanga.ui.LoadState.Error
import io.github.vivitoto.vanga.ui.LoadState.Loading
import io.github.vivitoto.vanga.ui.LoadState.Uninitialized
import io.github.vivitoto.vanga.ui.LocalReloadEvents
import io.github.vivitoto.vanga.ui.LocalViewModelFactory
import io.github.vivitoto.vanga.ui.ReloadableScreen
import io.github.vivitoto.vanga.ui.book.bookScreen
import io.github.vivitoto.vanga.ui.common.components.ErrorContent
import io.github.vivitoto.vanga.ui.common.components.LoadingMaxSizeIndicator
import io.github.vivitoto.vanga.ui.platform.BackPressHandler
import io.github.vivitoto.vanga.ui.platform.ScreenPullToRefreshBox
import io.github.vivitoto.vanga.ui.reader.readerScreen
import snd.komga.client.readlist.KomgaReadListId

class ReadListScreen(val readListId: KomgaReadListId) : ReloadableScreen {

    override val key: ScreenKey = readListId.toString()

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel(readListId.value) { viewModelFactory.getReadListViewModel(readListId) }
        val reloadEvents = LocalReloadEvents.current
        LaunchedEffect(readListId) {
            vm.initialize()
            reloadEvents.collect { vm.reload() }
        }
        DisposableEffect(Unit) {
            vm.startKomgaEventHandler()
            onDispose { vm.stopKomgaEventHandler() }
        }

        val navigator = LocalNavigator.currentOrThrow

        ScreenPullToRefreshBox(screenState = vm.state, onRefresh = vm::reload) {
            when (val state = vm.state.collectAsState().value) {
                Uninitialized -> LoadingMaxSizeIndicator()
                is Error -> ErrorContent(
                    message = state.exception.message ?: "未知错误",
                    onReload = vm::reload
                )

                is LoadState.Success, Loading -> {
                    val readList = vm.readList
                    if (readList == null) LoadingMaxSizeIndicator()
                    else
                        ReadListContent(
                            readList = readList,
                            onReadListDelete = vm::onReadListDelete,

                            books = vm.books,
                            bookMenuActions = vm.bookMenuActions(),
                            onBookClick = { navigator push bookScreen(it, BookSiblingsContext.ReadList(readListId)) },
                            onBookReadClick = { book, markProgress ->
                                navigator.parent?.push(
                                    readerScreen(
                                        book = book,
                                        markReadProgress = markProgress,
                                        bookSiblingsContext = BookSiblingsContext.ReadList(readListId)
                                    )
                                )
                            },

                            selectedBooks = vm.selectedBooks,
                            onBookSelect = vm::onBookSelect,

                            editMode = vm.isInEditMode,
                            onEditModeChange = vm::setEditMode,
                            onReorder = vm::onBookReorder,
                            onReorderDragStateChange = vm::onSeriesReorderDragStateChange,

                            totalPages = vm.totalBookPages,
                            currentPage = vm.currentBookPage,
                            pageSize = vm.pageLoadSize,
                            onPageChange = vm::onPageChange,
                            onPageSizeChange = vm::onPageSizeChange,

                            cardMinSize = vm.cardWidth.collectAsState().value,
                        )
                }
            }

            BackPressHandler { navigator.pop() }

        }
    }
}