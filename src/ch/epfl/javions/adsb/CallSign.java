package ch.epfl.javions.adsb;
import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public record CallSign(String Sign) {

    public CallSign {
        Preconditions.checkArgument(isCallSignValid(Sign));
        Preconditions.checkArgument(Sign != null && !Sign.isEmpty());
    }
    private static final Pattern pattern = Pattern.compile("[A-Z0-9 ]{0,8}");
    private boolean isCallSignValid (String Sign) {
        Matcher matcher = pattern.matcher(Sign);
        return matcher.matches();
    }
}