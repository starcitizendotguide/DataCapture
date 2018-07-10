package de.sweetcode.scpc.gui;

import com.sun.javafx.charts.Legend;
import de.sweetcode.scpc.data.CaptureSession;
import de.sweetcode.scpc.data.DataPoint;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.SnapshotResult;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.util.Callback;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * The line chart representing all captured values.
 */
public abstract class SessionChart {

    private final Map<DataPoint.Type, XYChart.Series<Number, Number>> seriesMap = new LinkedHashMap<>();
    private BackgroundLineChart lineChart;

    public SessionChart() {
        this.lineChart = this.generateLineChart();
    }

    public Map<DataPoint.Type, XYChart.Series<Number, Number>> getSeriesMap() {
        return seriesMap;
    }

    public BufferedImage screenshot(int screenshotWidth, int screenshotHeight) {

        double width = this.lineChart.getWidth();
        double height = this.lineChart.getHeight();

        this.lineChart.setMinSize(screenshotWidth, screenshotHeight);
        this.lineChart.redraw();

        WritableImage writableImage = new WritableImage(screenshotWidth, screenshotHeight);
        this.lineChart.snapshot(new SnapshotParameters(), writableImage);

        this.lineChart.setMinSize(0, 0);
        this.lineChart.setPrefSize(width, height);
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
