package ch.epfl.javions.adsb;

/**
 * La classe MessageParser publique et non instanciable, permet de transformer les messages ADS-B bruts
 * en messages d'un des trois types (AircraftIdentificationMessage, AirbornePositionMessage ou AirborneVelocityMessage).
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */
public class MessageParser {

    /**
     * Constructeur privé de la classe (non instantiable).
     */
    private MessageParser() {
    }

    /**
     * Méthode statique publique retourne l'instance de AircraftIdentificationMessage,
     * de AirbornePositionMessage ou de AirborneVelocityMessage correspondant au message brut donné,
     * ou null si le code de type de ce dernier ne correspond à aucun de ces trois types de messages,
     * ou s'il est invalide.
     *
     * @param rawMessage Le message brut à transformer.
     * @return Une instance de AircraftIdentificationMessage si le typeCode du message brut
     *         est compris entre 1 et 4 (inclus).
     *         Une instance de AirbornePositionMessage si le typeCode du message brut
     *         est compris entre 9 et 18 (inclus), ou 20 et 22 (inclus).
     *         Une instance de AirborneVelocityMessage si le typeCode du message brut vaut 19.
     *         Null si le typeCode ne correspond à aucun de ces trois types de messages (donc invalide).
     */
    public static Message parse(RawMessage rawMessage) {

        switch (rawMessage.typeCode()) {

            case 1, 2, 3, 4 -> {
                return AircraftIdentificationMessage.of(rawMessage);
            }

            case 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 20, 21, 22 -> {
                return AirbornePositionMessage.of(rawMessage);
            }

            case 19 -> {
                return AirborneVelocityMessage.of(rawMessage);
            }

            default -> {
                return null;
            }

        }
    }
}