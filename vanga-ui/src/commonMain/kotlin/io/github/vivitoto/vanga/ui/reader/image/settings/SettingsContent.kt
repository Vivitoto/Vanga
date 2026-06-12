package io.github.vivitoto.vanga.ui.reader.image.settings

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection.Ltr
import androidx.compose.ui.unit.LayoutDirection.Rtl
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import io.github.vivitoto.vanga.image.ReaderImage
import io.github.vivitoto.vanga.settings.model.ContinuousReadingDirection
import io.github.vivitoto.vanga.settings.model.PagedReadingDirection
import io.github.vivitoto.vanga.settings.model.ReaderType.CONTINUOUS
import io.github.vivitoto.vanga.settings.model.ReaderType.PAGED
import io.github.vivitoto.vanga.ui.LocalPlatform
import io.github.vivitoto.vanga.ui.LocalStrings
import io.github.vivitoto.vanga.ui.LocalWindowWidth
import io.github.vivitoto.vanga.ui.platform.PlatformType.DESKTOP
import io.github.vivitoto.vanga.ui.platform.WindowSizeClass.COMPACT
import io.github.vivitoto.vanga.ui.platform.WindowSizeClass.MEDIUM
import io.github.vivitoto.vanga.ui.reader.image.PageMetadata
import io.github.vivitoto.vanga.ui.reader.image.ReaderState
import io.github.vivitoto.vanga.ui.reader.image.ScreenScaleState
import io.github.vivitoto.vanga.ui.reader.image.common.PageSpreadProgressSlider
import io.github.vivitoto.vanga.ui.reader.image.common.ProgressSlider
import io.github.vivitoto.vanga.ui.reader.image.continuous.ContinuousReaderState
import io.github.vivitoto.vanga.ui.reader.image.paged.PagedReaderState

