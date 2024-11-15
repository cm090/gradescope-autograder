package rhit.presentation;

import javax.swing.JFrame;
import rhit.domain.PropertiesLoader;

public class AutograderBuilder {
  public static void main(String[] args) {
    JFrame frame = new JFrame(PropertiesLoader.get("windowTitle"));
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    InterfaceUtils.setFrame(frame);

    SwingGui.setVisibleFrame(new DirectorySelector());
    SwingGui.showFrame();
  }
}
