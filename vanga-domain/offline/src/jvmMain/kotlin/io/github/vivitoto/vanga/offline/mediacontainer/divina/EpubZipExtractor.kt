package io.github.vivitoto.vanga.offline.mediacontainer.divina

import io.github.vinceglb.filekit.PlatformFile
import io.github.vivitoto.vanga.offline.mediacontainer.EpubExtractor

class EpubZipExtractor(private val zipExtractor: ZipExtractor) : EpubExtractor {
    override fun getEntryBytes(file: PlatformFile, entryName: String): ByteArray {
        return zipExtractor.getEntryBytes(file, entryName)
    }
}