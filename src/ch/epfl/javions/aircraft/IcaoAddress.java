package ch.epfl.javions.aircraft;
import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public record IcaoAddress(String OACI) {

    private static final Pattern pattern = Pattern.compile("[0-9A-F]{6}");

    public IcaoAddress {
        Preconditions.checkArgument(isIcaoValid(OACI));
        Preconditions.checkArgument(OACI != null && !OACI.isEmpty());
    }

    private boolean isIcaoValid(String OACI) {
        Matcher matcher = pattern.matcher(OACI);
        return matcher.matches();
    }
}