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
 * @version 2.0
 */
import java.util.HashMap;

import org.junit.runner.JUnitCore;

public class GradescopeAutoGrader {
    private HashMap<Integer, TestData> data;
    private HashMap<String, Integer> idList;
    private PrintStream output;
    private int nextId;
    private boolean customTotal;
    private double testsPassed, testsTotal, assignmentTotalScore;

    public GradescopeAutoGrader() {
        this.data = new HashMap<Integer, TestData>();
        this.idList = new HashMap<String, Integer>();
        this.nextId = 0;
        this.customTotal = false;
        try {
            this.output = new PrintStream(new FileOutputStream("results.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Adds a test file to the map of tests. Takes in a name and max score.
    public void addTest(String name, double maxScore) {
        idList.put(name, nextId);
        this.data.put(idList.get(name), new TestData(name, maxScore));
        nextId++;
    }

    // Adds a result to an already existing test. takes in a name, number of points, and an optional student output.
    public void addResult(String name, double grade, String output) {
        this.data.get(idList.get(name)).setScore(grade, output);
    }
    
    // Overrides default scoring to use a maximum points system. Must run after all tests have processed.
    public void useCustomTotal(double assignmentTotalScore) {
        this.customTotal = true;
        this.testsPassed = 0;
        this.testsTotal = 0;
        this.assignmentTotalScore = assignmentTotalScore;
        for (int key : this.data.keySet()) {
            TestData current = this.data.get(key);
            this.testsPassed += current.grade;
            this.testsTotal += current.maxScore;
        }
    }

    // Converts map of scores to JSON. Exports to file for Gradescope to analyze.
    public void toJSON() {
        String json = "{ ";
        json += (this.customTotal) ? "\"score\": " + ((this.testsPassed / this.testsTotal) * this.assignmentTotalScore) + ",": "";
        json += "\"tests\":[";
        for (int key : this.data.keySet()) {
            TestData current = this.data.get(key);
            json += "{\"score\": " + current.grade + ",";
            json += "\"max_score\": " + current.maxScore + ",";
            json += "\"name\": \"" + current.name + "\",";
            json += "\"number\": \"" + key + "\",";
            json += "\"output\": \"" + current.output + "\",";
            json += "\"visibility\": \"hidden\"},";
        }
        json = json.substring(0, json.length() - 1) + "]}";
        output.append(json);
        output.close();
    }

    class TestData {
        public double maxScore, grade;
        public String name, output;

        public TestData(String name, double maxScore) {
            this.name = name;
            this.maxScore = maxScore;
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
