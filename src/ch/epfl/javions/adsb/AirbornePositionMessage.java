package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

public record AirbornePositionMessage(long timeStampNs, IcaoAddress icaoAddress, double altitude, int parity, double x, double y) implements Message {

    /* Index de Q dans l'attribut ME du message brut */
    private final static int Q_INDEX = 41;

    /* Taille des bits de l'altitude dans l'attribut ME du message brut */
    private final static int ALTITUDE_SIZE = 12;

    /* Altitude de base lorsque Q = 0 */
    private final static int BASE_ALTITUDE_Q_0 = 1000;

    /* Altitude de base lorsque Q = 1 */
    private final static int BASE_ALTITUDE_Q_1 = 1300;

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

        if(Bits.testBit(rawMessage.payload(),Q_INDEX)) {
            byte mask = (byte) ~(1 << 4);
            byte altitudeValue = (byte) (inputAltitude & mask);
            altitude = (Byte.toUnsignedInt(altitudeValue) * Units.Length.FOOT * 25) - BASE_ALTITUDE_Q_0;
        } else {

            int d = ((inputAltitude & 0b00000001) << 7) | ((inputAltitude & 0b00000010) << 5) | ((inputAltitude & 0b00000100) << 3);
            int a = ((inputAltitude & 0b00001000) >> 3) | ((inputAltitude & 0b00010000) >> 1) | ((inputAltitude & 0b00100000) >> 3);
            int b = ((inputAltitude & 0b01000000) >> 1) | ((inputAltitude & 0b10000000) >> 3) | ((inputAltitude & 0b000001000000) << 3);
            int multipleOf100Feet = ((inputAltitude & 0b000010000000) << 1) | ((inputAltitude & 0b000100000000) >> 1) | ((inputAltitude & 0b001000000000) << 1);

            int multipleOf500Feet = d | a | b;

            decodeGrayCodeLSB(multipleOf100Feet);
            decodeGrayCodeMSB(multipleOf500Feet);

            if(!isLSBValid(multipleOf100Feet)) {
                return null;
            }

            if(multipleOf100Feet == 7) {
                multipleOf100Feet = 5;
            }

            if(multipleOf500Feet % 2 == 1) {
                multipleOf100Feet = (6 - multipleOf100Feet);
            }

            altitude = (multipleOf500Feet * Units.Length.FOOT * 500) + (multipleOf100Feet * Units.Length.FOOT * 100) - BASE_ALTITUDE_Q_1;
        }
        return new AirbornePositionMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), altitude, parity, Math.scalb(longitude,-17), Math.scalb(latitude,-17));
    }

    private static void decodeGrayCodeLSB(int encodedByte) {
        for(int i = 0; i < 3; ++i) {
            encodedByte ^= encodedByte >> i;
        }
    }

    private static void decodeGrayCodeMSB(int encodedByte) {
        for(int i = 0; i < 9; ++i) {
            encodedByte ^= encodedByte >> i;
        }
    }

    private static boolean isLSBValid(int LSB) {
        return (LSB == 0) || (LSB == 5) || (LSB == 6);
    }
}