package io.github.vivitoto.vanga.offline.media.repository

import io.github.vivitoto.vanga.offline.media.model.OfflineMedia
import snd.komga.client.book.KomgaBookId

interface OfflineMediaRepository {
    suspend fun save(media: OfflineMedia)
    suspend fun find(id: KomgaBookId): OfflineMedia?
    suspend fun findAll(ids: List<KomgaBookId>): List<OfflineMedia>
    suspend fun get(id: KomgaBookId): OfflineMedia
    suspend fun delete(id: KomgaBookId)
    suspend fun delete(bookIds: List<KomgaBookId>)
}