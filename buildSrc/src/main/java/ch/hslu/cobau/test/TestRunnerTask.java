package ch.hslu.cobau.test;

import ch.hslu.cobau.test.model.TestSet;
import ch.hslu.cobau.test.reports.HTMLReport;
import ch.hslu.cobau.test.verifier.RelaxedVerifier;
import com.google.gson.Gson;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.*;

import java.io.*;

/**
 * Gradle task that performs an execution of a test set.
 */
public class TestRunnerTask extends DefaultTask {
    private File testSet;

    public TestRunnerTask() {
        getInputs().dir(getProject().getBuildDir());
    }

    @OutputFile
    public File reportDestination;

    /**
     * Get the currently set location of the file containing the test set.
     * @return The currently set location of the file containing the test set.
     */
    @InputFiles
    public File getTestSet() {
        return testSet;
    }

    /**
     * Set the location of the file containing the test set.
     * @param testSet A File instance that contains the location of the test set.
     */
    public void setTestSet(File testSet) {
        this.testSet = testSet;
    }

    /**
     * Get the currently set report destination.
     * @return The currently set report destination.
     */
    public File getReportDestination() {
        if (reportDestination == null) {
            return new File(getProject().getProjectDir(), "reports/" + getTestSet().getName().replaceAll("\\.json$", ".html"));
        } else {
            return reportDestination;
        }
    }

    /**
     * Sets the target of the report produced by this test set execution.
     * @param reportDestination The File instance that contains report path.
     */
    public void setReportDestination(File reportDestination) {
        this.reportDestination = reportDestination;
    }

    /**
     * Executes all test cases of this test set.
     */
    @TaskAction
    public void execute() {
        Reader reader;
        TestRunner testRunner;
        try {
            reader = new InputStreamReader(new FileInputStream(testSet));
            Gson gson = new Gson();
            TestSet testSet = gson.fromJson(reader, TestSet.class);
            testRunner = new TestRunner(testSet, new TestExecutor(getCommandLine(testSet), testSet.getTimeoutMilliseconds()), new RelaxedVerifier(), new HTMLReport(getReportDestination()));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("cannot find test set '" + testSet + "'");
        }

        testRunner.run();
    }

    /**
     * Resolve placeholders in commandline specified in the test set:
     * - Replace occurrences of ${buildDir} with the actual buildDir.
     * - Replace occurrences of ${jvm} with the current executing JVM.
     * - Add .exe suffix for if executed in a windows system.
     *
     * @param testSet The test set, for which to resolve the command line.
     * @return The resolved executable String.
     */
    private String[] getCommandLine(TestSet testSet) {
        String[] commandLine = testSet.getCommandline().split("\\s+");

        for(int i = 0; i < commandLine.length; ++i) {
            String part = commandLine[i];
            part = part.replace('/', File.separatorChar);
            part = part.replace('\\', File.separatorChar);
            part = part.replace("${buildDir}", getProject().getBuildDir().getAbsolutePath());
            part = part.replace("${jvm}", getJavaBinary());
            part = part.replace("${os}", getOs());
            part = part.replace("${exec_ext}", getExecSuffix());
            part = part.replace("${shell_ext}", getShellSuffix());

            commandLine[i] = part;
        }

        return commandLine;
    }

    private String getOs() {
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            return "windows";
        } else if (Os.isFamily(Os.FAMILY_MAC)) {
            return "macosx";
        } else if (Os.isFamily(Os.FAMILY_UNIX)) {
            return "linux";
        }
        throw new RuntimeException("unsupported operating system");
    }
    private String getExecSuffix() {
        switch (getOs()) {
            case "windows" :
                return ".exe";
            case "linux"   :
            case "macosx"  :
                return "";
        }
        throw new RuntimeException("unsupported operating system");
    }

    private String getShellSuffix() {
        switch (getOs()) {
            case "windows" :
                return ".bat";
            case "linux"   :
            case "macosx"  :
                return ".sh";
        }
        throw new RuntimeException("unsupported operating system");
    }

    /**
     * Returns the full path to the java executable of the currently executing JVM.
     *
     * @return Full path to java executable.
     */
    private String getJavaBinary() {
        return System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
    }
}
