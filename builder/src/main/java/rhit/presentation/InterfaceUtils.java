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

public class InterfaceUtils {
  private static final Pattern packagePattern = Pattern.compile("(?<=src[\\\\/]).+(?=[\\\\/])");

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
  }

  static void invokeClassMethod(Object object, String key, String stringText, String objectText,
                                Consumer<Object> callback) {
    Class<?> type = object.getClass();
    try {
      Object value = type == String.class ? stringText :
          type.getDeclaredMethod("valueOf", String.class).invoke(null, objectText);
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
    JSONObject original = (JSONObject) array.get(0);
    for (String packageName : packages) {
      JSONObject object = new JSONObject();
      for (Object key : original.keySet()) {
        object.put(key, key.equals("name") ? packageName : original.get(key));
      }
      array.add(object);
    }
    array.remove(original);
  }

  private static String getPackageFromFile(String path) {
    if (!(path.endsWith(".java") && path.contains(File.separator))) {
      return "";
    }
    Matcher matcher = packagePattern.matcher(path);
    return matcher.find() ? matcher.group().replace("/", ".") : "";
  }
}