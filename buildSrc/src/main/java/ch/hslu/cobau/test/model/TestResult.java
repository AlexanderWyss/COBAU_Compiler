package ch.hslu.cobau.test.model;

public class TestResult {
    private final TestCase testCase;
    private final TestStatus testStatus;
    private final String actualOutput;
    private final Integer actualExitcode;
    private final long actualTime;

    public TestResult(TestCase testCase, String actualOutput, TestStatus testStatus, Integer actualExitcode, long actualTime) {
        this.testCase     = testCase;
        this.testStatus   = testStatus;
        this.actualOutput = actualOutput;
        this.actualExitcode = actualExitcode;
        this.actualTime = actualTime;
    }

    public TestCase getTestCase() {
        return testCase;
    }

    public TestStatus getStatus() {
        return testStatus;
    }

    public String getActualOutput() {
        return actualOutput;
    }

    public Integer getActualExitCode() {
        return actualExitcode;
    }

    public long getActualExecutionTime() {
        return actualTime;
    }
}
