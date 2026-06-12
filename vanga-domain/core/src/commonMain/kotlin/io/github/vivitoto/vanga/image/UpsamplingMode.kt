package io.github.vivitoto.vanga.image

enum class UpsamplingMode {
    NEAREST,
    BILINEAR,
    MITCHELL,
    CATMULL_ROM,
}

expect fun availableUpsamplingModes(): List<UpsamplingMode>