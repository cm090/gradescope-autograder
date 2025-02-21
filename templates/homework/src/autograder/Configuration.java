package autograder;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Stores the input configuration data.
 */
public class Configuration {
  private static final Set<String> EXCLUDED_CLASSES = Set.of("RunAllTests", "TestRunner");
  private static final String OUTPUT_FILE = "results.json";
  static Configuration instance = new Configuration();

  private final Set<Class<?>> classes;
  private final Map<String, Double> testWeights;
  private JSONObject configObject;
  private JSONObject metadataObject;
  private Double maxScore;
  private int extraCreditTests;
  private int testTimeoutSeconds;
  private Visibility testVisibility;

  private Configuration() {
    classes = new HashSet<>();
    testWeights = new HashMap<>();
  }

  /**
   * Parse the configuration and metadata objects.
   *
   * @param configObject the configuration object
   * @param metadataObject the metadata object
   */
  static void build(JSONObject configObject, JSONObject metadataObject) {
    instance.configObject = configObject;
    instance.metadataObject = metadataObject;
    instance.parseMaxScore();
    instance.parseExtraCreditTests();
    instance.parseTestTimeoutSeconds();
    instance.parseTestVisibility();
    instance.parseClasses();
  }

  /**
   * Look for the weight of the programming question in the metadata file.
   *
   * @throws RuntimeException if no programming question is found
   */
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

  /**
   * Look for the number of extra credit tests in the configuration file. If not found, use the
   * default value.
   */
  private void parseExtraCreditTests() {
    try {
      extraCreditTests =
          configObject.getJSONObject("additional_options").getInt("extra_credit_amount");
    } catch (JSONException e) {
      extraCreditTests = 0;
    }
  }

  /**
   * Look for the test timeout in the configuration file. If not found, use the default value.
   */
  private void parseTestTimeoutSeconds() {
    try {
      testTimeoutSeconds =
          configObject.getJSONObject("additional_options").getInt("timeout_seconds");
    } catch (JSONException e) {
      testTimeoutSeconds = 30;
    }
  }

  /**
   * Look for the test visibility in the configuration file. If not found, use the default value.
   */
  private void parseTestVisibility() {
    String visibility;
    try {
      visibility = configObject.getJSONObject("additional_options").getString("test_visibility");
    } catch (JSONException e) {
      visibility = "visible";
    }
    testVisibility = Visibility.getVisibility(visibility.toLowerCase());
  }

  /**
   * Given a list of classes in the configuration file, find the corresponding Class objects and
   * update the test weights.
   *
   * @see ClassFinder#find(String)
   */
  private void parseClasses() {
    configObject.getJSONArray("classes").forEach((cls) -> {
      JSONObject classObject = (JSONObject) cls;
      classes.addAll(ClassFinder.find(classObject.getString("name")));
      testWeights.put(classObject.getString("name"), classObject.getDouble("weight"));
    });
  }

  /**
   * Write the JSON object to the output file.
   *
   * @param json JSON object to write
   * @throws RuntimeException if the file cannot be written to
   */
  void writeToOutput(JSONObject json) {
    try (PrintStream ps =
        new PrintStream(new FileOutputStream(OUTPUT_FILE), false, StandardCharsets.UTF_8)) {
      ps.append(json.toString());
    } catch (Exception e) {
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
    return Set.copyOf(EXCLUDED_CLASSES);
  }
}
