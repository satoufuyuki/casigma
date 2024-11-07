package dev.pbt.casigma.controllers

import dev.pbt.casigma.modules.AlertProvider
import dev.pbt.casigma.modules.Argon2
import dev.pbt.casigma.modules.NavigationProvider
import dev.pbt.casigma.modules.UserProvider
import dev.pbt.casigma.modules.database.DB
import dev.pbt.casigma.modules.database.models.User
import dev.pbt.casigma.modules.database.models.UserRole
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.URL
import java.util.ResourceBundle


class LoginController(
    val  argon2: Argon2,
    val navigationProvider: NavigationProvider,
    val db: DB, val alertProvider: AlertProvider,
    val userProvider: UserProvider
) : Initializable {
    lateinit var email: TextField
    lateinit var password: PasswordField

    @FXML
    private fun onLoginButtonClick() {
        val emailInput = email.text
        val passwordInput = password.text
        val result = userProvider.authenticate(emailInput, passwordInput)

        if (!result) {
            return alertProvider.error("E-mail atau password salah!")
        }

        // Navigate user
        when (userProvider.authenticatedUser?.role) {
//            UserRole.Admin -> navigationProvider.navigate("admin.fxml", AdminController::class.java)
//            UserRole.Cashier -> navigationProvider.navigate("cashier.fxml", CashierController::class.java)
//            UserRole.Chef -> navigationProvider.navigate("chef.fxml", ChefController::class.java)
            UserRole.Waiters -> navigationProvider.navigate("waiters.fxml", WaitersController::class.java)
            UserRole.Admin -> {
                navigationProvider.navigate("waiters.fxml", WaitersController::class.java)
            }
            UserRole.Cashier -> TODO()
            UserRole.Chef -> TODO()
            null -> alertProvider.error("Role tidak ditemukan!")
        }
    }

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        val eventHandler = EventHandler<KeyEvent> {
            if (it.code == KeyCode.ENTER) {
                onLoginButtonClick()
            }
        }

        password.onKeyPressed = eventHandler
        email.onKeyPressed = eventHandler

        email.requestFocus()
    }
}