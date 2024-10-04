package rhit.presentation;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import rhit.domain.BuilderData;
import rhit.domain.FileTree;

public class FileTreeSelector {
  private final JFrame frame;
  private JPanel panel;
  private FileTree fileTree;

  public FileTreeSelector() {
    this.frame = InterfaceUtils.getFrame();
    displayFileTreeSelector();
  }

  private void displayFileTreeSelector() {
    String startingDir = BuilderData.getStarterCodeDir();
    JLabel label = new JLabel("Starter code: ");
    JButton startingDirectoryButton = new JButton(startingDir == null ? "Select a directory" :
        startingDir.substring(startingDir.lastIndexOf(File.separator) + 1));
    startingDirectoryButton.addActionListener(e -> handleSelectStartingDirectory());

    JButton continueButton = new JButton("Continue");
    continueButton.addActionListener(e -> handleContinue());

    JPanel formPanel = new JPanel();
    formPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    formPanel.setLayout(new GridLayout(1, 2));
    formPanel.add(label);
    formPanel.add(startingDirectoryButton);

    panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.add(formPanel);
    panel.add(continueButton, BorderLayout.SOUTH);
    frame.add(panel);

    InterfaceUtils.updateFrame();
  }

  private void handleSelectStartingDirectory() {
    InterfaceUtils.hideFrame(panel);
    JFileChooser fileChooser = new JFileChooser();
    File startDir = new File("../../");
    if (!startDir.exists()) {
      startDir = new File(".");
    }
    fileChooser.setCurrentDirectory(startDir);
    fileChooser.setDialogTitle("Choose a directory");
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    fileChooser.setAcceptAllFileFilterUsed(false);
    if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
      String path = fileChooser.getSelectedFile().getAbsolutePath();
      BuilderData.setStarterCodeDir(path);
    }
    displayFileTreeSelector();
  }

  private void handleContinue() {
    InterfaceUtils.hideFrame(panel);
    new ConfigurationOptions();
  }
}
