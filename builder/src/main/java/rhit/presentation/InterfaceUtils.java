package rhit.presentation;

import javax.swing.JFrame;
import javax.swing.JPanel;
import lombok.Getter;
import lombok.Setter;

public class InterfaceUtils {
  @Getter
  @Setter
  private static JFrame frame;

  static void updateFrame() {
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  static void hideFrame(JPanel panel) {
    panel.removeAll();
    frame.getContentPane().removeAll();
    frame.setVisible(false);
  }
}
