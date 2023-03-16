package ch.epfl.javions.adsb;

import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

public record AirbornePositionMessage(long timeStampNs, IcaoAddress icaoAddress, double altitude, int parity, double x, double y) implements Message {

    public AirbornePositionMessage {
        Objects.requireNonNull(icaoAddress);
        Preconditions.checkArgument(timeStampNs >= 0);
        Preconditions.checkArgument((parity == 0) || (parity == 1));
        Preconditions.checkArgument(((0 <= x) && (x < 1)) && ((0 <= y) && (y < 1)));
    }

    public static AirbornePositionMessage of(RawMessage rawMessage) {
        return new AirbornePositionMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), 0.0, 0, 0, 0); // A Modifier
    }

}
