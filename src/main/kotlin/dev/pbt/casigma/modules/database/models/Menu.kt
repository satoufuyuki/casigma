package dev.pbt.casigma.modules.database.models

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object Menus : IntIdTable() {
    val name = varchar("name", 50)
    val price = float("price")
    val category = enumeration("category", MenuCategory::class)
    val image = varchar("image", 50)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}

data class MenuItem(
    val id: Int,
    val name: String,
    val price: Float,
    val quantity: Int,
    val category: MenuCategory,
    val image: String,
    val createdAt: LocalDateTime
)

enum class MenuCategory(val category: String) {
    Food("food"),
    Beverages("beverages"),
    Dessert("dessert")
}

object Menu : Table("menus") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 50)
    val price = float("price")
    val category = enumeration("category", MenuCategory::class)
    val image = varchar("image", 50)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}