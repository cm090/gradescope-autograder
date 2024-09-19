package rhit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileVisitOption;
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
  FileToolCli() {
  }

  FileToolCli(String[] args) throws IOException {
    File masterDir = new File(args[0]);
    File studentSubmissionDir = new File(args[1]);
    File outputDir = new File(args[2]);

    doGenerate(masterDir, studentSubmissionDir, outputDir, System.out);
  }

  private static Path pathAppend(Path source, String addition) {
    return source.resolve(Paths.get(addition));
  }

  private void copyDirTree(Path source, TreeVisitor tc) throws IOException {
    EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
    final int MAX_DIR_DEPTH = 20;
    try {
      Files.walkFileTree(source, opts, MAX_DIR_DEPTH, tc);
    } catch (Exception e) {
      throw new IOException("Error copying directory tree", e);
    }
    tc.getNumFilesCopied();
  }

  /**
   * Adapted file tool for Gradescope exports. Reads the submission_metadata.yml file and renames
   * the folders to the student's name and id
   */
  HashSet<String> copyFolders(File dir, File outputDir, PrintStream output,
                              Lambda copier) throws FileNotFoundException {
    output.println("Found Gradescope data file. Copying folders...");

    boolean isAnonymous = false;
    Yaml yaml = new Yaml();
    File file = new File(dir, "submission_metadata.yml");
    Map<String, Object> o = yaml.load(new FileInputStream(file));

    HashSet<String> failed = new HashSet<>();

    for (String s : o.keySet()) {
      @SuppressWarnings("unchecked")
      Map<String, Object> submission = (Map<String, Object>) o.get(s);
      @SuppressWarnings("unchecked")
      Map<String, Object> userData =
          ((Map<String, Object>) ((ArrayList<Object>) submission.get(":submitters")).get(0));
      String name =
          Normalizer.normalize((String) userData.get(":name"), Form.NFD).replaceAll("\\p{M}", "");
      String sid = "";
      if (!isAnonymous) {
        try {
          sid = ((String) userData.get(":email")).split("@")[0];
        } catch (Exception e) {
          output.print("Grading is anonymous, ignoring student ID\n");
          isAnonymous = true;
        }
      }
      int id = Integer.parseInt(s.split("_")[1]);
      Path inputDir = new File(dir, "submission_" + id).toPath();
      String outputDirRelative = String.format("%s_%s_%s", id, sid, name);
      Path outputDirectory = new File(outputDir, outputDirRelative).toPath();
      try {
        boolean useSrc = copier.op(outputDirectory);
        Path trueOutputDir = outputDirectory;
        if (useSrc) {
          trueOutputDir = pathAppend(outputDirectory, "src");
        }
        copyDirTree(inputDir, new TreeWithDirCopier(inputDir, trueOutputDir));
      } catch (IOException e) {
        output.printf("Unable to copy files for student %s. Did they submit the correct files?\n",
            name);
        failed.add(name + " (" + id + ")");
        continue;
      }
      output.printf("Copied submission_%d to %s\n", id, outputDirRelative);
    }

    return failed;
  }

  public void doRename(File studentSubmissionDir, PrintStream output, File outputDir)
      throws IOException {
    if (!studentSubmissionDir.exists()) {
      throw new IOException(
          "Student submission dir" + studentSubmissionDir.getName() + "does not exist");
    }

    if (Files.exists(pathAppend(studentSubmissionDir.toPath(), "submission_metadata.yml"))) {
      HashSet<String> failed = copyFolders(studentSubmissionDir, outputDir, output, (o) -> false);
      output.println("-------------------------------------------");
      output.println("Rename completed successfully.");
      if (!failed.isEmpty()) {
        output.print("The following students could not be copied:\n");
        for (String s : failed) {
          output.println(s);
        }
      }
    }
  }

  public void doGenerate(File masterDir, File studentSubmissionDir, File outputDir,
                         PrintStream output) throws IOException {
    if (!masterDir.exists()) {
      throw new IOException("Master dir" + masterDir.getName() + "does not exist");
    }
    if (!studentSubmissionDir.exists()) {
      throw new IOException(
          "Student submission dir" + studentSubmissionDir.getName() + "does not exist");
    }
    if (!outputDir.exists()) {
      throw new IOException("Output dir" + outputDir.getName() + "does not exist");
    }

    HashSet<String> failed = new HashSet<>();
    if (Files.exists(pathAppend(studentSubmissionDir.toPath(), "submission_metadata.yml"))) {
      failed = copyFolders(studentSubmissionDir, outputDir, output, (oDir) -> {
        copyDirTree(masterDir.toPath(), new TreeCopier(masterDir.toPath(), oDir));
        return true;
      });
    }

    output.println("-------------------------------------------");
    output.println("Generate completed successfully.");
    if (!failed.isEmpty()) {
      output.print("The following students could not be copied:\n");
      for (String s : failed) {
        output.println(s);
      }
    }
  }

  interface Lambda {
    boolean op(Path outputDir) throws IOException;
  }
}
