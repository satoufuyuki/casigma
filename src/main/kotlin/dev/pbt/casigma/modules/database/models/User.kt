package dev.pbt.casigma.modules.database.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

enum class UserRole(val role: String) {
    Admin("admin"),
    Waiters("waiters"),
    Cashier("cashier"),
    Chef("chef")
}

object User : Table("users") {
    val id = integer("id").autoIncrement()
    val email = varchar("email", 50).uniqueIndex()
    val name = varchar("name", 50)
    val role = enumeration("role", UserRole::class)
    val password = text("password")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}