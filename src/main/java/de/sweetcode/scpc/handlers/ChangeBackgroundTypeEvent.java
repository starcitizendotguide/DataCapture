package de.sweetcode.scpc.handlers;

import de.sweetcode.scpc.gui.BackgroundLineChart;
import de.sweetcode.scpc.gui.CaptureTab;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import java.util.List;

public class ChangeBackgroundTypeEvent implements EventHandler<ActionEvent> {

    private final BackgroundLineChart.BackgroundType backgroundType;
    private final List<CaptureTab> captureTabs;

    public ChangeBackgroundTypeEvent(BackgroundLineChart.BackgroundType backgroundType, List<CaptureTab> captureTabs) {
        this.backgroundType = backgroundType;
        this.captureTabs = captureTabs;
    }

    @Override
    public void handle(ActionEvent event) {
        this.captureTabs.forEach(e -> e.getCaptureSessionChart().getLineChart().setBackgroundType(this.backgroundType));
    }

}
