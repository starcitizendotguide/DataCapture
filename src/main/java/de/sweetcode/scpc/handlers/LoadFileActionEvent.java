package de.sweetcode.scpc.handlers;

import com.google.gson.*;
import de.sweetcode.scpc.DataPoint;
import de.sweetcode.scpc.Main;
import de.sweetcode.scpc.Utils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Called when the user wants to load a session from a file.
 */
public class LoadFileActionEvent implements EventHandler<ActionEvent> {

    private final Main main;

    public LoadFileActionEvent(Main main) {
        this.main = main;
    }

    @Override
    public void handle(ActionEvent event) {

        /*if(this.main.getCaptureTab().getDataPoints().size() > 0) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "You still have other captured data. If you\n" +
                    " open another file now, you will overwrite the currently captured data.");
            alert.setTitle("File - Load");
            Optional<ButtonType> buttonType = alert.showAndWait();

            if(buttonType.isPresent() && buttonType.get().getButtonData().isCancelButton()) {
                return;
            }
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files (*.json)", "*.json"));
        fileChooser.setTitle("Save Captured Data");

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

                List<DataPoint> dataPoints = new LinkedList<>();

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
                    dataPoints.add(dataPoint);

                });

                this.main.getCaptureTab().clear();
                dataPoints.forEach(this.main.getCaptureTab()::add);

            } catch (JsonParseException exception) {
                Utils.popup("File - Load", exception.getMessage(), Alert.AlertType.ERROR, false);
                return;
            }

            this.main.getCaptureTab().clear();


        }*/

    }


}
