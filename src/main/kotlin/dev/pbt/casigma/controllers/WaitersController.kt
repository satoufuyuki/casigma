package dev.pbt.casigma.controllers

import dev.pbt.casigma.modules.AlertProvider
import dev.pbt.casigma.modules.database.DB
import dev.pbt.casigma.modules.database.models.*
import dev.pbt.casigma.utils.OrderUtils
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.image.Image
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.RowConstraints
import javafx.scene.layout.VBox
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.URL
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.ResourceBundle
import kotlin.text.insert

class WaitersController(private val db: DB, private val orderUtils: OrderUtils, private val alertProvider: AlertProvider): Initializable {
    @FXML
    lateinit var scrollPane: ScrollPane
    @FXML
    lateinit var orderListScrollPane: ScrollPane
    @FXML
    lateinit var totalLabel: Label
    @FXML
    lateinit var orderComboBox: ComboBox<String>
    @FXML
    lateinit var currentOrderDate: Label
    @FXML
    lateinit var currentOrderTime: Label
    @FXML
    lateinit var currentOrderCustomerName: Label
    @FXML
    lateinit var additionalNotes: Label
    @FXML
    lateinit var addOrder: Button

    lateinit var gridPane: GridPane

    var orderListGridPane: GridPane = GridPane()
    var menu: ArrayList<MenuItem> = ArrayList()
    var currentTab = MenuCategory.Food
    var currentOrderId: Int? = null
    var currentOrderData: OrderWithItems? = null

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        transaction(db.conn) {
            Menu.selectAll().where {
                Menu.category eq currentTab
            }.orderBy(Menu.name, SortOrder.ASC).forEach {
                menu.add(MenuItem(
                    it[Menu.id],
                    it[Menu.name],
                    it[Menu.price],
                    0,
                    it[Menu.category],
                    it[Menu.image],
                    it[Menu.createdAt]
                ))
            }

            // init orderlist grid pane column
            orderListGridPane.hgap = 5.0
            val colConstraint = ColumnConstraints(240.0, 240.0, 240.0)
            colConstraint.hgrow = javafx.scene.layout.Priority.ALWAYS
            orderListGridPane.columnConstraints.addAll(colConstraint, colConstraint)

            // fetch all tables with pending orders
            Order.select(Order.tableNo).where { Order.status eq OrderStatus.Pending }.distinct().forEach {
                orderComboBox.items.add("Table ${it[Order.tableNo]}")
            }

            orderComboBox.setOnAction {
                handleOrderComboBox()
            }

            renderMenu()
        }

