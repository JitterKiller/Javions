package ch.epfl.javions.gui;

import ch.epfl.javions.adsb.AircraftStateAccumulator;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.io.IOException;
import java.util.*;

public final class AircraftStateManager {
    private final Map<IcaoAddress, AircraftStateAccumulator<ObservableAircraftState>> table = new HashMap<>();
    private final Set<ObservableAircraftState> knownAircraftPositionsStates = new HashSet<>();
    private final AircraftDatabase database;
    private static final long ONE_MINUTE_TIME_STAMP_NS = 60_000_000_000L;
    private long previousMessageTimeStampNs;

    public AircraftStateManager(AircraftDatabase database) {
        this.database = database;
    }

    public Set<ObservableAircraftState> states() {
        return Set.copyOf(knownAircraftPositionsStates);
    }

    public void updateWithMessage(Message message) throws IOException {
        IcaoAddress address = message.icaoAddress();

        if(table.isEmpty() || table.get(address) == null) {
            ObservableAircraftState state = new ObservableAircraftState(address,database.get(address));
            table.put(address, new AircraftStateAccumulator<>(state));
        }

        AircraftStateAccumulator<ObservableAircraftState> accumulator = table.get(address);
        accumulator.update(message);

        ObservableAircraftState stateSetter = accumulator.stateSetter();
        previousMessageTimeStampNs = accumulator.stateSetter().getLastMessageTimeStampNs();
        if(stateSetter.getPosition() != null) {
            knownAircraftPositionsStates.add(stateSetter);
        }
    }

    public void purge() {
        knownAircraftPositionsStates.removeIf(state -> previousMessageTimeStampNs - state.getLastMessageTimeStampNs() >= ONE_MINUTE_TIME_STAMP_NS);
    }
}
