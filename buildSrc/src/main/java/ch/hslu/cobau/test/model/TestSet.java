package ch.hslu.cobau.test.model;

/**
 * The parent element for a test set. Contains 0 or more test cases.
 */
public class TestSet {
    private final String name;
    private final String commandline;
    private final String description;
    private final TestCase[] testCases;
    private final int startScore;
    private final int timeoutMilliseconds;

    public TestSet(String name, String commandline, String description, TestCase[] testCases, int startScore, int timeoutMilliseconds) {
        this.name = name;
        this.commandline = commandline;
        this.description = description;
        this.testCases = testCases;
        this.startScore = startScore;
        this.timeoutMilliseconds = timeoutMilliseconds;
    }
    /**
     * Get the name of the test case.
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Get the description of the test set.
     * @return The description of the test set.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get timeout of a test case execution (in milliseconds).
     * @return timeout of a test case execution (in milliseconds).
     */
    public int getTimeoutMilliseconds() {
        return timeoutMilliseconds;
    }

    /**
     * Get all test cases belonging to this test set in an array.
     * @return An array of all test cases belonging to this test set.
     */
    public TestCase[] getTestCases() {
        return testCases;
    }

    /**
     * Gets the starting score for the entire test set (e.g., to offset tests with binary results).
     * @return The starting score for the entire test set.
     */
    public int getStartScore() {
        return startScore;
    }

    /**
     * Get the command line that is used to execute a test case for this test set.
     * @return The command line that is used to execute a test case for this test set.
     */
    public String getCommandline() {
        return commandline;
    }
}
