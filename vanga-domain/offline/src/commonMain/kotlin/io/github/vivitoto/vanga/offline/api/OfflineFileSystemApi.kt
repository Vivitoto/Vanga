package io.github.vivitoto.vanga.offline.api

import io.github.vivitoto.vanga.komga.api.KomgaFileSystemApi
import snd.komga.client.filesystem.DirectoryListing
import snd.komga.client.filesystem.DirectoryRequest

class OfflineFileSystemApi : KomgaFileSystemApi {
    override suspend fun getDirectoryListing(request: DirectoryRequest) =
        DirectoryListing(null, emptyList())
}