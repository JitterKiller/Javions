package ch.epfl.javions;

/**
 * La classe Crc24 du paquetage principal, publique, finale et immuable,
 * représente un calculateur de CRC de 24 bits.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */
public final class Crc24 {
    private static final int CRC_SIZE = 24, CRC_START = 0;
    private static final int TOP_BIT = CRC_SIZE - Byte.BYTES;
    private static final int TOP_BYTE = CRC_SIZE - Byte.SIZE;
    private static final int TABLE_SIZE = 256;
    private static final int[] TABLE = new int[TABLE_SIZE];

    /** Générateur utilisé pour calculer le CRC24 des messages ADS-B */
    public static final int GENERATOR = 0xFFF409;

    /**
     * Constructeur public de la classe Crc24 (appelle la méthode buildTable() pour
     * initialiser la table de 256 entrées.
     *
     * @param generator le générateur du Crc utilisé pour générer la table.
     */
    public Crc24(int generator) {
        /* Construction de la table de CRC */
        buildTable(generator);
    }

    /**
     * Algorithme travaillant bit par bit utilisé pour générer la table.
     *
     * @param generator le générateur du Crc utilisé pour générer la table.
     * @param bytes     message de bytes ADS-B.
     * @return le CRC24 du tableau de bits donné.
     */
    private static int crc_bitwise(int generator, byte[] bytes) {
        int crc = 0;
        int[] table = {0, generator};

        /* Première boucle traitant les bits du message */
        for (byte b : bytes) {
            for (int i = CRC_START; i < Byte.SIZE; ++i) {
                crc = ((crc << 1) | Byte.toUnsignedInt(b) >> (7 - i)) ^ table[Bits.extractUInt(crc, TOP_BIT, Byte.BYTES)];
            }
        }

        /* Seconde boucle traitant les 24 bits ajoutés */
        for (int i = CRC_START; i < CRC_SIZE; ++i) {
            crc = ((crc << 1)) ^ table[Bits.extractUInt(crc, TOP_BIT, Byte.BYTES)];
        }

        return Bits.extractUInt(crc, CRC_START, CRC_SIZE);
    }

    /**
     * Méthode privée pour construire la table.
     *
     * @param generator le générateur du Crc utilisé pour générer la table.
     */
    private static void buildTable(int generator) {
        for (int i = 0; i < TABLE_SIZE; i++) {
            TABLE[i] = crc_bitwise(generator, new byte[]{(byte) i});
        }
    }

    /**
     * Méthode principale de la classe Crc24 travaillant octet par octet.
     *
     * @param bytes message de bytes ADS-B.
     * @return le CRC24 du tableau de bits donné.
     */
    public int crc(byte[] bytes) {
        int crc = 0;

        /* Première boucle traitant les octets du message */
        for (byte b : bytes) {
            crc = ((crc << Byte.SIZE) | Byte.toUnsignedInt(b)) ^ TABLE[Bits.extractUInt(crc, TOP_BYTE, Byte.SIZE)];
        }

        /* Seconde boucle traitant les 3 octets ajoutés */
        for (int i = CRC_START; i < 3; ++i) {
            crc = ((crc << Byte.SIZE)) ^ TABLE[Bits.extractUInt(crc, TOP_BYTE, Byte.SIZE)];
        }
        return Bits.extractUInt(crc, CRC_START, CRC_SIZE);
    }
}
