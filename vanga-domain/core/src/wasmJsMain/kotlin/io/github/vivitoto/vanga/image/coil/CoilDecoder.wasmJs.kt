package io.github.vivitoto.vanga.image.coil

import coil3.Image
import coil3.asImage
import io.github.vivitoto.vanga.image.VangaImage
import io.github.vivitoto.vanga.image.toBitmap

actual suspend fun VangaImage.toCoilImage(): Image {
    return this.toBitmap().asImage()
}