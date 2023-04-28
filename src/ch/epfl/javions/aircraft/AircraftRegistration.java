package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * L'enregistrement AircraftRegistration, du sous-paquetage aircraft, public,
 * représente l'immatriculation d'un véhicule aérien.
 *
 * @param string une chaîne représentant une immatriculation de véhicule aérien.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */
public record AircraftRegistration(String string) {
    private static final Pattern PATTERN = Pattern.compile("[A-Z0-9 .?/_+-]+");

    /**
     * Le constructeur compact de cet enregistrement valide la chaîne qui lui est passée et lève
     * "IllegalArgumentException" si elle ne représente pas une immatriculation de véhicule aérien valide.
     *
     * @throws IllegalArgumentException si une chaîne n'est pas conforme à une expression régulière
     *                                  (pour vérifier cela on utilise certaines méthodes des classes
     *                                  Pattern et Matcher ainsi que la méthode checkArgument()).
     * @throws IllegalArgumentException si cette chaine est vide.
     */
    public AircraftRegistration {
        Preconditions.checkArgument(PATTERN.matcher(string).matches());
    }

}