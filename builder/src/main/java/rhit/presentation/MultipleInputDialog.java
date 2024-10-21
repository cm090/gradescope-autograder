package rhit.presentation;

import java.awt.GridLayout;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.json.simple.JSONObject;

public class MultipleInputDialog {
  private final Set<Runnable> callbacks;
  private final Map<String, JTextField> fields;
  private final JSONObject object;

  public MultipleInputDialog(JSONObject object) {
    this.callbacks = new HashSet<>();
    this.fields = new HashMap<>();
    this.object = object;
  }

  @SuppressWarnings("unchecked")
  public void show() {
    JPanel panel = new JPanel(new GridLayout(0, 2));
    object.forEach((key, value) -> {
      String keyString = key.toString();
      panel.add(new JLabel(
          keyString.substring(0, 1).toUpperCase() + keyString.substring(1).replaceAll("[-_]", " ") +
              ":"));
      JTextField textField = new JTextField(10);
      textField.setText(value.toString());
      panel.add(textField);
      fields.put(keyString, textField);
    });

    int result =
        JOptionPane.showConfirmDialog(null, panel, "Enter Details", JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);
    if (result == JOptionPane.OK_OPTION) {
      if (fields.values().stream().anyMatch(textField -> textField.getText().isEmpty())) {
        JOptionPane.showMessageDialog(null, "Fields cannot be empty! Item did not save.");
        return;
      }
      fields.forEach((key, textField) -> {
        Class<?> type = object.get(key).getClass();
        Object value;
        if (type == Long.class) {
          value = Long.parseLong(textField.getText());
        } else if (type == Double.class) {
          value = Double.parseDouble(textField.getText());
        } else if (type == Boolean.class) {
          value = Boolean.parseBoolean(textField.getText());
        } else {
          value = textField.getText();
        }
        object.put(key, value);
      });
      callbacks.forEach(Runnable::run);
    }
  }

  public void addCallback(Runnable callback) {
    callbacks.add(callback);
  }
}