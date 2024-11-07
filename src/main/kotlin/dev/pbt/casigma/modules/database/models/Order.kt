package dev.pbt.casigma.modules.database.models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object Orders : IntIdTable() {
    val name: Column<String> = varchar("name", 50)
    val status: Column<OrderStatus> = enumeration("status", OrderStatus::class)
    val tableNo: Column<Int> = integer("table_no")
    val createdAt: Column<LocalDateTime> = datetime("created_at").defaultExpression(CurrentDateTime)
}

enum class OrderStatus(val status: String) {
    Pending("pending"),
    Completed("completed"),
    Cancelled("confirmed")
}

object Order : Table("orders") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 50)
    val status = enumeration("status", OrderStatus::class)
    val additionalNotes = text("additional_notes").nullable()
    val tableNo = integer("table_no")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}