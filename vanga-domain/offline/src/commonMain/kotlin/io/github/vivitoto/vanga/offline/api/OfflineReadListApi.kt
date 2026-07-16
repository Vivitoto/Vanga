package io.github.vivitoto.vanga.offline.api

import io.github.vivitoto.vanga.komga.api.KomgaReadListApi
import io.github.vivitoto.vanga.komga.api.model.VangaBook
import io.github.vivitoto.vanga.offline.offlineUnsupported
import snd.komga.client.book.KomgaBookId
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.KomgaThumbnailId
import snd.komga.client.common.Page
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.readlist.KomgaReadList
import snd.komga.client.readlist.KomgaReadListCreateRequest
import snd.komga.client.readlist.KomgaReadListId
import snd.komga.client.readlist.KomgaReadListQuery
import snd.komga.client.readlist.KomgaReadListThumbnail
import snd.komga.client.readlist.KomgaReadListUpdateRequest

class OfflineReadListApi : KomgaReadListApi {
    override suspend fun getAll(
        search: String?,
        libraryIds: List<KomgaLibraryId>?,
        pageRequest: KomgaPageRequest?
    ): Page<KomgaReadList> {
        return Page.empty()
    }

    override suspend fun getOne(id: KomgaReadListId): KomgaReadList {
        offlineUnsupported("查看离线阅读清单详情")
    }

    override suspend fun addOne(request: KomgaReadListCreateRequest): KomgaReadList {
        offlineUnsupported("新增离线阅读清单")
    }

    override suspend fun updateOne(
        id: KomgaReadListId,
        request: KomgaReadListUpdateRequest
    ) {
    }

    override suspend fun deleteOne(id: KomgaReadListId) {
    }

    override suspend fun getBooksForReadList(
        id: KomgaReadListId,
        query: KomgaReadListQuery?,
        pageRequest: KomgaPageRequest?
    ): Page<VangaBook> {
        return Page.empty()
    }

    override suspend fun getDefaultThumbnail(readListId: KomgaReadListId): ByteArray? {
        offlineUnsupported("获取离线阅读清单默认缩略图")
    }

    override suspend fun getThumbnail(
        readListId: KomgaReadListId,
        thumbnailId: KomgaThumbnailId
    ): ByteArray {
        offlineUnsupported("获取离线阅读清单缩略图")
    }

    override suspend fun getThumbnails(readListId: KomgaReadListId): List<KomgaReadListThumbnail> {
        return emptyList()
    }

    override suspend fun uploadThumbnail(
        readListId: KomgaReadListId,
        file: ByteArray,
        filename: String,
        selected: Boolean
    ): KomgaReadListThumbnail {
        offlineUnsupported("上传离线阅读清单缩略图")
    }

    override suspend fun selectThumbnail(
        readListId: KomgaReadListId,
        thumbnailId: KomgaThumbnailId
    ) {
    }

    override suspend fun deleteThumbnail(
        readListId: KomgaReadListId,
        thumbnailId: KomgaThumbnailId
    ) {
    }

    override suspend fun getBookSiblingNext(
        readListId: KomgaReadListId,
        bookId: KomgaBookId
    ): VangaBook {
        offlineUnsupported("获取离线阅读清单下一本")
    }

    override suspend fun getBookSiblingPrevious(
        readListId: KomgaReadListId,
        bookId: KomgaBookId
    ): VangaBook {
        offlineUnsupported("获取离线阅读清单上一本")
    }
}
