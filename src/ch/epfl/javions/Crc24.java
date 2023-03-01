package ch.epfl.javions;

public final class Crc24 {

    private static final int CRCWIDTH = 24;
    private static final int TOPBIT = CRCWIDTH - 1;

    private static int[] table;

    public static int GENERATOR = 0xFFF409;

    public Crc24(int generator) {
        buildTable(generator);
    }

    public int crc(byte[] bytes) {
        int crc = 0;
        for (byte b : bytes) {
            crc = (crc << 8) ^ table[((crc >> 16) ^ (b & 0xFF)) & 0xFF];
        }
        return Bits.extractUInt(crc,0,CRCWIDTH);
    }

    private static int crc_bitwise(int generator, byte[] bytes) {
        int crc = 0;
        for (byte b : bytes) {
            crc ^= (b & 0xFF) << (CRCWIDTH - 8);
            for (int i = 0; i < 8; i++) {
                if (Bits.testBit(crc,TOPBIT)) {
                    crc = (crc << 1) ^ generator;
                } else {
                    crc <<= 1;
                }
            }
        }
        return Bits.extractUInt(crc,0,CRCWIDTH);
    }

    private static void buildTable(int generator) {
        int[] table = new int[256];
        for (int i = 0; i < 256; i++) {
            table[i] = crc_bitwise(generator, new byte[] { (byte) i });
        }
        Crc24.table = table;
    }

}
