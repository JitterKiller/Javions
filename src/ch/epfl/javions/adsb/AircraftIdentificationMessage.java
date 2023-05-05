package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

/**
 * L'enregistrement AircraftIdentificationMessage représente un message ADS-B d'identification et de catégorie.
 *
 * @param timeStampNs L'horodatage du message, en nanosecondes.
 * @param icaoAddress L'adresse ICAO de l'expéditeur du message.
 * @param category    La catégorie d'aéronef de l'expéditeur.
 * @param callSign    L'indicatif de l'expéditeur.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */
public record AircraftIdentificationMessage(long timeStampNs, IcaoAddress icaoAddress,
                                            int category, CallSign callSign) implements Message {

    private static final int LETTER_START = 1, LETTER_END = 26;
    private static final int NUMBER_START = 48, NUMBER_END = 57;
    private static final int ESCAPE_NUMBER = 32;
    private static final int ASCII_LETTER_OFFSET = 64;
    private static final int CALL_SIGN_CHAR_SIZE = 6;
    private static final int CA_START = 48, CA_SIZE = 3;

    /**
     * Constructeur compact de AircraftIdentificationMessage
     *
     * @throws NullPointerException     Si l'adresse ICAO est nulle.
     * @throws NullPointerException     Si l'indicatif callSign est null.
     * @throws IllegalArgumentException Si l'horodatage du message est strictement inférieur à 0.
     */
    public AircraftIdentificationMessage {
        Objects.requireNonNull(icaoAddress);
        Objects.requireNonNull(callSign);
        Preconditions.checkArgument(timeStampNs >= 0);
    }

    /**
     * Méthode statique qui retourne un message d'identification.
     *
     * @param rawMessage le message brut donné.
     * @return le message d'identification correspondant au message brut donné,
     * ou null si au moins un des caractères de l'indicatif qu'il contient est invalide.
     */
    public static AircraftIdentificationMessage of(RawMessage rawMessage) {

        int leftCategoryBits = (RawMessage.LENGTH - rawMessage.typeCode()) << 4;
        int rightCategoryBits = Bits.extractUInt(rawMessage.payload(), CA_START, CA_SIZE);
        int category = leftCategoryBits | rightCategoryBits;

        StringBuilder callSignID = new StringBuilder();

        for (int i = 42; i >= 0; i -= CALL_SIGN_CHAR_SIZE) {
            int callSignInt = Bits.extractUInt(rawMessage.payload(), i, CALL_SIGN_CHAR_SIZE);

            if (LETTER_START <= callSignInt && callSignInt <= LETTER_END) {
                callSignID.append((char) (callSignInt + ASCII_LETTER_OFFSET));
            } else if (NUMBER_START <= callSignInt && callSignInt <= NUMBER_END || callSignInt == ESCAPE_NUMBER) {
                callSignID.append((char) callSignInt);
            } else return null;
        }

        CallSign callSign = new CallSign(callSignID.toString().stripTrailing());

        return new AircraftIdentificationMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(),
                category, callSign);
    }
}