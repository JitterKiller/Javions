package ch.epfl.javions.aircraft;
import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;


/**
 * L'enregistrement IcaoAddress, du sous-paquetage aircraft, public,
 * représente l'adresse OACI d'un véhicule aérien.
 *
 * @param string la chaîne contenant la représentation textuelle de l'adresse OACI.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah Janati Idrissi (362341)
 */
public record IcaoAddress(String string) {

    /* Déclaration d'une expression régulière associée a IcaoAdress */
    private static final Pattern pattern = Pattern.compile("[0-9A-F]{6}");

    /**
     * Le constructeur compact de cet enregistrement valide la chaîne qui lui est passée et lève
     * "IllegalArgumentException" si elle ne représente pas une adresse OACI valide.
     * @throws IllegalArgumentException
     *              Si une chaîne n'est pas conforme à une expression régulière
     *              (pour verifier cela on utilise certaines méthodes des classes Pattern
     *              et Matcher ainsi que checkArgument).
     * @throws IllegalArgumentException
     *              Si cette chaine est vide.
     */
    public IcaoAddress {
        Preconditions.checkArgument(pattern.matcher(string).matches());
        Preconditions.checkArgument(!string.isEmpty());
    }

}