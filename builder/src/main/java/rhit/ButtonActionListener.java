package rhit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

public class ButtonActionListener implements ActionListener {
  private final JFrame frame;
  private final JButton homeworkDirButton;
  private final AutograderBuilder builder;

  ButtonActionListener(JFrame frame, JButton homeworkDirButton, AutograderBuilder builder) {
    this.frame = frame;
    this.homeworkDirButton = homeworkDirButton;
    this.builder = builder;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    JFileChooser chooser = new JFileChooser();
    chooser.setCurrentDirectory(new java.io.File("."));
    chooser.setDialogTitle("Choose a directory");
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    chooser.setAcceptAllFileFilterUsed(false);
    if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
      ((JButton) e.getSource()).setText(chooser.getSelectedFile().getAbsolutePath());
      if (e.getSource().equals(homeworkDirButton)) {
        builder.prepareTestClassesList();
      }
    }
  }
}
