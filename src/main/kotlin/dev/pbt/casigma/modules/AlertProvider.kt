package dev.pbt.casigma.modules

import javafx.scene.control.Alert

class AlertProvider() {
    fun error(message: String) {
        val alert = Alert(Alert.AlertType.ERROR)
        alert.title = "An error occurred"
        alert.headerText = message;

        return alert.show()
    }
}