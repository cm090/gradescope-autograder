package rhit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class BuilderInterface {
  private final JFrame frame = new JFrame("Autograder Builder");
  private final JPanel configPanes = new JPanel();
  private final JButton homeworkDirButton = new JButton();
  private final JButton templateDirButton = new JButton();
  private final JButton compileDirButton = new JButton();
  private final JButton startButton = new JButton("GENERATE AUTOGRADER");
  private final JTextArea outputArea = new JTextArea("Enter directories and then GENERATE");

  private final AutograderBuilder autograderBuilder =
      new AutograderBuilder(homeworkDirButton, outputArea, templateDirButton, compileDirButton,
          startButton, frame);

  BuilderInterface() {
    configPanes.setLayout(new BoxLayout(configPanes, BoxLayout.PAGE_AXIS));
    addConfigButton("Original assignment project directory, from 220 repo", homeworkDirButton);
    addConfigButton("Template folder inside the gradescope-autograder repo", templateDirButton);
    templateDirButton.setEnabled(false);
    addConfigButton("Output directory", compileDirButton);
    compileDirButton.setEnabled(false);

    JPanel bigButtonPanel = new JPanel();
    bigButtonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    startButton.addActionListener(e -> autograderBuilder.startGenerate());
    bigButtonPanel.add(startButton);
    configPanes.add(bigButtonPanel);
    JScrollPane lowerPanel = new JScrollPane(outputArea);

    frame.add(configPanes, BorderLayout.NORTH);
    frame.add(lowerPanel, BorderLayout.CENTER);
    frame.setSize(700, 300);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
  }

  private void addConfigButton(String description, JButton button) {
    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    panel.setLayout(new BorderLayout());
    JLabel label = new JLabel(description);
    int labelHeight = label.getPreferredSize().height;
    label.setPreferredSize(new Dimension(400, labelHeight));
    panel.add(label, BorderLayout.WEST);
    button.setText("Select a directory");
    button.addActionListener(new ButtonActionListener(frame, homeworkDirButton, autograderBuilder));
    panel.add(button);
    configPanes.add(panel);
  }
}
