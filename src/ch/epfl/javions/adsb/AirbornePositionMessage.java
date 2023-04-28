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
public record AirbornePositionMessage(long timeStampNs, IcaoAddress icaoAddress, double altitude, int parity,
                                      double x, double y) implements Message {
    private static final int Q_INDEX = 4;
    private static final int CPR_BITS = 17;
    private static final int ALT_START = 36, ALT_SIZE = 12;
    private static final int LEFT_ALT_START = ALT_START + 5, LEFT_ALT_SIZE = ALT_SIZE - 5;
    private static final int RIGHT_ALT_START = ALT_START, RIGHT_ALT_SIZE = ALT_SIZE - 8;
    private static final int BASE_ALTITUDE_Q_1 = 1000, BASE_ALTITUDE_Q_0 = 1300;
    private static final int LON_CPR_START = 0, LON_CPR_SIZE = CPR_BITS;
    private static final int LAT_CPR_START = LON_CPR_START + LON_CPR_SIZE, LAT_CPR_SIZE = CPR_BITS;
    private static final int Q1_MULTIPLIER_25 = 25, Q0_MULTIPLIER_100 = 100, Q0_MULTIPLIER_500 = 500;
    private static final int FORMAT_START = 34, FORMAT_SIZE = 1;
    private static final int NORMALIZED_FACTOR = -17;

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
        long payload = rawMessage.payload();

        int inputALT = Bits.extractUInt(payload, ALT_START, ALT_SIZE);

        int parity = Bits.extractUInt(payload, FORMAT_START, FORMAT_SIZE);
        double latitude = Bits.extractUInt(payload, LAT_CPR_START, LAT_CPR_SIZE);
        double longitude = Bits.extractUInt(payload, LON_CPR_START, LON_CPR_SIZE);

        double altitude;

        /* Selon la valeur du bit d'index Q, on calcule l'altitude différemment. */
        if (Bits.testBit(inputALT, Q_INDEX)) { /* Cas où Q = 1 */

            int leftAltitudeBits = Bits.extractUInt(payload, LEFT_ALT_START, LEFT_ALT_SIZE) << Q_INDEX;
            int rightAltitudeBits = Bits.extractUInt(payload, RIGHT_ALT_START, RIGHT_ALT_SIZE);
            int altitudeValue = leftAltitudeBits | rightAltitudeBits;

            altitude = altitudeValue * Q1_MULTIPLIER_25 - BASE_ALTITUDE_Q_1;

        } else { /* Cas où Q = 0 */

            int m100Feet = permutationOfM100FBits(inputALT);
            int m500Feet = permutationOfM500FBits(inputALT) >>> 3;

            m100Feet = convertGrayToBinary(m100Feet);
            m500Feet = convertGrayToBinary(m500Feet);

            if (areLSBSNotValid(m100Feet)) {
                return null;
            }

            if (m100Feet == 7) m100Feet = 5;
            if (m500Feet % 2 == 1) m100Feet = 6 - m100Feet;

            altitude = (m500Feet * Q0_MULTIPLIER_500) + (m100Feet * Q0_MULTIPLIER_100) - BASE_ALTITUDE_Q_0;
        }
        return new AirbornePositionMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(),
                Units.convertFrom(altitude, Units.Length.FOOT), parity,
                Math.scalb(longitude, NORMALIZED_FACTOR), Math.scalb(latitude, NORMALIZED_FACTOR));
    }

    /**
     * Méthode qui convertit un code de Gray en binaire.
     *
     * @param grayCode le code de Gray à convertir
     * @return l'équivalent du code de Gray en binaire.
     */
    private static int convertGrayToBinary(int grayCode) {
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
    private static boolean areLSBSNotValid(int LSB) {
        return (LSB == 0) || (LSB == 5) || (LSB == 6);
    }

    /**
     * Méthode qui permute l'ordre des bits encodant les multiples de
     * 500 pieds pour pouvoir les interprétés comme un code de Gray.
     * Cette méthode est utilisée lorsque le bit d'index Q de l'altitude encodée vaut 0.
     *
     * @param inputALT L'altitude de base (encodée)
     * @return Les bits encodant les multiples de 500 pieds.
     */
    private static int permutationOfM500FBits(int inputALT) {
        int d = 0, a = 0, b = 0;
        for (int i = 0; i < 3; ++i) {
            d |= (inputALT & 1 << (i * 2)) << (9 - i);
            a |= (inputALT & 64 << (i * 2)) >>> i;
            b |= (inputALT & 2 << (i * 2)) << (2 - i);
        }
        return d | a | b;
    }

    /**
     * Méthode qui permute l'ordre des bits encodant les multiples de
     * 100 pieds pour pouvoir les interprétés comme un code de Gray.
     * Cette méthode est utilisée lorsque le bit d'index Q de l'altitude encodée vaut 0.
     *
     * @param inputALT L'altitude de base (encodée)
     * @return Les bits encodant les multiples de 100 pieds.
     */
    private static int permutationOfM100FBits(int inputALT) {
        int c = 0;
        for (int i = 0; i < 3; ++i) {
            c |= (inputALT & 128 << (i * 2)) >> 7 + i;
        }
        return c;
    }
}