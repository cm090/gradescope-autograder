package autograder.calc;

import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import autograder.TestData;

/**
 * Calculates test scores based on the weight of each package.
 */
public class PackageWeightScoreCalculator extends ScoreCalculator {
  private final Map<String, Double> testWeights;

  public PackageWeightScoreCalculator(Map<String, Double> testWeights) {
    super(0);
    this.testWeights = testWeights;
  }

  @Override
  public JSONArray parseTestResults(Set<TestData> tests) {
    JSONArray results = new JSONArray();

    for (TestData test : tests) {
      results.put(toJsonObject(test));
      String name = test.getName();
      String packageName = name.contains(".") ? name.substring(0, name.lastIndexOf(".")) : name;
      double weight = testWeights.get(packageName);
      score += (test.getScore() * weight) / testCounts.getOrDefault(packageName, 1);
    }

    return results;
  }

  @Override
  public double getScore() {
    return score;
  }
}
