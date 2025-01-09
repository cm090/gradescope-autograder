package newAutograder;

import java.util.Map;

public class Results {
    private double totalScore;
    private Map<String, TestData> testResults;
    private Map<String, Double> testWeights;
    private Map<String, Integer> testCounts;
    private OutputMessage outputMessage;

    Results() {}

    void addTest(String name, Visibility visibility) {}

    void addTestResult(String name, int numTests, int numFailed) {}

    void addTestFailure(String name, String output) {}
}
