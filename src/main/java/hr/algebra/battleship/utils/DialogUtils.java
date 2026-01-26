package hr.algebra.battleship.utils;

import javafx.scene.control.Alert;

public class DialogUtils {

    private DialogUtils() {}

    public static void showDialog(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

}
