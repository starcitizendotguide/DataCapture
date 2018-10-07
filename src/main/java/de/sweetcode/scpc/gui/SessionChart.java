package de.sweetcode.scpc.gui;

import de.sweetcode.scpc.data.DataPoint;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.XYChart;
import javafx.scene.image.WritableImage;

import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The line chart representing all captured values.
 */
public abstract class SessionChart {

    private final Map<DataPoint.Type, XYChart.Series<Number, Number>> seriesMap = new LinkedHashMap<>();
    private BackgroundLineChart lineChart;

    public SessionChart() { }

    public void setLineChart(BackgroundLineChart lineChart) {
        this.lineChart = lineChart;
    }

    public Map<DataPoint.Type, XYChart.Series<Number, Number>> getSeriesMap() {
        return seriesMap;
    }

    public BufferedImage screenshot(int screenshotWidth, int screenshotHeight, BackgroundLineChart.BackgroundType backgroundType) {

        double width = this.lineChart.getWidth();
        double height = this.lineChart.getHeight();

        BackgroundLineChart.BackgroundType oldBackgroundType = this.lineChart.getBackgroundType();
        if(backgroundType != null) {
            this.lineChart.setBackgroundType(backgroundType);
        }

        this.lineChart.setMinSize(screenshotWidth, screenshotHeight);
        this.lineChart.redraw();

        WritableImage writableImage = new WritableImage(screenshotWidth, screenshotHeight);
        this.lineChart.snapshot(new SnapshotParameters(), writableImage);

        this.lineChart.setMinSize(0, 0);
        this.lineChart.setPrefSize(width, height);
        this.lineChart.setBackgroundType(oldBackgroundType);
        this.lineChart.redraw();

        return SwingFXUtils.fromFXImage(writableImage, null);
    }

    /**
     * Gives the line char.
     * @return Returns the line chart, never null.
     */
    public BackgroundLineChart getLineChart() {
        return this.lineChart;
    }

    public abstract void forceDraw();

    abstract BackgroundLineChart generateLineChart();
}
