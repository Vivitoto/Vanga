package io.github.vivitoto.vanga.offline.api

import kotlinx.coroutines.flow.StateFlow
import io.github.vivitoto.vanga.komga.api.KomgaLibraryApi
import io.github.vivitoto.vanga.offline.action.OfflineActions
import io.github.vivitoto.vanga.offline.library.actions.LibraryAnalyzeAction
import io.github.vivitoto.vanga.offline.library.actions.LibraryDeleteAction
import io.github.vivitoto.vanga.offline.library.actions.LibraryEmptyTrashAction
import io.github.vivitoto.vanga.offline.library.actions.LibraryPatchAction
import io.github.vivitoto.vanga.offline.library.actions.LibraryRefreshMetadataAction
import io.github.vivitoto.vanga.offline.library.actions.LibraryScanAction
import io.github.vivitoto.vanga.offline.library.model.OfflineLibrary
import io.github.vivitoto.vanga.offline.library.repository.OfflineLibraryRepository
import io.github.vivitoto.vanga.offline.server.model.OfflineMediaServer
import io.github.vivitoto.vanga.offline.user.model.OfflineUser
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.library.KomgaLibraryCreateRequest
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.library.KomgaLibraryUpdateRequest
import snd.komga.client.user.KomgaUserId

class OfflineLibraryApi(
    private val libraryRepository: OfflineLibraryRepository,
    private val mediaServer: StateFlow<OfflineMediaServer?>,
    private val offlineUserId: StateFlow<KomgaUserId>,
    private val actions: OfflineActions,
) : KomgaLibraryApi {

    override suspend fun getLibraries(): List<KomgaLibrary> {
        val userId = offlineUserId.value
        val currentMediaServer = mediaServer.value
        val libraries =
            if (userId == OfflineUser.ROOT || currentMediaServer == null) {
                libraryRepository.findAll().map { it.toKomgaLibrary() }
            } else {
                libraryRepository.findAllByMediaServer(currentMediaServer.id).map { it.toKomgaLibrary() }
            }

        return libraries
    }

    override suspend fun getLibrary(libraryId: KomgaLibraryId): KomgaLibrary {
        return libraryRepository.get(libraryId).toKomgaLibrary()
    }

    override suspend fun addOne(request: KomgaLibraryCreateRequest): KomgaLibrary {
        TODO()
//        return actions.get<LibraryAddAction>()
//            .run(request, mediaServer.value)
//            .toKomgaLibrary()
    }

    override suspend fun patchOne(
        libraryId: KomgaLibraryId,
        request: KomgaLibraryUpdateRequest
    ) {
        actions.get<LibraryPatchAction>()
            .run(libraryId, request)
    }

    override suspend fun deleteOne(libraryId: KomgaLibraryId) {
        actions.get<LibraryDeleteAction>()
            .execute(libraryId)
    }

    override suspend fun scan(libraryId: KomgaLibraryId, deep: Boolean) {
        actions.get<LibraryScanAction>()
            .run(libraryId)
    }

    override suspend fun analyze(libraryId: KomgaLibraryId) {
        actions.get<LibraryAnalyzeAction>()
            .run(libraryId)
    }

    override suspend fun refreshMetadata(libraryId: KomgaLibraryId) {
        actions.get<LibraryRefreshMetadataAction>()
            .run(libraryId)
    }

    override suspend fun emptyTrash(libraryId: KomgaLibraryId) {
        actions.get<LibraryEmptyTrashAction>()
            .execute(libraryId)
    }

    private fun OfflineLibrary.toKomgaLibrary() = KomgaLibrary(
        id = this.id,
        name = this.name,
        root = this.root,
        importComicInfoBook = this.importComicInfoBook,
        importComicInfoSeries = this.importComicInfoSeries,
        importComicInfoCollection = this.importComicInfoCollection,
        importComicInfoReadList = this.importComicInfoReadList,
        importComicInfoSeriesAppendVolume = this.importComicInfoSeriesAppendVolume,
        importEpubBook = this.importEpubBook,
        importEpubSeries = this.importEpubSeries,
        importMylarSeries = this.importMylarSeries,
        importLocalArtwork = this.importLocalArtwork,
        importBarcodeIsbn = this.importBarcodeIsbn,
        scanForceModifiedTime = this.scanForceModifiedTime,
        scanInterval = this.scanInterval,
        scanOnStartup = this.scanOnStartup,
        scanCbx = this.scanCbx,
        scanPdf = this.scanPdf,
        scanEpub = this.scanEpub,
        scanDirectoryExclusions = this.scanDirectoryExclusions,
        repairExtensions = this.repairExtensions,
        convertToCbz = this.convertToCbz,
        emptyTrashAfterScan = this.emptyTrashAfterScan,
        seriesCover = this.seriesCover,
        hashFiles = this.hashFiles,
        hashPages = this.hashPages,
        hashKoreader = this.hashKoreader,
        analyzeDimensions = this.analyzeDimensions,
        oneshotsDirectory = this.oneshotsDirectory,
        unavailable = this.unavailable
    )
}