@Composable
fun BoxScope.SettingsOverlay(
    show: Boolean,
    commonReaderState: ReaderState,
    pagedReaderState: PagedReaderState,
    continuousReaderState: ContinuousReaderState,
    screenScaleState: ScreenScaleState,
    onBackPress: () -> Unit,
    ohShowHelpDialogChange: (Boolean) -> Unit,
) {
    if (!show) return
    val windowWidth = LocalWindowWidth.current
    val platform = LocalPlatform.current
    val book = commonReaderState.booksState.collectAsState().value?.currentBook
    val readerType = commonReaderState.readerType.collectAsState().value
    val coroutineScope = rememberCoroutineScope()
    val zoom = screenScaleState.zoom.collectAsState().value
    val availableUpsamplingModes = commonReaderState.availableUpsamplingModes
    val upsamplingMode = commonReaderState.upsamplingMode.collectAsState().value
    val availableDownsamplingKernels = commonReaderState.availableDownsamplingKernels
    val downsamplingKernel = commonReaderState.downsamplingKernel.collectAsState().value
    val linearLightDownsampling = commonReaderState.linearLightDownsampling.collectAsState().value
    val stretchToFit = commonReaderState.imageStretchToFit.collectAsState().value
    val cropBorders = commonReaderState.cropBorders.collectAsState().value
    val flashEnabled = commonReaderState.flashOnPageChange.collectAsState().value
    val flashDuration = commonReaderState.flashDuration.collectAsState().value
    val flashEveryNPages = commonReaderState.flashEveryNPages.collectAsState().value
    val flashWith = commonReaderState.flashWith.collectAsState().value

    if ((windowWidth == COMPACT || windowWidth == MEDIUM) && platform != DESKTOP) {
        BottomSheetSettingsOverlay(
            book = book,
            readerType = readerType,
            onReaderTypeChange = commonReaderState::onReaderTypeChange,
            availableUpsamplingModes = availableUpsamplingModes,
            upsamplingMode = upsamplingMode,
            onUpsamplingModeChange = commonReaderState::onUpsamplingModeChange,
            availableDownsamplingKernels = availableDownsamplingKernels,
            downsamplingKernel = downsamplingKernel,
            onDownsamplingKernelChange = commonReaderState::onDownsamplingKernelChange,
            linearLightDownsampling = linearLightDownsampling,
            onLinearLightDownsamplingChange = commonReaderState::onLinearLightDownsamplingChange,
            stretchToFit = stretchToFit,
            onStretchToFitChange = commonReaderState::onStretchToFitChange,
            cropBorders = cropBorders,
            onCropBordersChange = commonReaderState::onCropBordersChange,
            zoom = zoom,
            pagedReaderState = pagedReaderState,
            continuousReaderState = continuousReaderState,

            flashEnabled = flashEnabled,
            onFlashEnabledChange = commonReaderState::onFlashEnabledChange,
            flashDuration = flashDuration,
            onFlashDurationChange = commonReaderState::onFlashDurationChange,
            flashEveryNPages = flashEveryNPages,
            onFlashEveryNPagesChange = commonReaderState::onFlashEveryNPagesChange,
            flashWith = flashWith,
            onFlashWithChange = commonReaderState::onFlashWithChange,

            onBackPress = onBackPress,
        )
    } else {
        SettingsSideMenuOverlay(
            book = book,
            readerType = readerType,
            onReaderTypeChange = commonReaderState::onReaderTypeChange,
            availableUpsamplingModes = availableUpsamplingModes,
            upsamplingMode = upsamplingMode,
            onUpsamplingModeChange = commonReaderState::onUpsamplingModeChange,
            availableDownsamplingKernels = availableDownsamplingKernels,
            downsamplingKernel = downsamplingKernel,
            onDownsamplingKernelChange = commonReaderState::onDownsamplingKernelChange,
            linearLightDownsampling = linearLightDownsampling,
            onLinearLightDownsamplingChange = commonReaderState::onLinearLightDownsamplingChange,
            stretchToFit = stretchToFit,
            onStretchToFitChange = commonReaderState::onStretchToFitChange,
            cropBorders = cropBorders,
            onCropBordersChange = commonReaderState::onCropBordersChange,
            zoom = zoom,
            showImageSettings = commonReaderState.expandImageSettings.collectAsState().value,
            onShowImageSettingsChange = { commonReaderState.expandImageSettings.value = it },

            flashEnabled = flashEnabled,
            onFlashEnabledChange = commonReaderState::onFlashEnabledChange,
            flashDuration = flashDuration,
            onFlashDurationChange = commonReaderState::onFlashDurationChange,
            flashEveryNPages = flashEveryNPages,
            onFlashEveryNPagesChange = commonReaderState::onFlashEveryNPagesChange,
            flashWith = flashWith,
            onFlashWithChange = commonReaderState::onFlashWithChange,

            pagedReaderState = pagedReaderState,
            continuousReaderState = continuousReaderState,

            onBackPress = onBackPress,
            onShowHelpMenu = { ohShowHelpDialogChange(true) },
        )
    }
    when (readerType) {
        PAGED -> {
            val readingDirection = pagedReaderState.readingDirection.collectAsState().value
            val layoutDirection = remember(readingDirection) {
                when (readingDirection) {
                    PagedReadingDirection.LEFT_TO_RIGHT -> Ltr
                    PagedReadingDirection.RIGHT_TO_LEFT -> Rtl
                }
            }
            PageSpreadProgressSlider(
                pageSpreads = pagedReaderState.pageSpreads.collectAsState().value,
                currentSpreadIndex = pagedReaderState.currentSpreadIndex.collectAsState().value,
                onPageNumberChange = pagedReaderState::onPageChange,
                show = show,
                layoutDirection = layoutDirection,
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }

        CONTINUOUS -> {
            val readingDirection = continuousReaderState.readingDirection.collectAsState().value
            val layoutDirection = remember(readingDirection) {
                when (readingDirection) {
                    ContinuousReadingDirection.TOP_TO_BOTTOM -> Ltr
                    ContinuousReadingDirection.LEFT_TO_RIGHT -> Ltr
                    ContinuousReadingDirection.RIGHT_TO_LEFT -> Rtl
                }
            }

            ProgressSlider(
                pages = continuousReaderState.currentBookPages.collectAsState(emptyList()).value,
                currentPageIndex = continuousReaderState.currentBookPageIndex.collectAsState(0).value,
                onPageNumberChange = { coroutineScope.launch { continuousReaderState.scrollToBookPage(it + 1) } },
                show = show,
                layoutDirection = layoutDirection,
                modifier = Modifier.align(Alignment.BottomStart)
            )

        }
    }
}

@Composable
fun PagedReaderPagesInfo(
    pages: List<PagedReaderState.Page>,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        val readerStrings = LocalStrings.current.reader
        pages.forEach { page ->
            val pageImage = page.imageResult?.image
            val pageSize = pageImage?.originalSize?.collectAsState()?.value
            if (pageImage != null) {
                val currentSize = pageImage.currentSize.collectAsState().value
                Text("${readerStrings.pageNumber} ${page.metadata.pageNumber}")

                if (currentSize != null) {
                    Text("${readerStrings.pageDisplaySize} ${currentSize.width} x ${currentSize.height}")
                }

                if (pageSize != null) {
                    Text("${readerStrings.pageOriginalSize}: ${pageSize.width} x ${pageSize.height}")
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 5.dp))
        }
    }
}

@Composable
fun ContinuousReaderPagesInfo(
    lazyListState: LazyListState,
    waitForImage: suspend (page: PageMetadata) -> ReaderImage?,
    modifier: Modifier = Modifier,
) {
    var visiblePages by remember { mutableStateOf<List<Pair<PageMetadata, ReaderImage?>>>(emptyList()) }
    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.layoutInfo }
            .collect { layout ->
                visiblePages = layout.visibleItemsInfo
                    .mapNotNull { lazyItem ->
                        if (lazyItem.key is PageMetadata) lazyItem.key as PageMetadata
                        else null
                    }
                    .map { it to waitForImage(it) }
            }
    }

    val readerStrings = LocalStrings.current.reader
    Column(modifier) {
        for ((page, image) in visiblePages) {
            Text("${readerStrings.pageNumber} ${page.pageNumber}.", style = MaterialTheme.typography.bodyMedium)

            val currentSize = image?.currentSize?.collectAsState()?.value
            if (currentSize != null) {
                Text("${readerStrings.pageDisplaySize} ${currentSize.width} x ${currentSize.height}")
            }

            if (page.size != null) {
                Text("${readerStrings.pageOriginalSize}: ${page.size.width} x ${page.size.height}")
            }

            HorizontalDivider(Modifier.padding(vertical = 5.dp))
        }
    }
}
