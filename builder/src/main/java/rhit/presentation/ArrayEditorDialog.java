package rhit.presentation;

import java.awt.BorderLayout;
import java.awt.GridLayout;
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
import rhit.domain.PropertiesLoader;

class ArrayEditorDialog extends JDialog {
  private final DefaultListModel<String> listModel;
  private final JList<String> itemList;
  private final JSONArray array;
  private final boolean isObjectArray;

  @SuppressWarnings("unchecked")
  ArrayEditorDialog(JFrame parent, JSONArray array) {
    super(parent, PropertiesLoader.get("arrayEditorTitle"), true);
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
    buttonPanel.setLayout(new GridLayout(1, 4));

    JButton addButton = getAddButton();
    buttonPanel.add(addButton);

    JButton editButton = getEditButton();
    buttonPanel.add(editButton);

    JButton removeButton = new JButton(PropertiesLoader.get("removeButton"));
    removeButton.addActionListener(e -> {
      if (array.size() < 2) {
        JOptionPane.showMessageDialog(ArrayEditorDialog.this, PropertiesLoader.get("removeError"));
        return;
      }
      int selectedIndex = itemList.getSelectedIndex();
      if (selectedIndex != -1) {
        listModel.remove(selectedIndex);
        array.remove(selectedIndex);
      }
    });
    buttonPanel.add(removeButton);

    JButton doneButton = new JButton(PropertiesLoader.get("doneButton"));
    doneButton.addActionListener(e -> dispose());
    buttonPanel.add(doneButton);

    add(buttonPanel, BorderLayout.SOUTH);

    setSize(400, 300);
    setLocationRelativeTo(getParent());
  }

  private JButton getAddButton() {
    JButton addButton = new JButton(PropertiesLoader.get("addButton"));
    addButton.addActionListener(e -> handleAddButtonClick());
    return addButton;
  }

  @SuppressWarnings("unchecked")
  private void handleAddButtonClick() {
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
      String newItem = JOptionPane.showInputDialog(ArrayEditorDialog.this,
          PropertiesLoader.get("addItem") + ":");
      if (newItem != null && !newItem.trim().isEmpty()) {
        listModel.addElement(newItem);
        array.add(newItem);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void generateEditableObject(JSONObject newItem) {
    ((JSONObject) array.get(0)).keySet().forEach(
        key -> InterfaceUtils.invokeClassMethod(((JSONObject) array.get(0)).get(key), (String) key,
            "", "0", (value) -> newItem.put(key, value)));
  }

  private JButton getEditButton() {
    JButton editButton = new JButton(PropertiesLoader.get("editButton"));
    editButton.addActionListener((e) -> handleEditButtonClick());
    return editButton;
  }

  @SuppressWarnings("unchecked")
  private void handleEditButtonClick() {
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
      String newItem = JOptionPane.showInputDialog(ArrayEditorDialog.this,
          PropertiesLoader.get("editItem") + ":", currentItem);
      if (newItem != null && !newItem.trim().isEmpty()) {
        listModel.setElementAt(newItem, selectedIndex);
        array.set(selectedIndex, newItem);
      }
    }
  }
}