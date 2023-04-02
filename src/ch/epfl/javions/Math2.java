package ch.epfl.javions;

/**
 * La classe Math2 définie des méthodes statiques permettant d'effectuer certains calculs mathématiques.
 * Elle est donc similaire à la classe Math de la bibliothèque standard Java.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */

public final class Math2 {

    /**
     * Constructeur privé de la classe Math2 (non instanciable).
     */
    private Math2() {
    }

    /**
     * Méthode clamp qui limite la valeur v dans l'intervalle allant de min à max.
     *
     * @param min valeur minimale.
     * @param v   valeur v.
     * @param max valeur maximale.
     * @return min si v est inférieure à min.
     *         max si v est supérieur à max.
     *         v sinon.
     * @throws IllegalArgumentException si min est strictement supérieur à max grâce
     *                                  à la méthode checkArgument() de la classe
     *                                  Preconditions.
     */
    public static int clamp(int min, int v, int max) {

        Preconditions.checkArgument(min <= max);

        if (v < min) {
            return min;
        } else return Math.min(v, max);

    }

    /**
     * Méthode pour calculer la réciproque du sinus hyperbolique d'une variable x.
     *
     * @param x argument x.
     * @return le sinus hyperbolique réciproque de son argument x.
     */
    public static double asinh(double x) {
        return Math.log(x + Math.sqrt(1 + Math.pow(x, 2)));
    }
}
