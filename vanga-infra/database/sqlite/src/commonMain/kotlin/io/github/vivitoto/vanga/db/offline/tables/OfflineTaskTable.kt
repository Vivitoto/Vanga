package io.github.vivitoto.vanga.db.offline.tables

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.json.json
import io.github.vivitoto.vanga.db.JsonDbDefault
import io.github.vivitoto.vanga.offline.tasks.model.TaskData

object OfflineTaskTable : Table("TASK") {
    val uniqueName = text("unique_name")
    val priority = integer("priority")
    val status = text("status")
    val task = json<TaskData>("task", JsonDbDefault)

    val createdDate = long("created_date")

    override val primaryKey = PrimaryKey(uniqueName)
}