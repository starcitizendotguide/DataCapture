package de.sweetcode.scpc.handlers;

import de.sweetcode.scpc.Main;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class OpenDebugConsoleEvent implements EventHandler<ActionEvent> {

    private final Stage popupWindow = new Stage();

    public OpenDebugConsoleEvent(Main main, TextArea debugConsole) {

        BorderPane borderPane = new BorderPane();

        this.popupWindow.initModality(Modality.WINDOW_MODAL);
        this.popupWindow.setTitle("Debug - Console");
    }

    @Override
    public void handle(ActionEvent event) {


    }

}
