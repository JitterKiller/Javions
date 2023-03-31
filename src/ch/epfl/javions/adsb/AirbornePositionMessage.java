package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

/**
 * L'enregistrement AirbornePositionMessage représente un message ADS-B de positionnement en vol.
 *
 * @param timeStampNs L'horodatage du message, en nanosecondes.
 * @param icaoAddress L'adresse ICAO de l'expéditeur du message.
 * @param altitude    L'altitude à laquelle se trouvait l'aéronef au moment de l'envoi du message, en mètres.
 * @param parity      La parité du message (0 s'il est pair, 1 s'il est impair).
 * @param x           La longitude locale et normalisée (comprise entre 0 et 1)
 *                    à laquelle se trouvait l'aéronef au moment de l'envoi du message.
 * @param y           La latitude locale et normalisée (comprise entre 0 et 1)
 *                    à laquelle se trouvait l'aéronef au moment de l'envoi du message.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */
public record AirbornePositionMessage(long timeStampNs,
                                      IcaoAddress icaoAddress,
                                      double altitude,
                                      int parity,
                                      double x,
                                      double y) implements Message {
    private final static int ALT_START = 36;
    private final static int ALT_SIZE = 12;
    private final static int Q_INDEX = 4;
    private final static int BASE_ALTITUDE_Q_1 = 1000;
    private final static int BASE_ALTITUDE_Q_0 = 1300;
    private static final int CPR_BITS = 17;
    private static final int LON_CPR_START = 0;
    private static final int LON_CPR_SIZE = CPR_BITS;
    private static final int LAT_CPR_START = LON_CPR_START + LON_CPR_SIZE;
    private static final int LAT_CPR_SIZE = CPR_BITS;
    private static final int FORMAT_START = 34;
    private static final int FORMAT_SIZE = 1;

    /**
     * Constructeur compact de AirbornePositionMessage
     *
     * @throws NullPointerException     Si l'adresse ICAO est nulle.
     * @throws IllegalArgumentException Si l'horodatage du message est strictement inférieur à 0.
     * @throws IllegalArgumentException Si l'argument parity est différent de 0 ou 1.
     * @throws IllegalArgumentException Si la latitude ou la longitude locale ne sont pas comprises entre 0 (inclus) et 1 (exclus).
     */
    public AirbornePositionMessage {
        Objects.requireNonNull(icaoAddress);
        Preconditions.checkArgument(timeStampNs >= 0);
        Preconditions.checkArgument(parity == 0 || parity == 1);
        Preconditions.checkArgument(0 <= x && x < 1);
        Preconditions.checkArgument(0 <= y && y < 1);
    }

    /**
     * Méthode statique qui retourne un message de positionnement en vol.
     *
     * @param rawMessage Le message brut donné.
     * @return le message de positionnement en vol correspondant au message brut donné
     * ou null si l'altitude est invalide
     */
    public static AirbornePositionMessage of(RawMessage rawMessage) {

        int inputAltitude = Bits.extractUInt(rawMessage.payload(), ALT_START, ALT_SIZE);

        int parity = Bits.extractUInt(rawMessage.payload(), FORMAT_START, FORMAT_SIZE);
        double latitude = Bits.extractUInt(rawMessage.payload(), LAT_CPR_START, LAT_CPR_SIZE);
        double longitude = Bits.extractUInt(rawMessage.payload(), LON_CPR_START, LON_CPR_SIZE);

        double altitude;

        if (Bits.testBit(inputAltitude, Q_INDEX)) {
            int altitudeValue = (Bits.extractUInt(rawMessage.payload(), ALT_START + 5, ALT_SIZE - 5) << 4) |
                                 Bits.extractUInt(rawMessage.payload(), ALT_START, ALT_SIZE - 8);
            altitude = altitudeValue * 25 - BASE_ALTITUDE_Q_1;
        } else {

            int d = ((inputAltitude & 0x1) << 9) | ((inputAltitude & 0x4) << 8) | ((inputAltitude & 0x10) << 7);
            int a = (inputAltitude & 0x40) | ((inputAltitude & 0x100) >>> 1) | ((inputAltitude & 0x400) >>> 2);
            int b = ((inputAltitude & 0x2) << 2) | ((inputAltitude & 0x8) << 1) | (inputAltitude & 0x20);
            int multipleOf100Feet = ((inputAltitude & 0x80) >>> 7) | ((inputAltitude & 0x200) >>> 8) | ((inputAltitude & 0x800) >>> 9);

            int multipleOf500Feet = (d | a | b) >>> 3;

            multipleOf100Feet = grayToBinary(multipleOf100Feet);
            multipleOf500Feet = grayToBinary(multipleOf500Feet);

            if (isLSBNotValid(multipleOf100Feet)) {
                return null;
            }

            if (multipleOf100Feet == 7) {
                multipleOf100Feet = 5;
            }

            if (multipleOf500Feet % 2 == 1) {
                multipleOf100Feet = (6 - multipleOf100Feet);
            }

            altitude = (multipleOf500Feet * 500) + (multipleOf100Feet * 100) - BASE_ALTITUDE_Q_0;
        }
        return new AirbornePositionMessage(rawMessage.timeStampNs(),
                                           rawMessage.icaoAddress(),
                                           Units.convertFrom(altitude, Units.Length.FOOT),
                                           parity,
                                           Math.scalb(longitude, -17),
                                           Math.scalb(latitude, -17));
    }

    /**
     * Méthode qui convertit un code de Gray en binaire.
     *
     * @param grayCode le code de Gray à convertir
     * @return l'équivalent du code de Gray en binaire.
     */
    private static int grayToBinary(int grayCode) {
        int binary = grayCode;
        int mask;
        for (mask = binary >> 1; mask != 0; mask = mask >> 1) {
            binary = binary ^ mask;
        }
        return binary;
    }

    /**
     * Méthode qui vérifie si le groupe des bits faibles est valide (égal
     * à 0, 5 ou 6) lorsque le bit d'index Q est égal à 0.
     *
     * @param LSB Le groupe des bits faibles en question.
     * @return Vrai s'il est valide, sinon faux.
     */
    private static boolean isLSBNotValid(int LSB) {
        return (LSB == 0) || (LSB == 5) || (LSB == 6);
    }
}