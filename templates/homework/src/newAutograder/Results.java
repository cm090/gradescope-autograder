package newAutograder;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONObject;

public class Results {
  static Results instance = new Results();

  private final Map<String, TestData> testResults;
  private final Map<String, Double> testWeights;
  private final Map<String, Integer> testCounts;
  private OutputMessage outputMessage;
  private boolean bypassScoreCalculation;
  private double totalScore;

  private Results() {
    testResults = new HashMap<>();
    testWeights = Configuration.instance.getTestWeights();
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
    String packageName = !name.contains(".") ? name : name.substring(0, name.lastIndexOf("."));
    testCounts.put(packageName, testCounts.getOrDefault(packageName, 0) + numTests);
  }

  void addTestFailure(String name, String output) {
    TestData current = testResults.get(name);
    String sb = current.getOutputText() +
        output.replaceAll("\\(.*\\):", "").replaceAll("\n", " ").replaceAll("\\P{Print}", "") +
        "\n";
    current.setOutputText(sb);
    current.setTestVisible();
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
    test.put("number", entry.getKey());
    test.put("output", entry.getValue().getOutputText().replaceAll("\t", " "));
    test.put("visibility", entry.getValue().getVisibility().getValue());
    if (!entry.getValue().getOutputText().isEmpty()) {
      test.put("status", "failed");
    }
    tests.put(test);
  }

  private void checkAlternateScoreCalculation(Entry<String, TestData> entry) {
    if (!bypassScoreCalculation) {
      // Calculate score based on test weight
      String currentName = !entry.getValue().getName().contains(".") ? entry.getValue().getName() :
          entry.getValue().getName().substring(0, entry.getValue().getName().lastIndexOf("."));
      double currentWeight = testWeights.get(currentName);
      if (currentWeight < 0) {
        // Calculate score based on number of tests
        bypassScoreCalculation = true;
      }
      totalScore +=
          (entry.getValue().getScore() * currentWeight) / testCounts.getOrDefault(currentName, 1);
    }
  }

  private void writeGlobalResults(JSONObject json, JSONArray tests, double percentage) {
    json.put("score",
        bypassScoreCalculation ? percentage * Configuration.instance.getMaxScore() : totalScore);
    json.put("output", outputMessage.getValue());
    json.put("output_format", "md");
    json.put("visibility", "visible");
    json.put("tests", tests);
  }
}
