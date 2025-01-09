package newAutograder;

public enum OutputMessage {
    DEFAULT("# Your submission has been successfully graded \nYour estimated grade is shown to the right under *\"Autograder Score\"*"), TEST_RUNNER_FAILED(
            "# ERROR: Grading Failed \n**There was a problem with your code** that caused some tests to unexpectedly fail. Please see the output below and resubmit. Contact an instructor or TA for more help.");

    private String message;

    OutputMessage(String message) {
        this.message = message;
    }

    String getValue() {
        return message;
    }
}
