package io.github.vivitoto.vanga.ui.settings.komf.providers

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import snd.komf.api.KomfAuthorRole
import snd.komf.api.KomfCoreProviders.ANILIST
import snd.komf.api.KomfCoreProviders.HENTAG
import snd.komf.api.KomfCoreProviders.MAL
import snd.komf.api.KomfCoreProviders.MANGA_BAKA
import snd.komf.api.KomfCoreProviders.MANGA_UPDATES
import snd.komf.api.KomfCoreProviders.NAUTILJON
import snd.komf.api.KomfMediaType
import snd.komf.api.KomfNameMatchingMode
import snd.komf.api.KomfProviders
import snd.komf.api.MangaBakaMode
import snd.komf.api.MangaDexLink
import snd.komf.api.PatchValue
import snd.komf.api.PatchValue.Some
import snd.komf.api.config.AniListConfigDto
import snd.komf.api.config.AniListConfigUpdateRequest
import snd.komf.api.config.BookMetadataConfigUpdateRequest
import snd.komf.api.config.MangaBakaConfigDto
import snd.komf.api.config.MangaBakaConfigUpdateRequest
import snd.komf.api.config.MangaDexConfigDto
import snd.komf.api.config.MangaDexConfigUpdateRequest
import snd.komf.api.config.ProviderConf
import snd.komf.api.config.ProviderConfigUpdateRequest
import snd.komf.api.config.SeriesMetadataConfigUpdateRequest

