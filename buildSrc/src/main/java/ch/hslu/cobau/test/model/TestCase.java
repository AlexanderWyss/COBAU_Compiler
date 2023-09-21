package ch.hslu.cobau.test.model;

public class TestCase {
    private final String name;
    private final String input;
    private final String expectedOutput;
    private final int expectedExitCode;
    private final double score;

    public TestCase(String name, String input, String expectedOutput, int expectedExitCode, int score) {
        this.name = name;
        this.input = input;
        this.expectedOutput = expectedOutput;
        this.expectedExitCode = expectedExitCode;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public String getInput() {
        return input;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public double getScore() {
        return score;
    }

    public int getExpectedExitCode() {
        return expectedExitCode;
    }
}
