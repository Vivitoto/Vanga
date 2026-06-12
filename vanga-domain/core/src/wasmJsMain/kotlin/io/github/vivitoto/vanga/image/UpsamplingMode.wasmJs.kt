package io.github.vivitoto.vanga.image

actual fun availableUpsamplingModes() = listOf(
    UpsamplingMode.CATMULL_ROM,
    UpsamplingMode.MITCHELL,
    UpsamplingMode.BILINEAR,
    UpsamplingMode.NEAREST,
)
