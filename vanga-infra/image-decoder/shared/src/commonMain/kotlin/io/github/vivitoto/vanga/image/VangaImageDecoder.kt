package io.github.vivitoto.vanga.image

interface VangaImageDecoder {
    suspend fun decode(encoded: ByteArray, nPages: Int? = null): VangaImage
    suspend fun decodeFromFile(path: String, nPages: Int? = null): VangaImage
    suspend fun decodeAndResize(
        encoded: ByteArray,
        scaleWidth: Int,
        scaleHeight: Int,
        crop: Boolean,
        nPages: Int? = null
    ): VangaImage

    suspend fun decodeAndResize(
        path: String,
        scaleWidth: Int,
        scaleHeight: Int,
        crop: Boolean,
        nPages: Int? = null
    ): VangaImage
}
