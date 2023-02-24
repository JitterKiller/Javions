package ch.epfl.javions.aircraft;
import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public record IcaoAddress(String OACI) {

    public IcaoAddress {
        Preconditions.checkArgument(isIcaoValid(OACI));
        Preconditions.checkArgument(OACI != null && !OACI.isEmpty());
    }
    private static final Pattern pattern = Pattern.compile("[0-9A-F]{6}");
    private boolean isIcaoValid(String OACI) {
        Matcher matcher = pattern.matcher(OACI);
        return matcher.matches();
    }
}