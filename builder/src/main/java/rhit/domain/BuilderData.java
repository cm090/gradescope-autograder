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
  @Getter
  private static final Set<String> templateFiles = new HashSet<>();

  @Getter
  @Setter
  private static String templateDir;

  @Getter
  @Setter
  // TODO: Add output selector to GUI
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
    File configFile = new File(templateDir, "config.json");
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
