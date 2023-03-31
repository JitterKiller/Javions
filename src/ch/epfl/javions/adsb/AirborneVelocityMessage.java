package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

/**
 * L'enregistrement AirborneVelocityMessage représente un message ADS-B de vitesse en vol.
 *
 * @param timeStampNs L'horodatage du message, en nanosecondes.
 * @param icaoAddress L'adresse ICAO de l'expéditeur du message.
 * @param speed La vitesse de l'aéronef, en m/s.
 * @param trackOrHeading La direction de déplacement de l'aéronef, en radians.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */
public record AirborneVelocityMessage(long timeStampNs, IcaoAddress icaoAddress,
                                      double speed, double trackOrHeading) implements Message {

    private final static int MESSAGE_START = 21;
    private final static int ST_START = 48;
    private final static int ST_SIZE = 3;
    private final static int V_SIZE = 10;
    private final static int D_SIZE = 1;
    private final static int VNS_START = MESSAGE_START;
    private final static int VEW_START = MESSAGE_START + V_SIZE + D_SIZE;
    private final static int DNS_START = VNS_START + V_SIZE;
    private final static int DEW_START = VEW_START + V_SIZE;
    private final static int SH_SIZE = 1;
    private final static int SH_START = MESSAGE_START + 21;
    private final static int HDG_START = MESSAGE_START + 11;
    private final static int HDG_SIZE = 10;
    private final static int AS_START = MESSAGE_START;
    private final static int AS_SIZE = 10;

    /**
     * Constructeur compact de AirborneVelocityMessage.
     *
     * @throws NullPointerException Si l'adresse ICAO est nulle.
     * @throws IllegalArgumentException Si l'horodatage du message est strictement inférieur à 0.
     * @throws IllegalArgumentException Si l'argument speed est strictement inférieur à 0.
     * @throws IllegalArgumentException Si l'argument trackOrHeading est strictement inférieur à 0.
     */
    public AirborneVelocityMessage {
        Objects.requireNonNull(icaoAddress);
        Preconditions.checkArgument(timeStampNs >= 0);
        Preconditions.checkArgument(speed >= 0);
        Preconditions.checkArgument(trackOrHeading >= 0);
    }

    /**
     * Méthode statique qui retourne un message de vitesse en vol.
     *
     * @param rawMessage Le message brut donné.
     * @return le message de vitesse en vol correspondant au message brut donné
     * ou null si le sous-type du message est invalide, ou si la vitesse ou la direction de
     * déplacement ne peuvent pas être déterminés.
     */
    public static AirborneVelocityMessage of(RawMessage rawMessage) {

        /* On commence par extraire l'attribut ST du message pour savoir comment interpréter
        *  les 22 bits commençant au bit 21 (MESSAGE_START). */
        int subType = Bits.extractUInt(rawMessage.payload(), ST_START, ST_SIZE);

        double speed;
        double trackOrHeading = 0;

        /* On vérifie si le message est un message à interpréter comme un Ground Speed message */
        if (isSubTypeGroundSpeed(subType)) {

            int directionEastWest = Bits.extractUInt(rawMessage.payload(), DEW_START, D_SIZE);
            int directionNorthSouth = Bits.extractUInt(rawMessage.payload(), DNS_START, D_SIZE);
            int velocityEastWest = Bits.extractUInt(rawMessage.payload(), VEW_START, V_SIZE);
            int velocityNorthSouth = Bits.extractUInt(rawMessage.payload(), VNS_START, V_SIZE);

            /* Si les attributs VNS ou VEW valent 0, on ne peut pas calculer la norme de la vitesse
            *  (Puisque les attributs VNS et VEW indiquent la valeur absolue de la vitesse (+1).
            *  On retourne donc null. */
            if (velocityNorthSouth == 0 || velocityEastWest == 0) {
                return null;
            }

            speed = Math.hypot(velocityEastWest - 1, velocityNorthSouth - 1);

            /* Selon les valeurs des attributs DNS et/ou DEW on calcule la route de l'aéronef (track) correspondant */
            switch (directionNorthSouth) {
                case 0 -> {
                    switch (directionEastWest) {
                        case 0 -> trackOrHeading = Math.atan2(velocityEastWest - 1, velocityNorthSouth - 1);
                        case 1 -> trackOrHeading = Math.atan2(-(velocityEastWest - 1), velocityNorthSouth - 1);
                    }
                }
                case 1 -> {
                    switch (directionEastWest) {
                        case 0 -> trackOrHeading = Math.atan2(velocityEastWest - 1, -(velocityNorthSouth - 1));
                        case 1 -> trackOrHeading = Math.atan2(-(velocityEastWest - 1), -(velocityNorthSouth - 1));
                    }
                }
            }

            /* On convertit enfin la vitesse de Nœuds en mètres par secondes si le sous-type vaut 1
            *  Sinon la vitesse est convertie d'une unité qui correspond à 4 nœuds en mètres par secondes.
            *  L'angle passé en argument de AirborneVelocityMessage est enfin recentré entre 0 et 2π
            *  grâce à la méthode statique refocusTrackOrHeading(). */
            if (subType == 1) {
                speed = Units.convert(speed, Units.Speed.KNOT, Units.Speed.METER_PER_SECOND);
            } else {
                speed = Units.convert(speed, Units.Speed.KNOT * 4, Units.Speed.METER_PER_SECOND);
            }
            return new AirborneVelocityMessage(rawMessage.timeStampNs(),
                                               rawMessage.icaoAddress(),
                                               speed,
                                               refocusTrackOrHeading(trackOrHeading));
        }

        /* On vérifie si le message est un message à interpréter comme un Air Speed message */
        if (isSubTypeAirSpeed(subType)) {

            /* On extrait le bit SH, s'il vaut 0 alors le cap (heading) de l'aéronef est inconnu.
            *  On retourne donc null */
            int statusHeading = Bits.extractUInt(rawMessage.payload(), SH_START, SH_SIZE);
            if (statusHeading != 1) {
                return null;
            }

            int heading = Bits.extractUInt(rawMessage.payload(), HDG_START, HDG_SIZE);
            int airSpeed = Bits.extractUInt(rawMessage.payload(), AS_START, AS_SIZE);

            /* L'attribut AS correspond à la vitesse de l'aéronef (+1).
            *  S'il vaut 0, on ne peut pas calculer la vitesse, on retourne donc null. */
            if(airSpeed == 0) {
                return null;
            }

            trackOrHeading = Units.convertFrom(Math.scalb(heading, -10),Units.Angle.TURN);

            /* On convertit enfin la vitesse de Nœuds en mètres par secondes si le sous-type vaut 3
            *  Sinon la vitesse est convertie d'une unité qui correspond à 4 nœuds en mètres par secondes.
            *  L'angle passé en argument de AirborneVelocityMessage est enfin recentré entre 0 et 2π
            *  grâce à la méthode statique refocusTrackOrHeading(). */
            if (subType == 3) {
                speed = Units.convert(airSpeed - 1, Units.Speed.KNOT, Units.Speed.METER_PER_SECOND);
            } else {
                speed = Units.convert(airSpeed - 1, Units.Speed.KNOT * 4, Units.Speed.METER_PER_SECOND);
            }

            return new AirborneVelocityMessage(rawMessage.timeStampNs(),
                                               rawMessage.icaoAddress(),
                                               speed,
                                               refocusTrackOrHeading(trackOrHeading));
        }

        /* Si l'attribut ST (sous-type) extrait du message ne correspond ni à un Ground Speed message,
        *  ni à un Air Speed message, le sous-type est invalide, on retourne donc null. */
        return null;
    }

    /**
     * Méthode qui vérifie si le sous-type du message brut est un message communiquant
     * la vitesse sol de l'aéronef, ainsi que sa route (Ground Speed).
     *
     * @param ST L'attribut ST (sous-type du message).
     * @return Vrai si le sous-type correspond à un message Ground Speed (sous-type de 1 ou 2), sinon faux.
     */
    private static boolean isSubTypeGroundSpeed(int ST) {
        return ST == 1 || ST == 2;
    }

    /**
     * Méthode qui vérifie si le sous-type du message brut est un message communiquant
     * la vitesse air de l'aéronef, ainsi que son cap (Air Speed).
     *
     * @param ST L'attribut ST (sous-type du message).
     * @return Vrai si le sous-type correspond à un message Air Speed (sous-type de 3 ou 4), sinon faux.
     */
    private static boolean isSubTypeAirSpeed(int ST) {
        return ST == 3 || ST == 4;
    }

    /**
     * Méthode qui permet de recentrer entre 0 et 2π les angles négatifs retournés par la méthode Math.atan2().
     *
     * @param trackOrHeading L'angle à recentrer (en radians).
     * @return L'angle recentré entre 0 et 2π s'il est négatif, sinon on retourne uniquement l'angle normal.
     */
    private static double refocusTrackOrHeading(double trackOrHeading) {
        return trackOrHeading < 0 ? trackOrHeading + (2d * Math.PI) : trackOrHeading;
    }

}