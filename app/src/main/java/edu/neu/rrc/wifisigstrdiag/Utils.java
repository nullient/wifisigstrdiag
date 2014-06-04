package edu.neu.rrc.wifisigstrdiag;

public final class Utils {

    private static final String WILDCARD_REGEXP = "\\*";

    /**
     * Returns whether a matching string matches a wildcard pattern (i.e, "abc*def*" matches
     * "abccdeff" but not "gabcdef").
     *
     * @param pattern
     * @param testString
     * @return
     */
    public static final boolean matchesWildcard(String pattern, String testString) {
        // split up into parts (-1 is needed so it's greedy)
        String[] patternSplit = pattern.split(WILDCARD_REGEXP, -1);

        // if the pattern doesn't start with the wildcard, make sure the string matches from the start
        // same thing for the end
        if (!testString.startsWith(patternSplit[0]) || !testString.endsWith(patternSplit[patternSplit.length - 1])) {
            return false;
        }

        int start = 0, i = 0, indexOf = 0;
        while (i < patternSplit.length) {
            // try and return if the next section can't be found
            indexOf = testString.indexOf(patternSplit[i], start);
            if (indexOf < 0) {
                return false;
            }

            start = indexOf + patternSplit[i].length();
            i++;
        }

        // we're good at the end
        return true;
    }

    public static int compareIntegers(int x, int y) {
        // from http://www.docjar.com/html/api/java/lang/Integer.java.html
        // Android needs min API 19
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }
}