        addOrder.onAction = EventHandler {
            handleAddOrderButton()
        }
    }

    fun renderMenu() {
        if (menu.isEmpty()) {
            return
        }

        var itemGrid = GridPane()
        // make the hgap auto to fit the scroll pane
        itemGrid.hgap = 40.0
        itemGrid.vgap = 20.0

        // chunk the menu to 2d array with 4 items each
        val chunkedMenu = menu.chunked(4)
        chunkedMenu.forEachIndexed { rowIndex, row ->
            val colConstraint = ColumnConstraints(240.0, 240.0, 240.0)
            colConstraint.hgrow = javafx.scene.layout.Priority.ALWAYS
            itemGrid.columnConstraints.add(colConstraint)
            row.forEachIndexed { columnIndex, item ->
                val rowConstraint = RowConstraints(370.0, 370.0, 370.0)
                rowConstraint.vgrow = javafx.scene.layout.Priority.ALWAYS
                itemGrid.rowConstraints.add(rowConstraint)
                itemGrid.add(constructMenuItem(item, columnIndex, rowIndex), columnIndex, rowIndex)
            }
        }

        scrollPane.background = javafx.scene.layout.Background.EMPTY
        gridPane = itemGrid
        scrollPane.content = itemGrid
    }

    fun constructMenuItem(item: MenuItem, col: Int, row: Int): VBox {
        // construct menu item view
        var itemVBox = VBox()
        itemVBox.alignment = javafx.geometry.Pos.CENTER
        itemVBox.prefHeight = 340.0
        itemVBox.prefWidth = 260.0
        itemVBox.spacing = 15.0
        itemVBox.style = "-fx-background-color: #ffffff; -fx-background-radius: 15;"
        itemVBox.padding = javafx.geometry.Insets(20.0, 10.0, 20.0, 10.0)

        // construct image view
        var imageView = javafx.scene.image.ImageView()
        imageView.fitHeight = 165.0
        imageView.fitWidth = 230.0
        imageView.isPreserveRatio = true

        // load image from resources
        val imageUrl = javaClass.getResource("/dev/pbt/casigma/assets/images/foods/${item.image}")
        if (imageUrl != null) {
            imageView.image = Image(imageUrl.toString())
        } else {
            println("Image not found: /dev/pbt/casigma/assets/images/foods/${item.image}")
        }

        var currentQuantity = currentOrderData?.orders?.find { it.id == item.id }?.quantity ?: 0
        // construct vbox for text
        var textVBox = VBox()
        textVBox.alignment = javafx.geometry.Pos.CENTER
        var nameLabel = javafx.scene.control.Label(item.name)
        nameLabel.font = javafx.scene.text.Font("Poppins Medium", 25.0)
        var priceLabel = javafx.scene.control.Label(orderUtils.formatRupiah(
            item.price.toInt()
        ))
        priceLabel.font = javafx.scene.text.Font("Poppins Medium", 20.0)
        textVBox.children.addAll(nameLabel, priceLabel)

        // construct hbox for quantity
        var quantityHBox = javafx.scene.layout.HBox()
        quantityHBox.alignment = javafx.geometry.Pos.CENTER
        quantityHBox.spacing = 20.0
        var minusButton = javafx.scene.control.Button("-")
        minusButton.style = "-fx-background-color: none; -fx-border-color: #000000; -fx-border-width: 1px; -fx-border-radius: 5px;"
        minusButton.font = javafx.scene.text.Font("Poppins Regular", 20.0)
        var quantityLabel = Label(currentQuantity.toString())
        quantityLabel.font = javafx.scene.text.Font("Poppins Regular", 20.0)
        var plusButton = javafx.scene.control.Button("+")
        plusButton.style = "-fx-background-color: none; -fx-border-color: #000000; -fx-border-width: 1px; -fx-border-radius: 5px;"
        plusButton.font = javafx.scene.text.Font("Poppins Regular", 20.0)

        minusButton.setOnAction {
            handleQuantityButton("decrement", col, row, item)
        }

        plusButton.setOnAction {
            handleQuantityButton("increment", col, row, item)
        }

        quantityHBox.children.addAll(minusButton, quantityLabel, plusButton)

        itemVBox.children.addAll(imageView, textVBox, quantityHBox)

        return itemVBox
    }

    private fun handleQuantityButton(mode: String, col: Int, row: Int, item: MenuItem) {
        if (currentOrderId == null) {
            return
        }

        val existingOrderData = currentOrderData?.orders?.find { it.id == item.id }
        if (existingOrderData == null) {
            currentOrderData?.orders?.add(item.copy(quantity = 0))
        }

        val itemVBox = gridPane.children[(row * 4) + col] as VBox
        val textHBox = itemVBox.children[2] as HBox
        val quantityLabel = textHBox.children[1] as Label
        val currentCount = quantityLabel.text.toInt()
        when (mode) {
            "increment" -> {
                quantityLabel.text = (currentCount + 1).toString()
                currentOrderData?.orders = currentOrderData!!.orders.map { orderItem ->
                    if (orderItem.id == item.id) {
                        orderItem.copy(quantity = orderItem.quantity + 1)
                    } else {
                        orderItem
                    }
                } as ArrayList<MenuItem>
            }
            "decrement" -> {
                if (currentCount > 0) {
                    currentOrderData?.orders = currentOrderData!!.orders.map { orderItem ->
                        if (orderItem.id == item.id) {
                            orderItem.copy(quantity = orderItem.quantity - 1)
                        } else {
                            orderItem
                        }
                    } as ArrayList<MenuItem>
                    quantityLabel.text = (currentCount - 1).toString()
                }

                currentOrderData?.orders?.filter{ it.quantity == 0 }?.forEach {
                    currentOrderData?.orders?.remove(it)
                }
            }
        }

        orderUtils.renderOrderList(currentOrderData!!.orders, orderListGridPane, orderListScrollPane, totalLabel)
    }

    private fun handleOrderComboBox() {
        val selectedTable = orderComboBox.selectionModel.selectedItem
        if (selectedTable != null) {
            currentOrderId = selectedTable.split(" ")[1].toInt()
        }

        if (currentOrderId === null) return

        val orderData = orderUtils.fetchOrderData(currentOrderId!!)
        if (orderData !== null) {
            currentOrderData = orderData
            orderUtils.renderOrderList(orderData.orders, orderListGridPane, orderListScrollPane, totalLabel)

            val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm 'WIB'")

            currentOrderDate.text = orderData.createdAt.format(dateFormatter)
            currentOrderTime.text = orderData.createdAt.format(timeFormatter)

            currentOrderCustomerName.text = orderData.name
            additionalNotes.text = orderData.additionalNotes ?: "No additional notes"

            renderMenu()
        }
    }

    private fun processOrderItem(orderId: Int, menuItem: MenuItem) {
        val existingOrder = OrderItem.selectAll().where {
            (OrderItem.orderId eq orderId) and (OrderItem.menuId eq menuItem.id)
        }.firstOrNull()

        if (existingOrder !== null) {
            OrderItem.update({ OrderItem.id eq existingOrder[OrderItem.id] }) {
                it[quantity] = menuItem.quantity
            }
        } else {
            OrderItem.insert {
                it[OrderItem.orderId] = orderId
                it[menuId] = menuItem.id
                it[quantity] = 1
            }
        }
    }

    fun handleAddOrderButton() {
        if (currentOrderId === null) return

        transaction(db.conn) {
            currentOrderData?.orders?.forEach { menuItem ->
                processOrderItem(currentOrderId!!, menuItem)
            }
        }

        alertProvider.success("Order added successfully")
        orderUtils.renderOrderList(currentOrderData!!.orders, orderListGridPane, orderListScrollPane, totalLabel)

    }
}