package ch.epfl.javions.aircraft;
import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public record AircraftTypeDesignator(String string) {

    private static final Pattern pattern = Pattern.compile("[A-Z0-9]{2,4}");

    public AircraftTypeDesignator {
        Preconditions.checkArgument(pattern.matcher(string).matches());
    }

}