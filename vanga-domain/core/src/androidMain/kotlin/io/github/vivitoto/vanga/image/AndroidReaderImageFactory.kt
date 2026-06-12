package io.github.vivitoto.vanga.image

import kotlinx.coroutines.flow.StateFlow
import io.github.vivitoto.vanga.image.ReaderImage.PageId
import io.github.vivitoto.vanga.image.processing.ImageProcessingPipeline

class AndroidReaderImageFactory(
    private val imageDecoder: VangaImageDecoder,
    private val upsamplingMode: StateFlow<UpsamplingMode>,
    private val downSamplingKernel: StateFlow<ReduceKernel>,
    private val linearLightDownSampling: StateFlow<Boolean>,
    private val stretchImages: StateFlow<Boolean>,
    private val processingPipeline: ImageProcessingPipeline,
) : ReaderImageFactory {

    override suspend fun getImage(
        imageSource: ImageSource,
        pageId: PageId
    ): ReaderImage {
        return AndroidReaderImage(
            imageDecoder = imageDecoder,
            imageSource = imageSource,
            processingPipeline = processingPipeline,
            stretchImages = stretchImages,
            upsamplingMode = upsamplingMode,
            downSamplingKernel = downSamplingKernel,
            linearLightDownSampling = linearLightDownSampling,
            pageId = pageId,
        )
    }
}

