package dev.pbt.casigma.modules.database.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object User : Table("users") {
    val id = integer("id").autoIncrement()
    val email = varchar("email", 50).uniqueIndex()
    val password = text("password")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")

    override val primaryKey = PrimaryKey(id)
}