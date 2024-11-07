package dev.pbt.casigma.modules

import javafx.scene.control.Alert

class AlertProvider() {
    fun error(message: String) {
        val alert = Alert(Alert.AlertType.ERROR)
        alert.title = "An error occurred"
        alert.headerText = message;

        return alert.show()
    }

    fun errorWait(message: String) {
        val alert = Alert(Alert.AlertType.ERROR)
        alert.title = "An error occurred"
        alert.headerText = message;

        alert.showAndWait()
    }

    fun success(message: String) {
        val alert = Alert(Alert.AlertType.INFORMATION)
        alert.title = "Success"
        alert.headerText = message;

        return alert.show()
    }
}