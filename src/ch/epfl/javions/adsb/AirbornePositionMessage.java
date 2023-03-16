package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

public record AirbornePositionMessage(long timeStampNs, IcaoAddress icaoAddress, double altitude, int parity, double x, double y) implements Message {

    static double xNormalized;
    static double yNormalized;
    public AirbornePositionMessage {
        Objects.requireNonNull(icaoAddress);
        xNormalized = this.x() / Math.scalb(1,17);
        yNormalized = this.y() / Math.scalb(1,17);
        Preconditions.checkArgument(timeStampNs >= 0 && (parity == 0 || parity == 1) && (xNormalized >= 0 && xNormalized < 1) && (yNormalized >= 0 && yNormalized < 1));
    }

    public static AirbornePositionMessage of(RawMessage rawMessage) {

        int messageParity = Bits.extractUInt(rawMessage.payload(), 34, 1);

        return new AirbornePositionMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(),0, messageParity, xNormalized, yNormalized);
    }
}