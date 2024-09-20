package rhit.presentation;

import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import rhit.domain.BuilderData;

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
    JPanel filePanel = createTemplateSelectorButton(templateDir);

    JButton continueButton = new JButton("Continue");
    continueButton.addActionListener(e -> handleContinue());

    panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.add(filePanel);
    panel.add(continueButton);
    frame.add(panel);

    updateFrame();
  }

  private JPanel createTemplateSelectorButton(String templateDir) {
    JLabel label = new JLabel("Select a template: ");
    JButton templateButton = new JButton(templateDir == null ? "Select a directory" :
        templateDir.substring(templateDir.lastIndexOf(File.separator) + 1));
    templateButton.addActionListener(e -> handleSelectTemplate());
    JPanel filePanel = new JPanel();
    filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.LINE_AXIS));
    filePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    filePanel.add(label);
    filePanel.add(templateButton);
    return filePanel;
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
  }
}
