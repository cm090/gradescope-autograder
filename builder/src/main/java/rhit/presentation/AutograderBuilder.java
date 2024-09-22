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
import rhit.domain.TemplateType;

public class AutograderBuilder {
  private final BuilderData builderData;
  private final JFrame frame;
  private JPanel panel;

  public AutograderBuilder() {
    builderData = new BuilderData();
    frame = new JFrame("Autograder Builder");
    displayTemplateSelector();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  public static void main(String[] args) {
    new AutograderBuilder();
  }

  private void updateFrame() {
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  private void hideFrame() {
    panel.removeAll();
    frame.getContentPane().removeAll();
    frame.setVisible(false);
  }

  private void displayTemplateSelector() {
    String templateDir = builderData.getTemplateDir();
    JLabel label = new JLabel("Select a template: ");
    JButton templateButton = new JButton(templateDir == null ? "Select a directory" :
        templateDir.substring(templateDir.lastIndexOf(File.separator) + 1));
    templateButton.addActionListener(e -> handleSelectTemplate());

    JRadioButton autogradedButton = new JRadioButton("Autograded");
    JRadioButton manualButton = new JRadioButton("Manual");
    autogradedButton.setSelected(true);
    manualButton.setSelected(false);
    autogradedButton.addActionListener(e -> {
      autogradedButton.setSelected(true);
      manualButton.setSelected(false);
      builderData.setTemplateType(TemplateType.AUTO);
    });
    manualButton.addActionListener(e -> {
      autogradedButton.setSelected(false);
      manualButton.setSelected(true);
      builderData.setTemplateType(TemplateType.MANUAL);
    });

    JButton continueButton = new JButton("Continue");
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

    updateFrame();
  }

  private void handleSelectTemplate() {
    hideFrame();
    JFileChooser fileChooser = new JFileChooser();
    File startDir = new File("../templates");
    if (!startDir.exists()) {
      startDir = new File(".");
    }
    fileChooser.setCurrentDirectory(startDir);
    fileChooser.setDialogTitle("Choose a directory");
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    fileChooser.setAcceptAllFileFilterUsed(false);
    if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
      String path = fileChooser.getSelectedFile().getAbsolutePath();
      builderData.setTemplateDir(path);
    }
    displayTemplateSelector();
  }

  private void handleContinue() {
    if (builderData.getTemplateDir() == null) {
      JOptionPane.showMessageDialog(frame, "Please select a template directory", "Error",
          JOptionPane.ERROR_MESSAGE);
      return;
    }
    hideFrame();
  }
}
