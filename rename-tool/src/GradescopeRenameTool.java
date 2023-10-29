import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;

import org.yaml.snakeyaml.Yaml;

public class GradescopeRenameTool {
    public static void main(String[] args) throws Exception {
        GradescopeRenameTool renameTool = new GradescopeRenameTool();
        renameTool.run();
    }

    public void run() {
        try {
            Path selectedDirectory = selectStartingDirectory();
            File metadataFile = findMetadataFile(selectedDirectory);
            renameDirectories(selectedDirectory, metadataFile);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private Path selectStartingDirectory() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setDialogTitle("Select the directory with student submissions");
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().toPath();
        }
        throw new RuntimeException("No directory selected");
    }

    private File findMetadataFile(Path selectedDirectory) {
        if (Files.exists(selectedDirectory.resolve("submission_metadata.yml"))) {
            return selectedDirectory.resolve("submission_metadata.yml").toFile();
        }
        throw new RuntimeException("Could not find submission_metadata.yml");
    }

    @SuppressWarnings("unchecked")
	private void renameDirectories(Path dir, File metadata) throws FileNotFoundException {
        Yaml yaml = new Yaml();
        Map<String, Object> submissionData = yaml.load(new FileInputStream(metadata));
        for (String id : submissionData.keySet()) {
            Map<String, Object> singleSubmission = (Map<String, Object>) submissionData.get(id);
            List<Object> submitters = (List<Object>) singleSubmission.get(":submitters");
            Map<String, Object> studentData = (Map<String, Object>) submitters.get(0);
            String name = Normalizer.normalize((String) studentData.get(":name"), Form.NFD).replaceAll("\\p{M}", "");
            String uid = ((String) studentData.get(":email")).split("@")[0];
            String newName = String.format("%s_%s_%s", name, uid, id.split("_")[1]);
            File toRename = dir.resolve(id).toFile();
            toRename.renameTo(dir.resolve(newName).toFile());
            System.out.printf("Renamed %s to %s\n", id, newName);
        }
    }
}
