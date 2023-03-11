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

    public RawMessage {
        Preconditions.checkArgument((timeStampNs >= 0) && (bytes.size() == LENGTH));
    }

    public static RawMessage of(long timeStampNs, byte[] bytes) {
        Objects.requireNonNull(bytes);
        Crc24 Crc24 = new Crc24(GENERATOR);
        int crc = Crc24.crc(bytes);
        return crc == 0 ? new RawMessage(timeStampNs, new ByteString(bytes)) : null;
    }

    public static int size(byte byte0) {
        int DF = Byte.toUnsignedInt(byte0) >>> 3;
        return DF == 17 ? LENGTH : 0;
    }

    public static int typeCode(long payload) {
        return Bits.extractUInt(payload,0,5);
    }

    public int downLinkFormat() {
        return bytes().byteAt(0) >>> 3;
    }

    public IcaoAddress icaoAddress() {
        return new IcaoAddress(HexFormat.of().toHexDigits(bytes().bytesInRange(1,4),6));
    }

    public long payload() {
        return bytes().bytesInRange(4,11);
    }

    public int typeCode() {
        return typeCode(payload());
    }
}

