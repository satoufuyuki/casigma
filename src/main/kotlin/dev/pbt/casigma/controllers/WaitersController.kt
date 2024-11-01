package dev.pbt.casigma.controllers

import dev.pbt.casigma.modules.database.DB
import dev.pbt.casigma.modules.database.models.Menu
import dev.pbt.casigma.modules.database.models.MenuCategory
import dev.pbt.casigma.modules.database.models.MenuItem
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.image.Image
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.RowConstraints
import javafx.scene.layout.VBox
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.URL
import java.text.NumberFormat
import java.util.ResourceBundle

class WaitersController(db: DB): Initializable {
    @FXML
    lateinit var scrollPane: ScrollPane

    @FXML
    lateinit var orderListScrollPane: ScrollPane

    @FXML
    lateinit var totalLabel: Label

    lateinit var gridPane: GridPane

    var orderListGridPane: GridPane = GridPane()
    var orderList: List<MenuItem> = ArrayList()
    var menu: ArrayList<MenuItem> = ArrayList()
    var conn = db.conn
    var currentTab = MenuCategory.Food

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        transaction(conn) {
            Menu.selectAll().where {
                Menu.category eq currentTab
            }.orderBy(Menu.name, SortOrder.ASC).forEach {
                menu.add(MenuItem(
                    it[Menu.id],
                    it[Menu.name],
                    it[Menu.price],
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

            renderMenu()
        }
    }

    fun formatItemList(): List<Map<String, Any>> {
        val itemCountMap = orderList.groupingBy { it.id }.eachCount()
        val itemMap = orderList.associateBy { it.id }

        return itemCountMap.entries.map { (id, count) ->
            val itemName = itemMap[id]?.name ?: "Unknown Item"
            return@map mapOf(
                "title" to "${count}x $itemName",
                "price" to "Rp${NumberFormat.getNumberInstance(java.util.Locale.GERMANY).format(itemMap[id]?.price?.toInt())}"
            )
        }
    }

    fun renderOrderList() {
        orderListGridPane.children.clear()
        val formattedOrderList = formatItemList()
        formattedOrderList.forEachIndexed { index, item ->
            val itemLabel = Label(item["title"].toString())
            itemLabel.font = javafx.scene.text.Font("Poppins Medium", 20.0)
            itemLabel.prefWidth = 240.0
            orderListGridPane.add(itemLabel, 0, index)

            val priceLabel = Label(item["price"].toString())
            priceLabel.font = javafx.scene.text.Font("Poppins Medium", 20.0)
            priceLabel.prefWidth = 220.0
            priceLabel.opacity = 0.6
            priceLabel.alignment = Pos.CENTER_RIGHT
            orderListGridPane.add(priceLabel, 1, index)
        }

        orderListScrollPane.content = orderListGridPane
        totalLabel.text = "Rp${NumberFormat.getNumberInstance(java.util.Locale.GERMANY).format(orderList.sumOf { it.price.toInt() })}"
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

        // construct vbox for text
        var textVBox = VBox()
        textVBox.alignment = javafx.geometry.Pos.CENTER
        var nameLabel = javafx.scene.control.Label(item.name)
        nameLabel.font = javafx.scene.text.Font("Poppins Medium", 25.0)
        var priceLabel = javafx.scene.control.Label("Rp${NumberFormat.getNumberInstance(java.util.Locale.GERMANY).format(item.price.toInt())}")
        priceLabel.font = javafx.scene.text.Font("Poppins Medium", 20.0)
        textVBox.children.addAll(nameLabel, priceLabel)

        // construct hbox for quantity
        var quantityHBox = javafx.scene.layout.HBox()
        quantityHBox.alignment = javafx.geometry.Pos.CENTER
        quantityHBox.spacing = 20.0
        var minusButton = javafx.scene.control.Button("-")
        minusButton.style = "-fx-background-color: none; -fx-border-color: #000000; -fx-border-width: 1px; -fx-border-radius: 5px;"
        minusButton.font = javafx.scene.text.Font("Poppins Regular", 20.0)
        var quantityLabel = javafx.scene.control.Label("0")
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

    fun handleQuantityButton(mode: String, col: Int, row: Int, item: MenuItem) {
        val itemVBox = gridPane.children[(row * 4) + col] as VBox
        val textHBox = itemVBox.children[2] as HBox
        val quantityLabel = textHBox.children[1] as Label
        val currentCount = quantityLabel.text.toInt()
        when (mode) {
            "increment" -> {
                quantityLabel.text = (currentCount + 1).toString()
                orderList = orderList.plus(item)
            }
            "decrement" -> {
                if (currentCount > 0) {
                    orderList = orderList.minus(item)
                    quantityLabel.text = (currentCount - 1).toString()
                }
            }
        }

        renderOrderList()
    }
}