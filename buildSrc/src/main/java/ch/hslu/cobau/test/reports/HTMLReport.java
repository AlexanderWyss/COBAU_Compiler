package ch.hslu.cobau.test.reports;

import ch.hslu.cobau.test.model.TestResult;
import ch.hslu.cobau.test.model.TestSet;
import ch.hslu.cobau.test.model.TestStatus;
import org.gradle.api.GradleException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Produces a (self-contained) HTML report out of the test results.
 * The report takes the locations of the HTML report as input upon creation.
 */
public class HTMLReport implements Report {
    private final File destination;
    private final List<TestResult> results = new ArrayList<>();
    private int timeoutMilliseconds;
    private int startScore;

    /**
     * Set the file location of the HTML report.
     * @param destination
     */
    public HTMLReport(File destination) {
        Objects.requireNonNull(destination);
        this.destination = destination;
    }

    /**
     * Called before adding the test results.
     *
     * @param testSet The test set for which the report is generated.
     */
    @Override
    public void header(TestSet testSet) {
        Objects.requireNonNull(testSet);
        timeoutMilliseconds = testSet.getTimeoutMilliseconds();
        startScore = testSet.getStartScore();
    }

    /**
     * Add results for a test case execution.
     *
     * @param testResult Must contain the test results.
     */
    @Override
    public void addTestResult(TestResult testResult) {
        Objects.requireNonNull(testResult);
        results.add(testResult);
    }

    /**
     * Produces the HTML report.
     */
    @Override
    public void footer() {
        double totalScore     = startScore;
        double succeededScore = startScore;

        for(TestResult testResult : results) {
            if (testResult.getStatus() == TestStatus.PASSED) {
                succeededScore += testResult.getTestCase().getScore();
            }
            totalScore += testResult.getTestCase().getScore();
        }
        totalScore     = Math.round(Math.max(totalScore,     0.0) * 10.0) / 10.0;
        succeededScore = Math.round(Math.max(succeededScore, 0.0) * 10.0) / 10.0;

        // print header
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html>\n");
        sb.append("<head>\n");
        sb.append("<meta charset=\"utf-8\">\n");
        sb.append("<style>\n");
        sb.append("    table { font-family: arial, sans-serif;border-collapse: collapse;width: 100% }\n");
        sb.append("    p { font-family:arial;font-size:120% }\n");
        sb.append("    td, th { border: 1px solid #dddddd; text-align: left; padding: 8px}\n");
        sb.append("    td.pass {border: 1px solid #90EE90; background-color: #90EE90;color: #007000}\n");
        sb.append("    td.fail {border: 1px solid #F08080; background-color: #F08080;color: #700000}\n");
        sb.append("    tr:nth-child(even) {background-color: #dddddd}\n");
        sb.append("    </style>");
        sb.append("</head>");
        sb.append("<body>");
        sb.append("<p><b>Test results (score: ").append(succeededScore).append(" of ").append(totalScore).append(" with a starting score of ").append(startScore).append("):</b></p>");
        sb.append("<table>");
        sb.append("  <col width=\"12%\">");
        sb.append("  <col width=\"13%\">");
        sb.append("  <col width=\"20%\">");
        sb.append("  <col width=\"20%\">");
        sb.append("  <col width=\"5%\">");
        sb.append("  <col width=\"5%\">");
        sb.append("  <col width=\"5%\">");
        sb.append("  <col width=\"5%\">");
        sb.append("  <col width=\"7%\">");
        sb.append("  <col width=\"8%\">");
        sb.append("  <tr>");
        sb.append("    <th>Test case</th>\n");
        sb.append("    <th>Input</th>\n");
        sb.append("    <th>Expected Output</th>\n");
        sb.append("    <th>Actual Output</th>\n");
        sb.append("    <th>Expected Exitcode</th>\n");
        sb.append("    <th>Actual Exitcode</th>\n");
        sb.append("    <th>Score</th>\n");
        sb.append("    <th>Result</th>\n");
        sb.append("    <th>Time [ms]</th>\n");
        sb.append("    <th>Timeout [ms]</th>\n");
        sb.append("  </tr>\n");

        // print test cases
        for(TestResult testResult : results) {
            String result = testResult.getStatus().toString();
            String tdClass = testResult.getStatus() == TestStatus.PASSED ? "pass" : "fail";

            sb.append("  <tr>\n");
            sb.append("    <td>").append(testResult.getTestCase().getName()).append("</td>\n");
            sb.append("    <td>").append(replaceNonPrintable(testResult.getTestCase().getInput())).append("</td>\n");
            sb.append("    <td>").append(replaceNonPrintable(testResult.getTestCase().getExpectedOutput())).append("</td>\n");
            sb.append("    <td>").append(replaceNonPrintable(testResult.getActualOutput())).append("</td>\n");
            sb.append("    <td>").append(testResult.getTestCase().getExpectedExitCode()).append("</td>\n");
            sb.append("    <td>").append(testResult.getActualExitCode()).append("</td>\n");
            sb.append("    <td>").append(testResult.getTestCase().getScore()).append("</td>\n");
            sb.append("    <td class=\"").append(tdClass).append("\">").append(result).append("</td>\n");
            sb.append("    <td>").append(testResult.getActualExecutionTime()).append("</td>\n");
            sb.append("    <td>").append(timeoutMilliseconds).append("</td>\n");
            sb.append("  </tr>\n");
        }

        // print footer
        sb.append("</table>\n");
        sb.append("<p><small><b>Disclaimer:</b> The calculated points serve only as an indication " +
                "of the achieved points. You must check yourself for obvious errors in the evaluation " +
                "(e.g., test shows green although feature has not been implemented). " +
                "Also, keep in mind that your submission will be tested against a similar test set with " +
                "minor differences to check for hard coded results.</small></p>\n");
        sb.append("</body>\n");
        sb.append("</html>\n");

        try {
            destination.getParentFile().mkdirs();
            FileWriter fileWriter = new FileWriter(destination);
            fileWriter.write(sb.toString());
            fileWriter.close();
            System.out.println("Detailed test report see 'file://" + destination.getAbsolutePath() + "'");
        } catch (IOException e) {
            throw new GradleException("Error writing HTML report to 'file://" + destination.getAbsolutePath() + "'", e);
        }
    }

    // Replace non-printable characters in the string with HTML codes.
    private String replaceNonPrintable(String s) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (c > 32 /* printable in ASCII */) {
                output.append(c); // append to output
            } else if (c == 32 /* space */) {
                output.append("&nbsp;");
            } else if (c == 10 /* new line */) {
                output.append("<br>");
            } else if (c != 13 /* CR */) {
                output.append("&#191;"); // reversed question mark
            }
        }
        return output.toString();
    }
}
