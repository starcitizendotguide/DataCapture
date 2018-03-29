package de.sweetcode.scpc;

import de.sweetcode.scpc.gui.CaptureTab;
import de.sweetcode.scpc.handlers.ApplicationCloseEvent;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main extends Application {

    private final ExecutorService threadPool = Executors.newWorkStealingPool();

    private Stage stage;
    private final TabPane tabPane = new TabPane();

    private final List<CaptureTab> captureTabs = new ArrayList<>();

    public Main() {}

    public Stage getStage() {
        return this.stage;
    }

    public CaptureTab getCaptureTab(long sessionId) {
        return this.captureTabs.stream().filter(e -> e.getCaptureSession().getSessionId() == sessionId).findAny().get();
    }

    public List<CaptureTab> getCaptureTabs() {
        return this.captureTabs;
    }

    public void removeCaptureTab(long sessionId) {
        if(this.hasSession(sessionId)) {
            this.captureTabs.removeIf(captureTab -> captureTab.getCaptureSession().getSessionId() == sessionId);
        }
    }

    public void addCaptureSession(CaptureSession captureSession) {
        CaptureTab tab = new CaptureTab(this, captureSession);
        this.captureTabs.add(tab);

        Platform.runLater(() -> this.tabPane.getTabs().add(tab));
    }

    public boolean hasSession(long sessionId) {
        return this.captureTabs.stream().anyMatch(e -> e.getCaptureSession().getSessionId() == sessionId);
    }

    @Override
    public void start(Stage stage) {

        this.stage = stage;

        Platform.setImplicitExit(false);

        stage.setTitle("StarCitizen - Data CaptureSession by u/yonasismad (github.com/sweetcode)");
        stage.setOnCloseRequest(new ApplicationCloseEvent(this));

        //--- Default Tab
        this.addCaptureSession(new CaptureSession());

        //---
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(this.tabPane);

        //---
        stage.setScene(new Scene(borderPane, 1280, 720));
        stage.show();

        //--- Get IP
        String address = null;
        try {
            TextInputDialog dialog = new TextInputDialog(InetAddress.getLocalHost().getHostAddress());
            dialog.setTitle("Your Local IP");
            dialog.setHeaderText("Please set your local IP address. This will allow us to identify the correct interface\n" +
                    "in case you own multiple (e.g. running VMs). The set value might already be correct.\n\n" +
                    "If it is wrong the program won't be able to captureSession any data.");
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
                    "If it is wrong the program won't be able to captureSession any data.");
            Optional<String> result = dialog.showAndWait();

            if(!(result.isPresent())) {
                Utils.popup("Error", "You have to enter an IP address.", Alert.AlertType.ERROR, true);
            } else {
                address = result.get();
            }
        }

        this.threadPool.execute(new CaptureDevice(this, address));

    }

    public static void main(String[] args) {
        launch(args);
    }

}
