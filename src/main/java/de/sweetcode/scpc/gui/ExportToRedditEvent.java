package de.sweetcode.scpc.gui;

import de.sweetcode.scpc.Main;
import de.sweetcode.scpc.generator.RedditPostGenerator;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ExportToRedditEvent implements EventHandler<ActionEvent> {

    private final static Map<Long, CacheEntry> cache = new HashMap<>();

    private CaptureTab captureTab;

    public ExportToRedditEvent(CaptureTab captureTab) {
        this.captureTab = captureTab;
    }


    @Override
    public void handle(ActionEvent event) {

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Reddit Data Export");
            alert.setContentText("Do you want to export the session to a Reddit-friendly format? This action will override" +
                    " your current clipboard content, and replace it with the Reddit-comment.");
            Optional<ButtonType> response = alert.showAndWait();
            response.ifPresent(e -> {
                if(!e.getButtonData().isCancelButton()) {
                    String value = null;
                    if(cache.containsKey(this.captureTab.getCaptureSession().getSessionId()) && !Main.GENERAOTR_DISABLE_CACHE) {
                        if(cache.get(this.captureTab.getCaptureSession().getSessionId()).getSize() == captureTab.getCaptureSession().getDataPoints().size()) {
                            value = cache.get(this.captureTab.getCaptureSession().getSessionId()).getResponse();
                        }
                    } else {
                        value = RedditPostGenerator.generate(this.captureTab);
                        cache.put(this.captureTab.getCaptureSession().getSessionId(), new CacheEntry(value, this.captureTab.getCaptureSession().getDataPoints().size()));
                    }

                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(value), null);
                }
            });
        });

    }

    private class CacheEntry {
        private final String response;
        private final int size;

        public CacheEntry(String response, int size) {
            this.size = size;
            this.response = response;
        }

        public String getResponse() {
            return this.response;
        }

        public int getSize() {
            return this.size;
        }
    }

}
