package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

public record AirborneVelocityMessage(long timeStampNs,
                                      IcaoAddress icaoAddress,
                                      double speed,
                                      double trackOrHeading) implements Message {

    private final static int MESSAGE_START = 21;
    private final static int ST_START = 48;
    private final static int ST_SIZE = 3;
    private final static int V_SIZE = 10;
    private final static int D_SIZE = 1;
    private final static int VNS_START = MESSAGE_START;
    private final static int VEW_START = MESSAGE_START + V_SIZE + D_SIZE;
    private final static int DNS_START = VNS_START + V_SIZE;
    private final static int DEW_START = VEW_START + V_SIZE;
    private final static int SH_SIZE = 1;
    private final static int SH_START = MESSAGE_START + 21;
    private final static int HDG_START = MESSAGE_START + 11;
    private final static int HDG_SIZE = 10;
    private final static int AS_START = MESSAGE_START;
    private final static int AS_SIZE = 10;

    public AirborneVelocityMessage {
        Objects.requireNonNull(icaoAddress);
        Preconditions.checkArgument(timeStampNs >= 0 && speed >= 0 && trackOrHeading >= 0);
    }

    public static AirborneVelocityMessage of(RawMessage rawMessage) {
        int subType = Bits.extractUInt(rawMessage.payload(), ST_START, ST_SIZE);

        double speed;
        double trackOrHeading = 0;

        if (isSubTypeGroundSpeed(subType)) {

            int directionEastWest = Bits.extractUInt(rawMessage.payload(), DEW_START, D_SIZE);
            int directionNorthSouth = Bits.extractUInt(rawMessage.payload(), DNS_START, D_SIZE);
            int velocityEastWest = Bits.extractUInt(rawMessage.payload(), VEW_START, V_SIZE);
            int velocityNorthSouth = Bits.extractUInt(rawMessage.payload(), VNS_START, V_SIZE);

            if (velocityNorthSouth == 0 || velocityEastWest == 0) {
                return null;
            }

            speed = Math.hypot(velocityEastWest - 1, velocityNorthSouth - 1);
            switch (directionNorthSouth) {
                case 0 -> {
                    switch (directionEastWest) {
                        case 0 -> trackOrHeading = Math.atan2(velocityEastWest - 1, velocityNorthSouth - 1);
                        case 1 -> trackOrHeading = Math.atan2(-(velocityEastWest - 1), velocityNorthSouth - 1);
                    }
                }
                case 1 -> {
                    switch (directionEastWest) {
                        case 0 -> trackOrHeading = Math.atan2(velocityEastWest - 1, -(velocityNorthSouth - 1));
                        case 1 -> trackOrHeading = Math.atan2(-(velocityEastWest - 1), -(velocityNorthSouth - 1));
                    }
                }
            }

            if (subType == 1) {
                speed = Units.convert(speed, Units.Speed.KNOT, Units.Speed.METER_PER_SECOND);
            } else {
                speed = Units.convert(speed, Units.Speed.KNOT * 4, Units.Speed.METER_PER_SECOND);
            }
            return new AirborneVelocityMessage(rawMessage.timeStampNs(),
                                               rawMessage.icaoAddress(),
                                               speed,
                                               refocusTrackOrHeading(trackOrHeading));
        }

        if (isSubTypeAirSpeed(subType)) {

            int statusHeading = Bits.extractUInt(rawMessage.payload(), SH_START, SH_SIZE);

            if (statusHeading != 1) {
                return null;
            }

            int heading = Bits.extractUInt(rawMessage.payload(), HDG_START, HDG_SIZE);
            int airSpeed = Bits.extractUInt(rawMessage.payload(), AS_START, AS_SIZE);

            if(airSpeed == 0) {
                return null;
            }

            trackOrHeading = Units.convertFrom(Math.scalb(heading, -10),Units.Angle.TURN);

            if (subType == 3) {
                speed = Units.convert(airSpeed - 1, Units.Speed.KNOT, Units.Speed.METER_PER_SECOND);
            } else {
                speed = Units.convert(airSpeed - 1, Units.Speed.KNOT * 4, Units.Speed.METER_PER_SECOND);
            }

            return new AirborneVelocityMessage(rawMessage.timeStampNs(),
                                               rawMessage.icaoAddress(),
                                               speed,
                                               refocusTrackOrHeading(trackOrHeading));
        }

        return null;
    }

    private static boolean isSubTypeGroundSpeed(int ST) {
        return ST == 1 || ST == 2;
    }

    private static boolean isSubTypeAirSpeed(int ST) {
        return ST == 3 || ST == 4;
    }

    private static double refocusTrackOrHeading(double trackOrHeading) {
        return trackOrHeading < 0 ? trackOrHeading + (2 * Math.PI) : trackOrHeading;
    }

}