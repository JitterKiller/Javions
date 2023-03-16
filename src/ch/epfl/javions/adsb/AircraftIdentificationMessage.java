package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

public record AircraftIdentificationMessage (long timeStampNs, IcaoAddress icaoAddress, int category, CallSign callSign) implements Message {

    public AircraftIdentificationMessage {
        Objects.requireNonNull(icaoAddress);
        Objects.requireNonNull(callSign);
        Preconditions.checkArgument(timeStampNs >= 0);
    }

    public static AircraftIdentificationMessage of(RawMessage rawMessage) {

        int aircraftCategory = rawMessage.typeCode() | Bits.extractUInt(rawMessage.payload(), 48, 3);

        StringBuilder callSignIdentificator = new StringBuilder();

        for (int i = 0; i <= 42; i += 6) {

            int charactersExtractedInt = Bits.extractUInt(rawMessage.payload(), i, 6);

            if (!Character.isDefined(charactersExtractedInt)) {return null;}

            callSignIdentificator.append((char) charactersExtractedInt);
        }

        return new AircraftIdentificationMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), aircraftCategory, new CallSign(callSignIdentificator.toString()));
    }
}