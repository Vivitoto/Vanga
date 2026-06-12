package io.github.vivitoto.vanga.offline.api.repository

import io.github.vivitoto.vanga.komga.api.model.VangaBook
import snd.komga.client.book.KomgaBookId
import snd.komga.client.book.KomgaBookSearch
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.Page
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.user.KomgaUserId

interface OfflineBookDtoRepository {
    suspend fun findAll(
        userId: KomgaUserId,
        pageRequest: KomgaPageRequest,
    ): Page<VangaBook>

    suspend fun findAll(
        userId: KomgaUserId,
        search: KomgaBookSearch,
        pageRequest: KomgaPageRequest,
    ): Page<VangaBook>

    suspend fun get(
        bookId: KomgaBookId,
        userId: KomgaUserId,
    ): VangaBook

    suspend fun findByIdOrNull(
        bookId: KomgaBookId,
        userId: KomgaUserId,
    ): VangaBook?

    suspend fun findPreviousInSeriesOrNull(
        bookId: KomgaBookId,
        userId: KomgaUserId,
    ): VangaBook?

    suspend fun findNextInSeriesOrNull(
        bookId: KomgaBookId,
        userId: KomgaUserId,
    ): VangaBook?

//    fun findPreviousInReadListOrNull(
//        readList: ReadList,
//        bookId: String,
//        userId: String,
//        filterOnLibraryIds: Collection<String>?,
////        restrictions: ContentRestrictions = ContentRestrictions(),
//    ): VangaBook?
//
//    fun findNextInReadListOrNull(
//        readList: ReadList,
//        bookId: String,
//        userId: String,
//        filterOnLibraryIds: Collection<String>?,
//        restrictions: ContentRestrictions = ContentRestrictions(),
//    ): BookDto?

    suspend fun findAllOnDeck(
        userId: KomgaUserId,
        filterOnLibraryIds: Collection<KomgaLibraryId>?,
        pageRequest: KomgaPageRequest,
//        restrictions: ContentRestrictions = ContentRestrictions(),
    ): Page<VangaBook>

//    fun findAllDuplicates(
//        userId: KomgaUserId,
//        pageable: KomgaPageRequest,
//    ): Page<VangaBook>

}