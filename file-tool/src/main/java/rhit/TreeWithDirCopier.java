package rhit;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

// Taken from java example. So verbose!
class TreeWithDirCopier implements TreeVisitor {
  private final Path source;
  private final Path target;
  private int numFilesCopied;

  TreeWithDirCopier(Path source, Path target) {
    this.source = source;
    this.target = target;
    this.numFilesCopied = 0;
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
    return CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
    // first, read the file to see package info
    InputStream in = null;
    String packageName = null;
    boolean found = false;
    try {
      in = Files.newInputStream(file);
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));

      String line;
      while ((line = reader.readLine()) != null && !found) {
        if (line.trim().startsWith("package")) {
          line = line.trim();
          int spaceIndex = line.indexOf(" ");
          packageName = line.substring(spaceIndex + 1);
          packageName = packageName.substring(0, packageName.indexOf(";"));
          found = true;
        }
      }
    } finally {
      if (in != null) {
        in.close();
      }
    }

    Path p;
    Path preamble;
    if (found) {
      // create directory path object from root up to but not including the file name
      preamble = target.resolve(source.relativize(file)).getParent();

      // add the name of the package to the end of that path
      p = preamble.resolve(Paths.get(packageName));

      // if the directory does not exist, make it
      if (!(p.toFile()).exists()) {
        if (!p.toFile().mkdirs()) {
          throw new IOException("Unable to create directory: " + p);
        }
      }

      // add the file name to the end of the path.
      p = p.resolve(target.resolve(source.relativize(file)).getFileName());

    } else {
      // otherwise, set p to the absolute path to the src directory
      p = target.resolve(source.relativize(file));
    }

    Files.copy(file, p, StandardCopyOption.REPLACE_EXISTING);
    numFilesCopied++;
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
