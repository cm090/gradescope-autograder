package AutoGrader;

import [HWName].*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
/** 
 * This class is used to output a Gradescope JSON file from JUnit tests.
 * Requires implementation in test files.
 *
 * https://github.com/cm090/gradescope-autograder
 * 
 * @author Canon Maranda
 * @version 2.5
 */
import java.util.HashMap;

import org.junit.runner.JUnitCore;

public class GradescopeAutoGrader {
    private HashMap<Integer, TestData> data;
    private HashMap<String, Integer> idList;
    private PrintStream output;
    private int nextId, numberOfTests;
    private double testsPassed, assignmentTotalScore;

    public GradescopeAutoGrader(int numberOfTests, double assignmentTotalScore) {
        this.data = new HashMap<Integer, TestData>();
        this.idList = new HashMap<String, Integer>();
        this.nextId = 0;
        this.testsPassed = 0;
        this.numberOfTests = numberOfTests;
        this.assignmentTotalScore = assignmentTotalScore;
        try {
            this.output = new PrintStream(new FileOutputStream("results.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Adds a test file to the map of tests. Takes in a name and max score. Optional third argument sets student visibility (hidden, after_due_date, after_published, visible).
    public void addTest(String name, double maxScore, String visibility) {
        idList.put(name, nextId);
        this.data.put(idList.get(name), new TestData(name, maxScore, visibility));
        nextId++;
    }
    // Visibility defaults to "after_due_date" if not specified
    public void addTest(String name, double maxScore) {
        this.addTest(name, maxScore, "after_due_date");
    }

    // Adds a result to an already existing test. takes in a name and number of points.
    public void addResult(String name, double grade) {
        this.testsPassed += grade;
        TestData current = this.data.get(idList.get(name));
        String output = (current.maxScore == 0) ? "There was an error running this test. Try fixing your " + name.replace("Test", "") + " method and try again." : "";
        current.visible = (output.length() > 0) ? "visible" : current.visible;
        current.setScore(grade, output);
    }

    // Converts map of scores to JSON. Exports to file for Gradescope to analyze.
    public void toJSON() {
        String json = "{ ";
        json += "\"score\": " + ((this.testsPassed / this.numberOfTests) * this.assignmentTotalScore) + ",";
        json += "\"tests\":[";
        for (int key : this.data.keySet()) {
            TestData current = this.data.get(key);
            json += "{\"score\": " + current.grade + ",";
            json += "\"max_score\": " + current.maxScore + ",";
            json += "\"name\": \"" + current.name + "\",";
            json += "\"number\": \"" + key + "\",";
            json += "\"output\": \"" + current.output + "\",";
            json += (current.output.length() > 0) ? "\"status\": \"failed\"," : "";
            json += "\"visibility\": \"" + current.visible + "\"},";
        }
        json = json.substring(0, json.length() - 1) + "]}";
        output.append(json);
        output.close();
    }

    class TestData {
        public double maxScore, grade;
        public String name, output, visible;

        public TestData(String name, double maxScore, String visibility) {
            this.name = name;
            this.maxScore = maxScore;
            this.visible = visibility;
        }

        public void setScore(double grade, String output) {
            this.grade = grade;
            this.output = output;
        }
    }

    // Runs all provided JUnit tests
    public static void main(String[] args) {
        JUnitCore runner = new JUnitCore();
        runner.run(RunAllTests.class);
    }
}