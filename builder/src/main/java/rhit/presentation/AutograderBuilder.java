package rhit.presentation;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import rhit.domain.BuilderData;
import rhit.domain.PropertiesLoader;
import rhit.domain.TemplateType;

public class AutograderBuilder {
  private final JFrame frame;
  private JPanel panel;

  private AutograderBuilder() {
    frame = new JFrame(PropertiesLoader.get("windowTitle"));
    InterfaceUtils.setFrame(frame);
    displayTemplateSelector();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  public static void main(String[] args) {
    new AutograderBuilder();
  }

  private void displayTemplateSelector() {
    String templateDir = BuilderData.getTemplateDir();
    JLabel label = new JLabel(PropertiesLoader.get("templateDirPrompt") + ": ");
    JButton templateButton = new JButton(
        templateDir == null ? PropertiesLoader.get("selectButtonHint") :
            templateDir.substring(templateDir.lastIndexOf(File.separator) + 1));
    templateButton.addActionListener(e -> handleSelectTemplate());

    JRadioButton autogradedButton = new JRadioButton(PropertiesLoader.get("autogradedOption"));
    JRadioButton manualButton = new JRadioButton(PropertiesLoader.get("manualOption"));
    autogradedButton.setSelected(BuilderData.getTemplateType() == TemplateType.AUTO);
    manualButton.setSelected(BuilderData.getTemplateType() == TemplateType.MANUAL);
    autogradedButton.addActionListener(e -> {
      autogradedButton.setSelected(true);
      manualButton.setSelected(false);
      BuilderData.setTemplateType(TemplateType.AUTO);
    });
    manualButton.addActionListener(e -> {
      autogradedButton.setSelected(false);
      manualButton.setSelected(true);
      BuilderData.setTemplateType(TemplateType.MANUAL);
    });

    JButton continueButton = new JButton(PropertiesLoader.get("continueButton"));
    continueButton.addActionListener(e -> handleContinue());

    JPanel formPanel = new JPanel();
    formPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    formPanel.setLayout(new GridLayout(2, 2));
    formPanel.add(label);
    formPanel.add(templateButton);
    formPanel.add(autogradedButton);
    formPanel.add(manualButton);

    panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.add(formPanel);
    panel.add(continueButton, BorderLayout.SOUTH);
    frame.add(panel);

    InterfaceUtils.updateFrame();
  }

  private void handleSelectTemplate() {
    InterfaceUtils.hideFrame(panel);
    JFileChooser fileChooser = new JFileChooser();
    File startDir = new File(PropertiesLoader.get("templateSelectStartingDir"));
    if (!startDir.exists()) {
      startDir = new File(".");
    }
    fileChooser.setCurrentDirectory(startDir);
    fileChooser.setDialogTitle(PropertiesLoader.get("selectButtonHint"));
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    fileChooser.setAcceptAllFileFilterUsed(false);
    if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
      String path = fileChooser.getSelectedFile().getAbsolutePath();
      BuilderData.setTemplateDir(path);
    }
    displayTemplateSelector();
  }

  private void handleContinue() {
    if (BuilderData.getTemplateDir() == null) {
      JOptionPane.showMessageDialog(frame, PropertiesLoader.get("templateSelectError"),
          PropertiesLoader.get("errorTitle"), JOptionPane.ERROR_MESSAGE);
      return;
    }
    InterfaceUtils.hideFrame(panel);
    if (BuilderData.getTemplateType() == TemplateType.AUTO) {
      new FileTreeSelector();
    } else {
      new ConfigurationOptions();
    }
  }
}
