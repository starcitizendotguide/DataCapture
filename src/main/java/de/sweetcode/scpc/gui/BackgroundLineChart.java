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
import java.util.Map;

public class BackgroundLineChart extends LineChart<Number, Number> {

    private final CaptureSession captureSession;
    private BackgroundType backgroundType = BackgroundType.NONE;

    public BackgroundLineChart(CaptureSession captureSession, Axis<Number> xAxis, Axis<Number> yAxis) {
        super(xAxis, yAxis);
        this.captureSession = captureSession;
    }

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

        switch (this.backgroundType) {

            case NONE: break;

            case COLOUR: {
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
            } break;

            case IMAGE: {

                Polygon polygon = new Polygon();
                final double ground = this.getYAxis().getDisplayPosition(0);

                boolean inMiddle = false;
                List<Double> bottom = new LinkedList<>();

                Series series = this.getData().get(0);
                ObservableList<Data<Number,Number>> data = series.getData();

                for(int i = 0; i < this.getData().get(0).getData().size(); i++) {

                    //
                    DataPoint pre = (i == 0 ? null : captureSession.get(data.get(i).getXValue().intValue()));
                    DataPoint current = captureSession.get(data.get(i).getXValue().intValue());
                    DataPoint post = (i == data.size() - 1 ? null : captureSession.get(data.get(i + 1).getXValue().intValue()));

                    /*double highestPreValue = pre == null ? ground : pre.getData().entrySet().stream()
                            .max(Map.Entry.comparingByValue((a, b) -> (Double.compare(a.doubleValue(), b.doubleValue()))))
                            .get().getValue().doubleValue();*/
                    /*
                    double highestCurrentValue = current.getData().entrySet().stream()
                            .max(Map.Entry.comparingByValue((a, b) -> (Double.compare(a.doubleValue(), b.doubleValue()))))
                            .get().getValue().doubleValue();

                    double highestPostValue = post == null ? ground : post.getData().entrySet().stream()
                            .max(Map.Entry.comparingByValue((a, b) -> (Double.compare(a.doubleValue(), b.doubleValue()))))
                            .get().getValue().doubleValue();
                     */

                    double highestPreValue = (pre == null ? ground : Double.NEGATIVE_INFINITY);
                    if(!(pre == null)) {
                        Iterator<Series<Number, Number>> preIterator = this.getData().iterator();
                        while (preIterator.hasNext()) {
                            Data<Number, Number> numberData = preIterator.next().getData().get(i - 1);
                            if (numberData.getNode().isVisible()) {
                                highestPreValue = Math.max(numberData.getYValue().doubleValue(), highestPreValue);
                            }
                        }
                    }

                    double highestCurrentValue = Double.NEGATIVE_INFINITY;
                    Iterator<Series<Number, Number>> currentIterator = this.getData().iterator();
                    while (currentIterator.hasNext()) {
                        Data<Number, Number> numberData = currentIterator.next().getData().get(i);
                        if(numberData.getNode().isVisible()) {
                            highestCurrentValue = Math.max(numberData.getYValue().doubleValue(), highestCurrentValue);
                        }
                    }

                    double highestPostValue = (post == null ? ground : Double.NEGATIVE_INFINITY);
                    if(!(post == null)) {
                        Iterator<Series<Number, Number>> postIterator = this.getData().iterator();
                        while (postIterator.hasNext()) {
                            Data<Number, Number> numberData = postIterator.next().getData().get(i + 1);
                            if (numberData.getNode().isVisible()) {
                                highestPostValue = Math.max(numberData.getYValue().doubleValue(), highestPostValue);
                            }
                        }
                    }



                    //--
                    double preX = (i == 0 ? ground : this.getXAxis().getDisplayPosition(data.get(i-1).getXValue()));
                    double preY = (i == 0 ? ground : this.getYAxis().getDisplayPosition(highestPreValue));

                    double currX = (this.getXAxis().getDisplayPosition(current.getData(DataPoint.Types.VEHICLES_AI).getXValue()));
                    double currY = (this.getYAxis().getDisplayPosition(highestCurrentValue));

                    double postX = (post == null ? ground : this.getXAxis().getDisplayPosition(data.get(i+1).getXValue()));
                    double postY = (post == null ? ground : this.getYAxis().getDisplayPosition(highestPostValue));

                    //--- Start
                    if(polygon.getPoints().isEmpty() && current.getGameState().getBackground() != null) {
                        polygon.getPoints().addAll(
                                currX,ground,   preX,ground,
                                preX,ground,    preX,preY,
                                preX,preY,      currX,currY
                        );
                        inMiddle = true;
                    }
                    //--- Middle
                    else if(inMiddle) {
                        inMiddle = (post != null && current.getGameState() == post.getGameState());

                        if(inMiddle) {
                            polygon.getPoints().addAll(
                                    preX,preY, currX, currY,
                                    currX, currY, postX, postY
                            );
                            bottom.add(preX);
                            bottom.add(ground);
                            bottom.add(currX);
                            bottom.add(ground);
                            bottom.add(currX);
                            bottom.add(ground);
                            bottom.add(postX);
                            bottom.add(ground);
                        } else {

                            polygon.getPoints().addAll(
                                    preX,preY, currX, currY,
                                    currX,currY, currX,ground,
                                    currX,ground,preX,ground
                            );

                            polygon.getPoints().addAll(bottom);
                            getPlotChildren().add(polygon);
                            polygon.toBack();

                            polygon.setOpacity(1D);
                            polygon.setFill(new ImagePattern(current.getGameState().getBackground()));

                            polygon = new Polygon();
                            bottom = new LinkedList<>();
                        }
                    }

                }

            } break;
        }
    }

    public enum BackgroundType {

        NONE,
        COLOUR,
        IMAGE;

    }

}
