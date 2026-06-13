package io.github.vivitoto.vanga

import io.github.vivitoto.vanga.fonts.UserFontsRepository
import io.github.vivitoto.vanga.favorites.FavoriteSyncSettingsRepository
import io.github.vivitoto.vanga.favorites.LocalFavoritesRepository
import io.github.vivitoto.vanga.homefilters.HomeScreenFilterRepository
import io.github.vivitoto.vanga.offline.OfflineModule
import io.github.vivitoto.vanga.settings.CommonSettingsRepository
import io.github.vivitoto.vanga.settings.EpubReaderSettingsRepository
import io.github.vivitoto.vanga.settings.ImageReaderSettingsRepository
import io.github.vivitoto.vanga.settings.KomfSettingsRepository
import io.github.vivitoto.vanga.settings.SecretsRepository

class CoreModule(
    val appRepositories: AppRepositories,
    private val offlineModule: OfflineModule
) {
}

data class AppRepositories(
    val settingsRepository: CommonSettingsRepository,
    val epubReaderSettingsRepository: EpubReaderSettingsRepository,
    val imageReaderSettingsRepository: ImageReaderSettingsRepository,
    val fontsRepository: UserFontsRepository,
    val secretsRepository: SecretsRepository,
    val komfSettingsRepository: KomfSettingsRepository,
    val homeScreenFilterRepository: HomeScreenFilterRepository,
    val localFavoritesRepository: LocalFavoritesRepository,
    val favoriteSyncSettingsRepository: FavoriteSyncSettingsRepository,
)
