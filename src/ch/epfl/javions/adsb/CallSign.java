package ch.epfl.javions.adsb;
import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public record CallSign(String string) {

    private static final Pattern pattern = Pattern.compile("[A-Z0-9 ]{0,8}");

    public CallSign {
        Preconditions.checkArgument(pattern.matcher(string).matches());
    }

}