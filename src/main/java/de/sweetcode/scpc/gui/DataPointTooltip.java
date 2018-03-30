package de.sweetcode.scpc.gui;

import de.sweetcode.scpc.data.DataPoint;
import javafx.scene.control.Tooltip;

public class DataPointTooltip extends Tooltip {

    private final DataPoint dataPoint;

    public DataPointTooltip(DataPoint dataPoint) {
        this.dataPoint = dataPoint;
        super.setWrapText(true);
        super.setText(this.generateText());
    }

    private String generateText() {

        StringBuilder builder = new StringBuilder();

        builder.append(String.format("%d\n", this.dataPoint.getTime()));
        for(DataPoint.Types type : DataPoint.Types.values()) {
            builder.append(String.format("%s %.2f", type.getName(), this.dataPoint.getData(type).getYValue().doubleValue()));
        }

        return builder.toString();

    }

}
