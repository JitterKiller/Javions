package ch.epfl.javions.aircraft;
import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public record AircraftDescription(String Description) {

    public AircraftDescription {
        Preconditions.checkArgument(isAircraftDescriptionValid(Description));
    }
    private static final Pattern pattern = Pattern.compile("[ABDGHLPRSTV-][0123468][EJPT-]");
    private boolean isAircraftDescriptionValid (String Description) {
        Matcher matcher = pattern.matcher(Description);
        return matcher.matches();
    }
}