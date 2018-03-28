package de.sweetcode.scpc;

import javafx.application.Platform;
import javafx.scene.control.Alert;

public class Utils {

    public static void popup(String title, String message, Alert.AlertType alertType, boolean quit) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType, message);
            alert.setTitle(title);
            alert.showAndWait();

            if(quit) {
                Platform.exit();
                System.exit(0);
            }
        });
    }

}
