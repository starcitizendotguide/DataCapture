package de.sweetcode.scpc;

import de.sweetcode.scpc.data.CaptureSession;
import de.sweetcode.scpc.gui.BackgroundLineChart;
import de.sweetcode.scpc.gui.CaptureTab;
import de.sweetcode.scpc.handlers.ApplicationCloseEvent;
import de.sweetcode.scpc.handlers.ChangeBackgroundTypeEvent;
import de.sweetcode.scpc.handlers.LoadFileActionEvent;
import de.sweetcode.scpc.handlers.OpenDebugConsoleEvent;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main extends Application {

    private final ExecutorService threadPool = Executors.newWorkStealingPool();


    //---
    private final TabPane tabPane = new TabPane();
    private final List<CaptureTab> captureTabs = new ArrayList<>();
    private final TextArea debugConsole = new TextArea();

    private Stage stage;
    private BackgroundLineChart.BackgroundType defaultBackgroundType = BackgroundLineChart.BackgroundType.NONE;

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

    public void setDefaultBackgroundType(BackgroundLineChart.BackgroundType defaultBackGroundType) {
        this.defaultBackgroundType = defaultBackGroundType;
    }

    public void removeCaptureTab(long sessionId) {
        if(this.hasSession(sessionId)) {
            this.captureTabs.removeIf(captureTab -> captureTab.getCaptureSession().getSessionId() == sessionId);
        }
    }

    public void addCaptureSession(CaptureSession captureSession, boolean containsImportedData) {
        CaptureTab tab = new CaptureTab(this, captureSession, containsImportedData);
        this.captureTabs.add(tab);
        tab.getCaptureSessionChart().getLineChart().setBackgroundType(this.defaultBackgroundType);

        Platform.runLater(() -> {
            this.tabPane.getTabs().add(tab);
            this.tabPane.getSelectionModel().select(tab);
        });
    }

    public boolean hasSession(long sessionId) {
        return this.captureTabs.stream().anyMatch(e -> e.getCaptureSession().getSessionId() == sessionId);
    }

    public void logToDebugConsole(String message) {
        this.debugConsole.setText(String.format("%s\n%s", this.debugConsole.getText(), message));
        System.out.println(message);
    }

    @Override
    public void start(Stage stage) {

        this.stage = stage;

        Platform.setImplicitExit(false);

        stage.setTitle("StarCitizen - Data CaptureSession by u/yonasismad (github.com/sweetcode)");
        stage.setOnCloseRequest(new ApplicationCloseEvent(this));

        //---
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(this.tabPane);

        //
        MenuBar menuBar = new MenuBar();
        Menu menu = new Menu("Sessions");

        MenuItem importFile = new MenuItem("Import");
        importFile.setOnAction(new LoadFileActionEvent(this));

        menu.getItems().add(importFile);

        Menu backgroundMenu = new Menu("Background");
        MenuItem backgroundNone = new MenuItem("None");
        MenuItem backgroundColour = new MenuItem("Colour");
        MenuItem backgroundImage = new MenuItem("Image");

        backgroundNone.setOnAction(new ChangeBackgroundTypeEvent(BackgroundLineChart.BackgroundType.NONE, this));
        backgroundColour.setOnAction(new ChangeBackgroundTypeEvent(BackgroundLineChart.BackgroundType.COLOUR, this));
        backgroundImage.setOnAction(new ChangeBackgroundTypeEvent(BackgroundLineChart.BackgroundType.IMAGE, this));

        backgroundMenu.getItems().addAll(backgroundNone, backgroundColour, backgroundImage);

        /*Menu debugMenu = new Menu("Debug Console");
        MenuItem openLol = new MenuItem("Open");
        debugMenu.getItems().addAll(openLol);
        openLol.setOnAction(new OpenDebugConsoleEvent(this, this.debugConsole));*/

        menuBar.getMenus().addAll(menu, backgroundMenu);
        borderPane.setTop(menuBar);

        //---
        stage.setScene(new Scene(borderPane, 1280, 720));
        stage.show();

        //--- Get IP
        String address = null;
        try {

            //--- Interface
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            List<NetworkInterface> unique = new ArrayList<>();
            for(NetworkInterface entry : Collections.list(networkInterfaces)) {
               if(entry.getInetAddresses().hasMoreElements()) unique.add(entry);
            }

            ChoiceDialog<NetworkInterface> choiceDialog = new ChoiceDialog<>(unique.get(0), unique);
            choiceDialog.setTitle("Network - Interface");
            choiceDialog.setHeaderText("Please select an network interface.");
            Optional<NetworkInterface> optional = choiceDialog.showAndWait();

            if(optional.isPresent()) {

                //--- Address
                List<InetAddress> inetAddresses = Collections.list(optional.get().getInetAddresses());

                if(inetAddresses.size() == 1) {
                    address = inetAddresses.get(0).getHostAddress();
                } else {
                    ChoiceDialog<InetAddress> inetAddressChoiceDialog = new ChoiceDialog<>(inetAddresses.get(0), inetAddresses);
                    inetAddressChoiceDialog.setTitle("Network - Address");
                    inetAddressChoiceDialog.setHeaderText("Please select an network address.");
                    Optional<InetAddress> inetAddressOptional = inetAddressChoiceDialog.showAndWait();

                    if (inetAddressOptional.isPresent()) {
                        address = inetAddressOptional.get().getHostAddress();
                    } else {
                        Utils.popup("IP Address", "You didn't select a IP address.", Alert.AlertType.ERROR, true);
                        return;
                    }
                }

            } else {
                Utils.popup("IP Address", "You didn't select a network interface.", Alert.AlertType.ERROR, true);
                return;
            }

        } catch (SocketException e) {
            e.printStackTrace();
        }

        this.threadPool.execute(new CaptureDevice(this, address));

    }

    public static void main(String[] args) {
        launch(args);
    }

}
