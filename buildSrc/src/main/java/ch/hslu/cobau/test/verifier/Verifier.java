package ch.hslu.cobau.test.verifier;

public interface Verifier {
    /**
     * Compares the expected and actual results.
     *
     * @param expected The expected result.
     * @param actual The actual result.
     * @return true if expected und actual results match.
     */
    boolean isValid(String expected, String actual);
}
