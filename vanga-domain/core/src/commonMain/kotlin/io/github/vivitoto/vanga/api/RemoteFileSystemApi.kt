package io.github.vivitoto.vanga.api

import io.github.vivitoto.vanga.komga.api.KomgaFileSystemApi
import snd.komga.client.filesystem.DirectoryRequest
import snd.komga.client.filesystem.KomgaFileSystemClient

class RemoteFileSystemApi(private val fileSystemClient: KomgaFileSystemClient) : KomgaFileSystemApi {
    override suspend fun getDirectoryListing(request: DirectoryRequest) = fileSystemClient.getDirectoryListing(request)
}