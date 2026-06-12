package io.github.vivitoto.vanga.image

import androidx.compose.ui.graphics.ImageBitmap

expect suspend fun VangaImage.toImageBitmap(): ImageBitmap
