package AutoGrader;

// TODO: Import the package with the RunAllTests file
// TODO: Open RunAllTests.java and add:
//       import AutoGrader.*;
// TODO: Build the class files by modifying in run.sh:
//       Replace [HWName] with the appropriate file structure

import [HWName].*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
/** 
 * This class is used to output a Gradescope JSON file from JUnit tests.
 * Requires implementation in test files.
 * 
 * @author Canon Maranda
 * @version 1.0
 */
import java.util.HashMap;

import org.junit.runner.JUnitCore;

public class GradescopeAutoGrader {
    private HashMap<Integer, TestData> data;
    private PrintStream output;

    public GradescopeAutoGrader() {
        this.data = new HashMap<Integer, TestData>();
        try {
            this.output = new PrintStream(new FileOutputStream("results.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void addTest(int id, String name, double maxScore) {
        this.data.put(id, new TestData(name, maxScore));
    }

    public void addResult(int id, double grade, String output) {
        this.data.get(id).setScore(grade, output);
    }

    public String toJSON() {
        String json = "{ \"tests\":[";
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
        return json;
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

    public static void main(String[] args) {
        JUnitCore runner = new JUnitCore();
        runner.run(RunAllTests.class);
    }
}
