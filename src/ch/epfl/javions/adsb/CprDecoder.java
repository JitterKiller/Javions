package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;

public class CprDecoder {

    private final static int EVEN_LATITUDE_ZONES = 60;
    private final static double EVEN_LATITUDE_ZONES_WIDTH = 1.0 / EVEN_LATITUDE_ZONES;
    private final static int ODD_LATITUDE_ZONES = 59;
    private final static double ODD_LATITUDE_ZONES_WIDTH = 1.0 / ODD_LATITUDE_ZONES;

    private CprDecoder(){}

    public static GeoPos decodePosition(double x0, double y0, double x1, double y1, int mostRecent) {
        Preconditions.checkArgument(mostRecent == 0 || mostRecent == 1);

        double evenLatitudeDegree;
        double oddLatitudeDegree;
        double evenLongitudeDegree = Units.convert(refocusingOf(x0), Units.Angle.TURN, Units.Angle.DEGREE);
        double oddLongitudeDegree = Units.convert(refocusingOf(x1), Units.Angle.TURN, Units.Angle.DEGREE);

        int latitudeZoneNumber = (int) Math.rint((y0 * ODD_LATITUDE_ZONES) - (y1 * EVEN_LATITUDE_ZONES));
        int evenLatitudeZoneNumber;
        int oddLatitudeZoneNumber;
        if(latitudeZoneNumber < 0) {
            evenLatitudeZoneNumber = latitudeZoneNumber + EVEN_LATITUDE_ZONES;
            oddLatitudeZoneNumber = latitudeZoneNumber + ODD_LATITUDE_ZONES;
        } else {
            evenLatitudeZoneNumber = latitudeZoneNumber;
            oddLatitudeZoneNumber = latitudeZoneNumber;
        }
        double evenLatitudeTurn = EVEN_LATITUDE_ZONES_WIDTH * (evenLatitudeZoneNumber + y0);
        evenLatitudeTurn = refocusingOf(evenLatitudeTurn);
        evenLatitudeDegree = Units.convert(evenLatitudeTurn, Units.Angle.TURN, Units.Angle.DEGREE);

        double oddLatitudeTurn = ODD_LATITUDE_ZONES_WIDTH * (oddLatitudeZoneNumber + y1);
        oddLatitudeTurn = refocusingOf(oddLatitudeTurn);
        oddLatitudeDegree = Units.convert(oddLatitudeTurn, Units.Angle.TURN, Units.Angle.DEGREE);

        double A;
        double AEven = (1 - Math.cos(2* Math.PI * EVEN_LATITUDE_ZONES_WIDTH)) / Math.pow(Math.cos(Units.convertFrom(evenLatitudeDegree, Units.Angle.RADIAN)), 2);
        double AOdd = (1 - Math.cos(2* Math.PI * EVEN_LATITUDE_ZONES_WIDTH)) / Math.pow(Math.cos(Units.convertFrom(oddLatitudeDegree, Units.Angle.RADIAN)), 2);

        double evenLongitudeZoneValue = Math.floor((2 * Math.PI) / Math.acos(1 - AEven));
        double oddLongitudeZoneValue = Math.floor((2 * Math.PI) / Math.acos(1 - AOdd));
        int evenLongitudeZone;

        if(evenLongitudeZoneValue == oddLongitudeZoneValue) {
            A = AEven;

            if(Double.isNaN(Math.acos(1 - A))) {
                evenLongitudeZone = 1;
            } else {
                evenLongitudeZone = (int) evenLongitudeZoneValue;
            }
        } else {
            return null;
        }

        int oddLongitudeZone = evenLongitudeZone - 1;

        int evenLongitudeZoneNumber;
        int oddLongitudeZoneNumber;

        double evenLongitudeTurn;
        double oddLongitudeTurn;

        if(evenLongitudeZone > 1) {

            int longitudeZoneNumber = (int) Math.rint((x0 * oddLongitudeZone) - (x1 * evenLongitudeZone));

            if(longitudeZoneNumber < 0) {
                evenLongitudeZoneNumber = longitudeZoneNumber + evenLongitudeZone;
                oddLongitudeZoneNumber = longitudeZoneNumber + oddLongitudeZone;
            } else {
                evenLongitudeZoneNumber = longitudeZoneNumber;
                oddLongitudeZoneNumber = longitudeZoneNumber;
            }

            evenLongitudeTurn = (1.0 / evenLongitudeZone) * (evenLongitudeZoneNumber + x0);
            evenLongitudeDegree = Units.convert(refocusingOf(evenLongitudeTurn), Units.Angle.TURN, Units.Angle.DEGREE);

            oddLongitudeTurn = (1.0 / oddLongitudeZone) * (oddLongitudeZoneNumber + x1);
            oddLongitudeDegree = Units.convert(refocusingOf(oddLongitudeTurn), Units.Angle.TURN, Units.Angle.DEGREE);

        }
        if(mostRecent == 0) {
            return getGeoPos(evenLongitudeDegree, evenLatitudeDegree);
        } else {
            return getGeoPos(oddLongitudeDegree, oddLatitudeDegree);
        }
    }

    private static GeoPos getGeoPos(double longitudeDegree, double latitudeDegree) {
        int longitudeT32;
        int latitudeT32;
        longitudeT32 = (int) Math.rint(Units.convert(longitudeDegree,Units.Angle.DEGREE, Units.Angle.T32));
        latitudeT32 = (int) Math.rint(Units.convert(latitudeDegree,Units.Angle.DEGREE, Units.Angle.T32));
        return (latitudeDegree <= 90.0) && (latitudeDegree >= -90.0) ? new GeoPos(longitudeT32, latitudeT32) : null;
    }

    private static double refocusingOf(double coordinate) {
        return coordinate >= (Units.Angle.TURN * 0.5) ? - coordinate : coordinate;
    }

}