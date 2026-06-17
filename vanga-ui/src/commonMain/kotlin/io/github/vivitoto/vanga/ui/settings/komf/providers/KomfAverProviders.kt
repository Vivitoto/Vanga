package io.github.vivitoto.vanga.ui.settings.komf.providers

import snd.komf.api.KomfProviders
import snd.komf.api.UnknownKomfProvider

internal const val NHENTAI_PROVIDER_KEY = "NHENTAI"
internal const val EHENTAI_PROVIDER_KEY = "EHENTAI"
internal const val SECRET_PLACEHOLDER = "********"

internal object KomfAverProviders {
    val NHENTAI = UnknownKomfProvider(NHENTAI_PROVIDER_KEY)
    val EHENTAI = UnknownKomfProvider(EHENTAI_PROVIDER_KEY)
    val entries: List<KomfProviders> = listOf(NHENTAI, EHENTAI)
}

internal val KomfProviders.providerKey: String
    get() = when (this) {
        is UnknownKomfProvider -> name
        is Enum<*> -> name
        else -> toString()
    }

internal val KomfProviders.providerConfigJsonKey: String
    get() = when (providerKey) {
        NHENTAI_PROVIDER_KEY -> "nhentai"
        EHENTAI_PROVIDER_KEY -> "ehentai"
        else -> providerKey
    }
