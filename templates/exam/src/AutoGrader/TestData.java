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

  public double getMaxScore() {
    return maxScore;
  }

  void setMaxScore(double maxScore) {
    if (maxScore < 0) {
      throw new RuntimeException("Max score cannot be negative.");
    }
    this.maxScore = maxScore;
  }

  public double getScore() {
    return score;
  }

  void setScore(double score) {
    if (score < 0) {
      throw new RuntimeException("Score cannot be negative.");
    }
    if (score > maxScore) {
      throw new RuntimeException("Score cannot be greater than max score.");
    }
    this.score = score;
  }

  public String getName() {
    return name;
  }

  public String getOutputText() {
    return outputText.toString();
  }

  void appendToOutput(String output) {
    this.outputText.append(output).append('\n');
  }

  public Visibility getVisibility() {
    return visibility;
  }
}
