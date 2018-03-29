package de.sweetcode.scpc.gui;

import de.sweetcode.scpc.data.CaptureSession;
import javafx.collections.ObservableList;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.shape.Polygon;

import java.util.Iterator;

public class BackgroundColourLineChart extends LineChart<Number, Number> implements CaptureLineChart {

    private final CaptureSession captureSession;

    public BackgroundColourLineChart(CaptureSession captureSession, Axis<Number> xAxis, Axis<Number> yAxis, ObservableList<Series<Number, Number>> data) {
        super(xAxis, yAxis, data);
        this.captureSession = captureSession;
    }


    @Override
    public void redraw() {
        this.layoutPlotChildren();
    }

    @Override
    protected void layoutPlotChildren() {
        super.layoutPlotChildren();

        //NOTE: We remove the old Polygons os that when we redraw, we only draw the new ones and the old
        // Polygons disappear.
        this.getPlotChildren().removeIf(e -> e instanceof Polygon);

        final double ground = this.getYAxis().getDisplayPosition(0);

        for(int i = 0; i < getData().get(0).getData().size() - 1; i++) {

            if(this.captureSession.get(i).getGameState().getBackgroundGradient() == null) {
                continue;
            }

            double currentMax = Double.NEGATIVE_INFINITY;
            Iterator<Series<Number, Number>> currentIterator = this.getData().iterator();
            while (currentIterator.hasNext()) {
                Data<Number, Number> data = currentIterator.next().getData().get(i);
                if(data.getNode().isVisible()) {
                    currentMax = Math.max(data.getYValue().doubleValue(), currentMax);
                }
            }

            double postMax = Double.NEGATIVE_INFINITY;
            Iterator<Series<Number, Number>> postIterator = this.getData().iterator();
            while (postIterator.hasNext()) {
                Data<Number, Number> data = postIterator.next().getData().get(i + 1);
                if(data.getNode().isVisible()) {
                    postMax = Math.max(data.getYValue().doubleValue(), postMax);
                }
            }

            double x = getXAxis().getDisplayPosition(this.getData().get(0).getData().get(i).getXValue());
            double y = getYAxis().getDisplayPosition(currentMax);
            double x2 = getXAxis().getDisplayPosition(this.getData().get(0).getData().get(i + 1).getXValue());
            double y2 = getYAxis().getDisplayPosition(postMax);
            Polygon polygon = new Polygon();

            polygon.getPoints().addAll(
                    x, ground, x,y,
                    x,y, x2,y2,
                    x2,y2, x2,ground,
                    x2,ground, x,ground
            );
            getPlotChildren().add(polygon);
            polygon.toFront();
            polygon.setFill(this.captureSession.get(i).getGameState().getBackgroundGradient());
        }

    }
}
