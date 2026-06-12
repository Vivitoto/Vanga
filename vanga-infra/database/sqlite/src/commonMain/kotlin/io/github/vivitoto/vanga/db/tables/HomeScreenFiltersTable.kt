package io.github.vivitoto.vanga.db.tables

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.json.json
import io.github.vivitoto.vanga.db.JsonDbDefault
import io.github.vivitoto.vanga.homefilters.HomeScreenFilter

object HomeScreenFiltersTable : Table("HomeScreenFilters") {
    val version = integer("version")
    val filters = json<List<HomeScreenFilter>>("filters", JsonDbDefault)

    override val primaryKey = PrimaryKey(version)
}