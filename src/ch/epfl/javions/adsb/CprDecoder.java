package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;

/**
 * La classe CprDecoder (non instanciable) représente un décodeur de position CPR.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */
public final class CprDecoder {
    private static final int EVEN_LATITUDE_ZONES = 60;
    private static final double EVEN_LATITUDE_ZONES_WIDTH = 1d / EVEN_LATITUDE_ZONES;
    private static final int ODD_LATITUDE_ZONES = 59;
    private static final double ODD_LATITUDE_ZONES_WIDTH = 1d / ODD_LATITUDE_ZONES;

    /**
     * Constructeur privé de la classe CprDecoder (non instanciable).
     */
    private CprDecoder() {
    }

    /**
     * Méthode statique unique de la classe CprDecoder.
     *
     * @param x0         La longitude locale (normalisée) d'un message pair.
     * @param y0         La latitude locale (normalisée) d'un message pair.
     * @param x1         La longitude locale (normalisée) d'un message impair.
     * @param y1         La latitude locale (normalisée) d'un message impair.
     * @param mostRecent l'index du message le plus récent (0 pour pair, 1 pour impair).
     * @return La position géographique correspondant aux arguments de la méthode,
     * ou null si la latitude décodée n'est pas valide (comprise entre -90° et 90°),
     * ou si la position ne peut pas être déterminée en raison d'un changement de bande de latitude.
     * @throws IllegalArgumentException si l'index du message le plus récent (mostRecent) n'est pas égal à 0 ou 1.
     */
    public static GeoPos decodePosition(double x0, double y0, double x1, double y1, int mostRecent) {
        Preconditions.checkArgument(mostRecent == 0 || mostRecent == 1);

        double evenLatitudeTurn, oddLatitudeTurn;

        /* On définit déjà les longitudes des messages pairs et impairs.
         * De ce fait, le cas où le nombre de zones de longitude est égal à 1 est déjà traité.
         * Ces longitudes seront redéfinies dans le cas ou le nombre de zones de longitude n'est pas égal à 1.*/
        double evenLongitudeTurn = refocusingOf(x0);
        double oddLongitudeTurn = refocusingOf(x1);

        /* On calcule le numéro de la zone de latitude dans lequel l'aéronef se
         * trouve dans chacun des deux découpages */
        int latitudeZoneNumber = (int) Math.rint((y0 * ODD_LATITUDE_ZONES) - (y1 * EVEN_LATITUDE_ZONES));

        evenLatitudeTurn = computeLatitudeTurn(EVEN_LATITUDE_ZONES_WIDTH, EVEN_LATITUDE_ZONES, latitudeZoneNumber, y0);
        oddLatitudeTurn = computeLatitudeTurn(ODD_LATITUDE_ZONES_WIDTH, ODD_LATITUDE_ZONES, latitudeZoneNumber, y1);

        /* On calcule ensuite le nombre de zones de longitude dans le découpage
         * pair avec les deux latitudes (paire et impaire). */
        double AEven = computeA(evenLatitudeTurn);
        double AOdd = computeA(oddLatitudeTurn);

        double evenLongitudeZoneValue = computeLongitudeZoneValue(AEven);
        double oddLongitudeZoneValue = computeLongitudeZoneValue(AOdd);

        int evenLongitudeZone;
         /* La méthode Double.compare() retourne 0 si les deux valeurs sont égales (même si c'est deux NaN).*/
        if (Double.compare(evenLongitudeZoneValue, oddLongitudeZoneValue) == 0) {
            if (areLongitudeZoneValuesNaN(AEven, AOdd)) evenLongitudeZone = 1;
            else evenLongitudeZone = (int) evenLongitudeZoneValue;
        } else return null;

        int oddLongitudeZone = evenLongitudeZone - 1;

        if (evenLongitudeZone > 1) {
            int longitudeZoneNumber = (int) Math.rint((x0 * oddLongitudeZone) - (x1 * evenLongitudeZone));
            evenLongitudeTurn = computeLongitudeTurn(evenLongitudeZone, longitudeZoneNumber, x0);
            oddLongitudeTurn = computeLongitudeTurn(oddLongitudeZone, longitudeZoneNumber, x1);
        }

        switch (mostRecent) {
            case 0 -> {
                return geoPosOf(evenLongitudeTurn,evenLatitudeTurn);
            }
            case 1 -> {
                return geoPosOf(oddLongitudeTurn,oddLatitudeTurn);
            }
            default -> {
                return null;
            }
        }
    }

