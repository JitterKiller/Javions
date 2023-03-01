package ch.epfl.javions.aircraft;
import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public record AircraftRegistration(String string) {

    private static final Pattern pattern = Pattern.compile("[A-Z0-9 .?/_+-]+");

    public AircraftRegistration {
        Preconditions.checkArgument(pattern.matcher(string).matches());
        Preconditions.checkArgument(!string.isEmpty());
    }

}