package rhit.presentation;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import lombok.Getter;
import lombok.Setter;
import rhit.domain.PropertiesLoader;

public class InterfaceUtils {
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
      JOptionPane.showMessageDialog(null, String.format(PropertiesLoader.get("typeError"), key));
    }
  }
}
