package io.github.vivitoto.vanga.offline

import io.github.vivitoto.vanga.offline.book.actions.BookAnalyzeAction
import io.github.vivitoto.vanga.offline.book.actions.BookMetadataRefreshAction
import io.github.vivitoto.vanga.offline.api.OfflineCollectionsApi
import io.github.vivitoto.vanga.offline.api.OfflineReadListApi
import io.github.vivitoto.vanga.offline.library.actions.LibraryAnalyzeAction
import io.github.vivitoto.vanga.offline.series.actions.SeriesAnalyzeAction
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import snd.komga.client.book.KomgaBookId
import snd.komga.client.collection.KomgaCollectionId
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.readlist.KomgaReadListId
import snd.komga.client.series.KomgaSeriesId

class OfflineUnsupportedActionsTest {

    @Test
    fun bookAnalyzeThrowsExplicitOfflineUnsupportedError() {
        assertOfflineUnsupported("分析单本") {
            BookAnalyzeAction().run(KomgaBookId("book-id"))
        }
    }

    @Test
    fun bookMetadataRefreshThrowsExplicitOfflineUnsupportedError() {
        assertOfflineUnsupported("刷新单本元数据") {
            BookMetadataRefreshAction().run(KomgaBookId("book-id"))
        }
    }

    @Test
    fun seriesAnalyzeThrowsExplicitOfflineUnsupportedError() {
        assertOfflineUnsupported("分析系列") {
            SeriesAnalyzeAction().run(KomgaSeriesId("series-id"))
        }
    }

    @Test
    fun libraryAnalyzeThrowsExplicitOfflineUnsupportedError() {
        assertOfflineUnsupported("分析库") {
            LibraryAnalyzeAction().run(KomgaLibraryId("library-id"))
        }
    }

    @Test
    fun collectionDetailThrowsExplicitOfflineUnsupportedError() {
        assertOfflineUnsupported("查看离线合集详情") {
            OfflineCollectionsApi().getOne(KomgaCollectionId("collection-id"))
        }
    }

    @Test
    fun readListDetailThrowsExplicitOfflineUnsupportedError() {
        assertOfflineUnsupported("查看离线阅读清单详情") {
            OfflineReadListApi().getOne(KomgaReadListId("read-list-id"))
        }
    }

    private fun assertOfflineUnsupported(
        operation: String,
        action: suspend () -> Unit,
    ) {
        var failure: Throwable? = null
        action.startCoroutine(
            object : Continuation<Unit> {
                override val context = EmptyCoroutineContext

                override fun resumeWith(result: Result<Unit>) {
                    failure = result.exceptionOrNull()
                }
            },
        )

        val exception = assertNotNull(failure, "Expected action to throw OfflineUnsupportedOperationException")
        assertTrue(
            exception is OfflineUnsupportedOperationException,
            "Expected OfflineUnsupportedOperationException, got ${exception::class.simpleName}: ${exception.message}",
        )
        val message = exception.message.orEmpty()
        assertTrue(message.contains("离线模式暂不支持"), message)
        assertTrue(message.contains(operation), message)
    }
}
