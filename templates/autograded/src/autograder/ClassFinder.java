package autograder;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A utility class that finds all classes in a given package.
 *
 * @see <a href="https://stackoverflow.com/a/15519745">source</a>
 */
public class ClassFinder {
  private static final char PACKAGE_SEPARATOR = '.';
  private static final char DIRECTORY_SEPARATOR = '/';
  private static final String CLASS_FILE_EXTENSION = ".class";
  private static final String PACKAGE_ERROR =
      "Unable to get resources from path '%s'. Are you sure the package '%s' exists?";

  static Set<Class<?>> find(String packageName) {
    String scannedPath = packageName.replace(PACKAGE_SEPARATOR, DIRECTORY_SEPARATOR);
    URL scannedUrl = Thread.currentThread().getContextClassLoader().getResource(scannedPath);
    if (scannedUrl == null) {
      throw new IllegalArgumentException(String.format(PACKAGE_ERROR, scannedPath, packageName));
    }
    File scannedDir = new File(scannedUrl.getFile());
    if (!scannedDir.canRead()) {
      throw new IllegalArgumentException(String.format(PACKAGE_ERROR, scannedPath, packageName));
    }
    Set<Class<?>> classes = new HashSet<>();
    for (File file : Objects.requireNonNull(scannedDir.listFiles())) {
      classes.addAll(find(file, packageName));
    }
    return classes;
  }

  private static Set<Class<?>> find(File location, String packageName) {
    Set<Class<?>> classes = new HashSet<>();
    String resource = packageName + PACKAGE_SEPARATOR + location.getName();
    if (location.isDirectory()) {
      for (File child : Objects.requireNonNull(location.listFiles())) {
        classes.addAll(find(child, resource));
      }
    } else if (resource.endsWith(CLASS_FILE_EXTENSION)) {
      int endIndex = resource.length() - CLASS_FILE_EXTENSION.length();
      String className = resource.substring(0, endIndex);
      try {
        classes.add(Class.forName(className));
      } catch (ClassNotFoundException ignore) {
        // Do nothing
      }
    }
    return classes;
  }
}