    /**
     * Méthode qui renvoie un Objet GeoPos (la position de l'aéronef au moment de l'envoi du message)
     * si la latitude de l'aéronef est valide.
     *
     * @param longitudeTurn la longitude de l'aéronef en tour.
     * @param latitudeTurn  la latitude de l'aéronef en tour.
     */
    private static GeoPos geoPosOf(double longitudeTurn, double latitudeTurn) {
        int longitudeT32, latitudeT32;
        longitudeT32 = (int) Math.rint(Units.convert(longitudeTurn, Units.Angle.TURN, Units.Angle.T32));
        latitudeT32 = (int) Math.rint(Units.convert(latitudeTurn, Units.Angle.TURN, Units.Angle.T32));
        return GeoPos.isValidLatitudeT32(latitudeT32) ? new GeoPos(longitudeT32, latitudeT32) : null;
    }

    /**
     * Méthode statique qui permet de recentrer autour de 0 les longitudes et latitudes calculées par les formules
     * de la méthode statique decodePosition().
     *
     * @param coordinate La longitude/latitude à recentrer.
     * @return La longitude/latitude recentrée vers 0 si elle est supérieure à 0,5 tour
     * (on soustrait 1 tour donc 360°), sinon on retourne uniquement la longitude/latitude normale.
     */
    private static double refocusingOf(double coordinate) {
        return coordinate >= 0.5d ? (coordinate - 1d) : coordinate;
    }

    /**
     * Méthode statique qui permet de savoir si le nombre de zones de longitude dans le découpage
     * pair avec les deux latitudes (paire et impaire) sont tous les deux des NaN (Not a Number)
     *
     * @param AEven La constante A calculé avec la latitude paire.
     * @param AOdd  La constante A calculé avec la latitude impaire
     * @return Vrai si les deux nombres de zones de longitudes dans le découpage
     * pair avec les deux latitudes (paire et impaire) sont tous les deux des NaN, faux sinon.
     */
    private static boolean areLongitudeZoneValuesNaN(double AEven, double AOdd) {
        return Double.isNaN(Math.acos(1 - AEven)) && Double.isNaN(Math.acos(1 - AOdd));
    }

    /**
     * Méthode statique qui permet de calculer le dénominateur A lors du calcul du nombre de zones de longitude
     * (paires ou impaires).
     *
     * @param latitudeTurn La latitude en tours (pair ou impair).
     * @return Le dénominateur A utilisé lors du calcul du nombre de zones de longitude.
     */
    private static double computeA(double latitudeTurn) {
        double numerator = (1d - Math.cos(Units.Angle.TURN * EVEN_LATITUDE_ZONES_WIDTH));
        return numerator / Math.pow(Math.cos(Units.convertFrom(latitudeTurn, Units.Angle.TURN)), 2);
    }

    /**
     * Méthode statique qui permet de calculer la latitude en tour selon le découpage (pair ou impair).
     *
     * @param zoneWidth          La largeur des zones.
     * @param numberOfZones      Le nombre de zones de latitude.
     * @param latitudeZoneNumber Le numéro de zone de latitude dans lequel l'aéronef se trouve.
     * @param y                  La latitude locale.
     * @return La latitude de l'aéronef en tour.
     */
    private static double computeLatitudeTurn(double zoneWidth,
                                              double numberOfZones,
                                              double latitudeZoneNumber,
                                              double y) {
        return latitudeZoneNumber < 0 ?
                refocusingOf(zoneWidth * (latitudeZoneNumber + numberOfZones + y)) :
                refocusingOf(zoneWidth * (latitudeZoneNumber + y));
    }

    /**
     * Méthode statique qui permet de calculer la longitude en tour selon le découpage (pair ou impair).
     *
     * @param numberOfZones      Le nombre de zones de longitude.
     * @param latitudeZoneNumber Le numéro de zone de longitude dans lequel l'aéronef se trouve.
     * @param x                  La longitude locale.
     * @return La longitude de l'aéronef en tour.
     */
    private static double computeLongitudeTurn(double numberOfZones,
                                               double latitudeZoneNumber,
                                               double x) {
        return latitudeZoneNumber < 0 ?
                refocusingOf((1d / numberOfZones) * (latitudeZoneNumber + numberOfZones + x)) :
                refocusingOf((1d / numberOfZones) * (latitudeZoneNumber + x));
    }

    /**
     * Méthode statique qui permet de calculer le nombre de zones de longitude dans le découpage pair et impair
     * (selon le dénominateur A).
     *
     * @param A Le dénominateur A utilisé dans le calcul.
     * @return Le nombre de zones de longitude.
     */
    private static double computeLongitudeZoneValue(double A) {
        return Math.floor(Units.Angle.TURN / Math.acos(1 - A));
    }

}