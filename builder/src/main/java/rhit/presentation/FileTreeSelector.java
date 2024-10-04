package rhit.presentation;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class FileTreeSelector {
  private final JFrame frame;
  private JPanel panel;

  public FileTreeSelector() {
    this.frame = InterfaceUtils.getFrame();
    displayFileTreeSelector();
  }

  private void displayFileTreeSelector() {
    JButton continueButton = new JButton("Continue");
    continueButton.addActionListener(e -> handleContinue());

    panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.add(continueButton, BorderLayout.SOUTH);
    frame.add(panel);

    InterfaceUtils.updateFrame();
  }

  private void handleContinue() {
    InterfaceUtils.hideFrame(panel);
    new ConfigurationOptions();
  }
}
