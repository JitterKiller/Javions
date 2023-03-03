package ch.epfl.javions;

/**
 *La classe Crc24 du paquetage principal, publique, finale et immuable,
 * représente un calculateur de CRC de 24 bits.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah Janati Idrissi (362341)
 */
public final class Crc24 {

    /* La taille du Crc de 24 bits, constante*/
    private static final int CRCWIDTH = 24;

    /* Le bit le plus fort du CRC24*/
    private static final int TOPBIT = CRCWIDTH - 1;

    /* La table de 256 entrées correspondant à un générateur*/
    private static int[] table;

    /* Générateur utilisé pour calculer le CRC24 des messages ADS-B*/
    public static int GENERATOR = 0xFFF409;

    /**
     * Constructeur public de la classe Crc24 (appelle la méthode buildTable() pour
     * initialiser la table de 256 entrées
     * @param generator le générateur du Crc utilisé pour générer la table.
     */
    public Crc24(int generator) {
        buildTable(generator);
    }

    /**
     * Méthode principale de la classe Crc24 travaillant octet par octet.
     * @param bytes message de bytes ADS-B.
     * @return le CRC24 du tableau de bits donné.
     */
    public int crc(byte[] bytes) {
        int crc = 0;
        for (byte b : bytes) {
            crc = (crc << 8) ^ table[((crc >> 16) ^ Byte.toUnsignedInt(b)) & 0xFF];
        }
        return Bits.extractUInt(crc,0,CRCWIDTH);
    }

    /**
     * Algorithme travaillant bit par bit utilisé pour générer la table.
     * @param generator le générateur du Crc utilisé pour générer la table.
     * @param bytes message de bytes ADS-B.
     * @return le CRC24 du tableau de bits donné.
     */
    private static int crc_bitwise(int generator, byte[] bytes) {
        int crc = 0;
        int[] table = {0, generator};
        for (byte b : bytes) {
            crc |= Byte.toUnsignedInt(b) << (CRCWIDTH - 8);
            for (int i = 0; i < 8; i++) {
                crc = (crc << 1) ^ table[(crc >>> (TOPBIT)) & 1];
            }
        }
        return Bits.extractUInt(crc,0,CRCWIDTH);
    }

    /**
     * Méthode privée pour construire la table.
     * @param generator le générateur du Crc utilisé pour générer la table.
     */
    private static void buildTable(int generator) {
        int[] table = new int[256];
        for (int i = 0; i < 256; i++) {
            table[i] = crc_bitwise(generator, new byte[] { (byte) i });
        }
        Crc24.table = table;
    }

}
