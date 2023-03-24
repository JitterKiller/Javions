package ch.epfl.javions.aircraft;

/**
 * L'énumération WakeTurbulenceCategory, du sous-paquetage aircraft, public,
 * représente la catégorie de turbulence du véhicule aérien (LIGHT, MEDIUM, HEAVY, UNKNOWN).
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */
public enum WakeTurbulenceCategory {
    LIGHT, MEDIUM, HEAVY, UNKNOWN;

    /**
     * Cette méthode statique permet d'attribuer à partir d'une chaine de caractères
     * un de ces 4 types énumérés à l'instance en question de cette classe.
     *
     * @param s une chaîne de caractères représentant une catégorie de turbulence.
     * @return une instance de la classe WakeTurbulenceCategory.
     */
    public static WakeTurbulenceCategory of(String s) {
        return switch (s) {
            case "L" -> LIGHT;
            case "M" -> MEDIUM;
            case "H" -> HEAVY;
            default -> UNKNOWN;
        };
    }
}