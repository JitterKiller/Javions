package ch.epfl.javions.gui;

import ch.epfl.javions.Math2;
import ch.epfl.javions.Preconditions;
import javafx.beans.property.*;

public final class MapParameters {

    private static final int ZOOM_MIN = 6;
    private static final int ZOOM_MAX = 19;

    private final IntegerProperty zoom = new SimpleIntegerProperty();
    private final IntegerProperty minX = new SimpleIntegerProperty();
    private final IntegerProperty minY = new SimpleIntegerProperty();

    public ReadOnlyIntegerProperty zoomProperty() {
        return zoom;
    }
    public ReadOnlyIntegerProperty minXProperty() {
        return minX;
    }
    public ReadOnlyIntegerProperty minYProperty() {
        return minY;
    }

    public int getZoom() {
        return zoomProperty().get();
    }
    public int getMinX() {
        return minXProperty().get();
    }
    public int getMinY() {
        return minYProperty().get();
    }

    private void setZoom(int zoom) {
        this.zoom.set(zoom);
    }
    private void setMinX(int minX) {
        this.minX.set(minX);
    }
    private void setMinY(int minY) {
        this.minY.set(minY);
    }

    public MapParameters(int zoom, int minX, int minY) {
        Preconditions.checkArgument(ZOOM_MIN <= zoom && zoom <= ZOOM_MAX);
        setZoom(zoom);
        setMinX(minX);
        setMinY(minY);
    }

    public void scroll(int x, int y) {
        setMinX(getMinX() + x);
        setMinY(getMinY() + y);
    }

    public void changeZoomLevel(int zoomDiff) {

        int newZoom = Math2.clamp(ZOOM_MIN,getZoom() + zoomDiff,ZOOM_MAX);;

        setZoom(newZoom);

        int scaleFactor = (int) Math.scalb(1d, zoomDiff);
        setMinX(getMinX() * scaleFactor);
        setMinY(getMinY() * scaleFactor);
    }
}