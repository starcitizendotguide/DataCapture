package de.sweetcode.scpc.handlers;

import com.google.gson.*;
import de.sweetcode.scpc.data.*;
import de.sweetcode.scpc.Main;
import de.sweetcode.scpc.Utils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;

/**
 * Called when the user wants to load a session from a file.
 */
public class LoadFileActionEvent implements EventHandler<ActionEvent> {

    private final static Gson gson = new Gson();

    private final Main main;

    public LoadFileActionEvent(Main main) {
        this.main = main;
    }

    @Override
    public void handle(ActionEvent event) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files (*.json)", "*.json"));
        fileChooser.setTitle("Import Captured Data");

        File file = fileChooser.showOpenDialog(this.main.getStage());
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

                CaptureSession captureSession = new CaptureSession(root.get("sessionId").getAsLong());
                this.main.addCaptureSession(captureSession, true); //NOTE It has to be added here so that the triggers can listen

                //--- Data Points
                JsonArray dataPointsArray = root.getAsJsonArray("dataPoints");
                final boolean[] error = {false};
                dataPointsArray.forEach((element) -> {

                    if(error[0]) return;

                    JsonObject object = element.getAsJsonObject();

                    if(!(object.has("index"))) {
                        error[0] = true;
                        Utils.popup("File - Load", "Invalid file. No 'index' found.", Alert.AlertType.ERROR, false);
                        return;
                    }

                    int index = object.get("index").getAsInt();
                    GameState gameState = GameStates.byName(object.get("gameState").getAsString());

                    DataPoint dataPoint = new DataPoint(gameState, index);
                    for (DataPoint.Type type : DataPoint.Types.values()) {
                        if(!(object.has("index"))) {
                            error[0] = true;
                            Utils.popup("File - Load", String.format("Invalid file. No '%s' found.", type.getSerializationKey()), Alert.AlertType.ERROR, false);
                            return;
                        }
                        dataPoint.add(type, object.get(type.getSerializationKey()).getAsInt());
                    }
                    captureSession.add(dataPoint);
                });

                //--- GPU
                JsonObject gpuObject = root.getAsJsonObject("gpu");
                GPUInformation gpuInformation = new GPUInformation();
                for(DataPoint.Type type : GPUInformation.Types.values()) {
                    if(gpuObject.has(type.getSerializationKey())) {
                        gpuInformation.add(type, gpuObject.get(type.getSerializationKey()).getAsString());
                    }
                }
                captureSession.setGPUInformation(gpuInformation);


            } catch (JsonParseException exception) {
                Utils.popup("File - Load", exception.getMessage(), Alert.AlertType.ERROR, false);
                return;
            }

        }

    }


}
