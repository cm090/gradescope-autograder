package rhit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.stream.Collectors;

// Taken from java example. So verbose!
class TreeCopier implements FileVisitor<Path> {
  private static final String PROJECT_FILE = ".project";
  private static final String STRING_TO_REPLACE = "PROJECT_NAME";

  private final Path source;
  private final Path target;
  private String projectFileContents;

  TreeCopier(Path source, Path target) {
    this.source = source;
    this.target = target;
    getProjectFileContents();
  }

  private void getProjectFileContents() {
    try (InputStream inputStream = Objects.requireNonNull(
        getClass().getClassLoader().getResourceAsStream(PROJECT_FILE));
         BufferedReader reader = new BufferedReader(
             new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      projectFileContents = reader.lines().collect(Collectors.joining(System.lineSeparator()));
    } catch (IOException e) {
      System.err.println(PropertiesLoader.get("fileReadError") + ": " + e.getMessage());
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
    Path currentFile = file.getFileName();
    Path parentPath = file.getParent();
    boolean shouldSkip = currentFile == null || parentPath == null;
    Path parentFile = shouldSkip ? null : parentPath.getFileName();

    if (!shouldSkip && currentFile.toString().equals(PROJECT_FILE) && parentFile != null) {
      Path newFile = target.resolve(source.relativize(file));
      Files.createFile(newFile);
      String content = projectFileContents.replace(STRING_TO_REPLACE, parentFile.toString());
      Files.writeString(newFile, content, Charset.defaultCharset());
    } else {
      Files.copy(file, target.resolve(source.relativize(file)),
          StandardCopyOption.REPLACE_EXISTING);
    }
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
    throw new IOException(String.format(PropertiesLoader.get("copyError"), file), exc);
  }
}
