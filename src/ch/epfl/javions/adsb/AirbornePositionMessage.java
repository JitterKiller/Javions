package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

public record AirbornePositionMessage(long timeStampNs, IcaoAddress icaoAddress, double altitude, int parity, double x, double y) implements Message {

    /* Taille des bits de l'altitude dans l'attribut ME du message brut */
    private final static int ALTITUDE_SIZE = 12;

    /* Index de Q dans les bits de l'altitude du message brut */
    private final static int Q_INDEX = 4;

    /* Altitude de base lorsque Q = 1 */
    private final static int BASE_ALTITUDE_Q_1 = 1000;

    /* Altitude de base lorsque Q = 0 */
    private final static int BASE_ALTITUDE_Q_0 = 1300;

    public AirbornePositionMessage {
        Objects.requireNonNull(icaoAddress);
        Preconditions.checkArgument(timeStampNs >= 0);
        Preconditions.checkArgument(parity == 0 || parity == 1);
        Preconditions.checkArgument(0 <= x && x < 1);
        Preconditions.checkArgument(0 <= y && y < 1);
    }

    public static AirbornePositionMessage of(RawMessage rawMessage) {

        int inputAltitude = Bits.extractUInt(rawMessage.payload(),36,ALTITUDE_SIZE);

        int parity = Bits.extractUInt(rawMessage.payload(),34,1);
        double latitude = Bits.extractUInt(rawMessage.payload(),17,17);
        double longitude = Bits.extractUInt(rawMessage.payload(), 0,17);

        double altitude;

        if(Bits.testBit(inputAltitude,Q_INDEX)) {
            int altitudeValue = (Bits.extractUInt(rawMessage.payload(),41,7) << 4) | Bits.extractUInt(rawMessage.payload(),36,4);
            altitude = altitudeValue * 25 - BASE_ALTITUDE_Q_1;
        } else {

            int d = ((inputAltitude & 0x1) << 9) | ((inputAltitude & 0x4) << 8) | ((inputAltitude & 0x10) << 7);
            int a = (inputAltitude & 0x40) | ((inputAltitude & 0x100) >>> 1) | ((inputAltitude & 0x400) >>> 2);
            int b = ((inputAltitude & 0x2) << 2) | ((inputAltitude & 0x8) << 1) | (inputAltitude & 0x20);
            int multipleOf100Feet = ((inputAltitude & 0x80) >>> 7) | ((inputAltitude & 0x200) >>> 8) | ((inputAltitude & 0x800) >>> 9);

            int multipleOf500Feet = (d | a | b) >>> 3;

            multipleOf100Feet = grayToBinary(multipleOf100Feet);
            multipleOf500Feet = grayToBinary(multipleOf500Feet);

            if(isLSBNotValid(multipleOf100Feet)) {
                return null;
            }

            if(multipleOf100Feet == 7) {
                multipleOf100Feet = 5;
            }

            if(multipleOf500Feet % 2 == 1) {
                multipleOf100Feet = (6 - multipleOf100Feet);
            }

            altitude = (multipleOf500Feet * 500) + (multipleOf100Feet* 100) - BASE_ALTITUDE_Q_0;
        }
        return new AirbornePositionMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), Units.convertFrom(altitude,Units.Length.FOOT), parity, Math.scalb(longitude,-17), Math.scalb(latitude,-17));
    }

    public static int grayToBinary(int grayCode) {
        int binary = grayCode;
        int mask;
        for (mask = binary >> 1; mask != 0; mask = mask >> 1) {
            binary = binary ^ mask;
        }
        return binary;
    }

    private static boolean isLSBNotValid(int LSB) {
        return (LSB == 0) || (LSB == 5) || (LSB == 6);
    }
}