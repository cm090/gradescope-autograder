package rhit.domain;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import javax.swing.tree.TreeNode;
import lombok.Builder;
import lombok.Getter;

@Getter
public class FileTree {
  private final FileTreeNode root;

  public FileTree(String path) {
    this.root = parseFileTree(path, 0, null);
  }

  private static FileTreeNode parseFileTree(String path, int index, FileTreeNode parent) {
    String filePath = path.substring(0, path.lastIndexOf(File.separatorChar));
    String fileName = path.substring(path.lastIndexOf(File.separatorChar) + 1);
    if (shouldIgnoreFile(fileName)) {
      return null;
    }
    File currentFile = new File(filePath, fileName);
    if (currentFile.isDirectory()) {
      FileTreeNode current =
          FileTreeNode.builder().index(index).parent(parent).path(filePath).name(fileName).build();
      for (File file : Objects.requireNonNull(currentFile.listFiles())) {
        FileTreeNode next = parseFileTree(file.getAbsolutePath(), index + 1, current);
        if (next != null) {
          current.children.add(next);
        }
      }
      return current.getChildCount() > 0 ? current : null;
    } else {
      return FileTreeNode.builder().index(index).parent(parent).path(filePath).name(fileName)
          .build();
    }
  }

  private static boolean shouldIgnoreFile(String fileName) {
    return fileName.startsWith(".") || fileName.equals("bin") || fileName.endsWith(".class");
  }

  @Builder
  private static class FileTreeNode implements TreeNode {
    private final List<FileTreeNode> children = new ArrayList<>();
    private FileTreeNode parent;
    private int index;

    @Getter
    private String path;

    @Getter
    private String name;

    @Override
    public TreeNode getChildAt(int childIndex) {
      return children.get(childIndex);
    }

    @Override
    public int getChildCount() {
      return children.size();
    }

    @Override
    public TreeNode getParent() {
      return parent;
    }

    @Override
    public int getIndex(TreeNode node) {
      return index;
    }

    @Override
    public boolean getAllowsChildren() {
      return true;
    }

    @Override
    public boolean isLeaf() {
      return children.isEmpty();
    }

    @Override
    public Enumeration<? extends TreeNode> children() {
      return Collections.enumeration(children);
    }

    @Override
    public String toString() {
      return name;
    }
  }
}
