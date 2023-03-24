package ch.epfl.javions;

/**
 * La classe Crc24 du paquetage principal, publique, finale et immuable,
 * représente un calculateur de CRC de 24 bits.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */
public final class Crc24 {

    /* La taille du Crc de 24 bits, constante */
    private static final int CRC_WIDTH = 24;

    /* Le bit le plus fort du CRC24 */
    private static final int TOP_BIT = CRC_WIDTH - 1;

    /* L'octet le plus fort du CRC24 */
    private static final int TOP_BYTE = CRC_WIDTH - 8;
    /* Générateur utilisé pour calculer le CRC24 des messages ADS-B */
    public static int GENERATOR = 0xFFF409;
    /* La table de 256 entrées correspondant à un générateur */
    private static int[] table;

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
            for (int i = 0; i < 8; ++i) {
                crc = ((crc << 1) | Byte.toUnsignedInt(b) >> (7 - i)) ^ table[Bits.extractUInt(crc, TOP_BIT, 1)];
            }
        }

        /* Seconde boucle traitant les 24 bits ajoutés */
        for (int i = 0; i < 24; ++i) {
            crc = ((crc << 1)) ^ table[Bits.extractUInt(crc, TOP_BIT, 1)];
        }

        return Bits.extractUInt(crc, 0, CRC_WIDTH);
    }

    /**
     * Méthode privée pour construire la table.
     *
     * @param generator le générateur du Crc utilisé pour générer la table.
     */
    private static void buildTable(int generator) {
        int[] table = new int[256];
        for (int i = 0; i < 256; i++) {
            table[i] = crc_bitwise(generator, new byte[]{(byte) i});
        }
        Crc24.table = table;
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
            crc = ((crc << 8) | Byte.toUnsignedInt(b)) ^ table[Bits.extractUInt(crc, TOP_BYTE, 8)];
        }

        /* Seconde boucle traitant les 3 octets ajoutés */
        for (int i = 0; i < 3; ++i) {
            crc = ((crc << 8)) ^ table[Bits.extractUInt(crc, TOP_BYTE, 8)];
        }
        return Bits.extractUInt(crc, 0, CRC_WIDTH);
    }
}
