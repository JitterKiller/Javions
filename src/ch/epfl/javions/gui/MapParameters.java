package ch.epfl.javions.gui;

import ch.epfl.javions.Math2;
import ch.epfl.javions.Preconditions;
import javafx.beans.property.*;

public final class MapParameters {

    private static final int ZOOM_MIN = 6;
    private static final int ZOOM_MAX = 19;

    private final IntegerProperty zoom = new SimpleIntegerProperty();
    private final DoubleProperty minX = new SimpleDoubleProperty();
    private final DoubleProperty minY = new SimpleDoubleProperty();

    public ReadOnlyIntegerProperty zoomProperty() {
        return zoom;
    }
    public ReadOnlyDoubleProperty minXProperty() {
        return minX;
    }
    public ReadOnlyDoubleProperty minYProperty() {
        return minY;
    }

    public int getZoomProperty() {
        return zoomProperty().get();
    }
    public double getMinXProperty() {
        return minXProperty().get();
    }
    public double getMinYProperty() {
        return minYProperty().get();
    }

    public void setZoom(int zoom) {
        this.zoom.set(zoom);
    }
    public void setMinX(double minX) {
        this.minX.set(minX);
    }
    public void setMinY(double minY) {
        this.minY.set(minY);
    }

    public MapParameters(int zoom, double minX, double minY) {
        Preconditions.checkArgument(ZOOM_MIN <= zoom && zoom <= ZOOM_MAX);
        setZoom(zoom);
        setMinX(minX);
        setMinY(minY);
    }

    public void scroll(double x, double y) {
        setMinX(getMinXProperty() + x);
        setMinY(getMinYProperty() + y);
    }

    public void changeZoomLevel(int zoomDelta) {

        int oldZoom = getZoomProperty();
        int newZoom = Math2.clamp(ZOOM_MIN, getZoomProperty() + zoomDelta,ZOOM_MAX);;

        setZoom(newZoom);

        double scaleFactor = Math.scalb(1d, newZoom - oldZoom);
        setMinX(getMinXProperty() * scaleFactor);
        setMinY(getMinYProperty() * scaleFactor);
    }
}