sealed class ProviderConfigState(
    config: ProviderConfigSnapshot?,
    val provider: KomfProviders,
) {
    var priority by mutableStateOf(config?.priority ?: 1)
        private set
    var enabled by mutableStateOf(config?.enabled ?: false)
        private set
    var nameMatchingMode by mutableStateOf(config?.nameMatchingMode)
        private set
    var mediaType by mutableStateOf(config?.mediaType)
        private set
    var authorRoles by mutableStateOf(config?.authorRoles ?: ProviderConfigSnapshot().authorRoles)
        private set
    var artistRoles by mutableStateOf(config?.artistRoles ?: ProviderConfigSnapshot().artistRoles)
        private set

    var seriesAgeRating by mutableStateOf(config?.seriesMetadata?.ageRating ?: true)
        private set
    var seriesAuthors by mutableStateOf(config?.seriesMetadata?.authors ?: true)
        private set
    var seriesCover by mutableStateOf(config?.seriesMetadata?.thumbnail ?: true)
        private set
    var seriesGenres by mutableStateOf(config?.seriesMetadata?.genres ?: true)
        private set
    var seriesLinks by mutableStateOf(config?.seriesMetadata?.links ?: true)
        private set
    var seriesPublisher by mutableStateOf(config?.seriesMetadata?.publisher ?: true)
        private set
    var seriesOriginalPublisher by mutableStateOf(config?.seriesMetadata?.useOriginalPublisher ?: true)
        private set
    var seriesReleaseDate by mutableStateOf(config?.seriesMetadata?.releaseDate ?: true)
        private set
    var seriesStatus by mutableStateOf(config?.seriesMetadata?.status ?: true)
        private set
    var seriesSummary by mutableStateOf(config?.seriesMetadata?.summary ?: true)
        private set
    var seriesTags by mutableStateOf(config?.seriesMetadata?.tags ?: true)
        private set
    var seriesTitle by mutableStateOf(config?.seriesMetadata?.title ?: true)
        private set
    var seriesBookCount by mutableStateOf(config?.seriesMetadata?.totalBookCount ?: true)
        private set

    val isBookMetadataAvailable = when (provider.providerKey) {
        ANILIST.name, MAL.name, MANGA_UPDATES.name, HENTAG.name, MANGA_BAKA.name -> false
        else -> true
    }
    val canHaveMultiplePublishers = when (provider.providerKey) {
        MANGA_UPDATES.name, NAUTILJON.name, MANGA_BAKA.name -> true
        else -> false
    }

    var bookEnabled by mutableStateOf(config?.seriesMetadata?.books ?: true)
        private set
    var bookAuthors by mutableStateOf(config?.bookMetadata?.authors ?: true)
        private set
    var bookCover by mutableStateOf(config?.bookMetadata?.thumbnail ?: true)
        private set
    var bookIsbn by mutableStateOf(config?.bookMetadata?.isbn ?: true)
        private set
    var bookLinks by mutableStateOf(config?.bookMetadata?.links ?: true)
        private set
    var bookNumber by mutableStateOf(config?.bookMetadata?.number ?: true)
        private set
    var bookReleaseDate by mutableStateOf(config?.bookMetadata?.releaseDate ?: true)
        private set
    var bookSummary by mutableStateOf(config?.bookMetadata?.summary ?: true)
        private set
    var bookTags by mutableStateOf(config?.bookMetadata?.tags ?: true)
        private set

    fun onPriorityChange(priority: Int) {
        this.priority = priority
        onPrioritySave(priority)
    }

    fun onEnabledChange(enabled: Boolean) {
        this.enabled = enabled
        onEnabledSave(enabled)
    }

    fun onSeriesAgeRatingChange(ageRating: Boolean) {
        this.seriesAgeRating = ageRating
        onSeriesMetadataSave(SeriesMetadataConfigUpdateRequest(ageRating = Some(ageRating)), "ageRating", ageRating)
    }

    fun onSeriesAuthorsChange(value: Boolean) {
        this.seriesAuthors = value
        onSeriesMetadataSave(SeriesMetadataConfigUpdateRequest(authors = Some(value)), "authors", value)
    }

    fun onSeriesCoverChange(value: Boolean) {
        this.seriesCover = value
        onSeriesMetadataSave(SeriesMetadataConfigUpdateRequest(thumbnail = Some(value)), "thumbnail", value)
    }

    fun onSeriesGenresChange(value: Boolean) {
        this.seriesGenres = value
        onSeriesMetadataSave(SeriesMetadataConfigUpdateRequest(genres = Some(value)), "genres", value)
    }

    fun onSeriesLinksChange(value: Boolean) {
        this.seriesLinks = value
        onSeriesMetadataSave(SeriesMetadataConfigUpdateRequest(links = Some(value)), "links", value)
    }

    fun onSeriesPublisherChange(value: Boolean) {
        this.seriesPublisher = value
        onSeriesMetadataSave(SeriesMetadataConfigUpdateRequest(publisher = Some(value)), "publisher", value)
    }

    fun onSeriesOriginalPublisherChange(value: Boolean) {
        this.seriesOriginalPublisher = value
        onSeriesMetadataSave(
            SeriesMetadataConfigUpdateRequest(useOriginalPublisher = Some(value)),
            "useOriginalPublisher",
            value
        )
    }

    fun onSeriesReleaseDateChange(value: Boolean) {
        this.seriesReleaseDate = value
        onSeriesMetadataSave(SeriesMetadataConfigUpdateRequest(releaseDate = Some(value)), "releaseDate", value)
    }

    fun onSeriesStatusChange(value: Boolean) {
        this.seriesStatus = value
        onSeriesMetadataSave(SeriesMetadataConfigUpdateRequest(status = Some(value)), "status", value)
    }

    fun onSeriesSummaryChange(value: Boolean) {
        this.seriesSummary = value
        onSeriesMetadataSave(SeriesMetadataConfigUpdateRequest(summary = Some(value)), "summary", value)
    }

    fun onSeriesTagsChange(value: Boolean) {
        this.seriesTags = value
        onSeriesMetadataSave(SeriesMetadataConfigUpdateRequest(tags = Some(value)), "tags", value)
    }

    fun onSeriesTitleChange(value: Boolean) {
        this.seriesTitle = value
        onSeriesMetadataSave(SeriesMetadataConfigUpdateRequest(title = Some(value)), "title", value)
    }

    fun onSeriesBookCountChange(value: Boolean) {
        this.seriesBookCount = value
        onSeriesMetadataSave(SeriesMetadataConfigUpdateRequest(totalBookCount = Some(value)), "totalBookCount", value)
    }

    fun onBookEnabledChange(value: Boolean) {
        this.bookEnabled = value
        onSeriesMetadataSave(SeriesMetadataConfigUpdateRequest(books = Some(value)), "books", value)
    }

    fun onBookAuthorsChange(value: Boolean) {
        this.bookAuthors = value
        onBookMetadataSave(BookMetadataConfigUpdateRequest(authors = Some(value)), "authors", value)
    }

    fun onBookCoverChange(value: Boolean) {
        this.bookCover = value
        onBookMetadataSave(BookMetadataConfigUpdateRequest(thumbnail = Some(value)), "thumbnail", value)
    }

    fun onBookIsbnChange(value: Boolean) {
        this.bookIsbn = value
        onBookMetadataSave(BookMetadataConfigUpdateRequest(isbn = Some(value)), "isbn", value)
    }

    fun onBookLinksChange(value: Boolean) {
        this.bookLinks = value
        onBookMetadataSave(BookMetadataConfigUpdateRequest(links = Some(value)), "links", value)
    }

    fun onBookNumberChange(value: Boolean) {
        this.bookNumber = value
        onBookMetadataSave(BookMetadataConfigUpdateRequest(number = Some(value)), "number", value)
    }

    fun onBookReleaseDateChange(value: Boolean) {
        this.bookReleaseDate = value
        onBookMetadataSave(BookMetadataConfigUpdateRequest(releaseDate = Some(value)), "releaseDate", value)
    }

    fun onBookSummaryChange(value: Boolean) {
        this.bookSummary = value
        onBookMetadataSave(BookMetadataConfigUpdateRequest(summary = Some(value)), "summary", value)
    }

    fun onBookTagsChange(value: Boolean) {
        this.bookTags = value
        onBookMetadataSave(BookMetadataConfigUpdateRequest(tags = Some(value)), "tags", value)
    }

    fun onMediaTypeChange(mediaType: KomfMediaType?) {
        this.mediaType = mediaType
        onMediaTypeSave(mediaType)
    }

    fun onNameMatchingModeChange(nameMatchingMode: KomfNameMatchingMode?) {
        this.nameMatchingMode = nameMatchingMode
        onNameMatchingModeSave(nameMatchingMode)
    }

    fun onAuthorSelect(role: KomfAuthorRole) {
        authorRoles = authorRoles.addOrRemove(role)
        onAuthorRolesSave(authorRoles)
    }

    fun onArtistSelect(role: KomfAuthorRole) {
        artistRoles = artistRoles.addOrRemove(role)
        onArtistRolesSave(artistRoles)
    }

    protected abstract fun onPrioritySave(priority: Int)
    protected abstract fun onEnabledSave(enabled: Boolean)
    protected abstract fun onSeriesMetadataSave(metadata: SeriesMetadataConfigUpdateRequest, key: String, value: Boolean)
    protected abstract fun onBookMetadataSave(metadata: BookMetadataConfigUpdateRequest, key: String, value: Boolean)
    protected abstract fun onMediaTypeSave(mediaType: KomfMediaType?)
    protected abstract fun onNameMatchingModeSave(nameMatchingMode: KomfNameMatchingMode?)
    protected abstract fun onAuthorRolesSave(roles: List<KomfAuthorRole>)
    protected abstract fun onArtistRolesSave(roles: List<KomfAuthorRole>)


    protected fun <T> List<T>.addOrRemove(value: T): List<T> {
        val mutable = this.toMutableList()
        val existingIndex = mutable.indexOf(value)
        if (existingIndex != -1) mutable.removeAt(existingIndex)
        else mutable.add(value)

        return mutable
    }
}

