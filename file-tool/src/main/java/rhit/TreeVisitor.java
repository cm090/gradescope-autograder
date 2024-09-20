package rhit;

import java.nio.file.FileVisitor;
import java.nio.file.Path;

public interface TreeVisitor extends FileVisitor<Path> {
  int getNumFilesCopied();
}
