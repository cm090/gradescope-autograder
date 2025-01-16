package autograder;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Stores the test result data.
 */
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

  /**
   * Adds a test to the list of results.
   *
   * @param name the name of the test
   * @param visibility the visibility of the test
   */
  void addTest(String name, Visibility visibility) {
    testResults.put(name, new TestData(name, visibility));
  }

  /**
   * Updates an existing test with its execution results.
   *
   * @param name the name of the test
   * @param numTests the total number of tests
   * @param numFailed the number of failed tests
   */
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

  /**
   * Adds a test failure to the output.
   *
   * @param name the name of the test
   * @param output failure output to append
   */
  void addTestFailure(String name, String output) {
    TestData current = testResults.get(name);
    current.appendToOutput(
        output.replaceAll("\\(.*\\):", "").replaceAll("\n", " ").replaceAll("\\P{Print}", ""));
    current.setTestVisible();
  }

  /**
   * Converts the test results to a JSON object.
   *
   * @param percentage the percentage of the total score
   */
  void toJson(double percentage) {
    percentage /= 100.0;
    JSONObject json = new JSONObject();
    JSONArray tests = new JSONArray();

    createDownloadLink(tests);

    testResults.entrySet().forEach((entry) -> {
      buildTestResultObject(entry, tests);
      checkAlternateScoreCalculation(entry);
    });

    writeGlobalResults(json, tests, percentage);
    Configuration.instance.writeToOutput(json);
  }

  private void createDownloadLink(JSONArray tests) {
    String downloadLink = Configuration.instance.getStarterCodeDownload();
    if (downloadLink.isBlank()) {
      return;
    }

    JSONObject download = new JSONObject();
    download.put("score", 1);
    download.put("max_score", 1);
    download.put("status", "passed");
    download.put("name", "Starter code download");
    download.put("output", String.format("Visit this link: [%s](%s)", downloadLink, downloadLink));
    download.put("output_format", "md");
    download.put("visibility", Visibility.VISIBLE.getValue());
    tests.put(download);
  }

  /**
   * Creates a JSON object for a single test.
   *
   * @param entry the test name and data
   * @param tests the JSON array to add the test to
   */
  private void buildTestResultObject(Entry<String, TestData> entry, JSONArray tests) {
    JSONObject test = new JSONObject();
    test.put("score", entry.getValue().getScore());
    test.put("max_score", entry.getValue().getMaxScore());
    test.put("name", entry.getValue().getName());
    test.put("output", entry.getValue().getOutputText().replaceAll("\t", " "));
    test.put("visibility", entry.getValue().getVisibility().getValue());
    if (!entry.getValue().getOutputText().isEmpty()) {
      test.put("status", "failed");
    }
    tests.put(test);
  }

  /**
   * Checks if the score should be calculated based on the number of tests.
   *
   * @param entry the test name and data
   */
  private void checkAlternateScoreCalculation(Entry<String, TestData> entry) {
    if (!bypassScoreCalculation) {
      // Calculate score based on test weight
      String currentName = !entry.getValue().getName().contains(".") ? entry.getValue().getName()
          : entry.getValue().getName().substring(0, entry.getValue().getName().lastIndexOf("."));
      double currentWeight = testWeights.get(currentName);
      if (currentWeight < 0) {
        // Calculate score based on number of tests
        bypassScoreCalculation = true;
      }
      totalScore +=
          (entry.getValue().getScore() * currentWeight) / testCounts.getOrDefault(currentName, 1);
    }
  }

  /**
   * Writes the overall results to the JSON object.
   *
   * @param json the JSON object to write to
   * @param tests the JSON array of test results
   * @param percentage the percentage of the total score
   */
  private void writeGlobalResults(JSONObject json, JSONArray tests, double percentage) {
    json.put("score",
        bypassScoreCalculation ? percentage * Configuration.instance.getMaxScore() : totalScore);
    json.put("output", outputMessage.getValue());
    json.put("output_format", "md");
    json.put("visibility", "visible");
    json.put("tests", tests);
  }
}
