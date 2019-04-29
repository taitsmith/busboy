package utils;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Here we keep all of our helpful utilities for doing things.
 */

public class HelpfulUtils {

    //get millis between current time and bus eta, convert to minutes
    public static long minutesUntilBus(String eta) {
        DateTimeFormatter dtf = DateTimeFormat.forPattern("HH:mm");
        return 0;
    }
}
