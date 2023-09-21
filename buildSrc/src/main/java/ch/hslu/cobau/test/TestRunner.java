package ch.hslu.cobau.test;

import ch.hslu.cobau.test.model.TestCase;
import ch.hslu.cobau.test.model.TestResult;
import ch.hslu.cobau.test.model.TestSet;
import ch.hslu.cobau.test.reports.Report;
import ch.hslu.cobau.test.verifier.RelaxedVerifier;

import java.util.Arrays;
import java.util.Objects;

/**
 * Test runner class that executes all test cases of a test set.
 */
public class TestRunner {
    private final TestSet testSet;
    private final TestExecutor testExecutor;
    private final RelaxedVerifier resultVerifier;
    private final Report[] reporters;

    public TestRunner(TestSet testSet, TestExecutor testExecutor, RelaxedVerifier resultVerifier, Report... reporter) {
        Objects.requireNonNull(testSet);
        Objects.requireNonNull(testExecutor);
        Objects.requireNonNull(resultVerifier);

        this.testSet = testSet;
        this.testExecutor = testExecutor;
        this.resultVerifier = resultVerifier;
        this.reporters = reporter;
    }

    /**
     * Executes all test cases of this test set.
     */
    void run() {
        Arrays.stream(reporters).forEach(report -> report.header(testSet));
        for (TestCase testCase : testSet.getTestCases()) {
            TestResult testResult = testExecutor.execute(testCase, resultVerifier);
            Arrays.stream(reporters).forEach(report -> report.addTestResult(testResult));
        }
        Arrays.stream(reporters).forEach(Report::footer);
    }
}
