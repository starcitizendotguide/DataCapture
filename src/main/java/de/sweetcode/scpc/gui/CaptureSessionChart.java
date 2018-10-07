package de.sweetcode.scpc.gui;

import com.sun.javafx.charts.Legend;
import de.sweetcode.scpc.data.CaptureSession;
import de.sweetcode.scpc.data.DataPoint;
import javafx.collections.FXCollections;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseButton;

import java.util.Arrays;
import java.util.List;

public class CaptureSessionChart extends SessionChart {

    private final CaptureSession captureSession;

    public CaptureSessionChart(CaptureSession captureSession) {
        this.captureSession = captureSession;
        this.captureSession.addListener(DataPoint.class, dataPoint -> {
            for (DataPoint.Type type : DataPoint.Types.values()) {
                this.getSeriesMap().get(type).getData().add(dataPoint.getData(type));
            }
        });
        this.setLineChart(this.generateLineChart());
    }

    @Override
    public void forceDraw() {
        this.getSeriesMap().forEach((k, v) -> v.getData().clear());

        this.captureSession.getDataPoints().forEach(dataPoint -> {
            for (DataPoint.Type type : DataPoint.Types.values()) {
                this.getSeriesMap().get(type).getData().add(dataPoint.getData(type));
            }
        });
    }

    /**
     * Generates the line chart and does all of the setup.
     * @return
     */
    @Override
    BackgroundLineChart generateLineChart() {

        final BackgroundLineChart lineChart;
        final NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Time");

        List<XYChart.Series<Number, Number>> seriesList = FXCollections.observableArrayList();

        for (DataPoint.Type type : DataPoint.Types.values()) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(type.getName());
            seriesList.add(series);
            this.getSeriesMap().put(type, series);
        }

        lineChart = new BackgroundLineChart(this.captureSession, xAxis, new NumberAxis(), FXCollections.observableArrayList(seriesList));

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
                                    lineChart.redraw();
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