class GenericProviderConfigState(
    provider: KomfProviders,
    config: ProviderConf?,
    private val onMetadataUpdate: (ProviderConfigUpdateRequest, KomfProviders) -> Unit,
) : ProviderConfigState(config.toProviderConfigSnapshot(), provider) {

    override fun onPrioritySave(priority: Int) {
        onMetadataUpdate(ProviderConfigUpdateRequest(priority = Some(priority)))
    }

    override fun onEnabledSave(enabled: Boolean) {
        onMetadataUpdate(ProviderConfigUpdateRequest(enabled = Some(enabled)))
    }

    override fun onSeriesMetadataSave(metadata: SeriesMetadataConfigUpdateRequest, key: String, value: Boolean) {
        onMetadataUpdate(ProviderConfigUpdateRequest(seriesMetadata = Some(metadata)))
    }

    override fun onBookMetadataSave(metadata: BookMetadataConfigUpdateRequest, key: String, value: Boolean) {
        onMetadataUpdate(ProviderConfigUpdateRequest(bookMetadata = Some(metadata)))
    }

    override fun onMediaTypeSave(mediaType: KomfMediaType?) {
        onMetadataUpdate(
            ProviderConfigUpdateRequest(
                mediaType = mediaType
                    ?.let { Some(it) } ?: PatchValue.None))
    }

    override fun onNameMatchingModeSave(nameMatchingMode: KomfNameMatchingMode?) {
        onMetadataUpdate(
            ProviderConfigUpdateRequest(
                nameMatchingMode = nameMatchingMode
                    ?.let { Some(nameMatchingMode) } ?: PatchValue.None
            ))
    }

    override fun onAuthorRolesSave(roles: List<KomfAuthorRole>) {
        onMetadataUpdate(ProviderConfigUpdateRequest(authorRoles = Some(roles)))
    }

    override fun onArtistRolesSave(roles: List<KomfAuthorRole>) {
        onMetadataUpdate(ProviderConfigUpdateRequest(artistRoles = Some(roles)))
    }

    private fun onMetadataUpdate(update: ProviderConfigUpdateRequest) {
        onMetadataUpdate(update, provider)
    }

}

