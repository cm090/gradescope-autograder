package rhit.presentation;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import rhit.domain.PropertiesLoader;
import rhit.domain.VersionManager;

final class InterfaceUtils {
  private static final String JAVA_EXTENSION = ".java";
  private static final String PACKAGE_NAME_KEY = "name";
  private static final Pattern PACKAGE_PATTERN = Pattern.compile("(?<=src[\\\\/]).+(?=[\\\\/])");

  @Getter
  @Setter
  private static JFrame frame;

  static void updateFrame() {
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  static void hideFrame(JPanel panel) {
    panel.removeAll();
    frame.getContentPane().removeAll();
    frame.setVisible(false);
    frame.dispose();
  }

  static void invokeClassMethod(Object object, String key, String stringText, String objectText,
      Consumer<Object> callback) {
    Class<?> type = object.getClass();
    try {
      Object value = type == String.class ? stringText
          : type.getDeclaredMethod("valueOf", String.class).invoke(null, objectText);
      callback.accept(value);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      JOptionPane.showMessageDialog(null, String.format(
          PropertiesLoader.get("typeError") + " " + PropertiesLoader.get("didNotSave"), key));
    }
  }

  @SuppressWarnings("unchecked")
  static void createClassObjectsFromPackages(JSONArray array, Set<String> files) {
    Set<String> packages = new HashSet<>();
    for (String file : files) {
      String packageName = getPackageFromFile(file);
      if (!packageName.isEmpty()) {
        packages.add(packageName);
      }
    }
    if (packages.isEmpty()) {
      return;
    }

    if (array.isEmpty()) {
      throw new IllegalArgumentException("Array must contain at least one object");
    }
    JSONObject original = (JSONObject) array.get(0);
    if (!original.containsKey(PACKAGE_NAME_KEY)) {
      throw new IllegalArgumentException(
          "Object must contain the key \"" + PACKAGE_NAME_KEY + "\"");
    }

    for (String packageName : packages) {
      JSONObject object = new JSONObject();
      for (Object key : original.keySet()) {
        object.put(key, key.equals(PACKAGE_NAME_KEY) ? packageName : original.get(key));
      }
      array.add(object);
    }
    array.remove(original);
  }

  private static String getPackageFromFile(String path) {
    if (!(path.endsWith(JAVA_EXTENSION) && path.contains(File.separator))) {
      return "";
    }
    Matcher matcher = PACKAGE_PATTERN.matcher(path);
    return matcher.find() ? matcher.group().replace("/", ".") : "";
  }

  static File setStartDirectory(String savedPath) {
    File startDir = new File(savedPath == null ? "." : savedPath).getParentFile();
    return startDir != null && startDir.exists() ? startDir : null;
  }

  static void checkTemplateVersion(String path) {
    if (!VersionManager.isLatestVersion(path)) {
      JOptionPane.showMessageDialog(frame, PropertiesLoader.get("templateVersionError"),
          PropertiesLoader.get("errorTitle"), JOptionPane.ERROR_MESSAGE);
    }
  }
}
