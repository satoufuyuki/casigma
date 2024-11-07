package dev.pbt.casigma.modules

import dev.pbt.casigma.CasigmaApplication
import dev.pbt.casigma.controllers.WaitersController
import dev.pbt.casigma.controllers.WaitersListOrderController
import dev.pbt.casigma.modules.database.models.UserRole
import dev.pbt.casigma.utils.OrderUtils
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.stage.Stage

data class NavigationEntry(val fxmlFile: String, val controllerClass: Class<*>)

class NavigationProvider(private val app: CasigmaApplication, private val userProvider: UserProvider, private val orderUtils: OrderUtils) {
    private val history = mutableListOf<NavigationEntry>()

    fun navigate(fxmlFile: String, controllerClass: Class<*>) {
        history.add(NavigationEntry(fxmlFile, controllerClass))
        val fxmlLoader = FXMLLoader(CasigmaApplication::class.java.getResource(fxmlFile))
        fxmlLoader.setControllerFactory { app.koin.get(controllerClass.kotlin) }
        val fontCss = app.loadFonts()
        val scene = Scene(fxmlLoader.load(), 1920.0, 1080.0)
        scene.stylesheets.add(fontCss)
        scene.stylesheets.add(fontCss)
        app.primaryStage.isResizable = true
        app.primaryStage.isMaximized = false
        app.primaryStage.scene = scene
        println("Navigated to $fxmlFile")

        val menuBar = MenuBar()
        menuBar.useSystemMenuBarProperty().set(true)
        val menus = generateMenu()
        menuBar.menus.addAll(menus)
        menuBar.menus.addAll(Menu("Logout"))
        (scene.root as VBox).children.add(0, menuBar)

        app.primaryStage.show()
    }

    fun generateMenu(): ArrayList<Menu> {
        val menus = ArrayList<Menu>()
        when (userProvider.authenticatedUser?.role) {
            UserRole.Admin -> {
                val menu = Menu("Waiters")
                var newOrder = MenuItem("New Order")
                var orderHistory = MenuItem("Order History")

                orderHistory.onAction = EventHandler {
                    navigate("waiters-all-order.fxml", WaitersListOrderController::class.java)
                }

                newOrder.onAction = EventHandler {
                    orderUtils.newOrderDialog()
                    // Re-render
                    navigate("waiters.fxml", WaitersController::class.java)
                }

                menu.items.addAll(
                    newOrder,
                    orderHistory
                )

                menus.add(menu)
            }
            UserRole.Chef -> {

            }
            UserRole.Waiters -> {
            }
            UserRole.Cashier -> {

            }
            null -> {}
        }

        return menus
    }
}