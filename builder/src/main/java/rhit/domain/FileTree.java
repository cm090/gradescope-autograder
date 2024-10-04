package rhit.domain;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;

@Getter
public class FileTree {
  private final Node root;

  public FileTree(String path) {
    this.root = parseFileTree(path);
  }

  private static Node parseFileTree(String path) {
    String filePath = path.substring(0, path.lastIndexOf(File.separatorChar));
    String fileName = path.substring(path.lastIndexOf(File.separatorChar) + 1);
    File currentFile = new File(filePath, fileName);
    if (currentFile.isDirectory()) {
      Node current = Node.builder().path(filePath).name(fileName).type(FileType.DIRECTORY).build();
      for (File file : Objects.requireNonNull(currentFile.listFiles())) {
        Node next = parseFileTree(file.getAbsolutePath());
        current.children.add(next);
      }
      return current;
    } else {
      String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
      FileType type = extension.equals("java") ? FileType.JAVA : FileType.NON_JAVA;
      return Node.builder().path(filePath).name(fileName).type(type).build();
    }
  }

  @Builder
  @Getter
  public static class Node {
    private final List<Node> children = new ArrayList<>();
    private String path;
    private String name;
    private FileType type;
  }
}
