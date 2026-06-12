package io.github.vivitoto.vanga

import kotlinx.coroutines.flow.Flow

interface AppWindowState {
    val isFullscreen: Flow<Boolean>
    fun setFullscreen(enabled: Boolean)
}