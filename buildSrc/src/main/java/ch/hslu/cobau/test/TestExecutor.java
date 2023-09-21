package ch.hslu.cobau.test;

import ch.hslu.cobau.test.model.TestCase;
import ch.hslu.cobau.test.model.TestResult;
import ch.hslu.cobau.test.model.TestStatus;
import ch.hslu.cobau.test.verifier.Verifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * A test executor that allows to execute multiple test cases of a test set with its command line
 * and timeout settings.
 *
 * Note: The test case is executed as a separate process to minimize the influces of previous test executions
 * and the testing framework itself on the result.
 */
public class TestExecutor {
    private final String[] commandLine;
    private final int timeOut;

    /**
     * Create a new TestExecutor instance that uses the given commandLine and timeout during
     * execution of the test cases.
     *
     * @param commandLine The full command line to execute the test cases with.
     * @param timeoutMilliseconds  The timeout in milliseconds for execution of a test case.
     */
    public TestExecutor(String[] commandLine, int timeoutMilliseconds) {
        Objects.requireNonNull(commandLine);

        this.commandLine = commandLine;
        this.timeOut = timeoutMilliseconds;
    }

    /**
     * The input writer class provides to input for a test case. It runs as a separate thread
     * since for large inputs, writing to the input stream may block.
     */
    private static class InputWriter extends Thread {
        private final OutputStream os;
        private final String input;

        public InputWriter(OutputStream os, String input) {
            this.os = os;
            this.input = input;
        }

        public void run() {
            try {
                os.write(input.getBytes()); // may block, but terminates in case the process is destroyed.
                os.flush();
                os.close();
            } catch (IOException ignore) {
                // In case the process terminates before it consumes all input, it raises an
                // IOException (linux: "broken pipe"). Thus, this IOException here is expected.
            }
        }
    }

    /**
     * The output reader class reads the output produced by the process that executes the test case.
     * This class uses a separate thread. This is not strictly necessary, but simplifies the implementation,
     * since waiting for the end of the test case process can now be done in the main thread.
     */
    private static class OutputReader extends Thread  {
        private final InputStream is;
        private final StringBuilder sb = new StringBuilder();

        public OutputReader(InputStream is) {
            this.is = is;
        }

        /**
         * Returns the actual output produced by the process executing the test case.
         * @return the actual output.
         * @throws InterruptedException
         */
        public String getOutput() throws InterruptedException {
            this.join();
            return sb.toString();
        }

        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append(System.lineSeparator());
                }
            } catch (IOException ignore) {
                // Program terminated while reading output
            }
        }
    }

    /**
     * Executes a test case using the provided result verifier.
     *
     * @param testCase The test case to execute.
     * @param verifier The verifier to use for this test case execution.
     * @return An instance of TestResult that contain the result of this test execution.
     */
    public TestResult execute(TestCase testCase, Verifier verifier) {
        Objects.requireNonNull(testCase);
        Objects.requireNonNull(verifier);

        ProcessBuilder pb = new ProcessBuilder();
        pb.redirectErrorStream(true);
        pb.command(commandLine);

        TestStatus testStatus = TestStatus.PASSED;
        String actualOutput = "";
        Integer actualExitcode = null;
        Process process = null;
        long timeActual = System.nanoTime();
        try {
            process = pb.start();
        } catch (IOException e) {
            testStatus = TestStatus.PROGRAM_ERROR;
            actualOutput = "Error starting process '" + Arrays.toString(commandLine) + "'";
        }

        if (process != null) {
            InputWriter inputWriter = new InputWriter(process.getOutputStream(), testCase.getInput());
            inputWriter.start();
            OutputReader outputReader = new OutputReader(process.getInputStream());
            outputReader.start();

            try {
                boolean status = process.waitFor(timeOut, TimeUnit.MILLISECONDS);
                timeActual = (System.nanoTime() - timeActual) / 1000000; // convert to [ms]

                if (!status) {
                    process.destroy();
                    testStatus = TestStatus.TIMEOUT;
                    actualOutput = outputReader.getOutput();
                    actualOutput += "\nProgram exceed time limit: abort";

                } else {
                    actualOutput = outputReader.getOutput();
                    actualExitcode = process.exitValue();
                    if (actualExitcode != testCase.getExpectedExitCode()) {
                        actualOutput += "\nProgram exited with exit code: " + actualExitcode + ", but expected: " + testCase.getExpectedExitCode();
                        testStatus = TestStatus.WRONG_EXITCODE;

                    } else {
                        if (verifier.isValid(testCase.getExpectedOutput(), actualOutput)) {
                            testStatus = TestStatus.PASSED;
                        } else {
                            testStatus = TestStatus.WRONG_ANSWER;
                        }
                    }
                }
            } catch (InterruptedException e) {
                testStatus = TestStatus.ABORTED;
                actualOutput = "Program aborted by user";
            }
        }
        return new TestResult(testCase, actualOutput, testStatus, actualExitcode, timeActual);
    }
}
