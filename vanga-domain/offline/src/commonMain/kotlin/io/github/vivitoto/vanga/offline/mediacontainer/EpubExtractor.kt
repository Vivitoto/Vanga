package io.github.vivitoto.vanga.offline.mediacontainer

import io.github.vinceglb.filekit.PlatformFile

interface EpubExtractor {
    fun getEntryBytes(file: PlatformFile, entryName: String): ByteArray

}