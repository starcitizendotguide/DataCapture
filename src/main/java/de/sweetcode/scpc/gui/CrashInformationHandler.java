package de.sweetcode.scpc.gui;

import de.sweetcode.scpc.Main;
import de.sweetcode.scpc.data.CaptureSession;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CrashInformationHandler implements EventHandler<ActionEvent> {

    private final Main main;
    private final CaptureSession captureSession;

    public CrashInformationHandler(Main main, CaptureSession captureSession) {
        this.captureSession = captureSession;
        this.main = main;
    }

    @Override
    public void handle(ActionEvent event) {

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(0, 10, 0, 10));


        Stage stage = new Stage();
        stage.setTitle(String.format("Session %d - Crash Report", this.captureSession.getSessionId()));
        stage.setScene(new Scene(grid, 640, 360));

        final int[] i = {0};
        this.captureSession.getCrashReport().getCrashData().forEach((k, v) -> {
            if(v) {
                grid.add(new Label(String.format("\t- %s", k.getDescription())), 0, i[0]);
                i[0]++;
            }
        });

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();

    }

}
