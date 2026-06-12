package io.github.vivitoto.vanga.image

interface ReaderImageFactory {
    suspend fun getImage(imageSource: ImageSource, pageId: ReaderImage.PageId): ReaderImage
}
