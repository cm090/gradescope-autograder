package rhit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class FileToolGui implements ActionListener, Runnable {
  private static final String ELLIPSIS = "...";

  private final JPanel configPanes;
  private final JFrame frame;
  private final JButton masterButton;
  private final JButton studentButton;
  private final JButton outputButton;
  private final JTextArea outputArea;
  private final JButton startButton;
  private final FileToolCli fileToolCli;

  public FileToolGui() {
    frame = new JFrame(PropertiesLoader.get("frameTitle"));

    configPanes = new JPanel();
    configPanes.setLayout(new BoxLayout(configPanes, BoxLayout.PAGE_AXIS));

    masterButton = new JButton();
    studentButton = new JButton();
    outputButton = new JButton();

    fileToolCli = new FileToolCli();

    addConfigButton(PropertiesLoader.get("starterCodeDirectoryDescription"), masterButton);
    addConfigButton(PropertiesLoader.get("submissionDirectoryDescription"), studentButton);
    addConfigButton(PropertiesLoader.get("outputDirectoryDescription"), outputButton);
    outputButton.setText(PropertiesLoader.get("defaultOutputLocationHint"));

    JPanel bigButtonPanel = new JPanel();
    bigButtonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    startButton = new JButton(PropertiesLoader.get("generateButtonText"));
    startButton.addActionListener(e -> startGenerate());
    bigButtonPanel.add(startButton);
    configPanes.add(bigButtonPanel);
    outputArea = new JTextArea(PropertiesLoader.get("outputAreaStartingMessage"));
    JScrollPane lowerPanel = new JScrollPane(outputArea);

    frame.add(configPanes, BorderLayout.NORTH);
    frame.add(lowerPanel, BorderLayout.CENTER);

    frame.setSize(800, 400);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
  }

  private void addConfigButton(String description, AbstractButton button) {
    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    panel.setLayout(new BorderLayout());
    JLabel label = new JLabel(description);
    int labelHeight = label.getPreferredSize().height;
    label.setPreferredSize(new Dimension(400, labelHeight));
    panel.add(label, BorderLayout.WEST);
    button.setText(PropertiesLoader.get("selectDirectoryHint") + ELLIPSIS);
    button.addActionListener(this);
    panel.add(button);
    configPanes.add(panel);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    JFileChooser chooser = new JFileChooser();
    chooser.setCurrentDirectory(new File("."));
    chooser.setDialogTitle(PropertiesLoader.get("directorySelectorTitle"));
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

    // disable the "All files" option.
    chooser.setAcceptAllFileFilterUsed(false);
    if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
      ((JButton) e.getSource()).setText(chooser.getSelectedFile().getAbsolutePath());
    }
  }

  private void startGenerate() {
    if (masterButton.getText().equals(PropertiesLoader.get("selectDirectoryHint") + ELLIPSIS)) {
      outputArea.setText(PropertiesLoader.get("generateWithoutAssigmentDirectory") + ELLIPSIS);
    }
    if (studentButton.getText().equals(PropertiesLoader.get("selectDirectoryHint") + ELLIPSIS)) {
      outputArea.setText(PropertiesLoader.get("noSubmissionDirectorySelected"));
      return;
    }

    outputArea.setText(PropertiesLoader.get("startingGeneration") + ELLIPSIS);
    startButton.setEnabled(false);
    Thread t = new Thread(this);
    t.start();
  }

  @Override
  public void run() {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(os, false, StandardCharsets.UTF_8);

    File master = new File(masterButton.getText());

    String outputText;

    if (outputButton.getText().equals(PropertiesLoader.get("defaultOutputLocationHint"))) {
      outputText = Paths.get(studentButton.getText(), "projects").toString();
      ps.printf(PropertiesLoader.get("outputDirectoryMessage") + "\n", outputText);
    } else {
      outputText = outputButton.getText();
    }

    File output = new File(outputText);

    if (Files.exists(output.toPath()) && !output.toPath().equals(master.toPath())) {
      try {
        //noinspection resource, ResultOfMethodCallIgnored
        Files.walk(output.toPath()).sorted(Comparator.reverseOrder()).map(Path::toFile)
            .forEach(File::delete);
      } catch (IOException e) {
        e.printStackTrace(ps);
      }
      ps.println(PropertiesLoader.get("clearingOutputDirectoryMessage") + ELLIPSIS);
    }
    if (!output.mkdirs()) {
      ps.println(PropertiesLoader.get("outputDirectoryCreationError"));
    }

    File student = new File(studentButton.getText());
    if (masterButton.getText().equals(PropertiesLoader.get("selectDirectoryHint") + ELLIPSIS)) {
      try {
        fileToolCli.doRename(student, ps, output);
      } catch (Exception e) {
        e.printStackTrace(ps);
      }
    } else {
      try {
        fileToolCli.doGenerate(master, student, output, ps);
      } catch (Exception e) {
        e.printStackTrace(ps);
      }
    }
    outputArea.setText(os.toString(StandardCharsets.UTF_8));
    startButton.setEnabled(true);
  }
}
