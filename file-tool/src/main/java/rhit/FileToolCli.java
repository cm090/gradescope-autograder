package rhit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

public class FileToolCli {
  private static final String GRADESCOPE_METADATA_FILE = "submission_metadata.yml";
  private static final String OUTPUT_DIR_FORMAT = "submission_%d";
  private static final String NAME_FIELD = ":name";
  private static final String EMAIL_FIELD = ":email";
  private static final String DEFAULT_NAME = "Unknown";
  private static final String ELLIPSIS = "...";
  private static final String NEW_LINE = "\n";
  private static final String SEPARATOR = "-------------------------------------------";

  FileToolCli() {
  }

  FileToolCli(String[] args) throws IOException {
    if (args.length != 3) {
      throw new IllegalArgumentException(PropertiesLoader.get("argumentsHint"));
    }

    File masterDir = new File(args[0]);
    File studentSubmissionDir = new File(args[1]);
    File outputDir = new File(args[2]);

    doGenerate(masterDir, studentSubmissionDir, outputDir, System.out);
  }

  private static Path pathAppend(Path source, String addition) {
    return source.resolve(Paths.get(addition));
  }

  private void copyDirTree(Path source, FileVisitor<Path> tc) throws IOException {
    EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
    final int MAX_DIR_DEPTH = 20;
    try {
      Files.walkFileTree(source, opts, MAX_DIR_DEPTH, tc);
    } catch (Exception e) {
      throw new IOException(PropertiesLoader.get("directoryTreeCopyError"), e);
    }
  }

  /**
   * Adapted file tool for Gradescope exports. Reads the submission_metadata.yml file and renames
   * the folders to the student's name and id
   */
  HashSet<String> copyFolders(File dir, File outputDir, PrintStream output,
                              OutputDirectoryHandler copier) throws FileNotFoundException {
    output.println(PropertiesLoader.get("dataFileFound") + ELLIPSIS);

    boolean isAnonymous = false;
    Yaml yaml = new Yaml();
    File file = new File(dir, GRADESCOPE_METADATA_FILE);
    Map<String, Object> o = yaml.load(new FileInputStream(file));

    HashSet<String> failed = new HashSet<>();

    for (String s : o.keySet()) {
      @SuppressWarnings("unchecked") Map<String, Object> submission =
          (Map<String, Object>) o.get(s);
      if (submission == null) {
        output.printf(PropertiesLoader.get("submissionDataNull") + NEW_LINE, s);
        continue;
      }
      @SuppressWarnings("unchecked") Map<String, Object> userData =
          ((Map<String, Object>) ((ArrayList<Object>) submission.get(":submitters")).get(0));
      if (userData == null) {
        output.printf(PropertiesLoader.get("submissionMissingUserData") + NEW_LINE, s);
        continue;
      }
      String name = Normalizer.normalize((String) userData.get(NAME_FIELD), Form.NFD)
          .replaceAll("\\p{M}", "");
      if (name.isEmpty()) {
        output.printf(PropertiesLoader.get("submissionMissingName") + NEW_LINE, s);
        name = DEFAULT_NAME;
      }
      String sid = "";
      if (!isAnonymous) {
        try {
          sid = ((String) userData.get(EMAIL_FIELD)).split("@")[0];
        } catch (Exception e) {
          output.print(PropertiesLoader.get("anonymousGradingEnabled") + NEW_LINE);
          isAnonymous = true;
        }
      }
      String[] parts = s.split("_");
      if (parts.length < 2) {
        output.printf(PropertiesLoader.get("submissionKeyFormatError") + NEW_LINE, s);
        continue;
      }
      int id;
      try {
        id = Integer.parseInt(parts[1]);
      } catch (NumberFormatException e) {
        output.printf(PropertiesLoader.get("invalidSubmissionId") + NEW_LINE, parts[1], s);
        continue;
      }
      Path inputDir = new File(dir, String.format(OUTPUT_DIR_FORMAT, id)).toPath();
      String outputDirRelative =
          String.format("%s_%s_%s", id, sid, name).replaceAll("[^a-zA-Z0-9_\\-]", "_");
      Path outputDirectory = new File(outputDir, outputDirRelative).toPath();
      try {
        boolean useSrc = copier.op(outputDirectory);
        Path trueOutputDir = outputDirectory;
        if (useSrc) {
          trueOutputDir = pathAppend(outputDirectory, "src");
        }
        copyDirTree(inputDir, new TreeWithDirCopier(inputDir, trueOutputDir));
      } catch (IOException e) {
        output.printf(PropertiesLoader.get("fileCopyError") + NEW_LINE, name);
        failed.add(name + " (" + id + ")");
        continue;
      }
      output.printf(PropertiesLoader.get("fileCopySuccess") + NEW_LINE, id, outputDirRelative);
    }

    return failed;
  }

  public void doRename(File studentSubmissionDir, PrintStream output, File outputDir)
      throws IOException {
    if (!studentSubmissionDir.exists()) {
      throw new IOException(String.format(PropertiesLoader.get("submissionDirectoryNotFound"),
          studentSubmissionDir.getName()));
    }

    if (Files.exists(pathAppend(studentSubmissionDir.toPath(), GRADESCOPE_METADATA_FILE))) {
      HashSet<String> failed = copyFolders(studentSubmissionDir, outputDir, output, (o) -> false);
      output.println(SEPARATOR);
      output.println(PropertiesLoader.get("renameSuccess"));
      if (!failed.isEmpty()) {
        output.println(PropertiesLoader.get("unableToCopyStudentsMessage") + ":");
        for (String s : failed) {
          output.println(s);
        }
      }
    }
  }

  public void doGenerate(File masterDir, File studentSubmissionDir, File outputDir,
                         PrintStream output) throws IOException {
    if (!masterDir.exists()) {
      throw new IOException(
          String.format(PropertiesLoader.get("masterDirectoryNotFound"), masterDir.getName()));
    }
    if (!studentSubmissionDir.exists()) {
      throw new IOException(String.format(PropertiesLoader.get("submissionDirectoryNotFound"),
          studentSubmissionDir.getName()));
    }
    if (!outputDir.exists()) {
      throw new IOException(
          String.format(PropertiesLoader.get("outputDirectoryNotFound"), outputDir.getName()));
    }

    HashSet<String> failed = new HashSet<>();
    if (Files.exists(pathAppend(studentSubmissionDir.toPath(), GRADESCOPE_METADATA_FILE))) {
      failed = copyFolders(studentSubmissionDir, outputDir, output, (oDir) -> {
        try {
          copyDirTree(masterDir.toPath(), new TreeCopier(masterDir.toPath(), oDir));
          return true;
        } catch (IOException e) {
          throw new IOException(PropertiesLoader.get("directoryTreeCopyError"), e);
        }
      });
    }

    output.println(SEPARATOR);
    output.println(PropertiesLoader.get("generateSuccess"));
    if (!failed.isEmpty()) {
      output.println(PropertiesLoader.get("unableToCopyStudentsMessage") + ":");
      for (String s : failed) {
        output.println(s);
      }
    }
  }

  interface OutputDirectoryHandler {
    boolean op(Path outputDir) throws IOException;
  }
}
