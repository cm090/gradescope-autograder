import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

public class Selector implements ItemListener {
    private static final String[] TOOLS = new String[] { "File Tool", "Rename Tool" };
    private JToggleButton toggleButton;
    private FileToolGUI parent;

    public Selector(JPanel configPanes, FileToolGUI parent) {
        toggleButton = new JToggleButton();
        this.parent = parent;

        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setLayout(new BorderLayout());
        JLabel label = new JLabel("Select which mode you'd like to run:");
        int labelHeight = label.getPreferredSize().height;
        label.setPreferredSize(new Dimension(400, labelHeight));
        panel.add(label, BorderLayout.WEST);
        toggleButton.setText(TOOLS[0]);
        toggleButton.addItemListener(this);
        panel.add(toggleButton);
        configPanes.add(panel);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (toggleButton.isSelected())
            toggleButton.setText(TOOLS[1]);
        else
            toggleButton.setText(TOOLS[0]);

        parent.toggleRenameTool();
    }

    public boolean isRenameTool() {
        return toggleButton.isSelected();
    }
}
