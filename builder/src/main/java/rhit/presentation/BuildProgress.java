package rhit.presentation;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import rhit.domain.BuildRunner;

public class BuildProgress {
  private final JFrame frame;

  BuildProgress() {
    this.frame = InterfaceUtils.getFrame();
    displayBuildProgress();
  }

  private void displayBuildProgress() {
    JTextArea textArea = new JTextArea();
    textArea.setEditable(false);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);

    JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    frame.add(scrollPane);

    BuildRunner runner = new BuildRunner(textArea);

    frame.setSize(300, 300);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
    runner.processBuild();
  }
}
