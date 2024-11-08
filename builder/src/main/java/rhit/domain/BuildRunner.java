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
    compileDir = new File(BuilderData.getOutputDir());

    copyTemplateDir();
    if (BuilderData.getTemplateType() == TemplateType.AUTO) {
      copyHomeworkTestFiles();
    }
    updateConfigFile();
    compressOutput();

    logOutput.append(PropertiesLoader.get("buildSuccess") + "\n");
    try {
      Desktop.getDesktop().open(compileDir);
    } catch (IOException e) {
      logOutput.append(
          String.format(PropertiesLoader.get("fileExplorerOpenError"), e.getMessage()) + "\n");
    }
    logOutput.append(PropertiesLoader.get("closeWindowReminder"));
  }

  private void copyTemplateDir() {
    if (!templateDir.exists() || !templateDir.isDirectory()) {
      logOutput.append(
          String.format(PropertiesLoader.get("templateDirectoryDoesNotExist"), templateDir) + "\n");
      return;
    }
    if (!compileDir.exists() && !compileDir.mkdirs()) {
      logOutput.append(
          String.format(PropertiesLoader.get("compileDirectoryFailure"), compileDir) + "\n");
      return;
    }

    try {
      FileUtils.copyDirectory(templateDir, compileDir);
    } catch (Exception e) {
      logOutput.append(
          String.format(PropertiesLoader.get("templateCopyError"), e.getMessage()) + "\n");
      return;
    }
    logOutput.append(PropertiesLoader.get("templateCopied") + "\n");
  }

  private void copyHomeworkTestFiles() {
    Set<File> homeworkFiles =
        BuilderData.getTemplateFiles().stream().map(File::new).filter(File::isFile)
            .collect(Collectors.toSet());
    for (File file : homeworkFiles) {
      String relativePath = file.getPath().replace(BuilderData.getStarterCodeDir(), "");
      File out = new File(compileDir, relativePath);
      try {
        FileUtils.copyFile(file, out);
      } catch (Exception e) {
        logOutput.append(
            String.format(PropertiesLoader.get("homeworkCopyError"), e.getMessage()) + "\n");
        return;
      }
      logOutput.append(
          String.format(PropertiesLoader.get("homeworkCopied"), file.getName()) + "\n");
    }
  }

  private void updateConfigFile() {
    File runnerFile = new File(compileDir, BuilderData.CONFIG_FILE);
    try {
      FileWriter fw = new FileWriter(runnerFile);
      fw.write(BuilderData.getConfigOptions().toJSONString().replace("\\/", "/"));
      fw.close();
    } catch (Exception e) {
      logOutput.append(
          String.format(PropertiesLoader.get("configUpdateError"), e.getMessage()) + "\n");
      return;
    }
    logOutput.append(PropertiesLoader.get("configUpdated") + "\n");
  }

  public void compressOutput() {
    Path folder = compileDir.toPath();
    String zipFileName = PropertiesLoader.get("outputZipName") + ".zip";
    Path zipFilePath = new File(compileDir, zipFileName).toPath();
    try {
      try (FileOutputStream fos = new FileOutputStream(zipFilePath.toFile());
           ZipOutputStream zos = new ZipOutputStream(fos)) {
        Files.walkFileTree(folder, new SimpleFileVisitor<>() {
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws IOException {
            if (file.toString().contains(zipFileName)) {
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
      logOutput.append(
          String.format(PropertiesLoader.get("compressionError"), e.getMessage()) + "\n");
      return;
    }
    logOutput.append(String.format(PropertiesLoader.get("compressionSuccess"), zipFileName) + "\n");
  }
}
