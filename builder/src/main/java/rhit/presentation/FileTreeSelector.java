package rhit.presentation;

import eu.essilab.lablib.checkboxtree.CheckboxTree;
import eu.essilab.lablib.checkboxtree.TreeCheckingModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import rhit.domain.BuilderData;
import rhit.domain.FileTree;
import rhit.domain.PropertiesLoader;

public class FileTreeSelector {
  private final JFrame frame;
  private JPanel panel;
  private CheckboxTree checkboxTree;

  public FileTreeSelector() {
    this.frame = InterfaceUtils.getFrame();
    displayFileTreeSelector();
  }

  private void displayFileTreeSelector() {
    String startingDir = BuilderData.getStarterCodeDir();
    JLabel label = new JLabel(PropertiesLoader.get("starterCodeDirPrompt") + ": ");
    JButton startingDirectoryButton = new JButton(
        startingDir == null ? PropertiesLoader.get("selectButtonHint") :
            startingDir.substring(startingDir.lastIndexOf(File.separator) + 1));
    startingDirectoryButton.addActionListener(e -> handleSelectStartingDirectory());

    JButton continueButton = new JButton(PropertiesLoader.get("continueButton"));
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
      formPanel.add(new JLabel(PropertiesLoader.get("includeFilesPrompt")), gbc);
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
    File startDir = new File(PropertiesLoader.get("starterCodeSelectStartingDir"));
    if (!startDir.exists()) {
      startDir = new File(".");
    }
    fileChooser.setCurrentDirectory(startDir);
    fileChooser.setDialogTitle(PropertiesLoader.get("selectButtonHint"));
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
    if (BuilderData.getStarterCodeDir() == null) {
      JOptionPane.showMessageDialog(frame, PropertiesLoader.get("starterCodeSelectError"),
          PropertiesLoader.get("errorTitle"), JOptionPane.ERROR_MESSAGE);
      return;
    }
    InterfaceUtils.hideFrame(panel);
    Arrays.stream(this.checkboxTree.getCheckingPaths()).forEach(path -> {
      File filePath = new File(BuilderData.getStarterCodeDir(),
          Arrays.stream(path.getPath()).skip(1).map(String::valueOf)
              .collect(Collectors.joining(File.separator)));
      if (filePath.exists() && filePath.isFile()) {
        BuilderData.addTemplateFile(filePath.getAbsolutePath());
      }
    });
    new ConfigurationOptions();
  }

  private void generateCheckboxTree() {
    FileTree fileTree = new FileTree(BuilderData.getStarterCodeDir());
    this.checkboxTree = new CheckboxTree(fileTree.getRoot());
    this.checkboxTree.getCheckingModel().setCheckingMode(TreeCheckingModel.CheckingMode.PROPAGATE);
    this.checkboxTree.setCellRenderer(new TreeCellRenderer());
    this.checkboxTree.expandAll();
  }
}