package newAutograder;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

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
            throw new IllegalArgumentException(
                    String.format(PACKAGE_ERROR, scannedPath, packageName));
        }
        File scannedDir = new File(scannedUrl.getFile());
        Set<Class<?>> classes = new HashSet<>();
        for (File file : scannedDir.listFiles()) {
            classes.addAll(find(file, packageName));
        }
        return classes;
    }

    private static Set<Class<?>> find(File location, String packageName) {
        Set<Class<?>> classes = new HashSet<>();
        String resource = packageName + PACKAGE_SEPARATOR + location.getName();
        if (location.isDirectory()) {
            for (File child : location.listFiles()) {
                classes.addAll(find(child, resource));
            }
        } else if (resource.endsWith(CLASS_FILE_EXTENSION)) {
            int endIndex = resource.length() - CLASS_FILE_EXTENSION.length();
            String className = resource.substring(0, endIndex);
            try {
                classes.add(Class.forName(className));
            } catch (ClassNotFoundException ignore) {
            }
        }
        return classes;
    }
}
