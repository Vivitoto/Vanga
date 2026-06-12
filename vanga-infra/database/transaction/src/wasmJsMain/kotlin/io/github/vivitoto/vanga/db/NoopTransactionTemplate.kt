package io.github.vivitoto.vanga.db

class NoopTransactionTemplate : TransactionTemplate {
    override suspend fun <T> execute(statement: suspend () -> T): T {
        return statement()
    }
}