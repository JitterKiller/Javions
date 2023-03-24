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
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */
public record AircraftIdentificationMessage(long timeStampNs, IcaoAddress icaoAddress, int category,
                                            CallSign callSign) implements Message {

    /* Constante ASCII pour le CallSign*/
    private static final int ASCII_LETTER = 64;

    /**
     * Constructeur compact de AircraftIdentificationMessage
     *
     * @throws NullPointerException     Si l'adresse ICAO est nulle.
     * @throws NullPointerException     Si l'indicatif callSign est null.
     * @throws IllegalArgumentException si l'horodatage du message est strictement inférieur à 0.
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

        byte category = (byte) (((RawMessage.LENGTH - rawMessage.typeCode()) << 4) | Bits.extractUInt(rawMessage.payload(), 48, 3));

        StringBuilder callSignID = new StringBuilder();

        for (int i = 42; i >= 0; i -= 6) {

            int callSignExtractedInt = Bits.extractUInt(rawMessage.payload(), i, 6);

            if (callSignExtractedInt >= 1 && callSignExtractedInt <= 26) {
                callSignID.append((char) (callSignExtractedInt + ASCII_LETTER));
            } else if ((callSignExtractedInt >= 48 && callSignExtractedInt <= 57) || callSignExtractedInt == 32) {
                callSignID.append((char) callSignExtractedInt);
            } else {
                return null;
            }
        }

        return new AircraftIdentificationMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), Byte.toUnsignedInt(category), new CallSign(callSignID.toString().stripTrailing()));
    }
}