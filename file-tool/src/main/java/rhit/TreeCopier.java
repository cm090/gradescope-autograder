package rhit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.stream.Collectors;

// Taken from java example. So verbose!
class TreeCopier implements TreeVisitor {
  private static final String PROJECT_FILE = ".project";
  private static final String STRING_TO_REPLACE = "PROJECT_NAME";

  private final Path source;
  private final Path target;
  private int numFilesCopied;
  private String projectFileContents;

  TreeCopier(Path source, Path target) {
    this.source = source;
    this.target = target;
    this.numFilesCopied = 0;
    getProjectFileContents();
  }

  private void getProjectFileContents() {
    try (InputStream inputStream = Objects.requireNonNull(
        getClass().getClassLoader().getResourceAsStream(PROJECT_FILE));
         BufferedReader reader = new BufferedReader(
             new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      projectFileContents = reader.lines().collect(Collectors.joining(System.lineSeparator()));
    } catch (IOException e) {
      System.err.println("Error reading project file contents: " + e.getMessage());
    }
  }

  @Override
  public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
    // before visiting entries in a directory we copy the directory
    // (okay if directory already exists).
    Path newDir = target.resolve(source.relativize(dir));
    try {
      Files.copy(dir, newDir);
    } catch (FileAlreadyExistsException x) {
      // ignore
    }
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
    if (file.getFileName().toString().equals(PROJECT_FILE)) {
      Path newFile = target.resolve(source.relativize(file));
      Files.createFile(newFile);
      String content = projectFileContents.replace(STRING_TO_REPLACE,
          newFile.getParent().getFileName().toString());
      Files.write(newFile, content.getBytes());
    } else {
      Files.copy(file, target.resolve(source.relativize(file)),
          StandardCopyOption.REPLACE_EXISTING);
      numFilesCopied++;
    }
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
    return FileVisitResult.CONTINUE;
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
