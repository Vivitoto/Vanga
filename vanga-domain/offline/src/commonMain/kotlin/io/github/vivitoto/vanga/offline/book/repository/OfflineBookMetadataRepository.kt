package io.github.vivitoto.vanga.offline.book.repository

import io.github.vivitoto.vanga.offline.book.model.OfflineBookMetadata
import snd.komga.client.book.KomgaBookId

interface OfflineBookMetadataRepository {
    suspend fun save(metadata: OfflineBookMetadata)
    suspend fun find(id: KomgaBookId): OfflineBookMetadata?
    suspend fun findAllByIds(bookIds: List<KomgaBookId>): List<OfflineBookMetadata>
    suspend fun get(id: KomgaBookId): OfflineBookMetadata
    suspend fun delete(id: KomgaBookId)

    suspend fun delete(bookIds: List<KomgaBookId>)
}