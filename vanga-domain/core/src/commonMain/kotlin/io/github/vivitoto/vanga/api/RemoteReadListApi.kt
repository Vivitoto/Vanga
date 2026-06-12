package io.github.vivitoto.vanga.api

import io.github.vivitoto.vanga.komga.api.KomgaReadListApi
import io.github.vivitoto.vanga.komga.api.model.VangaBook
import io.github.vivitoto.vanga.offline.book.repository.OfflineBookRepository
import snd.komga.client.book.KomgaBookId
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.KomgaThumbnailId
import snd.komga.client.common.Page
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.readlist.KomgaReadListClient
import snd.komga.client.readlist.KomgaReadListCreateRequest
import snd.komga.client.readlist.KomgaReadListId
import snd.komga.client.readlist.KomgaReadListQuery
import snd.komga.client.readlist.KomgaReadListUpdateRequest

class RemoteReadListApi(
    private val readListClient: KomgaReadListClient,
    private val offlineBookRepository: OfflineBookRepository,
) : KomgaReadListApi {
    override suspend fun getAll(
        search: String?,
        libraryIds: List<KomgaLibraryId>?,
        pageRequest: KomgaPageRequest?
    ) = readListClient.getAll(search, libraryIds, pageRequest)

    override suspend fun getOne(id: KomgaReadListId) = readListClient.getOne(id)

    override suspend fun addOne(request: KomgaReadListCreateRequest) = readListClient.addOne(request)

    override suspend fun updateOne(
        id: KomgaReadListId,
        request: KomgaReadListUpdateRequest
    ) = readListClient.updateOne(id, request)

    override suspend fun deleteOne(id: KomgaReadListId) = readListClient.deleteOne(id)

    override suspend fun getBooksForReadList(
        id: KomgaReadListId,
        query: KomgaReadListQuery?,
        pageRequest: KomgaPageRequest?
    ): Page<VangaBook> {
        val bookPage = readListClient.getBooksForReadList(id, query, pageRequest)
        val offlineBooks = offlineBookRepository.findIn(bookPage.content.map { it.id }).associateBy { it.id }
        val vangaBooks = bookPage.content.map {
            val offlineBook = offlineBooks[it.id]
            VangaBook(
                book = it,
                downloaded = offlineBook != null,
                localFileLastModified = offlineBook?.localFileLastModified,
                remoteFileUnavailable = offlineBook?.remoteUnavailable ?: false
            )
        }
        return bookPage.toVangaBookPage(vangaBooks)
    }

    override suspend fun getDefaultThumbnail(readListId: KomgaReadListId) =
        readListClient.getDefaultThumbnail(readListId)

    override suspend fun getThumbnail(readListId: KomgaReadListId, thumbnailId: KomgaThumbnailId) =
        readListClient.getThumbnail(readListId, thumbnailId)

    override suspend fun getThumbnails(readListId: KomgaReadListId) =
        readListClient.getThumbnails(readListId)

    override suspend fun uploadThumbnail(
        readListId: KomgaReadListId,
        file: ByteArray,
        filename: String,
        selected: Boolean
    ) = readListClient.uploadThumbnail(readListId, file, filename, selected)

    override suspend fun selectThumbnail(
        readListId: KomgaReadListId,
        thumbnailId: KomgaThumbnailId
    ) = readListClient.selectThumbnail(readListId, thumbnailId)

    override suspend fun deleteThumbnail(
        readListId: KomgaReadListId,
        thumbnailId: KomgaThumbnailId
    ) = readListClient.deleteThumbnail(readListId, thumbnailId)

    override suspend fun getBookSiblingNext(
        readListId: KomgaReadListId,
        bookId: KomgaBookId
    ): VangaBook? {
        val book = readListClient.getBookSiblingNext(readListId, bookId)
        return book?.let {
            val offlineBook = offlineBookRepository.find(it.id)
            VangaBook(
                book = book,
                downloaded = offlineBook != null,
                localFileLastModified = offlineBook?.localFileLastModified,
                remoteFileUnavailable = offlineBook?.remoteUnavailable ?: false
            )
        }
    }

    override suspend fun getBookSiblingPrevious(
        readListId: KomgaReadListId,
        bookId: KomgaBookId
    ): VangaBook? {
        val book = readListClient.getBookSiblingPrevious(readListId, bookId)
        return book?.let {
            val offlineBook = offlineBookRepository.find(it.id)
            VangaBook(
                book = book,
                downloaded = offlineBook != null,
                localFileLastModified = offlineBook?.localFileLastModified,
                remoteFileUnavailable = offlineBook?.remoteUnavailable ?: false
            )
        }
    }
}