open class KomfAverProviderConfigState(
    provider: KomfProviders,
    config: ProviderConfigSnapshot?,
    private val onMetadataUpdate: (JsonObject, KomfProviders) -> Unit,
) : ProviderConfigState(config, provider) {

    override fun onPrioritySave(priority: Int) {
        patch("priority", JsonPrimitive(priority))
    }

    override fun onEnabledSave(enabled: Boolean) {
        patch("enabled", JsonPrimitive(enabled))
    }

    override fun onSeriesMetadataSave(metadata: SeriesMetadataConfigUpdateRequest, key: String, value: Boolean) {
        patch(
            "seriesMetadata",
            buildJsonObject { put(key, value) }
        )
    }

    override fun onBookMetadataSave(metadata: BookMetadataConfigUpdateRequest, key: String, value: Boolean) {
        patch(
            "bookMetadata",
            buildJsonObject { put(key, value) }
        )
    }

    override fun onMediaTypeSave(mediaType: KomfMediaType?) {
        patch("mediaType", mediaType?.let { JsonPrimitive(it.name) } ?: JsonNull)
    }

    override fun onNameMatchingModeSave(nameMatchingMode: KomfNameMatchingMode?) {
        patch("nameMatchingMode", nameMatchingMode?.let { JsonPrimitive(it.name) } ?: JsonNull)
    }

    override fun onAuthorRolesSave(roles: List<KomfAuthorRole>) {
        patch("authorRoles", JsonArray(roles.map { JsonPrimitive(it.name) }))
    }

    override fun onArtistRolesSave(roles: List<KomfAuthorRole>) {
        patch("artistRoles", JsonArray(roles.map { JsonPrimitive(it.name) }))
    }

    protected fun patch(key: String, value: JsonElement) {
        onMetadataUpdate(buildJsonObject { put(key, value) }, provider)
    }
}

