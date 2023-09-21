package ch.hslu.cobau.test.model;

public enum TestStatus {
    PASSED("Passed"),
    PROGRAM_ERROR("Error starting process"),
    WRONG_ANSWER("Wrong Answer"),
    WRONG_EXITCODE("Wrong Exitcode"),
    TIMEOUT("Timeout"),
    ABORTED("Aborted")
    ;

    private final String text;

    TestStatus(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
