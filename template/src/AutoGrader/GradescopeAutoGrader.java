package AutoGrader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

/**
 * This class is used to output a Gradescope JSON file from JUnit tests.
 * Requires implementation in test files.
 *
 * https://github.com/cm090/gradescope-autograder
 * 
 * @author Canon Maranda
 * @version 3.2
 */
public class GradescopeAutoGrader {
    private HashMap<Integer, TestData> data;
    private HashMap<String, Integer> idList;
    private PrintStream output;
    private int nextId;
    private double assignmentTotalScore;

    public GradescopeAutoGrader(double assignmentTotalScore) {
        this.data = new HashMap<Integer, TestData>();
        this.idList = new HashMap<String, Integer>();
        this.nextId = 1;
        this.assignmentTotalScore = assignmentTotalScore;
        try {
            this.output = new PrintStream(new FileOutputStream("results.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a test file to the map of tests.
     * 
     * @param name       The name of the test
     * @param maxScore   The maximum score a student can get on the test
     * @param visibility "hidden", "after_due_date", "after_published", or "visible"
     */
    public void addTest(String name, double maxScore, String visibility) {
        idList.put(name, nextId);
        this.data.put(idList.get(name), new TestData(name, maxScore, visibility));
        nextId++;
    }

    /**
     * Adds a result to an already existing test. takes in a name and number of
     * points.
     * 
     * @param name  The name of the test
     * @param grade The grade the student received on the test
     */
    public void addResult(String name, double grade) {
        TestData current = this.data.get(idList.get(name));
        current.setScore(grade);
    }

    /**
     * This function takes in a name and output, and adds the output to that of the
     * test with the given name
     * 
     * @param name   The name of the test
     * @param output The output of the test
     */
    public void addFailure(String name, String output) {
        TestData current = this.data.get(idList.get(name));
        current.output += output + "\\n";
        current.visible = "visible";
    }

    /**
     * Converts map of scores to JSON. Exports to file for Gradescope to analyze.
     * 
     * @param percentage The percentage of the assignment that the student has
     *                   completed.
     */
    public void toJSON(double percentage) {
        percentage /= 100.0;
        StringBuilder json = new StringBuilder("{ ");
        json.append("\"score\": ").append(percentage * this.assignmentTotalScore).append(",")
                .append("\"tests\":[");
        for (int key : this.data.keySet()) {
            TestData current = this.data.get(key);
            json.append(String.format(
                    "{\"score\": %f, \"max_score\": %f, \"name\": \"%s\", \"number\": \"%d\", \"output\": \"%s\", %s \"visibility\": \"%s\"},",
                    current.grade, current.maxScore, current.name, key, current.output,
                    (current.output.length() > 0) ? "\"status\": \"failed\"," : "", current.visible));
        }
        json.setLength(json.length() - 1);
        json.append("]}");
        output.append(json.toString());
        output.close();
    }

    /**
     * Stores information about each test
     */
    class TestData {
        public double maxScore, grade;
        public String name, output, visible;

        public TestData(String name, double maxScore, String visibility) {
            this.name = name;
            this.maxScore = maxScore;
            this.visible = visibility;
            this.output = "";
        }

        public void setScore(double grade) {
            this.grade = grade;
        }
    }

    /**
     * Takes in a list of packages, finds all the classes in those packages, and
     * runs all the tests in those classes
     */
    public static void main(String[] args) throws InitializationError {
        try {
            if (args.length < 2)
                throw new IndexOutOfBoundsException();
            List<Class<?>> classes = new ArrayList<Class<?>>();
            GradescopeAutoGrader g = new GradescopeAutoGrader(Integer.parseInt(args[0]));
            for (int i = 1; i < args.length; i++)
                classes.addAll(ClassFinder.find(args[i]));
            HashSet<TestRunner> runners = new HashSet<TestRunner>();
            for (Class<?> c : classes) {
                if (!c.toString().contains("RunAllTests"))
                    // If you want to change the visibility of tests, add a String argument to the
                    // below TestRunner constructor. Supported inputs: hidden, after_due_date,
                    // after_published, visible
                    runners.add(new TestRunner(c, g));
            }
            for (TestRunner t : runners) {
                t.run(new RunNotifier());
            }
        } catch (IndexOutOfBoundsException e) {
            System.err.println(
                    "Incorrect command line arguments\nUsage: java -cp bin/:lib/* AutoGrader.GradescopeAutoGrader [maxScore] [testPackages]");
            System.exit(1);
        }
    }

    // Below code found at https://stackoverflow.com/a/15519745
    private static class ClassFinder {
        private static final char PKG_SEPARATOR = '.';
        private static final char DIR_SEPARATOR = '/';
        private static final String CLASS_FILE_SUFFIX = ".class";
        private static final String BAD_PACKAGE_ERROR = "Unable to get resources from path '%s'. Are you sure the package '%s' exists?";

        public static List<Class<?>> find(String scannedPackage) {
            String scannedPath = scannedPackage.replace(PKG_SEPARATOR, DIR_SEPARATOR);
            URL scannedUrl = Thread.currentThread().getContextClassLoader().getResource(scannedPath);
            if (scannedUrl == null) {
                throw new IllegalArgumentException(String.format(BAD_PACKAGE_ERROR, scannedPath, scannedPackage));
            }
            File scannedDir = new File(scannedUrl.getFile());
            List<Class<?>> classes = new ArrayList<Class<?>>();
            for (File file : scannedDir.listFiles()) {
                classes.addAll(find(file, scannedPackage));
            }
            return classes;
        }

        private static List<Class<?>> find(File file, String scannedPackage) {
            List<Class<?>> classes = new ArrayList<Class<?>>();
            String resource = scannedPackage + PKG_SEPARATOR + file.getName();
            if (file.isDirectory()) {
                for (File child : file.listFiles()) {
                    classes.addAll(find(child, resource));
                }
            } else if (resource.endsWith(CLASS_FILE_SUFFIX)) {
                int endIndex = resource.length() - CLASS_FILE_SUFFIX.length();
                String className = resource.substring(0, endIndex);
                try {
                    classes.add(Class.forName(className));
                } catch (ClassNotFoundException ignore) {
                }
            }
            return classes;
        }
    }
}