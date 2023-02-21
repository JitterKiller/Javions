package ch.epfl.javions;

public final class Math2 {

    private Math2(){}

    public static int clamp(int min, int v, int max){
        if(min <= max) {
            if(v < min) {
                return min;
            } else return Math.min(v, max);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static double asinh(double x){
        return Math.log(x+Math.sqrt(1+Math.pow(x,2)));
    }
}
