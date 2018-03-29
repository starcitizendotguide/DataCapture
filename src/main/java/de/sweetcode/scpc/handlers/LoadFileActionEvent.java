package de.sweetcode.scpc.handlers;

import com.google.gson.*;
import de.sweetcode.scpc.data.CaptureSession;
import de.sweetcode.scpc.data.DataPoint;
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

    private static int counter = 0; //@TODO Serialize session id
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
                JsonArray array = new Gson().fromJson(data, JsonArray.class);

                CaptureSession captureSession = new CaptureSession(counter++);
                final boolean[] error = {false};
                array.forEach((element) -> {

                    if(error[0]) return;

                    JsonObject object = element.getAsJsonObject();

                    if(!(object.has("index"))) {
                        error[0] = true;
                        Utils.popup("File - Load", "Invalid file. No 'index' found.", Alert.AlertType.ERROR, false);
                        return;
                    }

                    DataPoint dataPoint = new DataPoint(object.get("index").getAsInt());
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

                this.main.addCaptureSession(captureSession, true);

            } catch (JsonParseException exception) {
                Utils.popup("File - Load", exception.getMessage(), Alert.AlertType.ERROR, false);
                return;
            }

        }

    }


}
