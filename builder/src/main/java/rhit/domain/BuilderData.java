package rhit.domain;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class BuilderData {
  static final String CONFIG_FILE = "config.json";

  @Getter
  private static final Set<String> templateFiles = new HashSet<>();

  @Getter
  @Setter
  private static String templateDir;

  @Getter
  @Setter
  private static String outputDir;

  @Getter
  @Setter
  private static String starterCodeDir;

  @Getter
  @Setter
  private static TemplateType templateType = TemplateType.AUTO;

  @Getter
  private static JSONObject configOptions;

  public static void addTemplateFile(String file) {
    templateFiles.add(file);
  }

  public static void parseConfigFile() {
    File configFile = new File(templateDir, CONFIG_FILE);
    try (FileReader reader = new FileReader(configFile)) {
      configOptions = (JSONObject) new JSONParser().parse(reader);
    } catch (ParseException | IOException e) {
      System.err.println(e.getMessage());
    } finally {
      if (configOptions == null) {
        configOptions = new JSONObject();
      }
    }
  }
}
