package de.sweetcode.scpc.gui;

import de.sweetcode.scpc.Main;
import de.sweetcode.scpc.data.*;
import de.sweetcode.scpc.handlers.FileSaveAsActionEvent;
import de.sweetcode.scpc.handlers.FileTakeScreenshot;
import de.sweetcode.scpc.handlers.TabCloseEvent;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.apache.commons.math3.stat.StatUtils;

/**
 * The CaptureTab represents one CaptureSession and displays ALL data and is responsible for dealing
 * with all interactions related to the specific session it represents.
 */
public class CaptureTab extends Tab {

    private final CaptureSession captureSession;

    private final CaptureSessionChart captureSessionChart;
    private final HardwareSessionChart hardwareSessionChart = new HardwareSessionChart();

    //---
    private final Main main;
    private Label gpuLabel = new Label("GPU: -");
    private Label fpsLabel = new Label("FPS: -");
    private Label packagesCapturedLabel = new Label("Packages Captured: -");
    private Label statusLabel = new Label("-");
    private Label gameStateLabel = new Label("Game State: -");
    private Label gameVersionLabel = new Label("Game Version: -");

    private Button crashInformationButton = new Button("?");

    /**
     * @param main The main instance of the program.
     * @param captureSession The capture session this tab display.
     */
    public CaptureTab(Main main, CaptureSession captureSession) {
        this.main = main;
        this.captureSession = captureSession;
        this.captureSessionChart = new CaptureSessionChart(this.captureSession);
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

    public SessionChart getCaptureSessionChart() {
        return this.captureSessionChart;
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

        {
            Menu fileMenu = new Menu("File");

            {
                MenuItem screenshotMenuItem = new MenuItem("Screenshot");
                screenshotMenuItem.setOnAction(new FileTakeScreenshot(this));
                fileMenu.getItems().addAll(screenshotMenuItem);
            }

            {
                MenuItem saveAsMenuItem = new MenuItem("Save As");
                saveAsMenuItem.setOnAction(new FileSaveAsActionEvent(this));
                fileMenu.getItems().add(saveAsMenuItem);
            }

            {
                MenuItem exportMenuItem = new MenuItem("Export to Reddit");
                exportMenuItem.setDisable(true);
                fileMenu.getItems().add(exportMenuItem);

                this.captureSession.addListener(DataPoint.class, dataPoint -> {
                    if(this.captureSession.getDataPoints().size() >= 100 && exportMenuItem.isDisable()) {
                        exportMenuItem.setOnAction(new ExportToRedditEvent(this));
                        exportMenuItem.setDisable(false);
                    }
                });
            }

            {
                MenuItem submitMenuItem = new MenuItem("Submit");
                submitMenuItem.setDisable(true);
                /*this.captureSession.addListener(DataPoint.class, dataPoint -> {
                    if(this.captureSession.getDataPoints().size() >= 100 && submitMenuItem.isDisable()) {
                        submitMenuItem.setOnAction(new SubmitDataHandler(this));
                        submitMenuItem.setDisable(false);
                    }
                });*/
                fileMenu.getItems().add(submitMenuItem);
            }

            menuBar.getMenus().add(fileMenu);
        }

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
                this.gameStateLabel,
                this.gameVersionLabel
        );
        pane.setBottom(infoLabels);

        this.captureSession.addListener(DataPoint.class, dataPoint -> {
            Platform.runLater(() -> {
                this.packagesCapturedLabel.setText(String.format("Packages Captured: %d", this.captureSession.getDataPoints().size()));
                this.setStatusText("Capturing...", Alert.AlertType.INFORMATION);

                //---
                double[] fps = this.captureSession.getDataPoints().stream().mapToDouble(
                        e -> e.getData(DataPoint.Types.FPS).getYValue().doubleValue()
                ).toArray();

                this.fpsLabel.setText(String.format("FPS: %.2f (%.2f [50th]| %.2f [95th] | %.2f [99th])", dataPoint.getData(DataPoint.Types.FPS).getYValue().doubleValue(), StatUtils.percentile(fps, 50), StatUtils.percentile(fps, 95),  StatUtils.percentile(fps, 99)));
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

        this.captureSession.addListener(GameInformation.class, gameInformation -> {
            Platform.runLater(() -> this.gameVersionLabel.setText(String.format("Game Version: %s (%s)",
                        gameInformation.getVersion(),
                        gameInformation.getBranch())
            ));
        });

        this.captureSession.addListener(CaptureSession.class, captureSession -> {
            Platform.runLater(() -> this.setText(String.format("Session - %d", this.getCaptureSession().getSessionId())));
        });

        //---
        if(Main.FEATURE_CRASH_REPORT) {
            infoLabels.getChildren().add(this.crashInformationButton);
            this.crashInformationButton.setVisible(false);
            this.crashInformationButton.setOnAction(new CrashInformationHandler(this.main, this.captureSession));
            this.captureSession.addListener(GameState.class, gameState -> {
                Platform.runLater(() -> this.crashInformationButton.setVisible((gameState == GameStates.SHUTDOWN_CRASHED)));
            });
        }


        //---
        pane.setCenter(this.captureSessionChart.getLineChart());
        this.setContent(pane);

        //---
        this.setStatusText("Waiting for Star Citizen", Alert.AlertType.INFORMATION);

        if(this.captureSession.isArchived()) {
            this.captureSessionChart.forceDraw();
        }


    }

}
