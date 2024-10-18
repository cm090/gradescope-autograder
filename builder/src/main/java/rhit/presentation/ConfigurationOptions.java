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

  @SuppressWarnings("unchecked")
  private void generateFormPanel(JPanel formPanel) {
    JSONObject configOptions = BuilderData.getConfigOptions();
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridy = 0;

    configOptions.forEach((key, value) -> {
      gbc.gridx = 0;
      String keyLabel = String.join(" ", key.toString().split("[-_]"));
      keyLabel = keyLabel.substring(0, 1).toUpperCase() + keyLabel.substring(1);
      formPanel.add(new JLabel(keyLabel + ": "), gbc);
      gbc.gridx = 1;
      if (value instanceof JSONObject) {
        // TODO: Handle classes object
        formPanel.add(new JLabel("Class object"), gbc);
      } else if (value instanceof JSONArray) {
        // TODO: Handle array objects
        formPanel.add(new JLabel("Array object"), gbc);
      } else {
        JTextField textField = new JTextField(value.toString());
        textField.addActionListener(e -> {
          Object newValue = textField.getText();
          if (value instanceof Long) {
            newValue = Long.parseLong((String) newValue);
          } else if (value instanceof Boolean) {
            newValue = Boolean.parseBoolean((String) newValue);
          }
          BuilderData.getConfigOptions().put(key, newValue);
        });
        formPanel.add(textField, gbc);
      }
      gbc.gridy++;
    });
  }

  private void handleContinue() {
    InterfaceUtils.hideFrame(panel);
  }
}
