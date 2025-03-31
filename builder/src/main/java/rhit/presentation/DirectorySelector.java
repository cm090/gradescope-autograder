package rhit.presentation;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import java.nio.file.Path;
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

public class DirectorySelector extends SwingGui {
  private static final int BORDER_SIZE = 5;
  private static final int NUM_ROWS = 3;
  private static final int NUM_COLS = 2;
  private static final int GRID_PADDING = 2;

  private final JFrame frame;
  private final List<JRadioButton> radioButtons;
  private JPanel panel;

  DirectorySelector() {
    this.frame = InterfaceUtils.getFrame();
    super.verifyFrame(frame);
    this.radioButtons = new ArrayList<>();
  }

  void show() {
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
    JButton templateButton =
        new JButton(templateDir == null ? PropertiesLoader.get("selectButtonHint")
            : templateDir.substring(templateDir.lastIndexOf(File.separator) + 1));
    templateButton.addActionListener(e -> handleSelectTemplate());
    if (templateDir != null) {
      templateButton.setToolTipText(templateDir);
    }
    return templateButton;
  }

  private void handleSelectTemplate() {
    InterfaceUtils.hideFrame(panel);
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setCurrentDirectory(InterfaceUtils.setStartDirectory(BuilderData.getTemplateDir()));
    fileChooser.setDialogTitle(PropertiesLoader.get("templateDirPrompt"));
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    fileChooser.setAcceptAllFileFilterUsed(false);
    if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
      String path = fileChooser.getSelectedFile().getAbsolutePath();
      validateSelectedTemplate(path);
    }
    show();
  }

  private void validateSelectedTemplate(String path) {
    Path directory = Path.of(path);
    if (!directory.resolve("config.json").toFile().exists()) {
      JOptionPane.showMessageDialog(frame, PropertiesLoader.get("invalidTemplateError"),
          PropertiesLoader.get("errorTitle"), JOptionPane.ERROR_MESSAGE);
    } else {
      BuilderData.setTemplateDir(path);
    }
  }

  private JButton createOutputButton() {
    String outputDir = BuilderData.getOutputDir();
    JButton templateButton =
        new JButton(outputDir == null ? PropertiesLoader.get("selectButtonHint")
            : outputDir.substring(outputDir.lastIndexOf(File.separator) + 1));
    templateButton.addActionListener(e -> handleSelectOutput());
    if (outputDir != null) {
      templateButton.setToolTipText(outputDir);
    }
    return templateButton;
  }

  private void handleSelectOutput() {
    InterfaceUtils.hideFrame(panel);
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setCurrentDirectory(InterfaceUtils.setStartDirectory(BuilderData.getOutputDir()));
    fileChooser.setDialogTitle(
        String.format("%s (%s)", PropertiesLoader.get("outputDirPrompt"), String.format(
            PropertiesLoader.get("outputDirHint"), PropertiesLoader.get("outputActualDir"))));
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    fileChooser.setAcceptAllFileFilterUsed(false);
    if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
      String path = fileChooser.getSelectedFile().getAbsolutePath();
      int maxDepth = 10;
      int depth = 0;
      while (new File(path).exists()
          && Objects.requireNonNull(new File(path).listFiles()).length > 0 && depth++ < maxDepth) {
        JOptionPane.showMessageDialog(frame,
            String.format(PropertiesLoader.get("outputSelectDirectoryWarning"), path,
                PropertiesLoader.get("outputActualDir")),
            PropertiesLoader.get("errorTitle"), JOptionPane.WARNING_MESSAGE);
        path = new File(path, PropertiesLoader.get("outputActualDir")).getAbsolutePath();
      }
      if (depth >= maxDepth) {
        JOptionPane.showMessageDialog(frame, PropertiesLoader.get("maxDepthExceededError"),
            PropertiesLoader.get("errorTitle"), JOptionPane.ERROR_MESSAGE);
        return;
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

  protected void handleContinue() {
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
      SwingGui.setVisibleFrame(new FileTreeSelector());
    } else {
      SwingGui.setVisibleFrame(new ConfigurationOptions());
    }
    SwingGui.showFrame();
  }
}
