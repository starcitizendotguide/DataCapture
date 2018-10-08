package de.sweetcode.scpc.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.sweetcode.scpc.Main;
import de.sweetcode.scpc.Utils;
import de.sweetcode.scpc.crash.CrashDataType;
import de.sweetcode.scpc.crash.CrashDataTypes;
import de.sweetcode.scpc.crash.CrashReport;
import de.sweetcode.scpc.data.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

/**
 * Called when the user wants to load a session from a file.
 */
public class LoadFileActionEvent implements EventHandler<ActionEvent> {

    private final static Gson gson = new Gson();

    private final Main main;
    private final FileChooser fileChooser = new FileChooser();

    public LoadFileActionEvent(Main main) {
        this.main = main;
        this.fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files (*.json)", "*.json"));
        this.fileChooser.setTitle("Import Captured Data");
    }

    @Override
    public void handle(ActionEvent event) {


        File file = this.fileChooser.showOpenDialog(this.main.getStage());
        if(!(file == null)) {

            if(!(file.getName().toLowerCase().endsWith(".json"))) {
                Utils.popup("File - Load", "Invalid file extension. Please use '.json'.", Alert.AlertType.ERROR, false);
                return;
            }

            String data = null;
            try {
                data = new String(Files.readAllBytes(file.toPath()), Charset.forName("UTF-8"));
            } catch (IOException e) {
                Utils.popup("File - Load", e.getMessage(), Alert.AlertType.ERROR, false);
                return;
            }

            try {
                //--- Root
                JsonObject root = gson.fromJson(data, JsonObject.class);

                long sessionId = root.get("sessionId").getAsLong();

                if(this.main.hasSession(sessionId)) {
                    Utils.popup("File - Load", String.format("A session with the id (%d) is already open. Please close " +
                            "the tab before loading this file.", sessionId), Alert.AlertType.ERROR, false);
                    return;
                }

                CaptureSession captureSession = new CaptureSession(sessionId, true);
                this.main.addCaptureSession(captureSession); //NOTE It has to be added here so that the triggers can listen

                //--- Data Points
                JsonArray dataPointsArray = root.getAsJsonArray("dataPoints");
                final boolean[] data_point_error = {false};
                dataPointsArray.forEach((element) -> {

                    if(data_point_error[0]) return;

                    JsonObject object = element.getAsJsonObject();

                    if(!(object.has("index"))) {
                        data_point_error[0] = true;
                        Utils.popup("File - Load", "Invalid file. No 'index' found.", Alert.AlertType.ERROR, false);
                        return;
                    }

                    int index = object.get("index").getAsInt();
                    GameState gameState = GameStates.byName(object.get("gameState").getAsString());

                    DataPoint dataPoint = new DataPoint(gameState, index);
                    for (DataPoint.Type type : DataPoint.Types.values()) {
                        if(!(object.has("index"))) {
                            data_point_error[0] = true;
                            Utils.popup("File - Load", String.format("Invalid file. No '%s' found.", type.getSerializationKey()), Alert.AlertType.ERROR, false);
                            return;
                        }
                        if (object.has(type.getSerializationKey())) {
                            dataPoint.add(type, object.get(type.getSerializationKey()).getAsNumber());
                        }
                    }
                    captureSession.add(dataPoint);
                });

                //--- GPU
                GPUInformation gpuInformation = new GPUInformation();
                if(root.has("gpu")) {
                    JsonObject gpuObject = root.getAsJsonObject("gpu");
                    for (DataPoint.Type type : GPUInformation.Types.values()) {
                        if (gpuObject.has(type.getSerializationKey())) {
                            gpuInformation.add(type, gpuObject.get(type.getSerializationKey()).getAsString());
                        }
                    }
                }
                captureSession.setGPUInformation(gpuInformation);

                //--- CPU
                CPUInformation cpuInformation = new CPUInformation();
                if(root.has("cpu")) {
                    JsonObject cpuObject = root.getAsJsonObject("cpu");
                    for (DataPoint.Type type : CPUInformation.Types.values()) {
                        if (cpuObject.has(type.getSerializationKey())) {
                            cpuInformation.add(type, cpuObject.get(type.getSerializationKey()).getAsString());
                        }
                    }
                }
                captureSession.setCPUInformation(cpuInformation);

                //--- Disk
                DiskInformation diskInformation = new DiskInformation();
                if(root.has("disk")) {
                    JsonObject diskObject = root.getAsJsonObject("disk");
                    for (DataPoint.Type type : DiskInformation.Types.values()) {
                        if (diskObject.has(type.getSerializationKey())) {
                            diskInformation.add(type, diskObject.get(type.getSerializationKey()).getAsString());
                        }
                    }
                }
                captureSession.setDiskInformation(diskInformation);

                //--- OS
                OSInformation osInformation = new OSInformation();
                if(root.has("os")) {
                    JsonObject osObject = root.getAsJsonObject("os");
                    for (DataPoint.Type type : OSInformation.Types.values()) {
                        if (osObject.has(type.getSerializationKey())) {
                            osInformation.add(type, osObject.get(type.getSerializationKey()).getAsString());
                        }
                    }
                }
                captureSession.setOSInformation(osInformation);

                //--- Game Information
                if(root.has("game")) {
                    JsonObject gameInformationObject = root.getAsJsonObject("game");
                    GameInformation gameInformation = new GameInformation(
                            gameInformationObject.get("version").getAsString(),
                            gameInformationObject.get("branch").getAsString()
                    );
                    captureSession.setGameInformation(gameInformation);
                } else {
                    captureSession.setGameInformation(new GameInformation("N/A", "N/A"));
                }

                //--- Crash Information
                CrashReport crashReport = new CrashReport();
                if(root.has("crashReport")) {
                    JsonObject crashInformationObject = root.getAsJsonObject("crashReport");

                    {
                        crashReport.setGracefullyShutdown(crashInformationObject.get("isGracefullyShutdown").getAsBoolean());
                    }

                    {
                        JsonObject crashData = crashInformationObject.getAsJsonObject("data");
                        for(CrashDataType type : CrashDataTypes.values()) {
                            if(crashData.has(type.getSerializationKey())) {
                                crashReport.add(type, crashData.get(type.getSerializationKey()).getAsBoolean());
                            }
                        }
                    }

                }
                captureSession.setCrashReport(crashReport);

            } catch (JsonParseException exception) {
                Utils.popup("File - Load", exception.getMessage(), Alert.AlertType.ERROR, false);
                return;
            }

        }

    }


}
