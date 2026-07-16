package io.github.vivitoto.vanga.offline.api

import io.github.vivitoto.vanga.komga.api.KomgaCollectionsApi
import io.github.vivitoto.vanga.offline.offlineUnsupported
import snd.komga.client.collection.KomgaCollection
import snd.komga.client.collection.KomgaCollectionCreateRequest
import snd.komga.client.collection.KomgaCollectionId
import snd.komga.client.collection.KomgaCollectionQuery
import snd.komga.client.collection.KomgaCollectionThumbnail
import snd.komga.client.collection.KomgaCollectionUpdateRequest
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.KomgaThumbnailId
import snd.komga.client.common.Page
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.series.KomgaSeries

class OfflineCollectionsApi : KomgaCollectionsApi {
    override suspend fun getAll(
        search: String?,
        libraryIds: List<KomgaLibraryId>?,
        pageRequest: KomgaPageRequest?
    ): Page<KomgaCollection> {
        return Page.empty()
    }

    override suspend fun getOne(id: KomgaCollectionId): KomgaCollection {
        offlineUnsupported("查看离线合集详情")
    }

    override suspend fun addOne(request: KomgaCollectionCreateRequest): KomgaCollection {
        offlineUnsupported("新增离线合集")
    }

    override suspend fun updateOne(
        id: KomgaCollectionId,
        request: KomgaCollectionUpdateRequest
    ) {
    }

    override suspend fun deleteOne(id: KomgaCollectionId) {
    }

    override suspend fun getSeriesForCollection(
        id: KomgaCollectionId,
        query: KomgaCollectionQuery?,
        pageRequest: KomgaPageRequest?
    ): Page<KomgaSeries> {
        return Page.empty()
    }

    override suspend fun getDefaultThumbnail(collectionId: KomgaCollectionId): ByteArray? {
        offlineUnsupported("获取离线合集默认缩略图")
    }

    override suspend fun getThumbnail(
        collectionId: KomgaCollectionId,
        thumbnailId: KomgaThumbnailId
    ): ByteArray {
        offlineUnsupported("获取离线合集缩略图")
    }

    override suspend fun getThumbnails(collectionId: KomgaCollectionId): List<KomgaCollectionThumbnail> {
        return emptyList()
    }

    override suspend fun uploadThumbnail(
        collectionId: KomgaCollectionId,
        file: ByteArray,
        filename: String,
        selected: Boolean
    ): KomgaCollectionThumbnail {
        offlineUnsupported("上传离线合集缩略图")
    }

    override suspend fun selectThumbnail(
        collectionId: KomgaCollectionId,
        thumbnailId: KomgaThumbnailId
    ) {
    }

    override suspend fun deleteThumbnail(
        collectionId: KomgaCollectionId,
        thumbnailId: KomgaThumbnailId
    ) {
    }
}