class EHentaiConfigState(
    provider: KomfProviders,
    config: EHentaiConfigSnapshot?,
    private val onMetadataUpdate: (JsonObject, KomfProviders) -> Unit,
) : KomfAverProviderConfigState(provider, config?.providerConfig, onMetadataUpdate) {

    var useExhentai by mutableStateOf(config?.useExhentai ?: false)
        private set
    var cookieHeader by mutableStateOf(config?.cookieHeader)
        private set
    var cookies by mutableStateOf(config?.cookies ?: emptyMap())
        private set
    var userAgent by mutableStateOf(config?.userAgent)
        private set

    val hasCookieAuth: Boolean
        get() = cookieHeader != null || cookies.isNotEmpty()

    fun onUseExhentaiChange(value: Boolean) {
        useExhentai = value
        patch("useExhentai", JsonPrimitive(value))
    }

    fun onCookieHeaderChange(value: String) {
        if (value == SECRET_PLACEHOLDER) return
        cookieHeader = value.ifBlank { null }
        cookies = emptyMap()
        patch("cookieHeader", jsonOptionalString(cookieHeader))
        patch("cookies", JsonNull)
    }

    fun onUserAgentChange(value: String) {
        userAgent = value.ifBlank { null }
        patch("userAgent", jsonOptionalString(userAgent))
    }
}

class AniListConfigState(
    provider: KomfProviders,
    config: AniListConfigDto?,
    private val onMetadataUpdate: (AniListConfigUpdateRequest) -> Unit,
) : ProviderConfigState(config.toProviderConfigSnapshot(), provider) {

    var tagScoreThreshold by mutableStateOf(config?.tagsScoreThreshold ?: 60)
        private set
    var tagSizeLimit by mutableStateOf(config?.tagsSizeLimit ?: 15)
        private set

    fun onTagScoreThresholdChange(tagThreshold: Int) {
        this.tagScoreThreshold = tagThreshold
        onMetadataUpdate(AniListConfigUpdateRequest(tagsScoreThreshold = Some(tagThreshold)))
    }

    fun onTagSizeLimitChange(sizeLimit: Int) {
        this.tagSizeLimit = sizeLimit
        onMetadataUpdate(AniListConfigUpdateRequest(tagsSizeLimit = Some(tagSizeLimit)))
    }

    override fun onPrioritySave(priority: Int) {
        onMetadataUpdate(AniListConfigUpdateRequest(priority = Some(priority)))
    }

    override fun onEnabledSave(enabled: Boolean) {
        onMetadataUpdate(AniListConfigUpdateRequest(enabled = Some(enabled)))
    }

    override fun onSeriesMetadataSave(metadata: SeriesMetadataConfigUpdateRequest, key: String, value: Boolean) {
        onMetadataUpdate(AniListConfigUpdateRequest(seriesMetadata = Some(metadata)))
    }

    override fun onBookMetadataSave(metadata: BookMetadataConfigUpdateRequest, key: String, value: Boolean) {
    }

    override fun onMediaTypeSave(mediaType: KomfMediaType?) {
        onMetadataUpdate(
            AniListConfigUpdateRequest(
                mediaType = mediaType
                    ?.let { Some(it) } ?: PatchValue.None))
    }

    override fun onNameMatchingModeSave(nameMatchingMode: KomfNameMatchingMode?) {
        onMetadataUpdate(
            AniListConfigUpdateRequest(
                nameMatchingMode = nameMatchingMode
                    ?.let { Some(nameMatchingMode) } ?: PatchValue.None
            ))
    }

    override fun onAuthorRolesSave(roles: List<KomfAuthorRole>) {
        onMetadataUpdate(AniListConfigUpdateRequest(authorRoles = Some(roles)))
    }

    override fun onArtistRolesSave(roles: List<KomfAuthorRole>) {
        onMetadataUpdate(AniListConfigUpdateRequest(artistRoles = Some(roles)))
    }
}

