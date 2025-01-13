package newAutograder;

public class TestData {
  private final String name;
  private double maxScore;
  private double score;
  private String outputText;
  private Visibility visibility;

  TestData(String name, Visibility visibility) {
    this.name = name;
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
    return outputText;
  }

  void setOutputText(String outputText) {
    this.outputText = outputText;
  }

  Visibility getVisibility() {
    return visibility;
  }
}
