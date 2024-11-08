package rhit.presentation;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import rhit.domain.BuilderData;
import rhit.domain.PropertiesLoader;

class ConfigurationOptions extends SwingGui {
  private static final int BORDER_SIZE = 5;
  private static final int GRID_PADDING = 2;

  private final JFrame frame;
  private final Map<String, String> formValues;
  private JPanel panel;

  ConfigurationOptions() {
    this.frame = InterfaceUtils.getFrame();
    this.formValues = new HashMap<>();
    BuilderData.parseConfigFile();
  }

  void show() {
    JPanel formPanel = new JPanel(new GridBagLayout());
    formPanel.setBorder(
        BorderFactory.createEmptyBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE));

    generateFormPanel(formPanel);

    JButton continueButton = new JButton(PropertiesLoader.get("continueButton"));
    continueButton.addActionListener(e -> handleContinue());

    panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.add(formPanel);
    panel.add(continueButton, BorderLayout.SOUTH);
    frame.add(panel);

    InterfaceUtils.updateFrame();
  }

  private void generateFormPanel(JPanel formPanel) {
    JSONObject configOptions = BuilderData.getConfigOptions();
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets.set(GRID_PADDING, GRID_PADDING, GRID_PADDING, GRID_PADDING);
    iterativeFormPanel(configOptions, formPanel, gbc);
  }

  @SuppressWarnings("unchecked")
  private void iterativeFormPanel(JSONObject configOptions, JPanel formPanel,
                                  GridBagConstraints gbc) {
    configOptions.forEach((key, value) -> {
      if (value instanceof JSONObject) {
        iterativeFormPanel((JSONObject) value, formPanel, gbc);
        return;
      }
      gbc.gridx = 0;
      String keyLabel = String.join(" ", key.toString().split("[-_]"));
      keyLabel = keyLabel.substring(0, 1).toUpperCase() + keyLabel.substring(1);
      formPanel.add(new JLabel(keyLabel + ": "), gbc);
      gbc.gridx = 1;
      if (value instanceof JSONArray) {
        if (key.toString().equals("classes")) {
          InterfaceUtils.createClassObjectsFromPackages((JSONArray) value,
              BuilderData.getTemplateFiles());
        }
        JButton button = new JButton(PropertiesLoader.get("editButton"));
        button.addActionListener(e -> displayArrayEditor((JSONArray) value));
        formPanel.add(button, gbc);
      } else {
        JTextField textField = new JTextField(value.toString());
        formValues.put(key.toString(), value.toString());
        textField.getDocument().addDocumentListener(new DocumentListener() {
          @Override
          public void insertUpdate(DocumentEvent e) {
            handleChange();
          }

          @Override
          public void removeUpdate(DocumentEvent e) {
            handleChange();
          }

          @Override
          public void changedUpdate(DocumentEvent e) {
            handleChange();
          }

          private void handleChange() {
            Object newValue = textField.getText();
            formValues.put(key.toString(), (String) newValue);
            if (((String) newValue).isEmpty()) {
              return;
            }
            if (value instanceof Long) {
              newValue = Long.parseLong((String) newValue);
            } else if (value instanceof Boolean) {
              newValue = Boolean.parseBoolean((String) newValue);
            }
            configOptions.put(key, newValue);
          }
        });
        formPanel.add(textField, gbc);
      }
      gbc.gridy++;
    });
  }

  private void displayArrayEditor(JSONArray array) {
    ArrayEditorDialog dialog = new ArrayEditorDialog(frame, array);
    dialog.setVisible(true);
  }

  void handleContinue() {
    if (formValues.values().stream().anyMatch(String::isEmpty)) {
      JOptionPane.showMessageDialog(frame, PropertiesLoader.get("emptyFieldError"));
      return;
    }
    InterfaceUtils.hideFrame(panel);
    SwingGui.setVisibleFrame(new BuildProgress());
    SwingGui.showFrame();
  }
}
