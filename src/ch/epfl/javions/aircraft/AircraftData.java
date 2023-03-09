package ch.epfl.javions.aircraft;
import java.util.Objects;

/**
 * L'enregistrement AircraftData, du sous-paquetage aircraft, public, représente les données d'un véhicule aérien.
 * @param registration la registration de l'aéronef
 * @param typeDesignator le type de l'aéronef
 * @param model le modèle de l'aéronef
 * @param description la description de l'aéronef
 * @param wakeTurbulenceCategory la catégorie de turbulence de l'aéronef
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */
public record AircraftData(AircraftRegistration registration, AircraftTypeDesignator typeDesignator, String model, AircraftDescription description, WakeTurbulenceCategory wakeTurbulenceCategory) {

    /**
     * Le constructeur compact de cet enregistrement vérifie que les paramètres suivants ne sont pas nuls.
     * @throws NullPointerException
     *          si l'un des paramètres est nul
     *          (on utilise la méthode requireNonNull de la classe Objects).
     */
    public AircraftData {
        Objects.requireNonNull(registration);
        Objects.requireNonNull(typeDesignator);
        Objects.requireNonNull(model);
        Objects.requireNonNull(description);
        Objects.requireNonNull(wakeTurbulenceCategory);
    }
}