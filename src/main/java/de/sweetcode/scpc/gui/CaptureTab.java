package de.sweetcode.scpc.gui;

import de.sweetcode.scpc.Main;
import de.sweetcode.scpc.data.CaptureSession;
import de.sweetcode.scpc.data.DataPoint;
import de.sweetcode.scpc.data.GPUInformation;
import de.sweetcode.scpc.data.GameState;
import de.sweetcode.scpc.handlers.FileSaveAsActionEvent;
import de.sweetcode.scpc.handlers.TabCloseEvent;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

/**
 * The CaptureTab represents one CaptureSession and displays ALL data and is responsible for dealing
 * with all interactions related to the specific session it represents.
 */
public class CaptureTab extends Tab {

    private final CaptureSession captureSession;

    private final CaptureSessionChart captureSessionChart;

    private boolean containsImportedData = false;

    //---
    private final Main main;
    private Label gpuLabel = new Label("GPU: -");
    private Label fpsLabel = new Label("FPS: -");
    private Label packagesCapturedLabel = new Label("Packages Captured: -");
    private Label statusLabel = new Label("-");
    private Label gameStateLabel = new Label("Game State: -");

    /**
     * @param main The main instance of the program.
     * @param captureSession The capture session this tab display.
     */
    public CaptureTab(Main main, CaptureSession captureSession, boolean containsImportedData) {
        this.main = main;
        this.captureSession = captureSession;
        this.captureSessionChart = new CaptureSessionChart(this.captureSession);
        this.containsImportedData = containsImportedData;
        this.setContent();
    }

    /**
     * The associated main instance.
     * @return
     */
    public Main getMain() {
        return this.main;
    }

    /**
     * The associated capture session.
     * @return
     */
    public CaptureSession getCaptureSession() {
        return this.captureSession;
    }

    public CaptureSessionChart getCaptureSessionChart() {
        return this.captureSessionChart;
    }

    public boolean containsImportedData() {
        return this.containsImportedData;
    }

    /**
     * Sets the value of the status label.
     * @param statusText The status text.
     * @param status The status, determines colour and prefix.
     */
    public void setStatusText(String statusText, Alert.AlertType status) {

        Platform.runLater(() -> {
            if(status == Alert.AlertType.ERROR) {
                this.statusLabel.setTextFill(Color.web("#EE3F3F"));
                this.statusLabel.setText(String.format("Error: %s", statusText));
            } else {
                this.statusLabel.setTextFill(Color.web("#89AD83"));
                this.statusLabel.setText(String.format("Status: %s", statusText));
            }
        });

    }

    /**
     * Setup the content of the tab.
     */
    private void setContent() {

        this.setOnCloseRequest(new TabCloseEvent(this));

        //---
        this.setText(String.format("Session - %d", this.getCaptureSession().getSessionId()));

        //---
        BorderPane pane = new BorderPane();
        pane.setCenter(this.captureSessionChart.getLineChart());

        //-- Menu
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem saveAsMenuItem = new MenuItem("Save As");

        saveAsMenuItem.setOnAction(new FileSaveAsActionEvent(this));

        fileMenu.getItems().addAll(saveAsMenuItem);
        menuBar.getMenus().add(fileMenu);
        pane.setTop(menuBar);

        //--- Information
        HBox infoLabels = new HBox();
        infoLabels.setAlignment(Pos.CENTER);
        infoLabels.setSpacing(20);

        infoLabels.getChildren().addAll(
                this.fpsLabel,
                this.gpuLabel,
                this.packagesCapturedLabel,
                this.statusLabel,
                this.gameStateLabel
        );
        pane.setBottom(infoLabels);

        this.captureSession.addListener(DataPoint.class, dataPoint -> {
            Platform.runLater(() -> {
                this.fpsLabel.setText(String.format("FPS: %d", dataPoint.getData(DataPoint.Types.FPS).getYValue().intValue()));
                this.packagesCapturedLabel.setText(String.format("Packages Captured: %d", this.captureSession.getDataPoints().size()));
                this.setStatusText("Capturing...", Alert.AlertType.INFORMATION);
            });
        });

        this.captureSession.addListener(GPUInformation.class, gpuInformation -> {
            Platform.runLater(() -> {
                this.gpuLabel.setText("GPU: " + gpuInformation.getValue(GPUInformation.Types.GPU_NAME));
            });
        });

        this.captureSession.addListener(GameState.class, gameState -> {
            Platform.runLater(() -> this.gameStateLabel.setText(String.format("Game State: %s", gameState.getName())));
        });

        //---
        pane.setCenter(this.captureSessionChart.getLineChart());
        this.setContent(pane);

        //---
        this.setStatusText("Waiting for Star Citizen", Alert.AlertType.INFORMATION);

        if(this.containsImportedData) {
            this.captureSessionChart.forceDraw();
        }


    }

}
