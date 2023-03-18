package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

public record AircraftIdentificationMessage (long timeStampNs, IcaoAddress icaoAddress, int category, CallSign callSign) implements Message {

    /* Constante ASCII pour le CallSign*/
    private static final int ASCII_LETTER = 64;

    public AircraftIdentificationMessage {
        Objects.requireNonNull(icaoAddress);
        Objects.requireNonNull(callSign);
        Preconditions.checkArgument(timeStampNs >= 0);
    }

    public static AircraftIdentificationMessage of(RawMessage rawMessage) {

        byte category = (byte) (((RawMessage.LENGTH - rawMessage.typeCode()) << 4) | Bits.extractUInt(rawMessage.payload(), 48, 3));

        StringBuilder callSignID = new StringBuilder();

        for (int i = 42; i >= 0; i -= 6) {

            int callSignExtractedInt = Bits.extractUInt(rawMessage.payload(), i, 6);

            if(callSignExtractedInt >= 1 && callSignExtractedInt <= 26) {
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