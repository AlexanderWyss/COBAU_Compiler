package ch.hslu.cobau.test.reports;

import ch.hslu.cobau.test.model.TestResult;
import ch.hslu.cobau.test.model.TestSet;

public interface Report {
    /**
     * Called before adding test results.
     * @param testSet Reference to the test set of a test result.
     */
    void header(TestSet testSet);

    /**
     * Called when adding a test result.
     * @param testResult Reference to the test result to be added.
     */
    void addTestResult(TestResult testResult);

    /**
     * Called after having added all test results.
     */
    void footer();
}
