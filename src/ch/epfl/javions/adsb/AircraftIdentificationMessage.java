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

    private static final int ASCII_LETTER_OFFSET = 64;
    private static final int CALL_SIGN_SIZE = 6;

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

        int category = (RawMessage.LENGTH - rawMessage.typeCode()) << 4
                | Bits.extractUInt(rawMessage.payload(), 48, 3);

        StringBuilder callSignID = new StringBuilder();

        for (int i = 42; i >= 0; i -= 6) {

            int callSignExtractedInt = Bits.extractUInt(rawMessage.payload(), i, CALL_SIGN_SIZE);

            switch (callSignExtractedInt) {

                case 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26
                        -> callSignID.append((char) (callSignExtractedInt + ASCII_LETTER_OFFSET));

                case 32, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57
                        -> callSignID.append((char) callSignExtractedInt);

                default -> {
                    return null;
                }
            }
        }

        CallSign callSign = new CallSign(callSignID.toString().stripTrailing());

        return new AircraftIdentificationMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), category, callSign);
    }
}