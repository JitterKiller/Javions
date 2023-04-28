package ch.epfl.javions.adsb;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * L'enregistrement CallSign, du sous-paquetage adsb, public,
 * représente un identifiant de l'appareil.
 *
 * @param string une chaîne représentant un identifiant de l'appareil.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah Janati Idrissi (362341)
 */
public record CallSign(String string) {
    private static final Pattern PATTERN = Pattern.compile("[A-Z0-9 ]{0,8}");

    /**
     * Le constructeur compact de cet enregistrement valide la chaîne qui lui est passée et lève
     * "IllegalArgumentException" si elle ne représente pas un identifiant de l'appareil valide.
     *
     * @throws IllegalArgumentException si une chaîne n'est pas conforme à une expression régulière
     *                                  (pour vérifier cela on utilise certaines méthodes des classes
     *                                  Pattern et Matcher ainsi que checkArgument).
     */
    public CallSign {
        Preconditions.checkArgument(PATTERN.matcher(string).matches());
    }

}