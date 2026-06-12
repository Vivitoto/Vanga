package io.github.vivitoto.vanga.ui.dialogs.tabs

import androidx.compose.runtime.Composable

interface DialogTab {
    fun options(): TabItem

    @Composable
    fun Content()
}