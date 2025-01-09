package newAutograder;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Configuration {
    private static final Set<String> excludedClasses = Set.of("RunAllTests", "TestRunner");
    private static final String OUTPUT_FILE = "results.json";
    static Configuration instance = new Configuration();

    private JSONObject configObject;
    private JSONObject metadataObject;
    private Double maxScore;
    private int extraCreditTests;
    private int testTimeoutSeconds;
    private Visibility testVisibility;
    private Set<Class<?>> classes;
    private Map<String, Double> testWeights;

    private Configuration() {
        classes = new HashSet<>();
        testWeights = new HashMap<>();
    }

    static void build(JSONObject configObject, JSONObject metadataObject) {
        instance.configObject = configObject;
        instance.metadataObject = metadataObject;
        instance.parseMaxScore();
        instance.parseExtraCreditTests();
        instance.parseTestTimeoutSeconds();
        instance.parseTestVisibility();
        instance.parseClasses();
    }

    private void parseMaxScore() {
        JSONArray outline = metadataObject.getJSONObject("assignment").getJSONArray("outline");
        for (Object question : outline) {
            JSONObject questionObject = (JSONObject) question;
            if (questionObject.getString("type").equals("ProgrammingQuestion")) {
                maxScore = questionObject.getDouble("weight");
                break;
            }
        }
        if (maxScore == null) {
            throw new RuntimeException("No programming question found in metadata file.");
        }
    }

    private void parseExtraCreditTests() {
        try {
            extraCreditTests =
                    configObject.getJSONObject("additional_options").getInt("extra_credit_amount");
        } catch (JSONException e) {
            extraCreditTests = 0;
        }
    }

    private void parseTestTimeoutSeconds() {
        try {
            testTimeoutSeconds =
                    configObject.getJSONObject("additional_options").getInt("timeout_seconds");
        } catch (JSONException e) {
            testTimeoutSeconds = 30;
        }
    }

    private void parseTestVisibility() {
        String visibility;
        try {
            visibility =
                    configObject.getJSONObject("additional_options").getString("test_visibility");
        } catch (JSONException e) {
            visibility = "visible";
        }
        testVisibility = Visibility.get(visibility.toLowerCase());
    }

    private void parseClasses() {
        configObject.getJSONArray("classes").forEach((cls) -> {
            JSONObject classObject = (JSONObject) cls;
            classes.addAll(ClassFinder.find(classObject.getString("name")));
            testWeights.put(classObject.getString("name"), classObject.getDouble("weight"));
        });
    }

    void writeToOutput(JSONObject json) {
        try (PrintStream ps = new PrintStream(new FileOutputStream(OUTPUT_FILE))) {
            ps.append(json.toString());
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Could not write to output file.");
        }
    }

    double getMaxScore() {
        return maxScore;
    }

    int getExtraCreditTests() {
        return extraCreditTests;
    }

    int getTestTimeoutSeconds() {
        return testTimeoutSeconds;
    }

    Visibility getTestVisibility() {
        return testVisibility;
    }

    Set<Class<?>> getClasses() {
        return Set.copyOf(classes);
    }

    Map<String, Double> getTestWeights() {
        return Map.copyOf(testWeights);
    }

    Set<String> getExcludedClasses() {
        return Set.copyOf(excludedClasses);
    }
}
