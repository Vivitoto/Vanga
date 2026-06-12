package io.github.vivitoto.vanga.image

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import io.github.vivitoto.vanga.image.ReaderImage.PageId
import io.github.vivitoto.vanga.image.processing.ImageProcessingPipeline

class WasmReaderImageFactory(
    private val imageDecoder: VangaImageDecoder,
    private val downSamplingKernel: StateFlow<ReduceKernel>,
    private val upsamplingMode: StateFlow<UpsamplingMode>,
    private val linearLightDownSampling: StateFlow<Boolean>,
    private val stretchImages: StateFlow<Boolean>,
    private val processingPipeline: ImageProcessingPipeline,
) : ReaderImageFactory {

    override suspend fun getImage(imageSource: ImageSource, pageId: PageId): ReaderImage {
        return WasmReaderImage(
            imageDecoder = imageDecoder,
            imageSource = imageSource,
            processingPipeline = processingPipeline,
            stretchImages = stretchImages,
            upsamplingMode = upsamplingMode,
            downSamplingKernel = downSamplingKernel,
            linearLightDownSampling = linearLightDownSampling,
            pageId = pageId,
            showDebugGrid = MutableStateFlow(false)
        )

    }
}