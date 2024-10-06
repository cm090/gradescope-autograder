package rhit.presentation;

import eu.essilab.lablib.checkboxtree.CheckboxTree;
import eu.essilab.lablib.checkboxtree.TreeCheckingModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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

    JPanel formPanel = new JPanel(new GridBagLayout());
    formPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 0;
    gbc.gridy = 0;

    JPanel directorySelectorPanel = new JPanel(new GridLayout(1, 2));
    directorySelectorPanel.add(label);
    directorySelectorPanel.add(startingDirectoryButton);
    formPanel.add(directorySelectorPanel, gbc);

    if (checkboxTree != null) {
      gbc.gridy++;
      gbc.insets.top = 10;
      formPanel.add(new JLabel("Select files to exclude (ones that students will edit):"), gbc);
      gbc.gridy++;
      gbc.weightx = 1;
      gbc.weighty = 1;
      gbc.insets.top = 0;
      gbc.fill = GridBagConstraints.BOTH;
      JScrollPane scrollPane = new JScrollPane(checkboxTree);
      scrollPane.setPreferredSize(new Dimension(350, 500));
      formPanel.add(scrollPane, gbc);
    }

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
