package rhit.domain;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JTextArea;
import org.apache.commons.io.FileUtils;

public class BuildRunner {
  private final JTextArea logOutput;
  private File templateDir;
  private File compileDir;

  public BuildRunner(JTextArea logOutput) {
    this.logOutput = logOutput;
  }

  public void processBuild() {
    templateDir = new File(BuilderData.getTemplateDir());
    compileDir = new File(BuilderData.getOutputDir(), "autograder");

    copyTemplateDir();
    if (BuilderData.getTemplateType() == TemplateType.AUTO) {
      copyHomeworkTestFiles();
    }
    updateConfigFile();
    compressOutput();

    logOutput.append("Done! Opening output directory...");
    try {
      Desktop.getDesktop().open(compileDir);
    } catch (IOException e) {
      logOutput.append("Error opening output directory: " + e.getMessage());
    }
  }

  private void copyTemplateDir() {
    if (!templateDir.exists() || !templateDir.isDirectory()) {
      logOutput.append(
          "Template directory " + templateDir + " does not exist or is not a directory.\n");
      return;
    }
    if (!compileDir.exists() && !compileDir.mkdirs()) {
      logOutput.append("Failed to create compile directory " + compileDir + "\n");
      return;
    }

    try {
      FileUtils.copyDirectory(templateDir, compileDir);
    } catch (Exception e) {
      logOutput.append("Error copying template directory: " + e.getMessage() + "\n");
      return;
    }
    logOutput.append("Copied template directory\n");
  }

  private void copyHomeworkTestFiles() {
    // TODO: Copy files to the correct location
    Set<File> homeworkFiles =
        BuilderData.getTemplateFiles().stream().map(File::new).filter(File::isFile)
            .collect(Collectors.toSet());
    for (File file : homeworkFiles) {
      File out = new File(compileDir, file.getName());
      try {
        FileUtils.copyFile(file, out);
      } catch (Exception e) {
        logOutput.append("Error copying homework file: " + e.getMessage() + "\n");
        return;
      }
      logOutput.append("Copied homework file " + file.getName() + "\n");
    }
  }

  private void updateConfigFile() {
    File runnerFile = new File(compileDir, "config.json");
    try {
      FileWriter fw = new FileWriter(runnerFile);
      fw.write(BuilderData.getConfigOptions().toString());
      fw.close();
    } catch (Exception e) {
      logOutput.append("Error updating config file: " + e.getMessage() + "\n");
      return;
    }
    logOutput.append("Updated config file\n");
  }

  public void compressOutput() {
    Path folder = compileDir.toPath();
    Path zipFilePath = new File(compileDir, "autograder.zip").toPath();
    try {
      try (FileOutputStream fos = new FileOutputStream(zipFilePath.toFile());
           ZipOutputStream zos = new ZipOutputStream(fos)) {
        Files.walkFileTree(folder, new SimpleFileVisitor<>() {
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws IOException {
            if (file.toString().contains("autograder.zip")) {
              return FileVisitResult.CONTINUE;
            }
            zos.putNextEntry(new ZipEntry(folder.relativize(file).toString().replace("\\", "/")));
            Files.copy(file, zos);
            zos.closeEntry();
            return FileVisitResult.CONTINUE;
          }
        });
      }
    } catch (Exception e) {
      logOutput.append("Error compressing output: " + e.getMessage() + "\n");
      return;
    }
    logOutput.append("Compressed output to autograder.zip\n");
  }
}
