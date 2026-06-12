package io.github.vivitoto.vanga.db.settings

import io.github.vivitoto.vanga.fonts.UserFont
import io.github.vivitoto.vanga.fonts.UserFontsRepository

class NoopFontsRepository : UserFontsRepository {
    override suspend fun getAllFonts(): List<UserFont> {
        return emptyList()
    }

    override suspend fun getFont(name: String): UserFont? {
        return null
    }

    override suspend fun putFont(font: UserFont) {
    }

    override suspend fun deleteFont(font: UserFont) {
    }
}