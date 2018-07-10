package de.sweetcode.scpc.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.sweetcode.scpc.Main;
import de.sweetcode.scpc.crash.CrashReport;
import de.sweetcode.scpc.data.DataPoint;
import de.sweetcode.scpc.Utils;
import de.sweetcode.scpc.data.GPUInformation;
import de.sweetcode.scpc.data.GameInformation;
import de.sweetcode.scpc.data.Serializer;
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
    private final FileChooser fileChooser = new FileChooser();

    public FileSaveAsActionEvent(CaptureTab captureTab) {
        this.captureTab = captureTab;

        this.fileChooser.setInitialFileName(String.format("session-%d.json", this.captureTab.getCaptureSession().getSessionId()));
        this.fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files (*.json)", "*.json"));
        this.fileChooser.setTitle("Save Captured Data");
    }

    @Override
    public void handle(ActionEvent event) {

        if(this.captureTab.getCaptureSession().getDataPoints().size() == 0) {
            Utils.popup("File - Save As", "No packets captured yet.", Alert.AlertType.INFORMATION, false);
            return;
        }

        File file = this.fileChooser.showSaveDialog(this.captureTab.getMain().getStage());
        if(!(file == null)) {

            if(!(file.getName().toLowerCase().endsWith(".json"))) {
                Utils.popup("File - Save As", "Invalid file extension. Please use '.json'.", Alert.AlertType.ERROR, false);
                return;
            }

            this.fileChooser.setInitialDirectory(file.getParentFile());
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file, false);
                fileOutputStream.write(new Gson().toJson(Serializer.serialize(this.captureTab)).getBytes(Charset.forName("UTF-8")));
                fileOutputStream.close();

                Utils.popup("File - Save", "Successfully saved the data.", Alert.AlertType.INFORMATION, false);
            } catch (IOException e) {
                Utils.popup("File - Save As", e.getMessage(), Alert.AlertType.ERROR, false);
                return;
            }


        }

    }

}
