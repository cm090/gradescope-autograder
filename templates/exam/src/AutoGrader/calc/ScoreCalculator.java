package autograder.calc;

import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import autograder.TestData;

public abstract class ScoreCalculator {
  protected Map<String, Integer> testCounts;
  protected double score;
  protected String packageName;

  protected ScoreCalculator() {
    this.score = 0;
  }

  public abstract JSONArray parseTestResults(Set<TestData> tests);

  public abstract double getScore();

  public void setTestCounts(Map<String, Integer> testCounts) {
    this.testCounts = testCounts;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

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
