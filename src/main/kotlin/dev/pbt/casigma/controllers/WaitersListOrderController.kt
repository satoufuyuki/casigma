package dev.pbt.casigma.controllers

import dev.pbt.casigma.modules.database.DB
import dev.pbt.casigma.modules.database.models.MenuItem
import dev.pbt.casigma.utils.OrderUtils
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import java.net.URL
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.ResourceBundle
import kotlin.collections.component1
import kotlin.collections.component2


data class OrderWithItems(
    val id: Int,
    val name: String,
    val additionalNotes: String?,
    val createdAt: LocalDateTime,
    val tableNo: Int,
    var orders: ArrayList<MenuItem>
)

class WaitersListOrderController(private val db: DB, private val orderUtils: OrderUtils) : Initializable {
    @FXML
    lateinit var listContainer: VBox
    @FXML
    lateinit var currentOrderDate: Label
    @FXML
    lateinit var currentOrderTime: Label
    @FXML
    lateinit var currentOrderCustomerName: Label
    @FXML
    lateinit var additionalNotes: Label
    @FXML
    lateinit var orderListScrollPane: ScrollPane
    @FXML
    lateinit var totalLabel: Label

    val orderListGridPane = GridPane()
    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        renderList("all")

        // init orderlist grid pane column
        orderListGridPane.hgap = 5.0
        val colConstraint = ColumnConstraints(240.0, 240.0, 240.0)
        colConstraint.hgrow = javafx.scene.layout.Priority.ALWAYS
        orderListGridPane.columnConstraints.addAll(colConstraint, colConstraint)

    }

    fun renderList(filter: String) {
        val items = mutableMapOf<Int, OrderWithItems>()
        when (filter) {
            "all" -> {
                val orders = orderUtils.fetchOrders(null)
                items.putAll(orders)
            }
        }

        listContainer.children.clear()
        items.forEach { (_, order) ->
            val hBox = HBox()
            hBox.prefHeight = 100.0
            hBox.prefWidth = 200.0
            hBox.style = "-fx-background-color: #ffffff; -fx-border-radius: 10; -fx-background-radius: 10;"
            // padding left and right
            hBox.padding = javafx.geometry.Insets(0.0, 25.0, 0.0, 25.0)

            val tableLabel = Label("Table No. ${order.tableNo}")
            tableLabel.maxHeight = Double.MAX_VALUE
            tableLabel.opacity = 0.6
            tableLabel.font = javafx.scene.text.Font.font("Poppins Medium", 20.0)
            hBox.children.add(tableLabel)

            val sumPrice = order.orders.sumOf { it.price.toDouble() }
            val formatted = orderUtils.formatRupiah(sumPrice.toInt())
            val priceLabel = Label(formatted)
            priceLabel.maxHeight = Double.MAX_VALUE
            priceLabel.maxWidth = Double.MAX_VALUE
            priceLabel.minWidth = Double.MIN_VALUE
            priceLabel.font = javafx.scene.text.Font.font("Poppins Medium", 20.0)
            priceLabel.alignment = javafx.geometry.Pos.CENTER_RIGHT
            HBox.setHgrow(priceLabel, javafx.scene.layout.Priority.ALWAYS)
            hBox.children.add(priceLabel)

            hBox.onMouseClicked = javafx.event.EventHandler {
                onSelectOrder(order)
            }

            listContainer.children.add(hBox)
        }
    }

    // Handle order change
    fun onSelectOrder(order: OrderWithItems) {
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm 'WIB'")

        currentOrderDate.text = order.createdAt.format(dateFormatter)
        currentOrderTime.text = order.createdAt.format(timeFormatter)

        currentOrderCustomerName.text = order.name
        additionalNotes.text = order.additionalNotes ?: "No additional notes"

        orderUtils.renderOrderList(order.orders, orderListGridPane, orderListScrollPane, totalLabel)
    }
}