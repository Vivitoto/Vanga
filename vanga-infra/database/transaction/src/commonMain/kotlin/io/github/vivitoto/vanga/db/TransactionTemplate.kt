package io.github.vivitoto.vanga.db

interface TransactionTemplate {
    suspend fun <T> execute(statement: suspend () -> T): T
}