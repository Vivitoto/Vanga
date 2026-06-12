package io.github.vivitoto.vanga.ui.home.edit

import kotlinx.coroutines.flow.MutableStateFlow
import io.github.vivitoto.vanga.homefilters.HomeScreenFilter

sealed interface FilterEditState {
    val label: MutableStateFlow<String>
    fun toFilter(order: Int): HomeScreenFilter
}