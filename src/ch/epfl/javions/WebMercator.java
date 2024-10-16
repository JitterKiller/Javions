package ch.epfl.javions;

import static ch.epfl.javions.Math2.asinh;

/**
 * Classe WebMercator permettant la projection des coordonnées géographiques selon la projection WebMercator.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */
public final class WebMercator {

    /**
     * Constructeur privé de la classe (non instanciable).
     */
    private WebMercator() {
    }

    /**
     * Coordonnée x.
     *
     * @param zoomLevel le niveau de zoom.
     * @param longitude la longitude.
     * @return la coordonnée x correspondant à la longitude donnée (en radians) au niveau de zoom donné.
     */
    public static double x(int zoomLevel, double longitude) {
        return Math.scalb(Units.convertTo(longitude, Units.Angle.TURN) + 0.5d, 8 + zoomLevel);
    }


    /**
     * Coordonnée y.
     *
     * @param zoomLevel le niveau de zoom.
     * @param latitude  la latitude.
     * @return la coordonnée y correspondant à la latitude donnée (en radians) au niveau de zoom donné.
     */
    public static double y(int zoomLevel, double latitude) {
        return Math.scalb(-Units.convertTo(asinh(Math.tan(latitude)), Units.Angle.TURN) + 0.5d, 8 + zoomLevel);
    }
}
