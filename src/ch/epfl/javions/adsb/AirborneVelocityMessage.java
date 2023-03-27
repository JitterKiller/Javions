package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

public record AirborneVelocityMessage(long timeStampNs, IcaoAddress icaoAddress, double speed,
                                      double trackOrHeading) implements Message {

    private final static int MESSAGE_START = 21;

    public AirborneVelocityMessage {
        Objects.requireNonNull(icaoAddress);
        Preconditions.checkArgument(timeStampNs >= 0 && speed >= 0 && trackOrHeading >= 0);
    }

    public static AirborneVelocityMessage of(RawMessage rawMessage) {
        int ST = Bits.extractUInt(rawMessage.payload(), 48, 3);

        double speed;
        double trackOrHeading = 0;

        if (isSTGroundSpeed(ST)) {

            int Dew = Bits.extractUInt(rawMessage.payload(), 21 + MESSAGE_START, 1);
            int Dns = Bits.extractUInt(rawMessage.payload(), 10 + MESSAGE_START, 1);
            int Vew = Bits.extractUInt(rawMessage.payload(), 11 + MESSAGE_START, 10);
            int Vns = Bits.extractUInt(rawMessage.payload(), MESSAGE_START, 10);

            if (Vns == 0 || Vew == 0) {
                return null;
            }

            speed = Math.hypot(Vew - 1, Vns - 1);
            switch (Dns) {
                case 0 -> {
                    switch (Dew) {
                        case 0 -> trackOrHeading = Math.atan2(Vew - 1, Vns - 1);
                        case 1 -> trackOrHeading = Math.atan2(-(Vew - 1), Vns - 1);
                    }
                }
                case 1 -> {
                    switch (Dew) {
                        case 0 -> trackOrHeading = Math.atan2(Vew - 1, -(Vns - 1));
                        case 1 -> trackOrHeading = Math.atan2(-(Vew - 1), -(Vns - 1));
                    }
                }
            }

            if (ST == 1) {
                speed = Units.convert(speed, Units.Speed.KNOT, Units.Speed.METER_PER_SECOND);
            } else {
                speed = Units.convert(speed, Units.Speed.KNOT * 4, Units.Speed.METER_PER_SECOND);
            }
            return new AirborneVelocityMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), speed, refocusTrackOrHeading(trackOrHeading));
        }

        if (isSTAirSpeed(ST)) {

            int SH = Bits.extractUInt(rawMessage.payload(), 21 + MESSAGE_START, 1);

            if (SH != 1) {
                return null;
            }

            int HDG = Bits.extractUInt(rawMessage.payload(), 11 + MESSAGE_START, 10);
            int AS = Bits.extractUInt(rawMessage.payload(), MESSAGE_START, 10);

            if(AS == 0) {
                return null;
            }

            trackOrHeading = Units.convertFrom(Math.scalb(HDG, -10),Units.Angle.TURN);

            if (ST == 3) {
                speed = Units.convert(AS - 1, Units.Speed.KNOT, Units.Speed.METER_PER_SECOND);
            } else {
                speed = Units.convert(AS - 1, Units.Speed.KNOT * 4, Units.Speed.METER_PER_SECOND);
            }

            return new AirborneVelocityMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), speed, refocusTrackOrHeading(trackOrHeading));
        }

        return null;
    }

    private static boolean isSTGroundSpeed(int ST) {
        return ST == 1 || ST == 2;
    }

    private static boolean isSTAirSpeed(int ST) {
        return ST == 3 || ST == 4;
    }

    private static double refocusTrackOrHeading(double trackOrHeading) {
        return trackOrHeading < 0 ? trackOrHeading + (2 * Math.PI) : trackOrHeading;
    }

}