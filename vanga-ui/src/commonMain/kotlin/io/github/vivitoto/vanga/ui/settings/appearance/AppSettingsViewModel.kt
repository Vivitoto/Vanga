package io.github.vivitoto.vanga.ui.settings.appearance

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import io.github.vivitoto.vanga.settings.CommonSettingsRepository
import io.github.vivitoto.vanga.settings.model.AppTheme
import io.github.vivitoto.vanga.ui.LoadState
import io.github.vivitoto.vanga.ui.common.cards.defaultCardWidth

class AppSettingsViewModel(
    private val settingsRepository: CommonSettingsRepository,
) : StateScreenModel<LoadState<Unit>>(LoadState.Uninitialized) {
    var cardWidth by mutableStateOf(defaultCardWidth.dp)
    var currentTheme by mutableStateOf(AppTheme.SYSTEM)

    suspend fun initialize() {
        if (state.value !is LoadState.Uninitialized) return
        mutableState.value = LoadState.Loading
        cardWidth = settingsRepository.getCardWidth().map { it.dp }.first()
        currentTheme = settingsRepository.getAppTheme().first()
        mutableState.value = LoadState.Success(Unit)
    }

    fun onCardWidthChange(cardWidth: Dp) {
        this.cardWidth = cardWidth
        screenModelScope.launch { settingsRepository.putCardWidth(cardWidth.value.toInt()) }
    }

    fun onAppThemeChange(theme: AppTheme) {
        this.currentTheme = theme
        screenModelScope.launch { settingsRepository.putAppTheme(theme) }
    }

}
