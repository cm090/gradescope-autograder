package autograder;

/**
 * Represents a single test case.
 */
public class TestData {
  private final String name;
  private final StringBuilder outputText;
  private double maxScore;
  private double score;
  private Visibility visibility;

  TestData(String name, Visibility visibility) {
    this.name = name;
    this.outputText = new StringBuilder();
    this.visibility = visibility;
  }

  void setTestVisible() {
    this.visibility = Visibility.VISIBLE;
  }

  double getMaxScore() {
    return maxScore;
  }

  void setMaxScore(double maxScore) {
    this.maxScore = maxScore;
  }

  double getScore() {
    return score;
  }

  void setScore(double score) {
    this.score = score;
  }

  String getName() {
    return name;
  }

  String getOutputText() {
    return outputText.toString();
  }

  void appendToOutput(String output) {
    this.outputText.append(output).append('\n');
  }

  Visibility getVisibility() {
    return visibility;
  }
}
