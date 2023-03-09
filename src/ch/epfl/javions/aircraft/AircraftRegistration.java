package ch.epfl.javions.aircraft;
import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * L'enregistrement AircraftRegistration, du sous-paquetage aircraft, public,
 * représente l'immatriculation d'un véhicule aérien.
 * @param string une chaîne représentant une immatriculation de véhicule aérien.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */
public record AircraftRegistration(String string) {

    /*  Déclaration d'une expression régulière associée à AircraftRegistration. */
    private static final Pattern pattern = Pattern.compile("[A-Z0-9 .?/_+-]+");

    /**
     * Le constructeur compact de cet enregistrement valide la chaîne qui lui est passée et lève
     * "IllegalArgumentException" si elle ne représente pas une immatriculation de véhicule aérien valide.
     * @throws IllegalArgumentException
     *              si une chaîne n'est pas conforme à une expression régulière
     *              (pour vérifier cela on utilise certaines méthodes des classes
     *              Pattern et Matcher ainsi que checkArgument).
     * @throws IllegalArgumentException
     *              si cette chaine est vide.
     */
    public AircraftRegistration {
        Preconditions.checkArgument(pattern.matcher(string).matches());
    }

}