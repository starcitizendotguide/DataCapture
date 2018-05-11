package de.sweetcode.scpc.handlers;

import de.sweetcode.scpc.gui.CaptureTab;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Called when the user wants to close a CaptureTab.
 */
public class TabCloseEvent implements EventHandler<Event> {

    private final CaptureTab captureTab;

    public TabCloseEvent(CaptureTab captureTab) {
        this.captureTab = captureTab;
    }

    @Override
    public void handle(Event event) {

        if(this.captureTab.getCaptureSession().getDataPoints().size() > 0 && !this.captureTab.getCaptureSession().isArchived()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "All unsaved data will be lost.\n" +
                    "Do you really wanna close this tab now?");
            alert.setTitle("Exit");
            Optional<ButtonType> response = alert.showAndWait();

            if(response.isPresent() && response.get().getButtonData().isCancelButton()) {
                event.consume();
                return;
            }
        }

        this.captureTab.getMain().removeCaptureTab(this.captureTab.getCaptureSession().getSessionId());

    }
}
