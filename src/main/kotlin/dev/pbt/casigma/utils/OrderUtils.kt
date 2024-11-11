package dev.pbt.casigma.utils

import dev.pbt.casigma.controllers.OrderWithItems
import dev.pbt.casigma.modules.AlertProvider
import dev.pbt.casigma.modules.database.DB
import dev.pbt.casigma.modules.database.models.Menu
import dev.pbt.casigma.modules.database.models.MenuItem
import dev.pbt.casigma.modules.database.models.Order
import dev.pbt.casigma.modules.database.models.OrderItem
import dev.pbt.casigma.modules.database.models.OrderStatus
import javafx.geometry.Pos
import javafx.scene.control.ButtonType
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextInputDialog
import javafx.scene.layout.GridPane
import javafx.scene.text.Font
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.forEach

class OrderUtils(private val db: DB, private val alertProvider: AlertProvider) {
    fun formatItemList(orderList: ArrayList<MenuItem>): List<Map<String, Any>> {
        val itemMap = orderList.associateBy { it.id }

        return itemMap.entries.map { (id, data) ->
            val itemName = itemMap[id]?.name ?: "Unknown Item"
            return@map mapOf(
                "title" to "${data.quantity}x $itemName",
                "price" to formatRupiah(data.price.toInt() * data.quantity)
            )
        }
    }

    fun renderOrderList(orderList: ArrayList<MenuItem>, orderListGridPane: GridPane, orderListScrollPane: ScrollPane, totalLabel: Label) {
        orderListGridPane.children.clear()
        val formattedOrderList = formatItemList(orderList)
        formattedOrderList.forEachIndexed { index, item ->
            val itemLabel = Label(item["title"].toString())
            itemLabel.font = Font("Poppins Medium", 20.0)
            itemLabel.prefWidth = 240.0
            orderListGridPane.add(itemLabel, 0, index)

            val priceLabel = Label(item["price"].toString() )
            priceLabel.font = Font("Poppins Medium", 20.0)
            priceLabel.prefWidth = 220.0
            priceLabel.opacity = 0.6
            priceLabel.alignment = Pos.CENTER_RIGHT
            orderListGridPane.add(priceLabel, 1, index)
        }

        orderListScrollPane.content = orderListGridPane
        totalLabel.text = this.formatRupiah(orderList.sumOf { it.price.toInt() * it.quantity })
    }

    fun fetchOrders(status: OrderStatus?): Map<Int, OrderWithItems> {
        val ordersMap = mutableMapOf<Int, OrderWithItems>()

        transaction (db.conn) {
            val query = Join(
                Order, OrderItem,
                onColumn = Order.id, otherColumn = OrderItem.orderId,
                joinType = JoinType.INNER,
            ).join(Menu, JoinType.INNER, OrderItem.menuId, Menu.id)

            var res: List<ResultRow> = arrayListOf()
            if (status != null) {
                res = query.selectAll()
                    .where { Order.status eq status }
                    .sortedBy { Order.createdAt }.toList()
            }  else {
                res = query.selectAll()
                    .sortedBy { Order.createdAt }.toList()
            }

            res.forEach {
                val orderId = it[Order.id]
                val menuItem = MenuItem(
                    id = it[Menu.id],
                    name = it[Menu.name],
                    price = it[Menu.price],
                    category = it[Menu.category],
                    image = it[Menu.image],
                    createdAt = it[Menu.createdAt],
                    quantity = it[OrderItem.quantity]
                )

                if (ordersMap.containsKey(orderId)) {
                    ordersMap[orderId]?.orders?.add(menuItem)
                } else {
                    ordersMap[orderId] = OrderWithItems(
                        id = orderId,
                        name = it[Order.name],
                        createdAt = it[Order.createdAt],
                        additionalNotes = it[Order.additionalNotes],
                        tableNo = it[Order.tableNo],
                        orders = arrayListOf(menuItem)
                    )
                }
            }
        }

        return ordersMap
    }

    fun fetchOrderData(id: Int): OrderWithItems? {
        var orderData: OrderWithItems? = null
        return transaction (db.conn) {
            val query = Join(
                Order, OrderItem,
                onColumn = Order.id, otherColumn = OrderItem.orderId,
                joinType = JoinType.LEFT,
                additionalConstraint = { Order.id eq id }
            ).join(Menu, JoinType.LEFT, OrderItem.menuId, Menu.id)

            val res = query.selectAll()
                .where { Order.id eq id }
                .sortedBy { Order.createdAt }.toList()

            if (res.isEmpty()) {
                alertProvider.error("Order with id $id not found")
                return@transaction null
            }

            res.forEach {
                val menuItem = if (it[Menu.id] != null) {
                    MenuItem(
                        id = it[Menu.id],
                        name = it[Menu.name],
                        price = it[Menu.price],
                        category = it[Menu.category],
                        image = it[Menu.image],
                        createdAt = it[Menu.createdAt],
                        quantity = it[OrderItem.quantity]
                    )
                } else {
                    null
                }

                if (orderData == null) {
                    orderData = OrderWithItems(
                        id = id,
                        name = it[Order.name],
                        createdAt = it[Order.createdAt],
                        additionalNotes = it[Order.additionalNotes],
                        tableNo = it[Order.tableNo],
                        orders = if (menuItem != null) arrayListOf(menuItem) else arrayListOf()
                    )
                } else {
                    menuItem?.let { orderData!!.orders.add(it) }
                }
            }

            return@transaction orderData
        }
    }

    fun formatRupiah(amount: Int): String {
        return "Rp${NumberFormat.getNumberInstance(Locale.GERMANY).format(amount)}"
    }

    fun newOrderDialog() {
        transaction(db.conn) {
            while (true) {
                val tableNoDialog = TextInputDialog()
                tableNoDialog.title = "New Order"
                tableNoDialog.headerText = "Create a new order"
                tableNoDialog.isResizable = false
                tableNoDialog.contentText = "Enter table number:"
                val tableNoResult = tableNoDialog.showAndWait()


                if (tableNoResult.isEmpty || tableNoResult == ButtonType.CANCEL) {
                    break
                }

                val tableNo = tableNoResult?.get()?.toString()?.toIntOrNull()
                if (tableNo == null) {
                    alertProvider.errorWait("Table number must be a number")
                    continue
                }

                val existingTableNo = Order.select(Order.status).where { (Order.tableNo eq tableNo.toInt()) and (Order.status eq OrderStatus.Pending) }.count()
                if (existingTableNo > 0) {
                    alertProvider.errorWait("Table number $tableNo still has pending order")
                    continue
                }

                val customerNameDialog = TextInputDialog()
                customerNameDialog.title = "New Order"
                customerNameDialog.headerText = "Create a new order"
                customerNameDialog.contentText = "Enter customer name:"
                val customerNameResult = customerNameDialog.showAndWait()
                if (customerNameResult.isEmpty || customerNameResult == ButtonType.CANCEL) {
                    break
                }

                Order.insert {
                    it[name] = customerNameResult.get()
                    it[status] = OrderStatus.Pending
                    it[Order.tableNo] = tableNo.toInt()
                }

                alertProvider.success("New order created for table $tableNo")
                break
            }
        }

    }
}