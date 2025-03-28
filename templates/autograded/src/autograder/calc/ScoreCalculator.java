package autograder.calc;

import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import autograder.TestData;

/**
 * Calculates the score for a set of test results.
 */
public abstract class ScoreCalculator {
  protected Map<String, Integer> testCounts;
  protected double score;
  protected double totalPossibleScore;
  protected String packageName;
  protected int extraCreditTests;

  protected ScoreCalculator(double totalPossibleScore) {
    this.score = 0;
    this.totalPossibleScore = totalPossibleScore;
  }

  /**
   * Calculates the score for a package of test results.
   * 
   * @param tests the test results
   * @return the JSON representation of the test results
   */
  public abstract JSONArray parseTestResults(Set<TestData> tests);

  public abstract double getScore();

  public void setTestCounts(Map<String, Integer> testCounts) {
    this.testCounts = Map.copyOf(testCounts);
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public void setExtraCreditTests(int extraCreditTests) {
    this.extraCreditTests = extraCreditTests;
  }

  /**
   * Converts a test data object to a JSON object.
   * 
   * @param testData the test data object
   * @return the JSON object
   */
  protected JSONObject toJsonObject(TestData testData) {
    JSONObject test = new JSONObject();
    test.put("score", testData.getScore());
    test.put("max_score", testData.getMaxScore());
    test.put("name", testData.getName());
    test.put("output", testData.getOutputText().replaceAll("\t", " "));
    test.put("visibility", testData.getVisibility().getValue());
    if (!testData.getOutputText().isEmpty()) {
      test.put("status", "failed");
    }
    return test;
  }
}
