package de.sweetcode.scpc.handlers;


import de.sweetcode.scpc.Main;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.WindowEvent;

import java.util.Optional;

public class ApplicationCloseEvent implements EventHandler<WindowEvent> {

    private final Main main;

    public ApplicationCloseEvent(Main main) {
        this.main = main;
    }

    @Override
    public void handle(WindowEvent event) {

        if(this.main.getCapture().size() > 0) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "All unsaved data will be lost.\n" +
                    "Do you really wanna close the application now?");
            alert.setTitle("Exit");
            Optional<ButtonType> response = alert.showAndWait();

            if(response.isPresent() && response.get().getButtonData().isCancelButton()) {
                event.consume();
                return;
            }
        }

        Platform.exit();
        System.exit(0);

    }

}
