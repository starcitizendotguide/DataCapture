package de.sweetcode.scpc.gui;

import com.sun.javafx.charts.Legend;
import de.sweetcode.scpc.data.CaptureSession;
import de.sweetcode.scpc.data.DataPoint;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseButton;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The line chart representing all captured values.
 */
public class CaptureSessionChart {

    private final Map<DataPoint.Type, XYChart.Series<Number, Number>> seriesMap = new LinkedHashMap<>();
    private final LineChart<Number, Number> lineChart = this.generateLineChart();

    private final CaptureSession captureSession;

    /**
     * @param captureSession The session that the chart will display and track.
     */
    public CaptureSessionChart(CaptureSession captureSession) {
        this.captureSession = captureSession;
        this.captureSession.addListener(DataPoint.class, dataPoint -> {
            for (DataPoint.Type type : DataPoint.Types.values()) {
                this.seriesMap.get(type).getData().add(dataPoint.getData(type));
            }
        });
    }

    /**
     * Gives the line char.
     * @return Returns the line chart, never null.
     */
    public LineChart<Number, Number> getLineChart() {
        return this.lineChart;
    }

    public void forceDraw() {
        this.seriesMap.forEach((k, v) -> v.getData().clear());

        this.captureSession.getDataPoints().forEach(dataPoint -> {
            for (DataPoint.Type type : DataPoint.Types.values()) {
                this.seriesMap.get(type).getData().add(dataPoint.getData(type));
            }
        });
    }

    /**
     * Generates the line chart and does all of the setup.
     * @return
     */
    private LineChart<Number, Number> generateLineChart() {
        final LineChart<Number, Number> lineChart;
        final NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Time");
        lineChart = new LineChart<>(xAxis, new NumberAxis());

        for (DataPoint.Type type : DataPoint.Types.values()) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(type.getName());
            lineChart.getData().add(series);
            this.seriesMap.put(type, series);
        }


        //@Source - https://stackoverflow.com/a/44957354 - Enables Toggle of Data Series by clicking on their icon in the legend
        for (Node n : lineChart.getChildrenUnmodifiable()) {
            if (n instanceof Legend) {
                Legend l = (Legend) n;
                for (Legend.LegendItem li : l.getItems()) {
                    for (XYChart.Series<Number, Number> s : lineChart.getData()) {
                        if (s.getName().equals(li.getText())) {
                            li.getSymbol().setCursor(Cursor.HAND); // Hint user that legend symbol is clickable
                            li.setText(li.getText() + " On");
                            li.getSymbol().setOnMouseClicked(me -> {
                                if (me.getButton() == MouseButton.PRIMARY) {
                                    s.getNode().setVisible(!s.getNode().isVisible()); // Toggle visibility of line

                                    String[] displayName = li.getText().split(" ");
                                    li.setText(String.format("%s %s", String.join(" ", Arrays.copyOfRange(displayName, 0, displayName.length - 1)), (s.getNode().isVisible() ? "On" : "Off")));
                                    for (XYChart.Data<Number, Number> d : s.getData()) {
                                        if (d.getNode() != null) {
                                            d.getNode().setVisible(s.getNode().isVisible()); // Toggle visibility of every node in the series
                                        }
                                    }
                                }
                            });
                            break;
                        }
                    }
                }
            }
        }
        //---

        return lineChart;
    }

}
