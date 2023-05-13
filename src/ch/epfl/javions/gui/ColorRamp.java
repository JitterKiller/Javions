package ch.epfl.javions.gui;

import ch.epfl.javions.Preconditions;
import javafx.scene.paint.Color;

/**
 * La classe ColorRamp du sous-paquetage gui représente un dégradé de couleurs.
 * Un tel dégradé est vu comme une fonction associant une couleur à un nombre réel.
 * Cette classe sera utilisée pour colorier les icônes et trajectoires en fonction de l'altitude d'un aéronef.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */
public final class ColorRamp {

    /**
     * Dégradé de couleur Plasma utilisé pour colorier
     * les icônes et trajectoires d'aéronefs en fonction de leur altitude.
     * La couleur la plus à gauche - un bleu foncé - correspond aux valeurs inférieures ou égales à 0,
     * tandis que la couleur la plus à droite - un jaune clair - correspond aux valeurs supérieures ou égales à 1.
     * Les couleurs intermédiaires correspondent aux valeurs comprises entre 0 et 1.
     */
    public static final ColorRamp PLASMA = new ColorRamp(
            Color.valueOf("0x0d0887ff"), Color.valueOf("0x220690ff"),
            Color.valueOf("0x320597ff"), Color.valueOf("0x40049dff"),
            Color.valueOf("0x4e02a2ff"), Color.valueOf("0x5b01a5ff"),
            Color.valueOf("0x6800a8ff"), Color.valueOf("0x7501a8ff"),
            Color.valueOf("0x8104a7ff"), Color.valueOf("0x8d0ba5ff"),
            Color.valueOf("0x9814a0ff"), Color.valueOf("0xa31d9aff"),
            Color.valueOf("0xad2693ff"), Color.valueOf("0xb6308bff"),
            Color.valueOf("0xbf3984ff"), Color.valueOf("0xc7427cff"),
            Color.valueOf("0xcf4c74ff"), Color.valueOf("0xd6556dff"),
            Color.valueOf("0xdd5e66ff"), Color.valueOf("0xe3685fff"),
            Color.valueOf("0xe97258ff"), Color.valueOf("0xee7c51ff"),
            Color.valueOf("0xf3874aff"), Color.valueOf("0xf79243ff"),
            Color.valueOf("0xfa9d3bff"), Color.valueOf("0xfca935ff"),
            Color.valueOf("0xfdb52eff"), Color.valueOf("0xfdc229ff"),
            Color.valueOf("0xfccf25ff"), Color.valueOf("0xf9dd24ff"),
            Color.valueOf("0xf5eb27ff"), Color.valueOf("0xf0f921ff"));
    private static final int MINIMUM_COLORS = 2;
    private final Color[] colors;

    /**
     * Constructeur de ColorRamp, accepte en argument une séquence de couleurs JavaFX, de type Color.
     *
     * @param colors Séquence de couleurs JavaFX.
     * @throws IllegalArgumentException si la séquence de couleur JavaFX passée en arguments ne contient
     *                                  pas au moins 2 couleurs.
     */
    public ColorRamp(Color... colors) {
        Preconditions.checkArgument(colors.length >= MINIMUM_COLORS);
        this.colors = colors;
    }

    /**
     * Unique méthode publique de la classe, prend un argument de type double (altitude)
     * et retourne la couleur correspondante.
     *
     * @param t L'altitude calculée grâce à la fonction faisant correspondre la plage dans
     *          laquelle les aéronefs volent généralement avec l'intervalle [0, 1].
     *          On obtient t grâce à la fonction suivante : t = RacineCubique(altitude/12000)
     *          où l'altitude est exprimée en mètre, la constante 12000 correspond approximativement
     *          à la plus haute altitude à laquelle volent les avions de ligne et le rôle de la racine cubique
     *          est de distinguer plus finement les altitudes basses, qui sont les plus importantes.
     * @return La couleur correspondante.
     */
    public Color at(double t) {
        if (t <= 0) {
            return colors[0];
        }
        if (t >= 1) {
            return colors[colors.length - 1];
        }
        int i = (int) Math.floor(t * (colors.length - 1));
        double r = t * (colors.length - 1) - i;
        return colors[i].interpolate(colors[i + 1], r);
    }

}
