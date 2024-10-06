package rhit.domain;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {
  private static final PropertiesLoader instance = new PropertiesLoader("Strings.properties");
  private final Properties properties = new Properties();

  public PropertiesLoader(String fileName) {
    try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
      if (input == null) {
        System.out.println("Could not find properties file " + fileName);
        return;
      }
      properties.load(input);
    } catch (IOException ex) {
      System.err.println("Could not load properties file " + fileName);
      System.err.println(ex.getMessage());
    }
  }

  public static String get(String key) {
    return instance.getProperty(key);
  }

  public String getProperty(String key) {
    return properties.getProperty(key);
  }
}