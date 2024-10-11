package dev.pbt.casigma.controllers

import dev.pbt.casigma.modules.AlertProvider
import dev.pbt.casigma.modules.Argon2
import dev.pbt.casigma.modules.NavigationProvider
import dev.pbt.casigma.modules.database.DB
import dev.pbt.casigma.modules.database.models.User
import javafx.fxml.FXML
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import org.jetbrains.exposed.sql.transactions.transaction


class LoginController(
    val  argon2: Argon2,
    val navigationProvider: NavigationProvider,
    val db: DB, val alertProvider: AlertProvider
) {
    lateinit var email: TextField
    lateinit var password: PasswordField

    @FXML
    private fun onLoginButtonClick() {
        transaction(db.conn) {
            val user = User.select(User.password).where {
                User.email eq email.text
            }.firstOrNull()

            if (user == null) {
                return@transaction alertProvider.error("E-mail atau password salah!")
            }

            val isPasswordValid = argon2.verify(password.text, user[User.password])
            if (!isPasswordValid) {
                return@transaction alertProvider.error("E-mail atau password salah!")
            }

            navigationProvider.navigate("cashier.fxml", CashierController::class.java)
        }
    }
}