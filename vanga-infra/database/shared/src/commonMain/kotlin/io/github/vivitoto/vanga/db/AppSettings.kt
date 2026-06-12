package io.github.vivitoto.vanga.db

import kotlinx.serialization.Serializable
import io.github.vivitoto.vanga.settings.model.AppTheme
import io.github.vivitoto.vanga.settings.model.BooksLayout
import io.github.vivitoto.vanga.updates.AppVersion
import kotlin.time.Instant

@Serializable
data class AppSettings(
    val username: String = "admin@example.org",
    val serverUrl: String = "http://localhost:25600",

    val cardWidth: Int = 170,
    val seriesPageLoadSize: Int = 20,
    val bookPageLoadSize: Int = 20,
    val bookListLayout: BooksLayout = BooksLayout.GRID,
    val appTheme: AppTheme = AppTheme.SYSTEM,

    val checkForUpdatesOnStartup: Boolean = true,
    val updateLastCheckedTimestamp: Instant? = null,
    val updateLastCheckedReleaseVersion: AppVersion? = null,
    val updateDismissedVersion: AppVersion? = null,
)
