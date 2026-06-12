package io.github.vivitoto.vanga.image.processing

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import io.github.vivitoto.vanga.image.VangaImage
import io.github.vivitoto.vanga.image.ReaderImage
import kotlin.time.measureTimedValue

private val logger = KotlinLogging.logger {}

class CropBordersStep(
    private val enabled: StateFlow<Boolean>
) : ProcessingStep {
    override suspend fun process(pageId: ReaderImage.PageId, image: VangaImage): VangaImage? {
        if (!enabled.value) return null
        val result = measureTimedValue {
            val trim = image.findTrim()
            image.extractArea(trim)
        }
        logger.info { "page ${pageId.pageNumber} completed border crop in ${result.duration}" }
        return result.value
    }

    override suspend fun addChangeListener(callback: () -> Unit) {
        enabled.drop(1).collect { callback() }
    }
}