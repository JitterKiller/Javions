package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

public record AirborneVelocityMessage(long timeStampNs, IcaoAddress icaoAddress, double speed, double trackOrHeading) implements Message{

    public AirborneVelocityMessage {
        Objects.requireNonNull(icaoAddress);
        Preconditions.checkArgument(timeStampNs >= 0 && speed >= 0 && trackOrHeading >= 0);
    }


    public static AirborneVelocityMessage of(RawMessage rawMessage) {

        int subType = Bits.extractUInt(rawMessage.payload(), 48, 3);
        if (!(subType >= 1 && subType <= 4)) {
            return null;
        }

        double angle;
        double velocity;


        if (subType == 1 || subType == 2){

            int Dew = Bits.extractUInt(rawMessage.payload(), 21, 1);
            int Vew = Bits.extractUInt(rawMessage.payload(), 11, 10);
            int Dns = Bits.extractUInt(rawMessage.payload(), 10, 1);
            int Vns = Bits.extractUInt(rawMessage.payload(), 0, 10);

            if (Vns == 0 || Vew == 0) {
                return null;
            }

            velocity = Math.hypot(Vew - 1, Vns - 1);
            double defaultAngle = Math.atan2(Vns,Vew);

            if (Dew == 0) {
                if (Dns == 0) {
                    angle = Math.PI/2.0 - defaultAngle;
                } else {
                    angle = Math.PI/2.0 + defaultAngle;
                }
            } else {
                if (Dns == 0) {
                    angle = Math.PI * 1.5 + defaultAngle;
                } else {
                    angle = Math.PI * 1.5 - defaultAngle;
                }
            }

        } else {

            int SH = Bits.extractUInt(rawMessage.payload(), 21, 1);
            if (SH == 1) {
                byte HDG = (byte) Bits.extractUInt(rawMessage.payload(), 11, 10);
                angle = Units.convertFrom(Math.scalb(Byte.toUnsignedInt(HDG),- 10), Units.Angle.TURN);
                velocity = Bits.extractUInt(rawMessage.payload(), 0, 10);
            } else {return null;}
        }

        if (subType == 1 || subType == 3) {
            angle = Units.convert(angle, Units.Speed.KNOT, (Units.Time.HOUR/Units.KILO) * Units.Speed.KILOMETER_PER_HOUR);
        } else {
            angle = Units.convert(angle, 4 * Units.Speed.KNOT, (Units.Time.HOUR/Units.KILO) * Units.Speed.KILOMETER_PER_HOUR);
        }

        return new AirborneVelocityMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), velocity, angle);
    }
}