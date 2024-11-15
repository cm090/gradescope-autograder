package rhit.presentation;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import rhit.domain.BuildRunner;
import rhit.domain.PropertiesLoader;

public class BuildProgress extends SwingGui {
  private static final int FRAME_WIDTH = 300;
  private static final int FRAME_HEIGHT = 300;

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

    frame.setLayout(new BorderLayout());
    frame.add(scrollPane, BorderLayout.CENTER);
    frame.add(continueButton, BorderLayout.SOUTH);


    frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);

    BuildRunner runner = new BuildRunner(textArea);
    runner.processBuild();
  }

  void handleContinue() {
    frame.dispose();
  }
}
