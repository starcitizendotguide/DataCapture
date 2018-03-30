package de.sweetcode.scpc.gui;

import de.sweetcode.scpc.data.CaptureSession;
import de.sweetcode.scpc.data.DataPoint;
import javafx.collections.ObservableList;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Polygon;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class BackgroundLineChart extends LineChart<Number, Number> {

    private final CaptureSession captureSession;
    private BackgroundType backgroundType = BackgroundType.NONE;

    public BackgroundLineChart(CaptureSession captureSession, Axis<Number> xAxis, Axis<Number> yAxis, ObservableList<Series<Number, Number>> data) {
        super(xAxis, yAxis, data);
        this.captureSession = captureSession;
    }

    public void setBackgroundType(BackgroundType backgroundType) {
        this.backgroundType = backgroundType;
    }

    public void redraw() {
        this.layoutPlotChildren();
    }

    @Override
    protected void layoutPlotChildren() {
        super.layoutPlotChildren();

        //NOTE: We remove the old Polygons os that when we redraw, we only draw the new ones and the old
        // Polygons disappear.
        this.getPlotChildren().removeIf(e -> e instanceof Polygon);

        if(this.backgroundType == BackgroundType.NONE) return;

        Polygon polygon = new Polygon();
        final double ground = this.getYAxis().getDisplayPosition(0);

        boolean inMiddle = false;
        List<Double> bottom = new LinkedList<>();

        Series series = this.getData().get(0);
        ObservableList<Data<Number,Number>> data = series.getData();

        double startX = 0;

        for(int i = 0; i < this.getData().get(0).getData().size(); i++) {

            //
            DataPoint pre = (i == 0 ? null : captureSession.get(data.get(i).getXValue().intValue()));
            DataPoint current = captureSession.get(data.get(i).getXValue().intValue());
            DataPoint post = (i == data.size() - 1 ? null : captureSession.get(data.get(i + 1).getXValue().intValue()));

            HighestValue highestPreValue = (pre == null ? new HighestValue(null, ground) : highestYValue((i - 1)));
            HighestValue highestCurrentValue = highestYValue(i);
            HighestValue highestPostValue = (post == null ? new HighestValue(null, ground) : highestYValue((i + 1)));

            //--
            double preX = (i == 0 ? ground : this.getXAxis().getDisplayPosition(data.get(i-1).getXValue()));
            double preY = (i == 0 ? ground : this.getYAxis().getDisplayPosition(highestPreValue.getValue()));

            double currX = (this.getXAxis().getDisplayPosition(current.getData(DataPoint.Types.VEHICLES_AI).getXValue()));
            double currY = (this.getYAxis().getDisplayPosition(highestCurrentValue.getValue()));

            double postX = (post == null ? ground : this.getXAxis().getDisplayPosition(data.get(i+1).getXValue()));
            double postY = (post == null ? ground : this.getYAxis().getDisplayPosition(highestPostValue.getValue()));

            //--- Start
            if(polygon.getPoints().isEmpty() && current.getGameState().getBackground() != null) {
                polygon.getPoints().addAll(
                        currX,ground, currX,currY
                );
                startX = currX;
                inMiddle = true;
            }
            //--- Middle
            else if(inMiddle) {
                inMiddle = (post != null && current.getGameState() == post.getGameState());

                if(inMiddle) {
                    if(highestPreValue.getSeries() == highestCurrentValue.getSeries()) {
                        polygon.getPoints().addAll(
                                preX, preY, currX, currY,
                                currX, currY, postX, postY
                        );
                    } else {

                        double preB = highestPreValue.getValue();
                        double preM = (preB - highestPreValue.getSeries().getData().get(i).getYValue().doubleValue());

                        double lowB = secondHighestYValue(i - 1);
                        double lowM = (lowB - highestCurrentValue.getValue());

                        double interX = this.getXAxis().getDisplayPosition((i - 1) + (Math.abs((lowB-preB) / (preM-lowM))));
                        double interY = this.getYAxis().getDisplayPosition((preM * (lowB-preB) / (preM-lowM) + preB));

                        polygon.getPoints().addAll(
                                preX, preY, interX, interY,
                                interX,interY,currX, currY
                        );

                    }

                } else {

                    polygon.getPoints().addAll(
                            preX,preY, currX, currY,
                            currX,currY, currX,ground,
                            currX,ground,preX,ground,
                            preX,ground, startX,ground
                    );

                    polygon.getPoints().addAll(bottom);
                    getPlotChildren().add(polygon);
                    polygon.toBack();

                    polygon.setOpacity(0.4D);

                    switch (this.backgroundType) {
                        case COLOUR: polygon.setFill(this.captureSession.get(i).getGameState().getBackgroundGradient()); break;
                        case IMAGE: polygon.setFill(new ImagePattern(current.getGameState().getBackground())); break;
                    }


                    polygon = new Polygon();
                    bottom.clear();
                }
            }

        }

    }

    private HighestValue highestYValue(int index) {
        Iterator<Series<Number, Number>> iterator = this.getData().iterator();
        double max = Double.NEGATIVE_INFINITY;
        Series<Number, Number> maxSeries = null;
        while (iterator.hasNext()) {
            Series<Number, Number> series = iterator.next();
            Data<Number, Number> numberData = series.getData().get(index);
            if (numberData.getNode().isVisible()) {
                if(numberData.getYValue().doubleValue() > max) {
                    max = numberData.getYValue().doubleValue();
                    maxSeries = series;
                }
            }
        }
        return new HighestValue(maxSeries, max);
    }

    private double secondHighestYValue(int index) {
        Iterator<Series<Number, Number>> iterator = this.getData().iterator();

        double max = Double.NEGATIVE_INFINITY;
        double secondMax = 0;
        while (iterator.hasNext()) {
            Data<Number, Number> numberData = iterator.next().getData().get(index);
            if (numberData.getNode().isVisible()) {
                double value = numberData.getYValue().doubleValue();
                if(value > max) {
                    secondMax = max;
                    max = value;
                } else if(value > secondMax) {
                    secondMax = value;
                }
            }
        }
        return secondMax;
    }

    private class HighestValue {

        private Series<Number, Number> series;
        private double value;

        public HighestValue(Series<Number, Number> series, double value) {
            this.series = series;
            this.value = value;
        }

        public Series<Number, Number> getSeries() {
            return series;
        }

        public double getValue() {
            return value;
        }
    }

    public enum BackgroundType {

        NONE,
        COLOUR,
        IMAGE;

    }

}
