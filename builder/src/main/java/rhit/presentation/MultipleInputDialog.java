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
import rhit.domain.PropertiesLoader;

class MultipleInputDialog {
  private static final int NUM_COLS = 2;

  private final Set<Runnable> callbacks;
  private final Map<String, JTextField> fields;
  private final JSONObject object;

  MultipleInputDialog(JSONObject object) {
    this.callbacks = new HashSet<>();
    this.fields = new HashMap<>();
    this.object = object;
  }

  @SuppressWarnings("unchecked")
  public void show() {
    JPanel panel = new JPanel(new GridLayout(0, NUM_COLS));
    object.forEach((key, value) -> createFormRow(key.toString(), value, panel));

    handleDialogAction(
        JOptionPane.showConfirmDialog(null, panel, PropertiesLoader.get("detailsEditorTitle"),
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE));

  }

  private void createFormRow(String key, Object value, JPanel panel) {
    panel.add(new JLabel(
        key.substring(0, 1).toUpperCase() + key.substring(1).replaceAll("[-_]", " ") + ":"));
    JTextField textField = new JTextField(10);
    textField.setText(value.toString());
    panel.add(textField);
    fields.put(key, textField);
  }

  @SuppressWarnings("unchecked")
  private void handleDialogAction(int result) {
    if (result != JOptionPane.OK_OPTION) {
      return;
    }
    if (fields.values().stream().anyMatch(textField -> textField.getText().isEmpty())) {
      JOptionPane.showMessageDialog(null,
          PropertiesLoader.get("emptyFieldError") + " " + PropertiesLoader.get("didNotSave"));
      return;
    }
    fields.forEach((key, textField) -> InterfaceUtils.invokeClassMethod(object.get(key), key,
        textField.getText(), textField.getText(), (value) -> object.put(key, value)));
    callbacks.forEach(Runnable::run);
  }

  public void addCallback(Runnable callback) {
    if (callback == null) {
      throw new IllegalArgumentException("Callback cannot be null");
    }
    callbacks.add(callback);
  }
}
