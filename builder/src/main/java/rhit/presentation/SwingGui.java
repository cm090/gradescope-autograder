package rhit.presentation;

import javax.swing.JFrame;
import lombok.Setter;

abstract class SwingGui {
  @Setter
  private static SwingGui visibleFrame;

  static void showFrame() {
    if (visibleFrame == null) {
      throw new IllegalStateException(
          "No frame to show. There is a problem with the internal configuration.");
    }
    visibleFrame.show();
  }

  void verifyFrame(JFrame frame) {
    if (frame == null) {
      throw new IllegalStateException("Failed to initialize frame");
    }
  }

  abstract void show();

  abstract void handleContinue();
}
