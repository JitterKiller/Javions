package ch.epfl.javions.adsb;

import java.util.Objects;

public class AircraftStateAccumulator<T extends AircraftStateSetter> {
    private final static long TIME_STAMP_CONST_MULTIPLIER = 10000000000L;
    private final T stateSetter;
    private AirbornePositionMessage lastEvenMessage;
    private AirbornePositionMessage lastOddMessage;

    public AircraftStateAccumulator(T stateSetter) {
        Objects.requireNonNull(stateSetter);
        this.stateSetter = stateSetter;
    }

    public T stateSetter() {
        return stateSetter;
    }

    public void update(Message message) {
        stateSetter.setLastMessageTimeStampNs(message.timeStampNs());

        switch (message) {
            case AircraftIdentificationMessage aim -> {
                stateSetter.setCategory(aim.category());
                stateSetter.setCallSign(aim.callSign());
            }
            case AirbornePositionMessage apm -> {
                setParityMessage(apm);
                stateSetter.setAltitude(apm.altitude());
                switch (apm.parity()) {
                    case 0 -> {
                        if (lastOddMessage != null && (apm.timeStampNs() - lastOddMessage.timeStampNs() <= TIME_STAMP_CONST_MULTIPLIER)) {
                            stateSetter.setPosition(CprDecoder.decodePosition(apm.x(), apm.y(), lastOddMessage.x(), lastOddMessage.y(), 0));
                        }
                    }
                    case 1 -> {
                        if (lastEvenMessage != null && (apm.timeStampNs() - lastEvenMessage.timeStampNs() <= TIME_STAMP_CONST_MULTIPLIER)) {
                            stateSetter.setPosition(CprDecoder.decodePosition(lastEvenMessage.x(), lastEvenMessage.y(), apm.x(), apm.y(), 1));
                        }
                    }
                }
            }
            case AirborneVelocityMessage avm -> {
                stateSetter.setVelocity(avm.speed());
                stateSetter.setTrackOrHeading(avm.trackOrHeading());
            }
            default -> throw new Error("Unexpected value: " + message);
        }
    }

    private void setParityMessage(AirbornePositionMessage apm) {
        if (apm.parity() == 0) {
            lastEvenMessage = apm;
        } else {
            lastOddMessage = apm;
        }
    }
}
