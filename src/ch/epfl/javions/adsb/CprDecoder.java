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
public class CprDecoder {
    private final static int EVEN_LATITUDE_ZONES = 60;
    private final static double EVEN_LATITUDE_ZONES_WIDTH = 1d / EVEN_LATITUDE_ZONES;
    private final static int ODD_LATITUDE_ZONES = 59;
    private final static double ODD_LATITUDE_ZONES_WIDTH = 1d / ODD_LATITUDE_ZONES;

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

        double evenLatitudeDegree;
        double oddLatitudeDegree;

        /* On définit déjà les longitudes des messages pairs et impairs.
         * De ce fait, le cas où le nombre de zones de longitude est égal à 1 est déjà traité.
         * Ces longitudes seront redéfinies dans le cas ou le nombre de zones de longitude n'est pas égal à 1.*/
        double evenLongitudeDegree = Units.convert(refocusingOf(x0), Units.Angle.TURN, Units.Angle.DEGREE);
        double oddLongitudeDegree = Units.convert(refocusingOf(x1), Units.Angle.TURN, Units.Angle.DEGREE);

        /* On calcule le numéro de la zone de latitude dans lequel l'aéronef se trouve dans chacun des deux découpages */
        int latitudeZoneNumber = (int) Math.rint((y0 * ODD_LATITUDE_ZONES) - (y1 * EVEN_LATITUDE_ZONES));

        int evenLatitudeZoneNumber;
        int oddLatitudeZoneNumber;

        /* On calcule les numéros de la zone de latitude pour le découpage pair et impair selon "latitudeZoneNumber". */
        if (latitudeZoneNumber < 0) {
            evenLatitudeZoneNumber = latitudeZoneNumber + EVEN_LATITUDE_ZONES;
            oddLatitudeZoneNumber = latitudeZoneNumber + ODD_LATITUDE_ZONES;
        } else {
            evenLatitudeZoneNumber = latitudeZoneNumber;
            oddLatitudeZoneNumber = latitudeZoneNumber;
        }

        /* On détermine ensuite la latitude à laquelle l'aéronef se trouvait au moment de l'envoi de chacun des deux messages
         * Les latitudes des deux messages sont exprimés en tours, puis en degrés. */
        double evenLatitudeTurn = EVEN_LATITUDE_ZONES_WIDTH * (evenLatitudeZoneNumber + y0);
        evenLatitudeDegree = Units.convert(refocusingOf(evenLatitudeTurn), Units.Angle.TURN, Units.Angle.DEGREE);

        double oddLatitudeTurn = ODD_LATITUDE_ZONES_WIDTH * (oddLatitudeZoneNumber + y1);
        oddLatitudeDegree = Units.convert(refocusingOf(oddLatitudeTurn), Units.Angle.TURN, Units.Angle.DEGREE);


        /* On calcule ensuite le nombre de zones de longitude dans le découpage pair avec les deux latitudes (paire et impaire). */
        double AEven = (1d - Math.cos(2 * Math.PI * EVEN_LATITUDE_ZONES_WIDTH)) / Math.pow(Math.cos(Units.convertFrom(evenLatitudeDegree, Units.Angle.DEGREE)), 2);
        double AOdd = (1d - Math.cos(2 * Math.PI * EVEN_LATITUDE_ZONES_WIDTH)) / Math.pow(Math.cos(Units.convertFrom(oddLatitudeDegree, Units.Angle.DEGREE)), 2);

        double evenLongitudeZoneValue = Math.floor((2d * Math.PI) / Math.acos(1 - AEven));
        double oddLongitudeZoneValue = Math.floor((2d * Math.PI) / Math.acos(1 - AOdd));
        int evenLongitudeZone;

        /* Si on obtient ainsi deux valeurs différentes, cela signifie qu'entre les deux messages,
         * l'aéronef a changé de "bande d'altitude", et il n'est donc pas possible de déterminer sa position
         * La méthode Double.compare() retourne 0 si les deux valeurs sont égales (même si c'est deux NaN).*/
        if (Double.compare(evenLongitudeZoneValue, oddLongitudeZoneValue) == 0) {
            /* Si le résultat de la formule n'est pas défini, par définition le nombre dde zones de longitude vaut 1.*/
            if (areLongitudeZoneValuesNaN(AEven, AOdd)) {
                evenLongitudeZone = 1;
            } else {
                evenLongitudeZone = (int) evenLongitudeZoneValue;
            }
        } else {
            return null;
        }

        int oddLongitudeZone = evenLongitudeZone - 1;

        int evenLongitudeZoneNumber;
        int oddLongitudeZoneNumber;

        double evenLongitudeTurn;
        double oddLongitudeTurn;

        /* Enfin, on calcule le numéro de la zone de longitude dans lequel l'aéronef se trouve dans chacun des deux découpages.
         * S'il est égal à 1, les deux longitudes ont déjà été calculées plus haut dans la méthode,
         * S'il est supérieur à 1, on procède comme pour le calcul des numéros de numéros de la zone de latitude
         * pour le découpage pair et impair.*/
        if (evenLongitudeZone > 1) {

            int longitudeZoneNumber = (int) Math.rint((x0 * oddLongitudeZone) - (x1 * evenLongitudeZone));

            if (longitudeZoneNumber < 0) {
                evenLongitudeZoneNumber = longitudeZoneNumber + evenLongitudeZone;
                oddLongitudeZoneNumber = longitudeZoneNumber + oddLongitudeZone;
            } else {
                evenLongitudeZoneNumber = longitudeZoneNumber;
                oddLongitudeZoneNumber = longitudeZoneNumber;
            }

            evenLongitudeTurn = (1d / evenLongitudeZone) * (evenLongitudeZoneNumber + x0);
            evenLongitudeDegree = Units.convert(refocusingOf(evenLongitudeTurn), Units.Angle.TURN, Units.Angle.DEGREE);

            oddLongitudeTurn = (1d / oddLongitudeZone) * (oddLongitudeZoneNumber + x1);
            oddLongitudeDegree = Units.convert(refocusingOf(oddLongitudeTurn), Units.Angle.TURN, Units.Angle.DEGREE);

        }

        /* Renvoie un objet GeoPos selon le dernier message (s'il est pair, on utilise la méthode
         * statique geoPosOf() avec les longitudes et latitudes des messages pairs, sinon ceux des
         * messages impairs.) */
        if (mostRecent == 0) {
            return geoPosOf(evenLongitudeDegree, evenLatitudeDegree);
        } else {
            return geoPosOf(oddLongitudeDegree, oddLatitudeDegree);
        }
    }

    /**
     * Méthode qui renvoie un Objet GeoPos (la position de l'aéronef au moment de l'envoi du message)
     *
     * @param longitudeDegree la longitude de l'aéronef en degrés.
     * @param latitudeDegree  la latitude de l'aéronef en degrés.
     * @return La position géographique de l'aéronef au moment de l'envoi du message
     * selon sa latitude et sa longitude en T32 (converties depuis des degrés),
     * ou null si la latitude en degrés n'est pas comprise entre -90° et 90° inclus.
     */
    private static GeoPos geoPosOf(double longitudeDegree, double latitudeDegree) {
        int longitudeT32;
        int latitudeT32;
        longitudeT32 = (int) Math.rint(Units.convert(longitudeDegree, Units.Angle.DEGREE, Units.Angle.T32));
        latitudeT32 = (int) Math.rint(Units.convert(latitudeDegree, Units.Angle.DEGREE, Units.Angle.T32));
        return (latitudeDegree <= 90d) && (latitudeDegree >= -90d) ? new GeoPos(longitudeT32, latitudeT32) : null;
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
}