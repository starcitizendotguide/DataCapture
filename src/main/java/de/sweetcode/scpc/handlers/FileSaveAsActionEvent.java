package de.sweetcode.scpc.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.sweetcode.scpc.data.DataPoint;
import de.sweetcode.scpc.Utils;
import de.sweetcode.scpc.data.GPUInformation;
import de.sweetcode.scpc.gui.CaptureTab;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Called when the user wants to save a session to a file.
 */
public class FileSaveAsActionEvent implements EventHandler<ActionEvent> {

    private final CaptureTab captureTab;

    public FileSaveAsActionEvent(CaptureTab captureTab) {
        this.captureTab = captureTab;
    }

    @Override
    public void handle(ActionEvent event) {

        if(this.captureTab.getCaptureSession().getDataPoints().size() == 0) {
            Utils.popup("File - Save As", "No packets captured yet.", Alert.AlertType.INFORMATION, false);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(String.format("session-%d.json", this.captureTab.getCaptureSession().getSessionId()));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files (*.json)", "*.json"));
        fileChooser.setTitle("Save Captured Data");

        File file = fileChooser.showSaveDialog(this.captureTab.getMain().getStage());
        if(!(file == null)) {

            if(!(file.getName().toLowerCase().endsWith(".json"))) {
                Utils.popup("File - Save As", "Invalid file extension. Please use '.json'.", Alert.AlertType.ERROR, false);
                return;
            }

            JsonObject root = new JsonObject();

            //---
            List<DataPoint> dataPoints = this.captureTab.getCaptureSession().getDataPoints();
            JsonArray dataPointsArray = new JsonArray();

            for(DataPoint entry : dataPoints) {
                JsonObject object = new JsonObject();

                object.addProperty("index", entry.getTime());
                object.addProperty("gameState", entry.getGameState().getName());

                for(DataPoint.Type type : DataPoint.Types.values()) {
                    object.addProperty(type.getSerializationKey(), entry.getData(type).getYValue());
                }
                dataPointsArray.add(object);
            }

            //---
            JsonObject gpuObject = new JsonObject();
            GPUInformation gpuInformation = this.captureTab.getCaptureSession().getGPUInformation();
            gpuInformation.getData().forEach((k, v) -> gpuObject.addProperty(k.getSerializationKey(), v));

            //---
            root.addProperty("sessionId", this.captureTab.getCaptureSession().getSessionId());
            root.add("gpu", gpuObject);
            root.add("dataPoints", dataPointsArray);

            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file, false);
                fileOutputStream.write(new Gson().toJson(root).getBytes(Charset.forName("UTF-8")));
                fileOutputStream.close();

                Utils.popup("File - Save", "Successfully saved the data.", Alert.AlertType.INFORMATION, false);
            } catch (IOException e) {
                Utils.popup("File - Save As", e.getMessage(), Alert.AlertType.ERROR, false);
                return;
            }


        }

    }

}
