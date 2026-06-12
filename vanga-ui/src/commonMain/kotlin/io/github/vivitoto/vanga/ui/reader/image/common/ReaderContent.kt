package io.github.vivitoto.vanga.ui.reader.image.common

import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyUp
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import io.github.vivitoto.vanga.settings.model.ReaderType.CONTINUOUS
import io.github.vivitoto.vanga.settings.model.ReaderType.PAGED
import io.github.vivitoto.vanga.ui.LocalPlatform
import io.github.vivitoto.vanga.ui.LocalWindowState
import io.github.vivitoto.vanga.ui.common.components.LoadingMaxSizeIndicator
import io.github.vivitoto.vanga.ui.platform.BackPressHandler
import io.github.vivitoto.vanga.ui.platform.PlatformType.MOBILE
import io.github.vivitoto.vanga.ui.reader.image.ReaderState
import io.github.vivitoto.vanga.ui.reader.image.ScreenScaleState
import io.github.vivitoto.vanga.ui.reader.image.continuous.ContinuousReaderContent
import io.github.vivitoto.vanga.ui.reader.image.continuous.ContinuousReaderState
import io.github.vivitoto.vanga.ui.reader.image.paged.PagedReaderContent
import io.github.vivitoto.vanga.ui.reader.image.paged.PagedReaderState
import io.github.vivitoto.vanga.ui.reader.image.settings.SettingsOverlay
import kotlin.math.abs

@Composable
fun ReaderContent(
    commonReaderState: ReaderState,
    pagedReaderState: PagedReaderState,
    continuousReaderState: ContinuousReaderState,
    screenScaleState: ScreenScaleState,
    onExit: () -> Unit,
) {
    var showHelpDialog by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }
    BackPressHandler {
        if (showSettingsMenu) showSettingsMenu = false
        else onExit()
    }
    if (LocalPlatform.current == MOBILE) {
        val windowState = LocalWindowState.current
        DisposableEffect(showSettingsMenu) {
            if (showSettingsMenu) {
                windowState.setFullscreen(false)
            } else {
                windowState.setFullscreen(true)
            }
            onDispose {
                windowState.setFullscreen(false)
            }
        }
    }
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        screenScaleState.composeScope = coroutineScope
    }
    val density = LocalDensity.current
    LaunchedEffect(density) {
        commonReaderState.pixelDensity.value = density
    }

    val topLevelFocus = remember { FocusRequester() }
    val volumeKeysNavigation = commonReaderState.volumeKeysNavigation.collectAsState().value
    var hasFocus by remember { mutableStateOf(false) }
    Box(
        Modifier
            .fillMaxSize()
            .onSizeChanged {
                screenScaleState.setAreaSize(it)
            }
            .focusable()
            .focusRequester(topLevelFocus)
            .onFocusChanged { hasFocus = it.hasFocus }
            .onKeyEvent { event ->
                if (event.type != KeyUp) return@onKeyEvent false

                var consumed = true
                when (event.key) {
                    Key.M -> showSettingsMenu = !showSettingsMenu
                    Key.Escape -> showSettingsMenu = false
                    Key.H -> showHelpDialog = true
                    Key.DirectionLeft -> if (event.isAltPressed) onExit() else consumed = false
                    Key.Back -> if (showSettingsMenu) showSettingsMenu = false else onExit()
                    Key.U -> commonReaderState.onStretchToFitCycle()
                    else -> consumed = false
                }
                consumed
            }
    ) {
        val areaSize = screenScaleState.areaSize.collectAsState()
        if (areaSize.value == IntSize.Zero) {
            LoadingMaxSizeIndicator()
            return
        }

        when (commonReaderState.readerType.collectAsState().value) {
            PAGED -> {
                PagedReaderContent(
                    showHelpDialog = showHelpDialog,
                    onShowHelpDialogChange = { showHelpDialog = it },
                    showSettingsMenu = showSettingsMenu,
                    onShowSettingsMenuChange = { showSettingsMenu = it },
                    screenScaleState = screenScaleState,
                    pagedReaderState = pagedReaderState,
                    volumeKeysNavigation = volumeKeysNavigation,
                    onBackGesture = onExit,
                )
            }

            CONTINUOUS -> {
                ContinuousReaderContent(
                    showHelpDialog = showHelpDialog,
                    onShowHelpDialogChange = { showHelpDialog = it },
                    showSettingsMenu = showSettingsMenu,
                    onShowSettingsMenuChange = { showSettingsMenu = it },
                    screenScaleState = screenScaleState,
                    continuousReaderState = continuousReaderState,
                    volumeKeysNavigation = volumeKeysNavigation,
                    onBackGesture = onExit,
                )
            }

        }

        SettingsOverlay(
            show = showSettingsMenu,
            commonReaderState = commonReaderState,
            pagedReaderState = pagedReaderState,
            continuousReaderState = continuousReaderState,
            screenScaleState = screenScaleState,
            onBackPress = onExit,
            ohShowHelpDialogChange = { showHelpDialog = it },
        )

        EInkFlashOverlay(
            enabled = commonReaderState.flashOnPageChange.collectAsState().value,
            pageChangeFlow = commonReaderState.pageChangeFlow,
            flashEveryNPages = commonReaderState.flashEveryNPages.collectAsState().value,
            flashWith = commonReaderState.flashWith.collectAsState().value,
            flashDuration = commonReaderState.flashDuration.collectAsState().value
        )
    }
    LaunchedEffect(hasFocus) {
        if (!hasFocus) topLevelFocus.requestFocus()
    }
}

