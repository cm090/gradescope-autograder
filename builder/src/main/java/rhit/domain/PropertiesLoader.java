package rhit.domain;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {
  private static final String FILE_NAME = "Strings.properties";
  private static final PropertiesLoader instance = new PropertiesLoader();
  private final Properties properties = new Properties();

  private PropertiesLoader() {
    try (InputStream input = getClass().getClassLoader().getResourceAsStream(FILE_NAME)) {
      if (input == null) {
        System.out.println("Could not find properties file " + FILE_NAME);
        return;
      }
      properties.load(input);
    } catch (IOException ex) {
      System.err.println("Could not load properties file " + FILE_NAME);
      System.err.println(ex.getMessage());
    }
  }

  public static String get(String key) {
    return instance.getProperty(key);
  }

  private String getProperty(String key) {
    return properties.getProperty(key);
  }
}