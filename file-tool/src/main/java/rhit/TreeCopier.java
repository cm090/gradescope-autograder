package rhit;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

// Taken from java example. So verbose!
class TreeCopier implements TreeVisitor {
  private static final String PROJECT_FILE =
      Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResourceAsStream(".project"))
          .toString();

  private final Path source;
  private final Path target;
  private int numFilesCopied;

  TreeCopier(Path source, Path target) {
    this.source = source;
    this.target = target;
    this.numFilesCopied = 0;
  }

  @Override
  public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
      throws IOException {
    // before visiting entries in a directory we copy the directory
    // (okay if directory already exists).
    Path newDir = target.resolve(source.relativize(dir));
    try {
      Files.copy(dir, newDir);
    } catch (FileAlreadyExistsException x) {
      // ignore
    }
    return CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
    if (file.toString().contains(".project")) {
      Path newFile = target.resolve(source.relativize(file));
      Files.createFile(newFile);
      String content = PROJECT_FILE.replace("PROJECT_NAME",
          newFile.getParent().getFileName().toString());
      Files.write(newFile, content.getBytes());
    } else {
      Files.copy(file, target.resolve(source.relativize(file)),
          StandardCopyOption.REPLACE_EXISTING);
      numFilesCopied++;
    }
    return CONTINUE;
  }

  @Override
  public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
    return CONTINUE;
  }

  @Override
  public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
    String error = String.format("Unable to copy: %s: %s%n", file, exc);
    throw new IOException(error);
  }

  public int getNumFilesCopied() {
    return numFilesCopied;
  }
}
