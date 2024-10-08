package AutoGrader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import java.util.Set;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

/**
 * Given an input configuration file (config.json), run all the JUnit test in the provided
 * package(s) and output the results to results.json
 *
 * @author Canon Maranda
 * @see https://github.com/cm090/gradescope-autograder
 */
public class GradescopeAutoGrader {
    private static String downloadLink;

    private Map<Integer, TestData> data;
    private Map<String, Integer> idList;
    private Map<String, Double[]> testWeights;
    private Map<String, Integer> testsCount;
    private PrintStream output;
    private int nextId;
    private OutputMessage resultMessage;

    public GradescopeAutoGrader(Map<String, Double[]> testWeights) {
        this.data = new HashMap<Integer, TestData>();
        this.idList = new HashMap<String, Integer>();
        this.testWeights = testWeights;
        this.testsCount = new HashMap<String, Integer>();
        this.nextId = 1;
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
     * @param visibility "hidden", "after_due_date", "after_published", or "visible"
     */
    void addTest(String name, String visibility) {
        idList.put(name, nextId);
        this.data.put(idList.get(name), new TestData(name, visibility));
        nextId++;
    }

    /**
     * Adds a result to an already existing test.
     *
     * @param name The name of the test
     * @param numTests The number of tests that were run
     * @param numFailed The number of tests that failed
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
     * Logs a test failure and displays the output to the student.
     *
     * @param name The name of the test
     * @param output The output of the test
     */
    void addFailure(String name, String output) {
        TestData current = data.get(idList.get(name));
        StringBuilder sb = new StringBuilder(current.output);
        sb.append(output.replaceAll("\\(.*\\):", "").replaceAll("\n", " ").replaceAll("\\P{Print}",
                "")).append("\n");
        current.output = sb.toString();
        current.visible = "visible";
    }

    /**
     * Converts map of scores to JSON. Exports to file for Gradescope to analyze.
     */
    void toJson() {
        JSONObject json = new JSONObject();
        JSONArray tests = new JSONArray();
        double totalScore = 0;

        tests.put(createDownloadLink());
        for (String className : testWeights.keySet()) {
            double packagePoints = this.testWeights.get(className)[0];
            Set<Integer> keys =
                    this.idList.entrySet().stream().filter(e -> e.getKey().startsWith(className))
                            .map(Map.Entry::getValue).collect(Collectors.toSet());
            int testsToDrop = this.testWeights.get(className)[1].intValue();
            double scoreMultiplier = packagePoints / (keys.size() - testsToDrop);

            if (packagePoints == 0) {
                for (int key : keys) {
                    TestData current = this.data.get(key);
                    outputTest(tests, current, key);
                }
            } else if (testsToDrop <= 0) {
                // Calculate scores normally
                // If there are more tests to drop than there are tests, prevent negative scores
                int testsCount = this.testsCount.getOrDefault(className, 0);
                double testsPassed = 0;
                for (int key : keys) {
                    TestData current = this.data.get(key);
                    testsPassed += current.grade;
                    outputTest(tests, current, key);
                }
                if (testsPassed > 0) {
                    totalScore += (testsPassed / testsCount) * packagePoints;
                }
            } else {
                // Drop lowest test classes
                double testSum = 0;
                List<Double> testScores = new ArrayList<Double>();
                for (int key : keys) {
                    TestData current = this.data.get(key);
                    double currentScore = current.grade / current.maxScore;
                    testSum += currentScore;
                    testScores.add(currentScore);
                    outputTest(tests, current, key);
                }
                if (!testScores.isEmpty()) {
                    testScores.sort(Double::compareTo);
                    for (int i = 0; i < testsToDrop; i++) {
                        testSum -= testScores.get(i);
                    }
                }
                totalScore += testSum * scoreMultiplier;
            }
        }

        json.put("score", totalScore);
        json.put("output", resultMessage.getMessage());
        json.put("output_format", "md");
        json.put("visibility", "visible");
        json.put("tests", tests);

        output.append(json.toString());
        output.close();
    }

    /**
     * Creates a download link for the starter code
     * 
     * @return The JSON object for the download link
     */
    private JSONObject createDownloadLink() {
        JSONObject download = new JSONObject();
        download.put("score", 1);
        download.put("max_score", 1);
        download.put("status", "passed");
        download.put("name", "Starter code download");
        download.put("output",
                String.format("Visit this link: [%s](%s)", downloadLink, downloadLink));
        download.put("output_format", "md");
        download.put("visibility", "visible");
        return download;
    }

    /**
     * Outputs a test to the JSON file
     * 
     * @param tests The array to add the test to
     * @param current The current test
     * @param key The key of the current test in the map of tests
     */
    private void outputTest(JSONArray tests, TestData current, Integer key) {
        JSONObject test = new JSONObject();
        test.put("score", current.grade);
        test.put("max_score", current.maxScore);
        test.put("name", current.name);
        test.put("number", key.toString());
        test.put("output", current.output.replaceAll("\t", " "));
        if (current.output.length() > 0) {
            test.put("status", "failed");
        }
        test.put("visibility", current.visible);
        tests.put(test);
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
        DEFAULT("# Your submission has been successfully graded \nYour estimated grade is shown to the right under *\"Autograder Score\"*"), TEST_RUNNER_FAILED(
                "# ERROR: Grading Failed \n**There was a problem with your code** that caused some tests to unexpectedly fail. Please see the output below and resubmit. Contact an instructor or TA for more help.");

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

            // Read the config file for additional options
            TestRunner.testTimeoutSeconds =
                    config.getJSONObject("additional_options").getInt("timeout_seconds");
            downloadLink =
                    config.getJSONObject("additional_options").getString("starter_code_download");
            String testVisibility =
                    config.getJSONObject("additional_options").getString("test_visibility");

            // Parse the classes in the config file
            HashSet<Class<?>> allClasses = new HashSet<Class<?>>();
            JSONArray classes = config.getJSONArray("classes");
            Map<String, Double[]> testWeights = new HashMap<String, Double[]>();
            for (int i = 0; i < classes.length(); i++) {
                JSONObject currentClass = classes.getJSONObject(i);
                String className = currentClass.getString("name");
                allClasses.addAll(ClassFinder.find(className));
                testWeights.put(className, new Double[] {currentClass.getDouble("weight"),
                        (double) currentClass.getInt("drop_lowest")});
            }

            // Run the tests
            GradescopeAutoGrader g = new GradescopeAutoGrader(testWeights);
            HashSet<TestRunner> runners = new HashSet<TestRunner>();
            for (Class<?> c : allClasses) {
                if (!(c.toString().contains("RunAllTests")
                        || c.toString().contains("TestRunner"))) {
                    try {
                        TestRunner runner = new TestRunner(c, g, testVisibility);
                        runners.add(runner);
                    } catch (NoClassDefFoundError e) {
                        continue;
                    }
                }
            }
            for (TestRunner t : runners) {
                t.run(new RunNotifier());
            }
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
