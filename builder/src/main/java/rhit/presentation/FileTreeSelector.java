package rhit.presentation;

import eu.essilab.lablib.checkboxtree.CheckboxTree;
import eu.essilab.lablib.checkboxtree.TreeCheckingModel;
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
  private CheckboxTree checkboxTree;

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
    if (checkboxTree != null) {
      panel.add(checkboxTree);
    }
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
      generateCheckboxTree();
    }
    displayFileTreeSelector();
  }

  private void handleContinue() {
    InterfaceUtils.hideFrame(panel);
    new ConfigurationOptions();
  }

  private void generateCheckboxTree() {
    this.fileTree = new FileTree(BuilderData.getStarterCodeDir());
    this.checkboxTree = new CheckboxTree(fileTree.getRoot());
    this.checkboxTree.getCheckingModel().setCheckingMode(TreeCheckingModel.CheckingMode.PROPAGATE);
    this.checkboxTree.setCellRenderer(new TreeCellRenderer());
    this.checkboxTree.expandAll();
  }
}
