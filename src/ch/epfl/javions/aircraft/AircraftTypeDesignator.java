package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * L'enregistrement AircraftTypeDesignator, du sous-paquetage aircraft, public, représente un type de véhicule aérien.
 *
 * @param string une chaîne représentant un type de véhicule aérien.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */
public record AircraftTypeDesignator(String string) {
    private static final Pattern PATTERN = Pattern.compile("[A-Z0-9]{2,4}");

    /**
     * Le constructeur compact de cet enregistrement valide la chaîne qui lui est passée et lève
     * "IllegalArgumentException" si elle ne représente pas un type de véhicule aérien valide.
     *
     * @throws IllegalArgumentException si une chaîne n'est pas conforme à une expression régulière (pour vérifier
     *                                  cela on utilise certaines méthodes des classes Pattern et Matcher ainsi que
     *                                  la méthode checkArgument()).
     */
    public AircraftTypeDesignator {
        Preconditions.checkArgument(PATTERN.matcher(string).matches() || string.isEmpty());
    }

}