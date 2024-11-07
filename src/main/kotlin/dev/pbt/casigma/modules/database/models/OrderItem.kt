package dev.pbt.casigma.modules.database.models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object OrderItems : IntIdTable() {
    val menuId = reference("menu_id", Menus)
    val orderId = reference("order_id", Orders)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}

data class OrderItemModel(
    val id: Int,
    val menuId: Int,
    val orderId: Int,
    val createdAt: LocalDateTime
)

object OrderItem : Table("order_items") {
    val id = integer("id").autoIncrement()
    val menuId = integer("menu_id").references(Menu.id, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val orderId = integer("order_id").references(Order.id, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}