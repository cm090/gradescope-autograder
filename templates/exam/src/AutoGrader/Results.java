package autograder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
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
  private double totalScore;

  private Results() {
    testResults = new HashMap<>();
    testWeights = Configuration.instance.getTestWeights();
    testCounts = new HashMap<>();
    outputMessage = OutputMessage.DEFAULT;
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

  void toJson() {
    JSONObject json = new JSONObject();
    JSONArray tests = new JSONArray();
    totalScore = 0;

    createDownloadLink(tests);
    for (Entry<String, Double> entry : testWeights.entrySet()) {
      int testsToDrop = Configuration.instance.getNumTestsToDrop().get(entry.getKey());
      Set<Entry<String, TestData>> testData = testResults.entrySet().stream()
          .filter(e -> e.getKey().startsWith(entry.getKey())).collect(Collectors.toSet());
      double scoreMultiplier = entry.getValue() / (testData.size() - testsToDrop);

      if (entry.getValue() == 0) {
        testData.forEach(test -> buildTestResultObject(test, tests));
      } else if (testsToDrop <= 0) {
        // Calculate scores normally
        // If there are more tests to drop than there are tests, prevent negative scores
        int testsCount = testCounts.getOrDefault(entry.getKey(), 0);
        double testsPassed = testData.stream().mapToDouble(test -> {
          buildTestResultObject(test, tests);
          return test.getValue().getScore();
        }).sum();
        if (testsPassed > 0) {
          totalScore += (testsPassed / testsCount) * Configuration.instance.getMaxScore();
        }
      } else {
        // Drop lowest test classes
        List<Double> testScores = new ArrayList<Double>();
        double testSum = testData.stream().mapToDouble(test -> {
          buildTestResultObject(test, tests);
          double currentScore = test.getValue().getScore() / test.getValue().getMaxScore();
          testScores.add(currentScore);
          return currentScore;
        }).sum();
        if (!testScores.isEmpty()) {
          testScores.sort(Double::compareTo);
          for (int i = 0; i < testsToDrop; i++) {
            testSum -= testScores.get(i);
          }
        }
        totalScore += testSum * scoreMultiplier;
      }
    }

    writeGlobalResults(json, tests);
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
   * Writes the overall results to the JSON object.
   *
   * @param json the JSON object to write to
   * @param tests the JSON array of test results
   */
  private void writeGlobalResults(JSONObject json, JSONArray tests) {
    json.put("score", totalScore);
    json.put("output", outputMessage.getValue());
    json.put("output_format", "md");
    json.put("visibility", Visibility.VISIBLE.getValue());
    json.put("tests", tests);
  }
}
