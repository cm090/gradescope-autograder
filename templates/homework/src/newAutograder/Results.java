package newAutograder;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONObject;

public class Results {
    static Results instance = new Results();

    private Map<String, TestData> testResults;
    private Map<String, Double> testWeights;
    private Map<String, Integer> testCounts;
    private OutputMessage outputMessage;
    private boolean bypassScoreCalculation;
    private double totalScore;

    private Results() {
        testResults = new HashMap<>();
        testCounts = new HashMap<>();
        outputMessage = OutputMessage.DEFAULT;
        bypassScoreCalculation = false;
        totalScore = 0.0;
    }

    void addTest(String name, Visibility visibility) {
        testResults.put(name, new TestData(name, visibility));
    }

    void addTestResult(String name, int numTests, int numFailed) {
        TestData current = testResults.get(name);
        current.setMaxScore(numTests);
        current.setScore(numTests - numFailed);
        if (numTests == 0) {
            outputMessage = OutputMessage.TEST_RUNNER_FAILED;
        }
        String packageName =
                name.indexOf(".") == -1 ? name : name.substring(0, name.lastIndexOf("."));
        testCounts.put(packageName, testCounts.getOrDefault(packageName, 0) + numTests);
    }

    void addTestFailure(String name, String output) {
        TestData current = testResults.get(name);
        StringBuilder sb = new StringBuilder(current.getOutputText());
        sb.append(output.replaceAll("\\(.*\\):", "").replaceAll("\n", " ").replaceAll("\\P{Print}",
                "")).append("\n");
        current.setOutputText(sb.toString());
        current.setVisibility(Visibility.VISIBLE);
    }

    void toJson(double percentage) {
        percentage /= 100.0;
        JSONObject json = new JSONObject();
        JSONArray tests = new JSONArray();

        testResults.entrySet().forEach((entry) -> {
            buildTestResultObject(entry, tests);
            checkAlternateScoreCalculation(entry);
        });

        writeGlobalResults(json, tests, percentage);
        Configuration.instance.writeToOutput(json);
    }

    private void buildTestResultObject(Entry<String, TestData> entry, JSONArray tests) {
        JSONObject test = new JSONObject();
        test.put("score", entry.getValue().getScore());
        test.put("max_score", entry.getValue().getMaxScore());
        test.put("name", entry.getValue().getName());
        test.put("number", entry.getKey().toString());
        test.put("output", entry.getValue().getOutputText().replaceAll("\t", " "));
        test.put("visibility", entry.getValue().getVisibility().getValue());
        if (entry.getValue().getOutputText().length() > 0) {
            test.put("status", "failed");
        }
        tests.put(test);
    }

    private void checkAlternateScoreCalculation(Entry<String, TestData> entry) {
        if (!bypassScoreCalculation) {
            // Calculate score based on test weight
            String currentName =
                    entry.getValue().getName().indexOf(".") == -1 ? entry.getValue().getName()
                            : entry.getValue().getName().substring(0,
                                    entry.getValue().getName().lastIndexOf("."));
            double currentWeight = testWeights.get(currentName);
            if (currentWeight < 0) {
                // Calculate score based on number of tests
                bypassScoreCalculation = true;
            }
            totalScore += (entry.getValue().getScore() * currentWeight)
                    / testCounts.getOrDefault(currentName, 1);
        }
    }

    private void writeGlobalResults(JSONObject json, JSONArray tests, double percentage) {
        json.put("score", bypassScoreCalculation ? percentage * Configuration.instance.getMaxScore()
                : totalScore);
        json.put("output", outputMessage.getValue());
        json.put("output_format", "md");
        json.put("visibility", "visible");
        json.put("tests", tests);
    }
}
