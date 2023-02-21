package ch.epfl.javions;

public class Bits {

    private Bits() {}

    public static int extractUInt(long value, int start, int size) {
        if (size <= 0 || size >= 32) {
            throw new IllegalArgumentException("size have to be strictly greater than 0 and strictly lower than 32");
        }
        if (start < 0 || start + size > 64) {
            throw new IndexOutOfBoundsException("start and start + size must be between 0 (inclusive) and 64 (exclusive)");
        }

        long mask = (1L << size) - 1;
        return (int) ((value >>> start) & mask);

    }

    public static boolean testBit(long value, int index) {

        if (index < 0 || index >= 64) {
            throw new IndexOutOfBoundsException("L'index doit être compris entre 0 (inclus) et 64 (exclu)");
        }

        long mask = 1L << index;
        return (value & mask) != 0;
    }
}

