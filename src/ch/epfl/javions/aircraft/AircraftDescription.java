package ch.epfl.javions.aircraft;
import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public record AircraftDescription(String string) {

    private static final Pattern pattern = Pattern.compile("[ABDGHLPRSTV-][0123468][EJPT-]");

    public AircraftDescription {
        Preconditions.checkArgument(pattern.matcher(string).matches());
    }


}