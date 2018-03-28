package de.sweetcode.scpc;

import de.sweetcode.scpc.handlers.ApplicationCloseEvent;
import de.sweetcode.scpc.handlers.FileSaveAsActionEvent;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main extends Application {

    private final ExecutorService threadPool = Executors.newWorkStealingPool();
    private Stage stage;

    private Capture capture;

    private Label fpsLabel = new Label("FPS: -");
    private Label packagesCapturedLabel = new Label("Packages Captured: -");
    private Label isCapturingLabel = new Label("-");

    public Main() {}

    public Capture getCapture() {
        return this.capture;
    }

    public Stage getStage() {
        return this.stage;
    }

    public void setStatusText(String statusText, Alert.AlertType status) {

        Platform.runLater(() -> {
            if(status == Alert.AlertType.ERROR) {
                this.isCapturingLabel.setTextFill(Color.web("#EE3F3F"));
                this.isCapturingLabel.setText(String.format("Error: %s", statusText));
            } else {
                this.isCapturingLabel.setTextFill(Color.web("#89AD83"));
                this.isCapturingLabel.setText(String.format("Status: %s", statusText));
            }
        });

    }

    @Override
    public void start(Stage stage) {

        this.stage = stage;

        Platform.setImplicitExit(false);

        stage.setTitle("StarCitizen - Data Capture by u/yonasismad (github.com/sweetcode)");
        stage.setOnCloseRequest(new ApplicationCloseEvent(this));

        this.setStatusText("Waiting for Star Citizen", Alert.AlertType.INFORMATION);

        //--- Scene & Capture
        this.capture = new Capture();

        BorderPane stackPane = new BorderPane();

        //-- Menu
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem saveAsMenuItem = new MenuItem("Save As");
        saveAsMenuItem.setOnAction(new FileSaveAsActionEvent(this));

        fileMenu.getItems().addAll(saveAsMenuItem);
        menuBar.getMenus().add(fileMenu);
        stackPane.setTop(menuBar);

        //--- Information
        HBox infoLabels = new HBox();
        infoLabels.setAlignment(Pos.CENTER);
        infoLabels.setSpacing(20);

        infoLabels.getChildren().add(this.fpsLabel);
        infoLabels.getChildren().add(this.packagesCapturedLabel);
        infoLabels.getChildren().add(this.isCapturingLabel);
        stackPane.setBottom(infoLabels);

        this.capture.setListener(dataPoint -> {
            this.fpsLabel.setText(String.format("FPS: %d", dataPoint.getData(DataPoint.Types.FPS).getYValue().intValue()));
            packagesCapturedLabel.setText(String.format("Packages Captured: %d", this.capture.size()));

            this.setStatusText("Capturing...", Alert.AlertType.INFORMATION);
        });


        //--- Main
        stackPane.setCenter(this.capture.getLineChart());

        //---
        stage.setScene(new Scene(stackPane, 1280, 720));
        stage.show();

        //--- Get IP
        String address = null;
        try {
            TextInputDialog dialog = new TextInputDialog(InetAddress.getLocalHost().getHostAddress());
            dialog.setTitle("Your Local IP");
            dialog.setHeaderText("Please set your local IP address. This will allow us to identify the correct interface\n" +
                    "in case you own multiple (e.g. running VMs). The set value might already be correct.\n\n" +
                    "If it is wrong the program won't be able to capture any data.");
            Optional<String> result = dialog.showAndWait();

            if(!(result.isPresent())) {
                Utils.popup("Error", "You have to enter an IP address.", Alert.AlertType.ERROR, true);
            } else {
                address = result.get().trim().isEmpty() ? InetAddress.getLocalHost().getHostAddress() : result.get();
            }
        } catch (UnknownHostException e) {
            TextInputDialog dialog = new TextInputDialog("");
            dialog.setTitle("Your Local IP");
            dialog.setHeaderText("Please set your local IP address. This will allow us to identify the correct interface " +
                    "in case you own multiple (e.g. running VMs.\n\n" +
                    "If it is wrong the program won't be able to capture any data.");
            Optional<String> result = dialog.showAndWait();

            if(!(result.isPresent())) {
                Utils.popup("Error", "You have to enter an IP address.", Alert.AlertType.ERROR, true);
            } else {
                address = result.get();
            }
        }

        this.threadPool.execute(new SCPC(this, this.capture, address));

    }

    public static void main(String[] args) {
        launch(args);
    }

}
