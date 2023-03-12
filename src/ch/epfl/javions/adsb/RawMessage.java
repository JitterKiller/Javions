package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.ByteString;
import ch.epfl.javions.Crc24;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.HexFormat;
import java.util.Objects;

import static ch.epfl.javions.Crc24.GENERATOR;

public record RawMessage(long timeStampNs, ByteString bytes) {
    public static final int LENGTH = 14;
    private static final Crc24 Crc24 = new Crc24(GENERATOR);

    public RawMessage {
        Preconditions.checkArgument((timeStampNs >= 0) && (bytes.size() == LENGTH));
    }

    public static RawMessage of(long timeStampNs, byte[] bytes) {
        Objects.requireNonNull(bytes);
        return Crc24.crc(bytes) == 0 ? new RawMessage(timeStampNs, new ByteString(bytes)) : null;
    }

    public static int size(byte byte0) {
        return Byte.toUnsignedInt(byte0) >>> 3 == 17 ? LENGTH : 0;
    }

    public static int typeCode(long payload) {
        return Bits.extractUInt(payload,0,5);
    }

    public int downLinkFormat() {
        return bytes().byteAt(0) >>> 3;
    }

    public IcaoAddress icaoAddress() {
        return new IcaoAddress(HexFormat.of().toHexDigits(bytes().bytesInRange(1,4),6).toUpperCase());
    }

    public long payload() {
        return bytes().bytesInRange(4,11);
    }

    public int typeCode() {
        return typeCode(payload());
    }
}

