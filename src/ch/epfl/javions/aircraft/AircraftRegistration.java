package ch.epfl.javions.aircraft;
import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public record AircraftRegistration(String Registration) {

    private static final Pattern pattern = Pattern.compile("[A-Z0-9 .?/_+-]+");

    public AircraftRegistration {
        Preconditions.checkArgument(isAircraftRegistrationValid(Registration));
        Preconditions.checkArgument(Registration != null && !Registration.isEmpty());
    }

    private boolean isAircraftRegistrationValid (String Registration) {
        Matcher matcher = pattern.matcher(Registration);
        return matcher.matches();
    }
}