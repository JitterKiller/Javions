package ch.epfl.javions.aircraft;
import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public record AircraftTypeDesignator(String Designator) {

    public AircraftTypeDesignator {
        Preconditions.checkArgument(isAircraftTypeDesignatorValid(Designator));
    }
    private static final Pattern pattern = Pattern.compile("[A-Z0-9]{2,4}");
    private boolean isAircraftTypeDesignatorValid(String Designator) {
        Matcher matcher = pattern.matcher(Designator);
        return matcher.matches();
    }
}