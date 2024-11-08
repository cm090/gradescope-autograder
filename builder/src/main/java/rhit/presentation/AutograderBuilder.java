package rhit.presentation;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
  private static final int BORDER_SIZE = 5;
  private static final int NUM_ROWS = 3;
  private static final int NUM_COLS = 2;
  private static final int GRID_PADDING = 2;

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
    formPanel.setBorder(
        BorderFactory.createEmptyBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE));
    formPanel.setLayout(new GridLayout(NUM_ROWS, NUM_COLS, 0, GRID_PADDING));
    formPanel.add(new JLabel(PropertiesLoader.get("templateDirPrompt") + ": "));
    formPanel.add(createTemplateButton());
    formPanel.add(new JLabel(PropertiesLoader.get("outputDirPrompt") + ": "));
    formPanel.add(createOutputButton());

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

  private JButton createOutputButton() {
    String outputDir = BuilderData.getOutputDir();
    JButton templateButton = new JButton(
        outputDir == null ? PropertiesLoader.get("selectButtonHint") :
            outputDir.substring(outputDir.lastIndexOf(File.separator) + 1));
    templateButton.addActionListener(e -> handleSelectOutput());
    return templateButton;
  }

  private void handleSelectOutput() {
    InterfaceUtils.hideFrame(panel);
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setCurrentDirectory(new File("."));
    fileChooser.setDialogTitle(String.format("%s (%s)", PropertiesLoader.get("selectButtonHint"),
        String.format(PropertiesLoader.get("outputDirHint"),
            PropertiesLoader.get("outputActualDir"))));
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    fileChooser.setAcceptAllFileFilterUsed(false);
    if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
      String path = fileChooser.getSelectedFile().getAbsolutePath();
      while (Objects.requireNonNull(new File(path).listFiles()).length > 0) {
        JOptionPane.showMessageDialog(frame,
            String.format(PropertiesLoader.get("outputSelectDirectoryWarning"), path,
                PropertiesLoader.get("outputActualDir")), PropertiesLoader.get("errorTitle"),
            JOptionPane.WARNING_MESSAGE);
        path = new File(path, PropertiesLoader.get("outputActualDir")).getAbsolutePath();
      }
      BuilderData.setOutputDir(path);
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
    } else if (BuilderData.getOutputDir() == null) {
      JOptionPane.showMessageDialog(frame, PropertiesLoader.get("outputSelectError"),
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
