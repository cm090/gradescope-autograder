package newAutograder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Given an input configuration file (config.json) and metadata file (submission_metadata.json), run
 * all the JUnit test in the provided package(s) and output the results to a predefined JSON file.
 *
 * @author Canon Maranda
 * @see <a href="https://github.com/cm090/gradescope-autograder">GitHub</a>
 */
public class Main {
  private static final String INCORRECT_ARGUMENTS =
      "Usage: java -jar autograder.jar <config_file> <metadata_file>";
  private static final String INVALID_CONFIG_FILE = "Invalid configuration file.";
  private static final String INVALID_METADATA_FILE = "Invalid metadata file.";
  private static JSONObject configObject;
  private static JSONObject metadataObject;

  public static void main(String[] args) {
    checkArgs(args);
    parseConfiguration();
    runTests();
  }

  private static void checkArgs(String[] args) {
    if (args.length != 2) {
      throw new RuntimeException(INCORRECT_ARGUMENTS);
    }
    try {
      configObject = new JSONObject(new String(Files.readAllBytes(Paths.get(args[0]))));
    } catch (JSONException | IOException e) {
      throw new RuntimeException(INVALID_CONFIG_FILE);
    }
    try {
      metadataObject = new JSONObject(new String(Files.readAllBytes(Paths.get(args[1]))));
    } catch (JSONException | IOException e) {
      throw new RuntimeException(INVALID_METADATA_FILE);
    }
  }

  private static void parseConfiguration() {
    Configuration.build(configObject, metadataObject);
  }

  private static void runTests() {
    Runner runner = new Runner();
    runner.addRunners();
    runner.runTests();
  }
}
