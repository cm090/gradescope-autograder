package newAutograder;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class ClassFinder {
    private static final char PACKAGE_SEPARATOR = '.';
    private static final char DIRECTORY_SEPARATOR = '/';
    private static final String CLASS_FILE_EXTENSION = ".class";
    private static final String PACKAGE_ERROR = "";

    static Set<Class<?>> find(String packageName) {
        return new HashSet<Class<?>>();
    }

    private Set<Class<?>> find(File location, String packageName) {
        return new HashSet<Class<?>>();
    }
}
