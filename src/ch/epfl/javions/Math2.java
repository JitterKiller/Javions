package ch.epfl.javions;

/**
 * La classe Math2 définie des méthodes statiques permettant d'effectuer certains calculs mathématiques.
 * Elle est donc similaire à la classe Math de la bibliothèque standard Java.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah Janati Idrissi (362341)
 */

public final class Math2 {

    /**
     * Constructeur privé de la classe Time (non instanciable).
     */
    private Math2(){}


    /**
     * Méthode clamp limite la valeur v dans l'intervalle allant de min à max.
     * @param min
     *          valeur minimale
     * @param v
     *          valeur v
     * @param max
     *          valeur maximale
     * @return
     *          min si v est inférieure à min
     *          max si v est supérieur à max
     *          v sinon
     * @throws IllegalArgumentException
     *          si min est strictement supérieur à max.
     */
    public static int clamp(int min, int v, int max){
        if(min <= max) {
            if(v < min) {
                return min;
            } else return Math.min(v, max);
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Méthode pour calculer le sinus hyperbolique d'une variable x.
     * @param x
     *          argument x
     * @return le sinus hyperbolique réciproque de son argument x
     */
    public static double asinh(double x){
        return Math.log(x+Math.sqrt(1+Math.pow(x,2)));
    }
}
