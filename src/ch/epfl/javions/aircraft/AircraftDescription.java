package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * L'enregistrement AircraftDescription, du sous-paquetage aircraft, public,
 * représente une description de véhicule aérien.
 *
 * @param string une chaîne représentant une immatriculation de véhicule aérien.
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */
public record AircraftDescription(String string) {

    /* Déclaration d'une expression régulière associée à AircraftDescription. */
    private static final Pattern pattern = Pattern.compile("[ABDGHLPRSTV-][0123468][EJPT-]");


    /**
     * Le constructeur compact de cet enregistrement valide la chaîne qui lui est passée et lève
     * "IllegalArgumentException" si elle ne représente pas une description de véhicule aérien valide.
     *
     * @throws IllegalArgumentException si une chaîne n'est pas conforme à une expression régulière
     *                                  (pour vérifier cela on utilise certaines méthodes des classes
     *                                  Pattern et Matcher ainsi que la méthode checkArgument()).
     */
    public AircraftDescription {
        Preconditions.checkArgument(pattern.matcher(string).matches() || string.isEmpty());
    }


}