package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.ByteString;
import ch.epfl.javions.Crc24;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.HexFormat;
import java.util.Objects;

import static ch.epfl.javions.Crc24.GENERATOR;

/**
 * L'enregistrement RawMessage représente un message ADS-B brut.
 *
 * @param timeStampNs Le temps d'arrivée (en nanosecondes) du message.
 * @param bytes       Les octets du message.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */
public record RawMessage(long timeStampNs, ByteString bytes) {
    private static final Crc24 Crc24 = new Crc24(GENERATOR);
    private static final int VALID_DF = 17;
    private static final int ME_START = 4, ME_END = 10 + 1;
    private static final int ICAO_START = 1, ICAO_END = 3 + 1, ICAO_SIZE = 6;
    private static final int TYPE_CODE_START = 51, TYPE_CODE_SIZE = 5;

    /** La longueur en octets des messages ADS-B */
    public static final int LENGTH = 14;

    /**
     * Constructeur compact de l'enregistrement RawMessage.
     *
     * @throws IllegalArgumentException si le temps d'arrivée est négatif ou
     *                                  si la longueur du message n'est pas égale à LENGTH (14).
     */
    public RawMessage {
        Preconditions.checkArgument((timeStampNs >= 0) && (bytes.size() == LENGTH));
    }

    /**
     * Crée un objet RawMessage à partir d'un temps d'arrivée et des octets du message.
     *
     * @param timeStampNs Le temps d'arrivée (en nanosecondes) du message.
     * @param bytes       Les octets du message.
     * @return Un RawMessage si le CRC du message est valide (égal à 0), null sinon.
     * @throws NullPointerException si le tableau d'octets est null.
     */
    public static RawMessage of(long timeStampNs, byte[] bytes) {
        Objects.requireNonNull(bytes);
        return Crc24.crc(bytes) == 0 ? new RawMessage(timeStampNs, new ByteString(bytes)) : null;
    }

    /**
     * Retourne la longueur du message en fonction du premier octet.
     *
     * @param byte0 Le premier octet du message.
     * @return La longueur du message (14) si le downlink format (DF) du message est 17, 0 sinon.
     */
    public static int size(byte byte0) {
        return Byte.toUnsignedInt(byte0) >>> 3 == VALID_DF ? LENGTH : 0;
    }

    /**
     * Retourne le code de type (typecode) du message.
     *
     * @param payload La charge utile du message.
     * @return le code de type (typecode) du message.
     */
    public static int typeCode(long payload) {
        return Bits.extractUInt(payload, TYPE_CODE_START, TYPE_CODE_SIZE);
    }

    /**
     * Retourne le downlink format (DF) du message.
     *
     * @return le downlink format (DF) du message.
     */
    public int downLinkFormat() {
        return bytes().byteAt(0) >>> 3;
    }

    /**
     * Retourne l'adresse ICAO du message.
     *
     * @return l'adresse ICAO du message.
     */
    public IcaoAddress icaoAddress() {
        return new IcaoAddress(HexFormat.of().withUpperCase().toHexDigits(bytes().bytesInRange(ICAO_START, ICAO_END), ICAO_SIZE));
    }

    /**
     * Retourne la charge utile du message (ME).
     *
     * @return la charge utile du message (ME).
     */
    public long payload() {
        return bytes().bytesInRange(ME_START, ME_END);
    }

    /**
     * Retourne le code de type (typecode) du message.
     *
     * @return le code de type (un entier) extrait de la charge utile (payload) du message.
     */
    public int typeCode() {
        return typeCode(payload());
    }
}

