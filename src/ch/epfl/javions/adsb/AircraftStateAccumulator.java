package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;

import java.util.Objects;

/**
 * La classe AircraftStateAccumulator représente un "accumulateur d'état d'aéronef",
 * autrement dit un objet accumulant les messages ADS-B provenant d'un seul aéronef
 * afin de déterminer son état au cours du temps.
 * AircraftStateAccumulator est générique, son paramètre de type, nommé T ci-dessous, est borné par AircraftStateSetter.
 *
 * @param <T> L'état modifiable de l'aéronef passé à son constructeur (borné par AircraftStateSetter).
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */
public final class AircraftStateAccumulator<T extends AircraftStateSetter> {
    private static final long TIME_STAMP_NS_TEN_SEC = 10_000_000_000L;
    private final T stateSetter;
    private final AirbornePositionMessage[] lastMessages = new AirbornePositionMessage[2];

    /**
     * Constructeur de la classe AircraftStateAccumulator.
     * Retourne un accumulateur d'état d'aéronef associé à l'état modifiable donné
     * Ou lève NullPointerException si celui-ci est nul.
     *
     * @param stateSetter L'état modifiable de l'aéronef.
     * @throws NullPointerException Si L'état modifiable passé en argument est null.
     */
    public AircraftStateAccumulator(T stateSetter) {
        this.stateSetter = Objects.requireNonNull(stateSetter);
    }

    /**
     * Méthode publique retournant l'état modifiable de l'aéronef passé à son constructeur.
     *
     * @return l'état modifiable de l'aéronef passé à son constructeur.
     */
    public T stateSetter() {
        return stateSetter;
    }

    /**
     * Méthode publique qui met à jour l'état modifiable en fonction du message donné.
     *
     * @param message le message à accumulé provenant d'un aéronef afin de déterminer son état au cours du temps.
     */
    public void update(Message message) {
        /* On met à jour l'horodatage de chaque message. */
        stateSetter.setLastMessageTimeStampNs(message.timeStampNs());

        /* Selon le type de message, on met à jour les informations nécessaires. */
        switch (message) {
            case AircraftIdentificationMessage aim -> {
                stateSetter.setCategory(aim.category());
                stateSetter.setCallSign(aim.callSign());
            }
            case AirbornePositionMessage apm -> {
                stateSetter.setAltitude(apm.altitude());
                /* On mémorise le dernier message reçu dans son index du tableau correspondant
                 * (lastMessages[0] si le message est pair, sinon lastMessages[1] s'il est impair). */
                setParityMessage(apm);
                if (canPositionBeDetermined(lastMessages[apm.parity() ^ 1], apm)) {
                    GeoPos pos = getPos(apm.parity(), apm);
                    if (pos != null) stateSetter.setPosition(pos);
                }
            }
            case AirborneVelocityMessage avm -> {
                stateSetter.setVelocity(avm.speed());
                stateSetter.setTrackOrHeading(avm.trackOrHeading());
            }
            default -> throw new Error("Unexpected value: " + message);
        }
    }

    /**
     * Méthode privée qui permet de mémoriser le dernier message pair
     * et le dernier message impair reçu de l'aéronef, dans le but de pouvoir déterminer sa position
     * (grâce à la seconde méthode privée canPositionBeDetermined()).
     *
     * @param apm Le message ADS-B de positionnement en vol.
     */
    private void setParityMessage(AirbornePositionMessage apm) {
        lastMessages[apm.parity()] = apm;
    }

    /**
     * Méthode privée qui permet de savoir si la position de l'aéronef peut être déterminé.
     *
     * @param lastMessage    Le dernier message reçu de l'aéronef (soit pair, soit impair).
     * @param currentMessage Le message actuel reçu de l'aéronef.
     * @return Vrai si la position peut être déterminée (si la différence entre l'horodatage du message passé
     * à la méthode publique update() (donc l'argument currentMessage) et celui du dernier message de parité opposée
     * reçu de l'aéronef (donc l'argument lastMessage) soit inférieur ou égal à 10 secondes (qui correspond à la
     * constante TIME_STAMP_NS_TEN_SEC).
     * Sinon retourne faux.
     */
    private boolean canPositionBeDetermined(AirbornePositionMessage lastMessage, AirbornePositionMessage currentMessage) {
        return (lastMessage != null) && (currentMessage.timeStampNs() - lastMessage.timeStampNs() <= TIME_STAMP_NS_TEN_SEC);
    }

    /**
     * Méthode privée permettant d'avoir la position (GeoPos) d'un AirbornePositionMessage selon sa parité.
     *
     * @param parity La parité du message.
     * @param apm    Le AirbornePositionMessage.
     * @return La position (GeoPos) d'un AirbornePositionMessage.
     */
    private GeoPos getPos(int parity, AirbornePositionMessage apm) {
        switch (parity) {
            case 0 -> {
                return CprDecoder.decodePosition(apm.x(), apm.y(),
                        lastMessages[parity ^ 1].x(), lastMessages[parity ^ 1].y(), parity);
            }
            case 1 -> {
                return CprDecoder.decodePosition(lastMessages[parity ^ 1].x(), lastMessages[parity ^ 1].y(),
                        apm.x(), apm.y(), parity);
            }
            default -> {
                return null;
            }
        }
    }

}
