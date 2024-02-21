import java.nio.file.*;

import java.io.IOException;
import java.nio.file.attribute.*;
import static java.nio.file.FileVisitResult.*;

// Taken from java example. So verbose!
class TreeCopier implements TreeVisitor {
    private static final String PROJECT_FILE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
            "<projectDescription>\r\n" + //
            "\t<name>PROJECT_NAME</name>\r\n" + //
            "\t<comment></comment>\r\n" + //
            "\t<projects>\r\n" + //
            "\t</projects>\r\n" + //
            "\t<buildSpec>\r\n" + //
            "\t\t<buildCommand>\r\n" + //
            "\t\t\t<name>org.eclipse.jdt.core.javabuilder</name>\r\n" + //
            "\t\t\t<arguments>\r\n" + //
            "\t\t\t</arguments>\r\n" + //
            "\t\t</buildCommand>\r\n" + //
            "\t</buildSpec>\r\n" + //
            "\t<natures>\r\n" + //
            "\t\t<nature>org.eclipse.jdt.core.javanature</nature>\r\n" + //
            "\t</natures>\r\n" + //
            "\t<filteredResources>\r\n" + //
            "\t\t<filter>\r\n" + //
            "\t\t\t<id>1670592752340</id>\r\n" + //
            "\t\t\t<name></name>\r\n" + //
            "\t\t\t<type>30</type>\r\n" + //
            "\t\t\t<matcher>\r\n" + //
            "\t\t\t\t<id>org.eclipse.core.resources.regexFilterMatcher</id>\r\n" + //
            "\t\t\t\t<arguments>node_modules|\\.git|__CREATED_BY_JAVA_LANGUAGE_SERVER__</arguments>\r\n"
            + //
            "\t\t\t</matcher>\r\n" + //
            "\t\t</filter>\r\n" + //
            "\t</filteredResources>\r\n" + //
            "</projectDescription>\r\n" + //
            "";

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
