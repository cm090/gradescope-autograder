package autograder.calc;

import java.util.Set;
import org.json.JSONArray;
import autograder.TestData;

/**
 * Calculates test scores based on the number of tests run and passed.
 */
public class TestCountScoreCalculator extends ScoreCalculator {
  private double testsRun;
  private double testsPassed;

  public TestCountScoreCalculator() {
    super();
    testsRun = 0;
    testsPassed = 0;
  }

  @Override
  public JSONArray parseTestResults(Set<TestData> tests) {
    JSONArray results = new JSONArray();

    for (TestData test : tests) {
      results.put(toJsonObject(test));
      testsPassed += test.getScore();
      testsRun += test.getMaxScore();
    }

    return results;
  }

  @Override
  public double getScore() {
    score = testsRun == 0 ? 0 : testsPassed / testsRun;
    return score;
  }
}
