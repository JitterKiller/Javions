package ch.epfl.javions;

/**
 * La classe Units publique et non instanciable, contient la définition des préfixes SI utiles au projet,
 * des classes imbriquées contenant les définitions des différentes unités,
 * ainsi que des méthodes de conversion.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah Janati Idrissi (362341)
 */
public final class Units {


    /**
     * Préfixes SI utiles au projet (Unité de base (1), Centième d'unité (0.01), Millième d'unité (1000)).
     */
    public static final int BASE_UNIT = 1;
    public static final double CENTI = 1e-2d;
    public static final double KILO = 1e3d;
    /**
     * Constructeur privé de la classe Units (non instanciable).
     */
    private Units() {
    }

    /**
     * Methode convert qui convertit la valeur donnée, exprimée dans l'unité
     * fromUnit (l'unité de départ), en l'unité toUnit (l'unité d'arrivée).
     *
     * @param value    la valeur de l'unité de départ.
     * @param fromUnit l'unité de départ.
     * @param toUnit   l'unité d'arrivée.
     * @return la valeur convertie.
     */
    public static double convert(double value, double fromUnit, double toUnit) {
        return value * (fromUnit / toUnit);
    }

    /**
     * Méthode équivalente à convert lorsque l'unité d'arrivée (toUnit) est l'unité de base et vaut 1.
     *
     * @param value    la valeur de l'unité.
     * @param fromUnit l'unité de départ.
     * @return la valeur convertie.
     */
    public static double convertFrom(double value, double fromUnit) {
        return value * fromUnit;
    }

    /**
     * Méthode équivalente à convert lorsque l'unité de départ (fromUnit) est l'unité de base et vaut 1.
     *
     * @param value  la valeur de l'unité.
     * @param toUnit l'unité d'arrivée.
     * @return la valeur convertie.
     */
    public static double convertTo(double value, double toUnit) {
        return value * (1d / toUnit);
    }

    /**
     * Classe imbriquée Angle contenant les définitions des unités d'angles.
     *
     * @author Adam AIT BOUSSELHAM (356365)
     * @author Abdellah Janati Idrissi (362341)
     */
    public static final class Angle {

        /**
         * Les unités d'angles en question (Radian, Tour, Degré, T32).
         */
        public static final double RADIAN = BASE_UNIT;
        public static final double TURN = 2d * Math.PI * RADIAN;
        public static final double DEGREE = TURN / 360d;
        public static final double T32 = Math.scalb(TURN, -32);
        /**
         * Constructeur privé de la classe Angle (non instanciable).
         */
        private Angle() {
        }

    }

    /**
     * Classe imbriquée Length contenant les définitions des unités de longeur.
     *
     * @author Adam AIT BOUSSELHAM (356365)
     * @author Abdellah Janati Idrissi (362341)
     */

    public static final class Length {

        /**
         * Les unités de longeur en question (Mètre, Centimètre, Kilomètre, Pouce, Pied, Mile Nautique).
         */
        public static final double METER = BASE_UNIT;
        public static final double CENTIMETER = CENTI * METER;
        public static final double INCH = 2.54d * CENTIMETER;
        public static final double FOOT = 12d * INCH;
        public static final double KILOMETER = KILO * METER;
        public static final double NAUTICAL_MILE = 1852d * METER;
        /**
         * Constructeur privé de la classe Length (non instanciable).
         */
        private Length() {
        }

    }

    /**
     * Classe imbriquée Time contenant les définitions des unités de temps.
     *
     * @author Adam AIT BOUSSELHAM (356365)
     * @author Abdellah Janati Idrissi (362341)
     */

    public static final class Time {

        /**
         * Les unités de temps en question (Seconde, Minute, Heure).
         */
        public static final double SECOND = BASE_UNIT;
        public static final double MINUTE = 60d * SECOND;
        public static final double HOUR = 60d * MINUTE;
        /**
         * Constructeur privé de la classe Time (non instanciable).
         */
        private Time() {
        }
    }

    /**
     * Classe imbriquée Speed contenant les définitions des unités de vitesse.
     *
     * @author Adam AIT BOUSSELHAM (356365)
     * @author Abdellah JANATI IDRISSI (362341)
     */
    public static final class Speed {

        /**
         * Les unités de vitesses en question (Nœud, Mètre par secondes, Kilomètre par heure).
         */
        public static final double KNOT = Length.NAUTICAL_MILE / Time.HOUR;
        public static final double METER_PER_SECOND = Length.METER / Time.SECOND;
        public static final double KILOMETER_PER_HOUR = Length.KILOMETER / Time.HOUR;
        /**
         * Constructeur privé de la classe Speed (non instanciable).
         */
        private Speed() {
        }
    }

}