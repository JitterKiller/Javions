package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;

public class CprDecoder {

    private final static int latitudeZones_0 = 60;
    private final static double zonesWidth_0 = 1 / latitudeZones_0;
    private final static int latitudeZones_1 = 59;
    private final static double zonesWidth_1 = 1 / latitudeZones_1;

    private CprDecoder(){}

    private static double coordinatesRefocusing(double coordinate) {
        return coordinate >= 0.5 ? - coordinate : coordinate;
    }

    public static GeoPos decodePosition(double x0, double y0, double x1, double y1, int mostRecent) {
        Preconditions.checkArgument(mostRecent == 0 || mostRecent == 1);

        double z_phi = y0 * latitudeZones_1 - y1 * latitudeZones_0;
        double z_phi_i;
        double latitude = 0;
        int latitudeT32 = (int) Math.rint(Units.convert(coordinatesRefocusing(latitude), Units.Angle.TURN, Units.Angle.T32));
        double latitudeDegree = Units.convert(coordinatesRefocusing(latitude), Units.Angle.TURN, Units.Angle.DEGREE);
        if (latitudeDegree <= -90 || latitudeDegree >= 90){return null;}

        double longitude = 0;
        int longitudeT32 = (int) Math.rint(Units.convert(coordinatesRefocusing(longitude), Units.Angle.TURN, Units.Angle.T32));

        int longitudeZones_0 = (int) Math.floor((2*Math.PI)/(Math.acos(1-(1-Math.cos(2*Math.PI*zonesWidth_0))/(Math.pow(Math.cos(latitude),2)))));
        int longitudeZones_1 = longitudeZones_0 - 1;

        double z_lambda = x0 * longitudeZones_1 - x1 * longitudeZones_0;
        double z_lambda_i;

        if (mostRecent == 0) {

            if (z_phi < 0) {
                z_phi_i = z_phi + latitudeZones_0;
            } else {z_phi_i = z_phi;}

            latitude = zonesWidth_0 * (z_phi_i + y0);

            if (longitudeZones_0 == 1) {

                longitude = x0;

            } else {

                if (z_lambda < 0) {
                    z_lambda_i = z_lambda + longitudeZones_0;
                } else {z_lambda_i = z_lambda;}

                longitude = (1/longitudeZones_0) * (z_lambda_i + x0);
            }
        } else {

            if (z_phi < 0) {
                z_phi_i = z_phi + latitudeZones_1;
            } else {z_phi_i = z_phi;}

            latitude = zonesWidth_1 * (z_phi_i + y1);

            if (longitudeZones_0 == 1) {

                longitude = x1;

            } else {

                if (z_lambda < 0) {
                    z_lambda_i = z_lambda + longitudeZones_1;
                } else {z_lambda_i = z_lambda;}

                longitude = (1/longitudeZones_1) * (z_lambda_i + x1);
            }
        }
        return new GeoPos(longitudeT32, latitudeT32);
    }
}