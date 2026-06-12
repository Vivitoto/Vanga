package io.github.vivitoto.vanga.db

import kotlinx.serialization.Serializable
import io.github.vivitoto.vanga.image.ReduceKernel
import io.github.vivitoto.vanga.image.UpsamplingMode
import io.github.vivitoto.vanga.settings.model.ContinuousReadingDirection
import io.github.vivitoto.vanga.settings.model.LayoutScaleType
import io.github.vivitoto.vanga.settings.model.PageDisplayLayout
import io.github.vivitoto.vanga.settings.model.PagedReadingDirection
import io.github.vivitoto.vanga.settings.model.ReaderFlashColor
import io.github.vivitoto.vanga.settings.model.ReaderType
import io.github.vivitoto.vanga.settings.model.ReaderType.PAGED

@Serializable
data class ImageReaderSettings(
    val readerType: ReaderType = PAGED,
    val stretchToFit: Boolean = true,
    val pagedScaleType: LayoutScaleType = LayoutScaleType.SCREEN,
    val pagedReadingDirection: PagedReadingDirection = PagedReadingDirection.LEFT_TO_RIGHT,
    val pagedPageLayout: PageDisplayLayout = PageDisplayLayout.SINGLE_PAGE,
    val continuousReadingDirection: ContinuousReadingDirection = ContinuousReadingDirection.TOP_TO_BOTTOM,
    val continuousPadding: Float = 0f,
    val continuousPageSpacing: Int = 0,
    val cropBorders: Boolean = false,

    val flashOnPageChange: Boolean = false,
    val flashDuration: Long = 100L,
    val flashEveryNPages: Int = 1,
    val flashWith: ReaderFlashColor = ReaderFlashColor.BLACK,
    val downsamplingKernel: ReduceKernel = ReduceKernel.LANCZOS3,
    val linearLightDownsampling: Boolean = false,
    val upsamplingMode: UpsamplingMode = UpsamplingMode.CATMULL_ROM,
    val loadThumbnailPreviews: Boolean = true,
    val volumeKeysNavigation: Boolean = false,
)
