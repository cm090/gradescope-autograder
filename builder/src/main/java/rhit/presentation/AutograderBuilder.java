package rhit.presentation;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
  private final List<JRadioButton> radioButtons;
  private JPanel panel;

  private AutograderBuilder() {
    this.frame = new JFrame(PropertiesLoader.get("windowTitle"));
    this.radioButtons = new ArrayList<>();
    InterfaceUtils.setFrame(frame);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  public static void main(String[] args) {
    AutograderBuilder gui = new AutograderBuilder();
    gui.show();
  }

  private void show() {
    JButton continueButton = new JButton(PropertiesLoader.get("continueButton"));
    continueButton.addActionListener(e -> handleContinue());

    panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.add(createFormPanel());
    panel.add(continueButton, BorderLayout.SOUTH);
    frame.add(panel);

    InterfaceUtils.updateFrame();
  }

  private JPanel createFormPanel() {
    JPanel formPanel = new JPanel();
    formPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    formPanel.setLayout(new GridLayout(2, 2));
    formPanel.add(new JLabel(PropertiesLoader.get("templateDirPrompt") + ": "));
    formPanel.add(createTemplateButton());

    radioButtons.clear();
    createRadioButton(PropertiesLoader.get("autogradedOption"), TemplateType.AUTO);
    createRadioButton(PropertiesLoader.get("manualOption"), TemplateType.MANUAL);
    radioButtons.forEach(formPanel::add);

    return formPanel;
  }

  private JButton createTemplateButton() {
    String templateDir = BuilderData.getTemplateDir();
    JButton templateButton = new JButton(
        templateDir == null ? PropertiesLoader.get("selectButtonHint") :
            templateDir.substring(templateDir.lastIndexOf(File.separator) + 1));
    templateButton.addActionListener(e -> handleSelectTemplate());
    return templateButton;
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
    show();
  }

  private void createRadioButton(String text, TemplateType type) {
    JRadioButton radioButton = new JRadioButton(text);
    radioButtons.add(radioButton);
    radioButton.setSelected(BuilderData.getTemplateType() == type);
    radioButton.addActionListener(e -> {
      radioButtons.forEach(button -> button.setSelected(button == radioButton));
      BuilderData.setTemplateType(type);
    });
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
