package autograder.calc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import autograder.TestData;

/**
 * Calculates test scores by dropping the lowest scores where applicable.
 */
public class DropLowestScoreCalculator extends ScoreCalculator {
  private final Map<String, Double> testWeights;
  private final Map<String, Integer> numTestsToDrop;

  public DropLowestScoreCalculator(Map<String, Double> testWeights,
      Map<String, Integer> numTestsToDrop) {
    super(0);
    this.testWeights = testWeights;
    this.numTestsToDrop = numTestsToDrop;
    if (!testWeights.keySet().equals(numTestsToDrop.keySet())) {
      throw new IllegalArgumentException(
          "Test weights and number of tests to drop must have the same keys");
    }
  }

  /**
   * @throws IllegalStateException if the package name is not set
   */
  @Override
  public JSONArray parseTestResults(Set<TestData> tests) {
    if (packageName == null) {
      throw new IllegalStateException("Package name not set");
    }

    JSONArray testResults = new JSONArray();
    double packagePoints = testWeights.get(packageName);
    int testsToDrop = numTestsToDrop.get(packageName);
    double scoreMultiplier =
        tests.size() > testsToDrop ? packagePoints / (tests.size() - testsToDrop) : 0;
    double testSum = 0;
    List<Double> testScores = new ArrayList<Double>();
    for (TestData test : tests) {
      testResults.put(toJsonObject(test));
      double currentScore = test.getScore() / test.getMaxScore();
      testSum += currentScore;
      testScores.add(currentScore);
    }
    if (!testScores.isEmpty()) {
      testScores.sort(Double::compareTo);
      testSum -= testScores.stream().limit(testsToDrop).mapToDouble(Double::doubleValue).sum();
    }
    score += testSum * scoreMultiplier;
    packageName = null;
    return testResults;
  }

  @Override
  public double getScore() {
    return score;
  }
}