class MangaDexConfigState(
    provider: KomfProviders,
    config: MangaDexConfigDto?,
    private val onMetadataUpdate: (MangaDexConfigUpdateRequest) -> Unit,
) : ProviderConfigState(config.toProviderConfigSnapshot(), provider) {

    var coverLanguages by mutableStateOf(config?.coverLanguages ?: listOf("en", "ja"))
    var links by mutableStateOf(config?.links ?: emptyList())

    fun onCoverLanguagesChange(languages: List<String>) {
        this.coverLanguages = languages
        onMetadataUpdate(MangaDexConfigUpdateRequest(coverLanguages = Some(languages)))
    }

    fun onLinkSelect(link: MangaDexLink) {
        links = links.addOrRemove(link)
        onMetadataUpdate(MangaDexConfigUpdateRequest(links = Some(links)))
    }

    override fun onPrioritySave(priority: Int) {
        onMetadataUpdate(MangaDexConfigUpdateRequest(priority = Some(priority)))
    }

    override fun onEnabledSave(enabled: Boolean) {
        onMetadataUpdate(MangaDexConfigUpdateRequest(enabled = Some(enabled)))
    }

    override fun onSeriesMetadataSave(metadata: SeriesMetadataConfigUpdateRequest, key: String, value: Boolean) {
        onMetadataUpdate(MangaDexConfigUpdateRequest(seriesMetadata = Some(metadata)))
    }

    override fun onBookMetadataSave(metadata: BookMetadataConfigUpdateRequest, key: String, value: Boolean) {
        onMetadataUpdate(MangaDexConfigUpdateRequest(bookMetadata = Some(metadata)))
    }

    override fun onMediaTypeSave(mediaType: KomfMediaType?) {
        onMetadataUpdate(
            MangaDexConfigUpdateRequest(
                mediaType = mediaType
                    ?.let { Some(it) } ?: PatchValue.None))
    }

    override fun onNameMatchingModeSave(nameMatchingMode: KomfNameMatchingMode?) {
        onMetadataUpdate(
            MangaDexConfigUpdateRequest(
                nameMatchingMode = nameMatchingMode
                    ?.let { Some(nameMatchingMode) } ?: PatchValue.None
            ))
    }

    override fun onAuthorRolesSave(roles: List<KomfAuthorRole>) {
        onMetadataUpdate(MangaDexConfigUpdateRequest(authorRoles = Some(roles)))
    }

    override fun onArtistRolesSave(roles: List<KomfAuthorRole>) {
        onMetadataUpdate(MangaDexConfigUpdateRequest(artistRoles = Some(roles)))
    }
}


class MangaBakaConfigState(
    provider: KomfProviders,
    config: MangaBakaConfigDto?,
    private val onMetadataUpdate: (MangaBakaConfigUpdateRequest) -> Unit,
) : ProviderConfigState(config.toProviderConfigSnapshot(), provider) {

    var mode by mutableStateOf(config?.mode ?: MangaBakaMode.API)
        private set

    fun onModeChange(mode: MangaBakaMode) {
        this.mode = mode
        onMetadataUpdate(MangaBakaConfigUpdateRequest(mode = Some(mode)))
    }

    override fun onPrioritySave(priority: Int) =
        onMetadataUpdate(MangaBakaConfigUpdateRequest(priority = Some(priority)))

    override fun onEnabledSave(enabled: Boolean) =
        onMetadataUpdate(MangaBakaConfigUpdateRequest(enabled = Some(enabled)))

    override fun onSeriesMetadataSave(metadata: SeriesMetadataConfigUpdateRequest, key: String, value: Boolean) =
        onMetadataUpdate(MangaBakaConfigUpdateRequest(seriesMetadata = Some(metadata)))

    override fun onBookMetadataSave(metadata: BookMetadataConfigUpdateRequest, key: String, value: Boolean) = Unit

    override fun onMediaTypeSave(mediaType: KomfMediaType?) =
        onMetadataUpdate(
            MangaBakaConfigUpdateRequest(
                mediaType = mediaType
                    ?.let { Some(it) } ?: PatchValue.None))

    override fun onNameMatchingModeSave(nameMatchingMode: KomfNameMatchingMode?) =
        onMetadataUpdate(
            MangaBakaConfigUpdateRequest(
                nameMatchingMode = nameMatchingMode
                    ?.let { Some(nameMatchingMode) } ?: PatchValue.None
            ))

    override fun onAuthorRolesSave(roles: List<KomfAuthorRole>) =
        onMetadataUpdate(MangaBakaConfigUpdateRequest(authorRoles = Some(roles)))

    override fun onArtistRolesSave(roles: List<KomfAuthorRole>) =
        onMetadataUpdate(MangaBakaConfigUpdateRequest(artistRoles = Some(roles)))
}
