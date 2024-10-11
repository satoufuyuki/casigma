package dev.pbt.casigma.modules

import dev.pbt.casigma.CasigmaApplication
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
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
        app.primaryStage.show()
    }
}