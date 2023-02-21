package ch.epfl.javions;

import static ch.epfl.javions.Math2.asinh;

public class WebMercator {

    private WebMercator(){}

    public static double x (int zoomLevel, double longitude){
        return Math.scalb(1,8 + zoomLevel) * (Units.convertTo(longitude,Units.Angle.TURN) + 0.5);
    }

    public static double y  (int zoomLevel, double latitude){
        return Math.scalb(1,8 + zoomLevel) * (-Units.convertTo(asinh(Math.tan(latitude)),Units.Angle.TURN) + 0.5);
    }
}