@Composable
fun ReaderControlsOverlay(
    readingDirection: LayoutDirection,
    onNexPageClick: suspend () -> Unit,
    onPrevPageClick: suspend () -> Unit,
    isSettingsMenuOpen: Boolean,
    onSettingsMenuToggle: () -> Unit,
    canNavigateByHorizontalSwipe: () -> Boolean = { true },
    onBackGesture: () -> Unit = {},
    contentAreaSize: IntSize,
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val edgeSwipeWidth = with(density) { 32.dp.toPx() }
    val minSwipeDistance = with(density) { 72.dp.toPx() }
    val minSwipeDistanceFloor = with(density) { 48.dp.toPx() }
    val currentCanNavigateByHorizontalSwipe by rememberUpdatedState(canNavigateByHorizontalSwipe)
    val currentOnBackGesture by rememberUpdatedState(onBackGesture)
    val leftAction = {
        if (isSettingsMenuOpen) onSettingsMenuToggle()
        else if (readingDirection == LayoutDirection.Ltr) coroutineScope.launch { onPrevPageClick() }
        else coroutineScope.launch { onNexPageClick() }
    }
    val centerAction = { onSettingsMenuToggle() }
    val rightAction = {
        if (isSettingsMenuOpen) onSettingsMenuToggle()
        else if (readingDirection == LayoutDirection.Ltr) coroutineScope.launch { onNexPageClick() }
        else coroutineScope.launch { onPrevPageClick() }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .focusable()
            .pointerInput(
                contentAreaSize,
                readingDirection,
                isSettingsMenuOpen,
            ) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var maxPointers = 1
                    var lastPosition = down.position

                    while (true) {
                        val event = awaitPointerEvent()
                        val pressedPointers = event.changes.count { it.pressed }
                        if (pressedPointers > maxPointers) maxPointers = pressedPointers

                        val tracked = event.changes.firstOrNull { it.id == down.id }
                        if (tracked != null) {
                            lastPosition = tracked.position
                            if (!tracked.pressed) break
                        } else if (pressedPointers == 0) {
                            break
                        }
                    }

                    val drag = lastPosition - down.position
                    val threshold = minSwipeDistance
                        .coerceAtMost(contentAreaSize.width * .28f)
                        .coerceAtLeast(minSwipeDistanceFloor)
                    val isHorizontalSwipe = maxPointers == 1 &&
                        abs(drag.x) > threshold &&
                        abs(drag.x) > abs(drag.y) * 1.4f

                    if (isHorizontalSwipe) {
                        when {
                            isSettingsMenuOpen -> onSettingsMenuToggle()
                            down.position.x <= edgeSwipeWidth && drag.x > threshold -> currentOnBackGesture()
                            currentCanNavigateByHorizontalSwipe() -> if (drag.x < 0) rightAction() else leftAction()
                        }
                    }
                }
            }
            .pointerInput(
                contentAreaSize,
                readingDirection,
                onSettingsMenuToggle,
                isSettingsMenuOpen
            ) {
                detectTapGestures { offset ->
                    val actionWidth = contentAreaSize.width.toFloat() / 3
                    when (offset.x) {
                        in 0f..<actionWidth -> leftAction()
                        in actionWidth..actionWidth * 2 -> centerAction()
                        else -> rightAction()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
