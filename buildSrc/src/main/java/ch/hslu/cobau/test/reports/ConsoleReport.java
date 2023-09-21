package ch.hslu.cobau.test.reports;

import ch.hslu.cobau.test.model.TestResult;
import ch.hslu.cobau.test.model.TestSet;

public class ConsoleReport implements Report {

    @Override
    public void header(TestSet testSet) {
        System.out.println("System test: " + testSet.getName());
    }

    @Override
    public void addTestResult(TestResult testResult) {
        StringBuilder sb = new StringBuilder();
        sb.append("Test: '" ).append(testResult.getTestCase().getName()).append("': ").append(testResult.getStatus());
        System.out.println(sb.toString());
    }

    @Override
    public void footer() {

    }
}
