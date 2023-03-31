package ch.epfl.javions;

/**
 * Cet enregistrement GeoPos représente des coordonnées géographiques, c.-à-d. un couple longitude/latitude.
 * Ces coordonnées sont exprimées en t32 et stockées sous la forme d'entiers de 32 bits (type int).
 *
 * @param latitudeT32  la valeur de l'unité de la latitude.
 * @param longitudeT32 la valeur de l'unité de la latitude.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */
public record GeoPos(int longitudeT32, int latitudeT32) {

    /**
     * Constructeur compact de GeoPos.
     *
     * @throws IllegalArgumentException si la latitude n'est pas comprise entre (-2 puissance 30) et 2 puissance 30
     *                                  grâce à la méthode checkArgument() de la classe Preconditions.
     */
    public GeoPos {
        Preconditions.checkArgument(isValidLatitudeT32(latitudeT32));
    }

    /**
     * La methode isValidLatitudeT32 retourne vrai si et seulement si la valeur passée, interprétée comme une latitude exprimée
     * en t32, est valide, c.-à-d. comprise entre -(2 puissance 30) (inclus, et qui correspond à -90°)
     * et 2 puissance 30 (inclus, et qui correspond à +90°).
     *
     * @param latitudeT32 la latitude.
     * @return vrai si la latitude est comprise entre -(2 puissance 30)
     * et 2 puissance 30, sinon ça retourne faux.
     */
    public static boolean isValidLatitudeT32(int latitudeT32) {
        return latitudeT32 <= (Math.scalb(1.0, 30)) && latitudeT32 >= (Math.scalb(-1.0, 30));
    }

    /**
     * Méthode qui convertie la longitude de T32 à radian.
     *
     * @return la longitude convertie de T32 à radian.
     */
    public double longitude() {
        return Units.convertFrom(longitudeT32, Units.Angle.T32);
    }

    /**
     * Méthode qui convertie la latitude de T32 à radian.
     *
     * @return la latitude convertie de T32 à radian.
     */
    public double latitude() {
        return Units.convertFrom(latitudeT32, Units.Angle.T32);
    }

    /**
     * Cette methode est une redefinition de toString de Object qui retourne une représentation textuelle de la position
     * dans laquelle la longitude et la latitude sont données dans cet ordre, en degrés.
     *
     * @return la longitude et la latitude en degrés (à partir des données en T32).
     */

    @Override
    public String toString() {
        return "(" + Units.convert(longitudeT32, Units.Angle.T32, Units.Angle.DEGREE) + "°, " + Units.convert(latitudeT32, Units.Angle.T32, Units.Angle.DEGREE) + "°)";
    }
}
