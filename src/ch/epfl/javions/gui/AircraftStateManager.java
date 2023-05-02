package ch.epfl.javions.gui;

import ch.epfl.javions.adsb.AircraftStateAccumulator;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * La classe AircraftStateManager du sous-paquetage gui, a pour but de garder à jour les états
 * d'un ensemble d'aéronefs en fonction des messages reçus d'eux.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */
public final class AircraftStateManager {
    private static final long ONE_MINUTE_TIME_STAMP_NS = 60_000_000_000L;
    private final Map<IcaoAddress, AircraftStateAccumulator<ObservableAircraftState>> table = new HashMap<>();
    private final ObservableSet<ObservableAircraftState> aircraftStatesPosition = FXCollections.observableSet();
    private final AircraftDatabase database;
    private long previousMessageTimeStampNs;

    /**
     * Constructeur public de AircraftStateManager.
     *
     * @param database La base de données contenant les caractéristiques fixes des aéronefs.
     */
    public AircraftStateManager(AircraftDatabase database) {
        this.database = database;
    }

    /**
     * Méthode retournant l'ensemble observable, mais non modifiable,
     * des états observables des aéronefs dont la position est connue.
     *
     * @return L'ensemble des états observables des aéronefs dont la position est connue.
     */
    public ObservableSet<ObservableAircraftState> states() {
        return FXCollections.unmodifiableObservableSet(aircraftStatesPosition);
    }

    /**
     * Méthode prenant en argument n message et l'utilisant pour mettre à jour l'état de l'aéronef qui
     * l'a envoyé (créant cet état lorsque le message est le premier reçu de cet aéronef)
     *
     * @param message Le message utilisé pour mettre à jour l'état de l'aéronef qui l'a envoyé.
     * @throws IOException Si une erreur se produit lors de l'accès au fichier ZIP ou à un des
     *                     fichiers CSV de la base de données.
     */
    public void updateWithMessage(Message message) throws IOException {
        IcaoAddress address = message.icaoAddress();

        if (table.isEmpty() || table.get(address) == null) {
            ObservableAircraftState state = new ObservableAircraftState(address, database.get(address));
            table.put(address, new AircraftStateAccumulator<>(state));
        }

        AircraftStateAccumulator<ObservableAircraftState> accumulator = table.get(address);
        accumulator.update(message);

        ObservableAircraftState stateSetter = accumulator.stateSetter();
        previousMessageTimeStampNs = stateSetter.getLastMessageTimeStampNs();
        if (stateSetter.getPosition() != null) {
            aircraftStatesPosition.add(stateSetter);
        }
    }

    /**
     * Méthode supprimant de l'ensemble des états observables tous ceux
     * correspondant à des aéronefs dont aucun message n'a été reçu dans la minute précédant
     * la réception du dernier message passé à updateWithMessage.
     */
    public void purge() {
        aircraftStatesPosition.removeIf(state ->
                previousMessageTimeStampNs - state.getLastMessageTimeStampNs() >= ONE_MINUTE_TIME_STAMP_NS);

    }
}
