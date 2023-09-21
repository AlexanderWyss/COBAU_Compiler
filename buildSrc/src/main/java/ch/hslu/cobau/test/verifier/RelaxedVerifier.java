package ch.hslu.cobau.test.verifier;

import java.util.Objects;

/**
 * This class implements a relaxed result verifier. This means it tries to find
 * the expected result within the actual result while ignoring non-matching lines.
 * Further, the verifier is case insensitive and ignores superfluous whitespace.
 */
public class RelaxedVerifier implements Verifier {

    /**
     * Compares the expected result with the actual result.
     *
     * @return true if the expected result is contained within the actual result.
     */
    public boolean isValid(String expected, String actual) {
        Objects.requireNonNull(expected);
        Objects.requireNonNull(actual);

        String[] expectedLines = splitLines(expected);
        String[] actualLines = splitLines(actual);

        if (actualLines.length == 0 || expectedLines.length == 0) {
            return expectedLines.length == 0;
        }

        int actualIndex = 0;
        int expectIndex = 0;
        while (!containsElements(expectedLines[expectIndex], actualLines[actualIndex])) {
            actualIndex++;
            if (actualIndex == actualLines.length) {
                return false;
            }
        }

        while(expectIndex < expectedLines.length && actualIndex < actualLines.length
                && containsElements(expectedLines[expectIndex], actualLines[actualIndex])) {
            ++actualIndex;
            ++expectIndex;
        }

        return expectIndex == expectedLines.length; // all expected lines found
    }

    /**
     * Compares the expected line with the actual line.
     *
     * @return true, if all elements from expectedLine are contained in actualLine.
     * Elements are separated by whitespace. Superfluous whitespace is ignored.
     */
    private boolean containsElements(String expectedLine, String actualLine) {
        Objects.requireNonNull(expectedLine);
        Objects.requireNonNull(actualLine);

        String[] expectedElements = splitWords(expectedLine);
        String[] actualElements = splitWords(actualLine);

        if (actualElements.length == 0 || expectedElements.length == 0) {
            return expectedElements.length == 0;
        }

        int actualIndex = 0;
        int expectIndex = 0;
        while (!isEqual(actualElements[actualIndex], (expectedElements[expectIndex]))) {
            actualIndex++;
            if (actualIndex == actualElements.length) {
                return false;
            }
        }

        while(expectIndex < expectedElements.length && actualIndex < actualElements.length
                && isEqual(expectedElements[expectIndex], actualElements[actualIndex])) {
            ++actualIndex;
            ++expectIndex;
        }

        return expectIndex == expectedElements.length; // all expected elements found
    }

    private String[] splitLines(String input) {
        if ("".equals(input.trim())) {
            return new String[0];
        } else {
            return input.split("\\r?\\n");
        }
    }

    private String[] splitWords(String input) {
        if ("".equals(input.trim())) {
            return new String[0];
        } else {
            return input.split("\\s+");
        }
    }

    /**
     * Compares two strings (case insensitive).
     *
     * @return true if two strings are considered equal, otherwise false is returned.
     */
    private static boolean isEqual(String s1, String s2) {
        Objects.requireNonNull(s1);
        Objects.requireNonNull(s2);

        return s1.toLowerCase().equals(s2.toLowerCase());
    }
}
