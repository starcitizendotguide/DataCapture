package de.sweetcode.scpc.handlers;

import de.sweetcode.scpc.Main;
import de.sweetcode.scpc.gui.BackgroundLineChart;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class ChangeBackgroundTypeEvent implements EventHandler<ActionEvent> {

    private final BackgroundLineChart.BackgroundType backgroundType;
    private final Main main;

    public ChangeBackgroundTypeEvent(BackgroundLineChart.BackgroundType backgroundType, Main main) {
        this.backgroundType = backgroundType;
        this.main = main;
    }

    @Override
    public void handle(ActionEvent event) {
        this.main.getCaptureTabs().forEach(e -> {
            e.getCaptureSessionChart().getLineChart().setBackgroundType(this.backgroundType);
            e.getCaptureSessionChart().getLineChart().redraw();
        });
        this.main.setDefaultBackgroundType(this.backgroundType);
    }

}
