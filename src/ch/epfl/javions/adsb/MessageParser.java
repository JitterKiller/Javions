package ch.epfl.javions.adsb;

public final class MessageParser {

    private MessageParser(){};

    public static Message parse(RawMessage rawMessage) {
        int typeCode = rawMessage.typeCode();

        if (messageType == null) {
            // Le code de type de message est invalide
            return null;
        }

        return switch (messageType) {
            case AIRCRAFT_IDENTIFICATION -> new AircraftIdentificationMessage(rawMessage);
            case AIRBORNE_POSITION -> new AirbornePositionMessage(rawMessage);
            case AIRBORNE_VELOCITY -> new AirborneVelocityMessage(rawMessage);
            default ->
                // Le code de type de message n'est pas supporté
                    null;
        };
    }
}
}
