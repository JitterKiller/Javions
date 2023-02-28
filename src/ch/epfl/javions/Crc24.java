package ch.epfl.javions;

public final class Crc24 {
    public static final int GENERATOR = 0xFFF40916;

    private final int[] table = new int[256];
    private final int polynomial;

    public Crc24(int generator) {
        this.polynomial = generator & 0xFFFFFF;
        generateTable();
    }

    public int crc(byte[] bytes) {
        int crc = 0;
        for (byte b : bytes) {
            crc = ((crc << 8) | (b & 0xFF)) ^ table[(crc >> 16) & 0xFF];
        }
        return crc & 0xFFFFFF;
    }

    private void generateTable() {
        for (int i = 0; i < table.length; i++) {
            int crc = i << 16;
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x800000) != 0) {
                    crc = (crc << 1) ^ polynomial;
                } else {
                    crc <<= 1;
                }
            }
            table[i] = crc & 0xFFFFFF;
        }
    }
}
