package rhit.presentation;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import rhit.domain.BuilderData;
import rhit.domain.PropertiesLoader;

public class ConfigurationOptions {
  private final JFrame frame;
  private JPanel panel;

  public ConfigurationOptions() {
    this.frame = InterfaceUtils.getFrame();
    BuilderData.parseConfigFile();
    displayConfigurationOptions();
  }

  private void displayConfigurationOptions() {
    JPanel formPanel = new JPanel(new GridBagLayout());
    formPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

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
    gbc.insets.set(2, 2, 2, 2);
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
        JButton button = new JButton("Edit");
        button.addActionListener(e -> displayArrayEditor((JSONArray) value));
        formPanel.add(button, gbc);
      } else {
        JTextField textField = new JTextField(value.toString());
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

  private void handleContinue() {
    InterfaceUtils.hideFrame(panel);
  }
}
