package AutoGrader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

/**
 * Given an input configuration file (config.json), run all the JUnit test in the provided
 * package(s) and output the results to results.json
 *
 * @author Canon Maranda
 * @version 5.0
 * @see https://github.com/cm090/gradescope-autograder
 */
public class GradescopeAutoGrader {
    private Map<Integer, TestData> data;
    private Map<String, Integer> idList;
    private Map<String, Double> testWeights;
    private Map<String, Integer> testsCount;
    private PrintStream output;
    private int nextId;
    private double assignmentTotalScore;
    private OutputMessage resultMessage;

    public GradescopeAutoGrader(double assignmentTotalScore, Map<String, Double> testWeights) {
        this.data = new HashMap<Integer, TestData>();
        this.idList = new HashMap<String, Integer>();
        this.testWeights = testWeights;
        this.testsCount = new HashMap<String, Integer>();
        this.nextId = 1;
        this.assignmentTotalScore = assignmentTotalScore;
        this.resultMessage = OutputMessage.DEFAULT;
        try {
            this.output = new PrintStream(new FileOutputStream("results.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a test file to the map of tests.
     *
     * @param name The name of the test
     * @param maxScore The maximum score a student can get on the test
     * @param visibility "hidden", "after_due_date", "after_published", or "visible"
     */
    void addTest(String name, String visibility) {
        idList.put(name, nextId);
        this.data.put(idList.get(name), new TestData(name, visibility));
        nextId++;
    }

    /**
     * Adds a result to an already existing test. takes in a name and number of points.
     *
     * @param name The name of the test
     * @param grade The grade the student received on the test
     */
    void addResult(String name, int numTests, int numFailed) {
        TestData current = this.data.get(idList.get(name));
        current.maxScore = numTests;
        current.grade = numTests - numFailed;
        if (numTests == 0) {
            resultMessage = OutputMessage.TEST_RUNNER_FAILED;
        }
        String packageName =
                name.indexOf(".") == -1 ? name : name.substring(0, name.lastIndexOf("."));
        this.testsCount.put(packageName, testsCount.getOrDefault(packageName, 0) + numTests);
    }

    /**
     * This function takes in a name and output, and adds the output to that of the test with the
     * given name
     *
     * @param name The name of the test
     * @param output The output of the test
     */
    void addFailure(String name, String output) {
        TestData current = data.get(idList.get(name));
        StringBuilder sb = new StringBuilder(current.output);
        sb.append(output.replaceAll("\\(.*\\):", "")).append("\\n");
        current.output = sb.toString();
        current.visible = "visible";
    }

    /**
     * Converts map of scores to JSON. Exports to file for Gradescope to analyze.
     *
     * @param percentage The percentage of the assignment that the student has completed.
     */
    void toJSON(double percentage) {
        percentage /= 100.0;
        StringJoiner tests = new StringJoiner(",");
        double totalScore = 0.0;
        boolean bypassScoreCalculation = false;

        for (int key : this.data.keySet()) {
            TestData current = this.data.get(key);
            tests.add(String.format(
                    "{\"score\": %f, \"max_score\": %f, \"name\": \"%s\", \"number\": \"%d\", \"output\": \"%s\", %s \"visibility\": \"%s\"}",
                    current.grade, current.maxScore, current.name, key,
                    current.output.replace("\n", " ").replace("\t", " "),
                    (current.output.length() > 0) ? "\"status\": \"failed\"," : "",
                    current.visible));
            if (!bypassScoreCalculation) {
                // Calculate score based on test weight
                String currentName = current.name.indexOf(".") == -1 ? current.name
                        : current.name.substring(0, current.name.lastIndexOf("."));
                double currentWeight = testWeights.get(currentName);
                if (currentWeight < 0) {
                    // Calculate score based on number of tests
                    bypassScoreCalculation = true;
                }
                totalScore += (current.grade / current.maxScore) * (currentWeight * current.maxScore
                        / this.testsCount.getOrDefault(currentName, 1));
            }
        }

        String json = String.format(
                "{ \"score\": %.2f, \"output\": \"%s\", \"visibility\": \"visible\", \"tests\":[%s]}",
                bypassScoreCalculation ? percentage * this.assignmentTotalScore : totalScore,
                resultMessage.getMessage(), tests);
        output.append(json);
        output.close();
    }

    /**
     * Stores information about each test, including its name, visibility, output, maximum score,
     * and grade
     */
    private class TestData {
        private double maxScore, grade;
        private String name, output, visible;

        private TestData(String name, String visibility) {
            this.name = name;
            this.maxScore = 0;
            this.grade = 0;
            this.visible = visibility;
            this.output = "";
        }
    }

    /**
     * Stores success and failure output messages
     */
    private enum OutputMessage {
        DEFAULT("Your submission has been successfully graded."), TEST_RUNNER_FAILED(
                "There was a problem with your code that caused some tests to unexpectedly fail. Please see the output below and resubmit.");

        private String message;

        OutputMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Takes in a configuration file and prepares the grading process
     *
     * @param args The location of the configuration file
     */
    public static void main(String[] args) throws InitializationError {
        try {
            // Check if config file was specified and convert to JSON object
            if (args.length == 0) {
                throw new IndexOutOfBoundsException(
                        "Configuration file location must be specified.");
            }
            JSONObject config = new JSONObject(new String(Files.readAllBytes(Paths.get(args[0]))));

            // Read the Gradescope submission metadata and get the maximum score
            BufferedReader reader =
                    new BufferedReader(new FileReader("../submission_metadata.json"));
            String submissionData = reader.readLine();
            Pattern pattern = Pattern.compile(
                    "\"type\":\\s*\"ProgrammingQuestion\",\\s*\"title\":\\s*\"Autograder\".*?\"weight\":\\s*\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(submissionData);
            double score = matcher.find() ? Double.parseDouble(matcher.group(1)) : 0.0;

            // Read the config file for additional options
            TestRunner.extraCreditTests =
                    config.getJSONObject("additional_options").getInt("extra_credit_amount");
            TestRunner.testTimeoutSeconds =
                    config.getJSONObject("additional_options").getInt("timeout_seconds");

            // Parse the classes in the config file
            HashSet<Class<?>> allClasses = new HashSet<Class<?>>();
            JSONArray classes = config.getJSONArray("classes");
            Map<String, Double> testWeights = new HashMap<>();
            for (int i = 0; i < classes.length(); i++) {
                allClasses.addAll(ClassFinder.find(classes.getJSONObject(i).getString("name")));
                testWeights.put(classes.getJSONObject(i).getString("name"),
                        classes.getJSONObject(i).getDouble("weight"));
            }

            // Run the tests
            GradescopeAutoGrader g = new GradescopeAutoGrader(score, testWeights);
            HashSet<TestRunner> runners = new HashSet<TestRunner>();
            String testVisibility =
                    config.getJSONObject("additional_options").getString("test_visibility");
            for (Class<?> c : allClasses) {
                if (!c.toString().contains("RunAllTests")) {
                    runners.add(new TestRunner(c, g, testVisibility));
                }
            }
            for (TestRunner t : runners) {
                t.run(new RunNotifier());
            }
            reader.close();
        } catch (IndexOutOfBoundsException | IOException e) {
            e.printStackTrace();
        }
    }

    // From https://stackoverflow.com/a/15519745
    private static class ClassFinder {
        private static final char PKG_SEPARATOR = '.';
        private static final char DIR_SEPARATOR = '/';
        private static final String CLASS_FILE_SUFFIX = ".class";
        private static final String BAD_PACKAGE_ERROR =
                "Unable to get resources from path '%s'. Are you sure the package '%s' exists?";

        private static List<Class<?>> find(String scannedPackage) {
            String scannedPath = scannedPackage.replace(PKG_SEPARATOR, DIR_SEPARATOR);
            URL scannedUrl =
                    Thread.currentThread().getContextClassLoader().getResource(scannedPath);
            if (scannedUrl == null) {
                throw new IllegalArgumentException(
                        String.format(BAD_PACKAGE_ERROR, scannedPath, scannedPackage));
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
