package de.sweetcode.scpc.handlers;

import de.sweetcode.scpc.Utils;
import de.sweetcode.scpc.gui.CaptureTab;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.stage.DirectoryChooser;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class FileTakeScreenshot implements EventHandler<ActionEvent> {

    private final CaptureTab captureTab;
    private final DirectoryChooser directoryChooser = new DirectoryChooser();

    public FileTakeScreenshot(CaptureTab captureTab) {
        this.captureTab = captureTab;
        this.directoryChooser.setTitle("Save Screenshot");
    }


    @Override
    public void handle(ActionEvent event) {

        File file = this.directoryChooser.showDialog(this.captureTab.getMain().getStage());
        if(!(file == null)) {

            this.directoryChooser.setInitialDirectory(file);
            try {
                final String filePath = String.format("%s\\session_%d_%d.png", file.getAbsolutePath(), this.captureTab.getCaptureSession().getSessionId(), System.currentTimeMillis());
                // TODO let the user select the resolution
                ImageIO.write(this.captureTab.getCaptureSessionChart().screenshot(1920, 1080, null), "png", new File(filePath));
                Utils.popup("Screenshot", String.format("Saved screenshot to %s", filePath), Alert.AlertType.CONFIRMATION, false);
            } catch (IOException e) {
                Utils.popup("Screenshot", String.format("Failed to save image. (%s)", e.getMessage()), Alert.AlertType.ERROR, false);
            }
        }


    }
}
