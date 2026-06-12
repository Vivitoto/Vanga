package io.github.vivitoto.vanga.image

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import io.github.vivitoto.vanga.image.SkiaBitmap.toSkiaBitmap

actual suspend fun VangaImage.toImageBitmap(): ImageBitmap =
    this.toSkiaBitmap().asComposeImageBitmap()