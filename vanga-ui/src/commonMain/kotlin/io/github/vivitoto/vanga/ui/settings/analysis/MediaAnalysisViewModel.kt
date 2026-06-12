package io.github.vivitoto.vanga.ui.settings.analysis

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import io.github.vivitoto.vanga.AppNotifications
import io.github.vivitoto.vanga.komga.api.KomgaBookApi
import io.github.vivitoto.vanga.komga.api.model.VangaBook
import io.github.vivitoto.vanga.ui.LoadState
import snd.komga.client.book.KomgaMediaStatus
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.search.allOfBooks

class MediaAnalysisViewModel(
    private val bookApi: KomgaBookApi,
    private val appNotifications: AppNotifications
) : StateScreenModel<LoadState<Unit>>(LoadState.Uninitialized) {

    var books by mutableStateOf<List<VangaBook>>(emptyList())
    var currentPage by mutableStateOf(1)
        private set
    var totalPages by mutableStateOf(1)
        private set
    private val pageLoadSize by mutableStateOf(20)

    fun initialize() {
        if (state.value !is LoadState.Uninitialized) return
        loadPage(1)
    }

    fun loadPage(page: Int) {
        screenModelScope.launch {
            appNotifications.runCatchingToNotifications {
                mutableState.value = LoadState.Loading
                val pageResponse = bookApi.getBookList(
                    conditionBuilder = allOfBooks {
                        mediaStatus { isEqualTo(KomgaMediaStatus.ERROR) }
                        mediaStatus { isEqualTo(KomgaMediaStatus.UNSUPPORTED) }
                    },
//                    KomgaBookQuery(mediaStatus = listOf(KomgaMediaStatus.ERROR, KomgaMediaStatus.UNSUPPORTED)),
                    pageRequest = KomgaPageRequest(pageIndex = page - 1, size = pageLoadSize)
                )

                books = pageResponse.content
                currentPage = pageResponse.number + 1
                totalPages = pageResponse.totalPages
                mutableState.value = LoadState.Success(Unit)

            }.onFailure { mutableState.value = LoadState.Error(it) }
        }
    }
}