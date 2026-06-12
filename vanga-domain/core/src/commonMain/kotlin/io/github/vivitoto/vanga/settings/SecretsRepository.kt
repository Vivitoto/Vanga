package io.github.vivitoto.vanga.settings


interface SecretsRepository {

    suspend fun getCookie(url: String): String?

    suspend fun setCookie(url: String, cookie: String)

    suspend fun deleteCookie(url: String)

}