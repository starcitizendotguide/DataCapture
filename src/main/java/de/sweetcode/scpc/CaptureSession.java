package de.sweetcode.scpc;

import com.sun.javafx.charts.Legend;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseButton;

import java.util.*;

public class Capture {

    private final LineChart<Number, Number> lineChart;
    private final Map<DataPoint.Type, XYChart.Series<Number, Number>> seriesMap = new LinkedHashMap<>();

    private List<DataPoint> dataPoints = new LinkedList<>();

    private Capture.Listener listener;

    public Capture() {
        final NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Time");
        /*xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                //return new Date(new Timestamp(object.longValue()).getTime()).toString();
                return object.toString();
            }

            @Override
            public Number fromString(String string) {
                return Long.valueOf(string);
            }
        });*/
        this.lineChart = new LineChart<>(xAxis, new NumberAxis());

        for (DataPoint.Type type : DataPoint.Types.values()) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(type.getName());
            this.lineChart.getData().add(series);
            this.seriesMap.put(type, series);
        }


        //@Source - https://stackoverflow.com/a/44957354 - Enables Toggle of Data Series by clicking on their icon in the legend
        for (Node n : this.lineChart.getChildrenUnmodifiable()) {
            if (n instanceof Legend) {
                Legend l = (Legend) n;
                for (Legend.LegendItem li : l.getItems()) {
                    for (XYChart.Series<Number, Number> s : this.lineChart.getData()) {
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
    }

    public List<DataPoint> getDataPoints() {
        return this.dataPoints;
    }

    public LineChart<Number, Number> getLineChart() {
        return this.lineChart;
    }

    public void clear() {
        this.seriesMap.forEach((k,v) -> v.getData().clear());
        this.dataPoints.clear();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void add(DataPoint dataPoint) {
        this.dataPoints.add(dataPoint);
        if(this.listener != null) {
            this.listener.captured(dataPoint);
        }

        for (DataPoint.Type type : DataPoint.Types.values()) {
            this.seriesMap.get(type).getData().add(dataPoint.getData(type));
        }
    }

    public int size() {
        return this.dataPoints.size();
    }

    public interface Listener {
        void captured(DataPoint dataPoint);
    }

}
