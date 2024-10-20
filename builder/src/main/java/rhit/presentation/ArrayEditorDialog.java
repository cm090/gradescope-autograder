package rhit.presentation;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.lang.reflect.InvocationTargetException;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@SuppressWarnings("unchecked")
public class ArrayEditorDialog extends JDialog {
  private final DefaultListModel<String> listModel;
  private final JList<String> itemList;
  private final JSONArray array;
  private final boolean isObjectArray;

  public ArrayEditorDialog(JFrame parent, JSONArray array) {
    super(parent, "Edit Array (close when finished)", true);
    this.array = array;
    this.listModel = new DefaultListModel<>();
    array.forEach(item -> listModel.addElement(item.toString()));
    this.itemList = new JList<>(listModel);
    this.isObjectArray = array.get(0) instanceof JSONObject;
    buildInterface();
  }

  private void buildInterface() {
    setLayout(new BorderLayout());

    JScrollPane scrollPane = new JScrollPane(itemList);
    add(scrollPane, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(1, 3));

    JButton addButton = getAddButton();
    buttonPanel.add(addButton);

    JButton editButton = getEditButton();
    buttonPanel.add(editButton);

    JButton removeButton = new JButton("Remove");
    removeButton.addActionListener(e -> {
      if (array.size() < 2) {
        JOptionPane.showMessageDialog(ArrayEditorDialog.this,
            "The array cannot be empty! Add another item to remove this one.");
        return;
      }
      int selectedIndex = itemList.getSelectedIndex();
      if (selectedIndex != -1) {
        listModel.remove(selectedIndex);
        array.remove(selectedIndex);
      }
    });
    buttonPanel.add(removeButton);

    add(buttonPanel, BorderLayout.SOUTH);

    setSize(400, 300);
    setLocationRelativeTo(getParent());
  }

  private JButton getAddButton() {
    JButton addButton = new JButton("Add");
    addButton.addActionListener(e -> {
      if (isObjectArray) {
        JSONObject newItem = new JSONObject();
        generateEditableObject(newItem);
        MultipleInputDialog dialog = new MultipleInputDialog(newItem);
        dialog.addCallback(() -> {
          listModel.addElement(newItem.toString());
          array.add(newItem);
        });
        dialog.show();
      } else {
        String newItem = JOptionPane.showInputDialog(ArrayEditorDialog.this, "Enter new item:");
        if (newItem != null && !newItem.trim().isEmpty()) {
          listModel.addElement(newItem);
          array.add(newItem);
        }
      }
    });
    return addButton;
  }

  private void generateEditableObject(JSONObject newItem) {
    // TODO: Use appropriate constructors
    ((JSONObject) array.get(0)).keySet().forEach(key -> {
      try {
        newItem.put(key,
            ((JSONObject) array.get(0)).get(key).getClass().getDeclaredConstructor()
                .newInstance());
      } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
               InvocationTargetException ex) {
        throw new RuntimeException(ex);
      }
    });
  }

  private JButton getEditButton() {
    JButton editButton = new JButton("Edit");
    editButton.addActionListener(e -> {
      int selectedIndex = itemList.getSelectedIndex();
      if (selectedIndex == -1) {
        return;
      }
      if (isObjectArray) {
        JSONObject currentItem = (JSONObject) array.get(selectedIndex);
        MultipleInputDialog dialog = new MultipleInputDialog(currentItem);
        dialog.addCallback(() -> {
          listModel.setElementAt(currentItem.toString(), selectedIndex);
          array.set(selectedIndex, currentItem);
        });
        dialog.show();
      } else {
        String currentItem = listModel.getElementAt(selectedIndex);
        String newItem =
            JOptionPane.showInputDialog(ArrayEditorDialog.this, "Edit item:", currentItem);
        if (newItem != null && !newItem.trim().isEmpty()) {
          listModel.setElementAt(newItem, selectedIndex);
          array.set(selectedIndex, newItem);
        }
      }
    });
    return editButton;
  }
}