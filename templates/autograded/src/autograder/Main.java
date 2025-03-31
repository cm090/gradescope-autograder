package autograder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONException;
import org.json.JSONObject;

/*
  Gradescope Autograder: JUnit Test Runner
  Copyright (C) 2025 Canon Maranda <https://about.canon.click>

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

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

  /**
   * Determine whether valid configuration and metadata JSON files were provided.
   *
   * @param args the command line arguments
   * @throws RuntimeException if the number of arguments is incorrect or the files are invalid
   */
  private static void checkArgs(String[] args) {
    if (args.length != 2) {
      throw new RuntimeException(INCORRECT_ARGUMENTS);
    }
    try {
      configObject = new JSONObject(Files.readString(Paths.get(args[0])));
    } catch (JSONException | IOException e) {
      throw new RuntimeException(INVALID_CONFIG_FILE);
    }
    try {
      metadataObject = new JSONObject(Files.readString(Paths.get(args[1])));
    } catch (JSONException | IOException e) {
      throw new RuntimeException(INVALID_METADATA_FILE);
    }
  }

  /**
   * Add the configuration and metadata objects to the Configuration class.
   *
   * @see Configuration#build(JSONObject, JSONObject)
   */
  private static void parseConfiguration() {
    Configuration.build(configObject, metadataObject);
  }

  /**
   * Create test runners and run all the JUnit tests.
   *
   * @see Runner#addRunners()
   * @see Runner#runTests()
   */
  private static void runTests() {
    Runner runner = new Runner();
    runner.addRunners();
    runner.runTests();
  }
}
