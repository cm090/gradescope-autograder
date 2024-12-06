package rhit.presentation;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import rhit.domain.BuildRunner;
import rhit.domain.PropertiesLoader;

public class BuildProgress extends SwingGui {
  private static final int FRAME_WIDTH = 300;
  private static final int FRAME_HEIGHT = 300;
  private static final int NUM_ROWS = 1;
  private static final int NUM_COLS = 2;

  private final JFrame frame;

  BuildProgress() {
    this.frame = InterfaceUtils.getFrame();
    super.verifyFrame(frame);
  }

  void show() {
    JTextArea textArea = new JTextArea();
    textArea.setEditable(false);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);

    JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    JButton continueButton = new JButton(PropertiesLoader.get("continueButton"));
    continueButton.addActionListener(e -> handleContinue());

    JButton closeButton = new JButton(PropertiesLoader.get("closeButton"));
    closeButton.addActionListener(e -> frame.dispose());

    frame.setLayout(new BorderLayout());
    frame.add(scrollPane, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel(new GridLayout(NUM_ROWS, NUM_COLS));
    buttonPanel.add(continueButton);
    buttonPanel.add(closeButton);
    frame.add(buttonPanel, BorderLayout.SOUTH);

    frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);

    BuildRunner runner = new BuildRunner(textArea);
    runner.processBuild();
  }

  protected void handleContinue() {
    frame.dispose();
    AutograderBuilder.main(new String[0]);
  }
}
