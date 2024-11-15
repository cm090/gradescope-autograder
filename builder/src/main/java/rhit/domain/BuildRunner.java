package rhit.domain;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
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

    if (!copyTemplateDir()) {
      throw new RuntimeException("Failed to copy template directory");
    }
    if (BuilderData.getTemplateType() == TemplateType.AUTO) {
      if (!copyHomeworkTestFiles()) {
        throw new RuntimeException("Failed to copy homework test files");
      }
    }
    if (!updateConfigFile()) {
      throw new RuntimeException("Failed to update configuration file");
    }
    if (!compressOutput()) {
      throw new RuntimeException("Failed to compress output");
    }

    logOutput.append(PropertiesLoader.get("buildSuccess") + "\n");
    try {
      Desktop.getDesktop().open(compileDir);
    } catch (IOException e) {
      logOutput.append(
          String.format(PropertiesLoader.get("fileExplorerOpenError"), e.getMessage()) + "\n");
    }
    logOutput.append(PropertiesLoader.get("closeWindowReminder"));
  }

  private boolean copyTemplateDir() {
    if (!templateDir.exists() || !templateDir.isDirectory()) {
      logOutput.append(
          String.format(PropertiesLoader.get("templateDirectoryDoesNotExist"), templateDir) + "\n");
      return false;
    }
    if (!compileDir.exists() && !compileDir.mkdirs()) {
      logOutput.append(
          String.format(PropertiesLoader.get("compileDirectoryFailure"), compileDir) + "\n");
      return false;
    }

    try {
      FileUtils.copyDirectory(templateDir, compileDir);
    } catch (Exception e) {
      logOutput.append(
          String.format(PropertiesLoader.get("templateCopyError"), e.getMessage()) + "\n");
      return false;
    }
    logOutput.append(PropertiesLoader.get("templateCopied") + "\n");
    return true;
  }

  private boolean copyHomeworkTestFiles() {
    Set<File> homeworkFiles =
        BuilderData.getTemplateFiles().stream().map(File::new).filter(File::isFile)
            .collect(Collectors.toSet());
    for (File file : homeworkFiles) {
      Path relativePath =
          new File(BuilderData.getStarterCodeDir()).toPath().relativize(file.toPath());
      File out = new File(compileDir, relativePath.toString());
      try {
        FileUtils.copyFile(file, out);
      } catch (Exception e) {
        logOutput.append(
            String.format(PropertiesLoader.get("homeworkCopyError"), e.getMessage()) + "\n");
        return false;
      }
      logOutput.append(
          String.format(PropertiesLoader.get("homeworkCopied"), file.getName()) + "\n");
    }
    return true;
  }

  private boolean updateConfigFile() {
    File runnerFile = new File(compileDir, BuilderData.CONFIG_FILE);
    try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(runnerFile),
        StandardCharsets.UTF_8)) {
      writer.write(BuilderData.getConfigOptions().toJSONString().replace("\\/", "/"));
    } catch (Exception e) {
      logOutput.append(
          String.format(PropertiesLoader.get("configUpdateError"), e.getMessage()) + "\n");
      return false;
    }
    logOutput.append(PropertiesLoader.get("configUpdated") + "\n");
    return true;
  }

  public boolean compressOutput() {
    Path folder = compileDir.toPath();
    String zipFileName = PropertiesLoader.get("outputZipName") + ".zip";
    Path zipFilePath = new File(compileDir, zipFileName).toPath();
    try {
      try (FileOutputStream fos = new FileOutputStream(zipFilePath.toFile());
           ZipOutputStream zos = new ZipOutputStream(fos)) {
        Files.walkFileTree(folder, new SimpleFileVisitor<>() {
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws IOException {
            if (file.getFileName().equals(zipFilePath.getFileName())) {
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
      return false;
    }
    logOutput.append(String.format(PropertiesLoader.get("compressionSuccess"), zipFileName) + "\n");
    return true;
  }
}
