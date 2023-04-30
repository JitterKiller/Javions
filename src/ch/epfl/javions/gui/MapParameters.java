package ch.epfl.javions.gui;

import ch.epfl.javions.Math2;
import ch.epfl.javions.Preconditions;
import javafx.beans.property.*;

/**
 * La classe MapParameters du sous-paquetage gui, représente
 * les paramètres de la portion de la carte visible dans l'interface graphique.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */
public final class MapParameters {

    private static final int ZOOM_MIN = 6;
    private static final int ZOOM_MAX = 19;

    private final IntegerProperty zoom = new SimpleIntegerProperty();
    private final DoubleProperty minX = new SimpleDoubleProperty();
    private final DoubleProperty minY = new SimpleDoubleProperty();

    /**
     * Méthode d'accès à la propriété zoom en lecture seule.
     *
     * @return La propriété zoom.
     */
    public ReadOnlyIntegerProperty zoomProperty() {
        return zoom;
    }

    /**
     * Méthode d'accès à la propriété minX en lecture seule.
     *
     * @return La propriété minX.
     */
    public ReadOnlyDoubleProperty minXProperty() {
        return minX;
    }

    /**
     * Méthode d'accès à la propriété minY en lecture seule.
     *
     * @return La propriété minY.
     */
    public ReadOnlyDoubleProperty minYProperty() {
        return minY;
    }

    /**
     * Méthode d'accès à la valeur contenue dans la propriété zoom.
     *
     * @return La valeur contenue dans la propriété zoom.
     */
    public int getZoomProperty() {
        return zoomProperty().get();
    }

    /**
     * Méthode d'accès à la valeur contenue dans la propriété minX.
     *
     * @return La valeur contenue dans la propriété minX.
     */
    public double getMinXProperty() {
        return minXProperty().get();
    }

    /**
     * Méthode d'accès à la valeur contenue dans la propriété minY.
     *
     * @return La valeur contenue dans la propriété minY.
     */
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