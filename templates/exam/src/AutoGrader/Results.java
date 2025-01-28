package autograder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import autograder.calc.ScoreCalculator;

/**
 * Stores the test result data.
 */
public class Results {
  static Results instance = new Results();

  private final Map<String, TestData> testResults;
  private final Map<String, Integer> testCounts;
  private OutputMessage outputMessage;

  private Results() {
    testResults = new HashMap<>();
    testCounts = new HashMap<>();
    outputMessage = OutputMessage.DEFAULT;
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
   */
  void toJson() {
    ScoreCalculator scoreCalculator = Configuration.instance.getScoreCalculator();
    scoreCalculator.setTestCounts(testCounts);
    JSONObject json = new JSONObject();
    JSONArray tests = new JSONArray();

    createDownloadLink(tests);

    Configuration.instance.getClasses().stream().map(Class::getPackageName).distinct()
        .forEach((pkg) -> {
          List<TestData> results = testResults.entrySet().stream()
              .filter(entry -> entry.getKey().startsWith(pkg)).map(Entry::getValue).toList();
          scoreCalculator.setPackageName(pkg);
          scoreCalculator.parseTestResults(Set.copyOf(results)).forEach(tests::put);
        });

    writeGlobalResults(json, tests, scoreCalculator.getScore());
    Configuration.instance.writeToOutput(json);
  }

  /**
   * Adds the starter code download link to the list of tests.
   * 
   * @param tests the JSON array of test results
   */
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
   * Writes the overall results to the JSON object.
   *
   * @param json the JSON object to write to
   * @param tests the JSON array of test results
   * @param percentage the percentage of the total score
   */
  private void writeGlobalResults(JSONObject json, JSONArray tests, double score) {
    json.put("score", score);
    json.put("output", outputMessage.getValue());
    json.put("output_format", "md");
    json.put("visibility", "visible");
    json.put("tests", tests);
  }
}
