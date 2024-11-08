package rhit.presentation;

import eu.essilab.lablib.checkboxtree.CheckboxTree;
import eu.essilab.lablib.checkboxtree.CheckboxTreeCellRenderer;
import eu.essilab.lablib.checkboxtree.TreeCheckingModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.TreePath;

/**
 * @see <a href="https://github.com/lorebiga/CheckboxTree/tree/master/src/main/java/eu/essilab/lablib/checkboxtree">Source</a>
 * @see <a href="https://www.infoworld.com/article/2175895/swing-based-tree-layouts-with-checkboxtree.html">Example</a>
 */
class TreeCellRenderer implements CheckboxTreeCellRenderer {
  final JCheckBox checkBox = new JCheckBox();
  final JPanel panel = new JPanel();
  final JLabel label = new JLabel();

  TreeCellRenderer() {
    label.setFocusable(true);
    label.setOpaque(true);
    panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    panel.add(checkBox);
    panel.add(label);
    checkBox.setBackground(UIManager.getColor("Tree.textBackground"));
    panel.setBackground(UIManager.getColor("Tree.textBackground"));
  }

  public boolean isOnHotspot(int x, int y) {
    return (checkBox.getBounds().contains(x, y));
  }

  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
                                                boolean expanded, boolean leaf, int row,
                                                boolean hasFocus) {
    label.setText(value.toString());
    if (selected) {
      label.setBackground(UIManager.getColor("Tree.selectionBackground"));
    } else {
      label.setBackground(UIManager.getColor("Tree.textBackground"));
    }
    TreeCheckingModel checkingModel = ((CheckboxTree) tree).getCheckingModel();
    TreePath path = tree.getPathForRow(row);
    boolean enabled = checkingModel.isPathEnabled(path);
    boolean checked = checkingModel.isPathChecked(path);
    boolean grayed = checkingModel.isPathGreyed(path);
    checkBox.setEnabled(enabled);
    if (grayed) {
      label.setForeground(Color.lightGray);
    } else {
      label.setForeground(Color.black);
    }
    checkBox.setSelected(checked);
    return panel;
  }
}