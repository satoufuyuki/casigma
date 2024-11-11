package dev.pbt.casigma

import dev.pbt.casigma.controllers.LoginController
import dev.pbt.casigma.controllers.WaitersController
import dev.pbt.casigma.controllers.WaitersListOrderController
import dev.pbt.casigma.modules.AlertProvider
import dev.pbt.casigma.modules.Argon2
import dev.pbt.casigma.modules.NavigationProvider
import dev.pbt.casigma.modules.UserProvider
import javafx.application.Application
import javafx.stage.Stage
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import dev.pbt.casigma.modules.database.DB
import dev.pbt.casigma.utils.OrderUtils
import org.koin.core.Koin

class CasigmaApplication : Application() {
    lateinit var koin: Koin
    lateinit var primaryStage: Stage

    override fun start(stage: Stage) {
        val appModules = module {
            singleOf(::DB)
            singleOf(::AlertProvider)
            single { OrderUtils(get(), get()) }
            single { UserProvider(get(), get()) }
            single { Argon2() }
            single { NavigationProvider(this@CasigmaApplication, get(), get()) }
            factory { LoginController(get(), get(), get(), get(), get()) }
            factory { WaitersController(get(), get(), get()) }
            factory { WaitersListOrderController(get(), get()) }
        }

        koin = startKoin { modules(appModules) }.koin
        primaryStage = stage

        // Migrate db
//        val db: DB = koin.get()
//        db.migrate()

        // Navigate to the first view
        val navigationProvider: NavigationProvider = koin.get()
//        navigationProvider.navigate("waiters-all-order.fxml", WaitersListOrderController::class.java)
        navigationProvider.navigate("login.fxml", LoginController::class.java)
    }


    fun loadFonts(): String {
        val myStyles = """
            @import url('https://fonts.googleapis.com/css2?family=Poppins:ital,wght@0,100;0,200;0,300;0,400;0,500;0,600;0,700;0,800;0,900;1,100;1,200;1,300;1,400;1,500;1,600;1,700;1,800;1,900&display=swap');
            .root {
                -fx-font-family: "Poppins", sans-serif;
            }""".trimIndent()

        val cssFile = File.createTempFile("assets", "css")
        cssFile.deleteOnExit()
        Files.writeString(cssFile.toPath(), myStyles, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
        val cssURL = cssFile.toURI().toString()
        return cssURL
    }
}

fun main() {
    Application.launch(CasigmaApplication::class.java)
}

