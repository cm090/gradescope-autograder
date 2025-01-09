package newAutograder;

public class TestData {
    private double maxScore;
    private double score;
    private String name;
    private String outputText;
    private Visibility visibility;

    TestData(String name, Visibility visibility) {
        this.name = name;
        this.visibility = visibility;
    }

    void setMaxScore(double maxScore) {
        this.maxScore = maxScore;
    }

    void setScore(double score) {
        this.score = score;
    }

    void setOutputText(String outputText) {
        this.outputText = outputText;
    }

    void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    double getMaxScore() {
        return maxScore;
    }

    double getScore() {
        return score;
    }

    String getName() {
        return name;
    }

    String getOutputText() {
        return outputText;
    }

    Visibility getVisibility() {
        return visibility;
    }
}
