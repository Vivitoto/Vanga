package io.github.vivitoto.vanga.updates

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

@Serializable(with = AppVersionSerializer::class)
data class AppVersion(
    val major: Int,
    val minor: Int,
    val patch: Int
) : Comparable<AppVersion> {

    companion object {
        val current = AppVersion(1, 0, 10)

        private val versionPattern = Regex("v?(\\d+)[._](\\d+)(?:[._](\\d+))?")

        fun fromString(value: String): AppVersion {
            val version = value.trim()
                .removePrefix("v")
                .replace('_', '.')
                .split(".")
            return when (version.size) {
                3 -> AppVersion(version[0].toInt(), version[1].toInt(), version[2].toInt())
                2 -> AppVersion(version[0].toInt(), version[1].toInt(), 0)
                else -> error("Can't parse version number")
            }
        }

        fun fromGithubRelease(release: GithubRelease): AppVersion {
            sequenceOf(release.tagName, release.name.orEmpty())
                .plus(release.assets.asSequence().map { it.name })
                .forEach { candidate ->
                    versionPattern.find(candidate)?.value?.let { return fromString(it) }
                }

            error("Can't parse version number from GitHub release ${release.tagName}")
        }
    }

    override fun compareTo(other: AppVersion): Int {
        return compareBy(
            AppVersion::major,
            AppVersion::minor,
            AppVersion::patch
        ).compare(this, other)
    }

    override fun toString(): String {
        return "$major.$minor.$patch"
    }
}

data class AppRelease(
    val version: AppVersion,
    val publishDate: Instant,
    val releaseNotesBody: String,
    val htmlUrl: String,

    val assetName: String?,
    val assetUrl: String?,
)

object AppVersionSerializer : KSerializer<AppVersion> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("AppVersion", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): AppVersion {
        val version = decoder.decodeString()
        return AppVersion.fromString(version)
    }

    override fun serialize(encoder: Encoder, value: AppVersion) {
        encoder.encodeString(value.toString())
    }
}
