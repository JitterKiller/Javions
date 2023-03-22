package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;

import javax.swing.text.Position;
import java.util.Objects;
import java.util.Optional;

public class AircraftStateAccumulator<T extends AircraftStateSetter> {
    private final T stateSetter;
    private Message lastEvenMessage;
    private Message lastOddMessage;

    public AircraftStateAccumulator(T stateSetter) {
        Objects.requireNonNull(stateSetter);
        this.stateSetter = stateSetter;
    }

    public T stateSetter() {
        return stateSetter;
    }

    public void update(Message message) {
//        stateSetter.setLastMessageTimeStampNs(message.timeStampNs());
//
//        switch (message) {
//            case AircraftIdentificationMessage aim -> {
//                stateSetter.setCategory(aim.category());
//                stateSetter.setCallSign(aim.callSign());
//            }
//            case AirbornePositionMessage apm -> {
//                stateSetter.setAltitude(apm.altitude());
//                if((message.timeStampNs() - apm.timeStampNs()) <= 10) {
//                    stateSetter.setPosition(new GeoPos((int) apm.x(),(int) apm.y()));
//                }
//            }
//            case AirborneVelocityMessage avm -> {
//                stateSetter.setVelocity(avm.speed());
//                stateSetter.setTrackOrHeading(avm.trackOrHeading());
//            }
//            default:
//                throw new IllegalStateException("Unexpected value: " + message);
//        }
//
//        // Update lastEvenMessage and lastOddMessage
//        if (message.messageType() % 2 == 0) {
//            lastEvenMessage = message;
//        } else {
//            lastOddMessage = message;
//        }
   }
}
