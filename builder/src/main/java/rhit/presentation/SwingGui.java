package rhit.presentation;

import lombok.Setter;

abstract class SwingGui {
  @Setter
  private static SwingGui visibleFrame;

  static void showFrame() {
    visibleFrame.show();
  }

  abstract void show();

  abstract void handleContinue();
}
