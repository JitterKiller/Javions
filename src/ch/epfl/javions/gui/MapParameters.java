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
     * Constructeur de MapParameters.
     *
     * @param zoom Le niveau de zoom.
     * @param minX La coordonnée x du coin haut-gauche de la portion visible de la carte.
     * @param minY La coordonnée y du coin haut-gauche de la portion visible de la carte.
     * @throws IllegalArgumentException si le niveau de zoom n'est pas compris entre 6 et 19 inclus.
     */
    public MapParameters(int zoom, double minX, double minY) {
        Preconditions.checkArgument(ZOOM_MIN <= zoom && zoom <= ZOOM_MAX);
        setZoom(zoom);
        setMinX(minX);
        setMinY(minY);
    }

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
    public int getZoom() {
        return zoomProperty().get();
    }

    /**
     * Méthode de modification de la valeur contenue dans la propriété zoom.
     *
     * @param zoom Le nouveau niveau zoom.
     */
    private void setZoom(int zoom) {
        this.zoom.set(zoom);
    }

    /**
     * Méthode d'accès à la valeur contenue dans la propriété minX.
     *
     * @return La valeur contenue dans la propriété minX.
     */
    public double getMinX() {
        return minXProperty().get();
    }

    /**
     * Méthode de modification de la valeur contenue dans la propriété minX.
     *
     * @param minX La nouvelle coordonnée x du coin haut-gauche de la portion visible de la carte.
     */
    private void setMinX(double minX) {
        this.minX.set(minX);
    }

    /**
     * Méthode d'accès à la valeur contenue dans la propriété minY.
     *
     * @return La valeur contenue dans la propriété minY.
     */
    public double getMinY() {
        return minYProperty().get();
    }

    /**
     * Méthode de modification de la valeur contenue dans la propriété minY.
     *
     * @param minY La nouvelle coordonnée y du coin haut-gauche de la portion visible de la carte.
     */
    private void setMinY(double minY) {
        this.minY.set(minY);
    }

    /**
     * Méthode qui permet la translation du coin haut-gauche de la portion visible de la carte vers
     * le vecteur de composante (x,y).
     *
     * @param x La composante x du vecteur utilisé pour la translation de la portion visible de la carte.
     * @param y La composante y du vecteur utilisé pour la translation de la portion visible de la carte.
     */
    public void scroll(double x, double y) {
        setMinX(getMinX() + x);
        setMinY(getMinY() + y);
    }

    /**
     * Méthode qui permet d'ajouter zoomDelta (négatif ou positif) au niveau de zoom.
     *
     * @param zoomDelta La différence de niveau de zoom à ajouter.
     */
    public void changeZoomLevel(int zoomDelta) {

        int oldZoom = getZoom();
        int newZoom = Math2.clamp(ZOOM_MIN, getZoom() + zoomDelta, ZOOM_MAX);

        setZoom(newZoom);

        double scaleFactor = Math.scalb(1, newZoom - oldZoom);
        setMinX(getMinX() * scaleFactor);
        setMinY(getMinY() * scaleFactor);
    }
}