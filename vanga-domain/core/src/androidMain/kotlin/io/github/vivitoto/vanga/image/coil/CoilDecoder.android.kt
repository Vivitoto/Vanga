package io.github.vivitoto.vanga.image.coil

import coil3.Image
import coil3.asImage
import io.github.vivitoto.vanga.image.AndroidBitmap.toBitmap
import io.github.vivitoto.vanga.image.VangaImage

actual suspend fun VangaImage.toCoilImage(): Image {
    return this.toBitmap().asImage()
}