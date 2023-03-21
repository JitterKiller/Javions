package ch.epfl.javions.adsb;

import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

public record AirborneVelocityMessage(long timeStampNs, IcaoAddress icaoAddress, double speed, double trackOrHeading) implements Message{

    public AirborneVelocityMessage {
        Objects.requireNonNull(icaoAddress);
        Preconditions.checkArgument(timeStampNs >= 0 && trackOrHeading >= 0);
    }

    public static AirborneVelocityMessage of(RawMessage rawMessage) {
        return new AirborneVelocityMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), 1,1);
    }
}
