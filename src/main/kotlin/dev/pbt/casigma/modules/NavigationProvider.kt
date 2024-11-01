package dev.pbt.casigma.modules

import dev.pbt.casigma.CasigmaApplication
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

class NavigationProvider(private val app: CasigmaApplication) {
    private val history = mutableListOf<NavigationEntry>()

    fun navigate(fxmlFile: String, controllerClass: Class<*>) {
        history.add(NavigationEntry(fxmlFile, controllerClass))
        val fxmlLoader = FXMLLoader(CasigmaApplication::class.java.getResource(fxmlFile))
        fxmlLoader.setControllerFactory { app.koin.get(controllerClass.kotlin) }
        val fontCss = app.loadFonts()
        val scene = Scene(fxmlLoader.load(), 1920.0, 1080.0)
        scene.stylesheets.add(fontCss)
        scene.stylesheets.add(fontCss)
        app.primaryStage.scene = scene
        println("Navigated to $fxmlFile")

        val menuBar = MenuBar()
        menuBar.useSystemMenuBarProperty().set(true)

        val menu = Menu("Waiters")
        menu.items.addAll(
            MenuItem("New Order"),
            MenuItem("Order History")
        )

        menuBar.menus.addAll(menu, Menu("Logout"))


        (scene.root as VBox).children.add(0, menuBar)

        app.primaryStage.show()
    }
}