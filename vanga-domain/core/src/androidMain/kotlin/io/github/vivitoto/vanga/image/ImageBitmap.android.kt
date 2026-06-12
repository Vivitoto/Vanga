package io.github.vivitoto.vanga.image

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import io.github.vivitoto.vanga.image.AndroidBitmap.toBitmap

actual suspend fun VangaImage.toImageBitmap(): ImageBitmap {
    return this.toBitmap().asImageBitmap()
}