import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.EnumSet;
import java.util.Map;
import java.util.ArrayList;
import java.util.Scanner;

import org.yaml.snakeyaml.Yaml;

public class Csse220FileTool {

	public static void main(String[] args) throws IOException {
		if(args.length > 0) {
		
			File masterDir = new File(args[0]);
			File studentSubmissionDir = new File(args[1]);
			File outputDir = new File(args[2]);
		
			doGenerate(masterDir, studentSubmissionDir, outputDir, System.out);
		} else {
			FileToolGUI gui = new FileToolGUI();
			gui.openGUI();
		}
				
	}
	
	private static int copyDirTree(Path source, Path target) throws IOException {
		EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
		final int MAX_DIR_DEPTH=20;			
        TreeCopier tc = new TreeCopier(source, target);
        Files.walkFileTree(source, opts, MAX_DIR_DEPTH, tc);
        return tc.getNumFilesCopied();
	}
	
	private static int copyDirWithProjectTree(Path source, Path target) throws IOException {
		EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
		final int MAX_DIR_DEPTH=20;			
        TreeWithDirCopier tc = new TreeWithDirCopier(source, target);
        Files.walkFileTree(source, opts, MAX_DIR_DEPTH, tc);
        return tc.getNumFilesCopied();
	}
	
	private static Path pathAppend(Path source, String addition) {
		return source.resolve(Paths.get(addition));
	}

	/**
	 * Adapted file tool for Gradescope exports
	 * Reads the submission_metadata.yml file and renames the folders to the student's name and id
	 */
	public static void renameFolders(File dir, PrintStream output) throws FileNotFoundException {
		output.println("Found Gradescope data file. Renaming folders...");

		boolean isAnonymous = false;
		Yaml yaml = new Yaml();
        File file = new File(dir, "submission_metadata.yml");
        Map<String, Object> o = yaml.load(new FileInputStream(file));

        for (String s : o.keySet()) {
            Map<String, Object> submission = (Map<String, Object>) o.get(s);
            Map<String, Object> userData = ((Map<String, Object>) ((ArrayList<Object>) submission.get(":submitters")).get(0));
            String name = Normalizer.normalize((String) userData.get(":name"), Form.NFD).replaceAll("\\p{M}", "");
            String sid = "";
            if (!isAnonymous)
	            try {
	            	sid = ((String) userData.get(":email")).split("@")[0];
	            } catch (Exception e) {
	            	output.printf("Grading is anonymous, ignoring student ID\n");
	            	isAnonymous = true;
	            }
            int id = Integer.parseInt(s.split("_")[1]);
            File toRename = new File(dir, "submission_" + id);
            String renameTo = (sid.equals("")) ? name + "_" + id : sid + " " + name + "_" + id;
            toRename.renameTo(new File(dir, renameTo));
			output.printf("Renamed submission_%d to %s\n", id, renameTo);
        }
	}
	
	public static void doGenerate(File masterDir, File studentSubmissionDir, File outputDir, PrintStream output)
			throws IOException, FileNotFoundException {
		if(!masterDir.exists()) {
			throw new IOException("Master dir" + masterDir.getName() + "does not exist");
		}
		if(!studentSubmissionDir.exists()) {
			throw new IOException("Student submission dir" + studentSubmissionDir.getName() + "does not exist");
		}
		if(!outputDir.exists()) {
			throw new IOException("Output dir" + outputDir.getName() + "does not exist");
		}

		if(Files.exists(pathAppend(studentSubmissionDir.toPath(), "submission_metadata.yml"))) {
			renameFolders(studentSubmissionDir, output);
		}
		
		String fullProjectFile = getProjectFileContents(masterDir, output);
		
		for(File submissionDir : studentSubmissionDir.listFiles()) {
			String dirName = submissionDir.getName();
			//if there is no "_" in the filenames it could be the question text
			if(dirName.startsWith(".") ||  dirName.indexOf('_') == -1   || dirName.contains("onlinetext") || dirName.contains("submission_metadata") ) {
				output.printf("Skipping dir %s as probably not a student code submission folder\n", dirName);
				continue;
			}
			String studentName = dirName.substring(0, dirName.indexOf('_'));
			studentName = studentName.replace(' ', '_');
			
			// copy everything from the master dir to student dir
			Path studentOutputDir = pathAppend(outputDir.toPath(), studentName);
			
			
			//check to see if the master has been added already
			if(Files.exists(studentOutputDir) ) {
				throw new IOException("Student submission dir" + studentSubmissionDir.getName() + "already exists, are there extra folders included that should not be?");
			}
			
			int numMasterCopied = copyDirTree(masterDir.toPath(), studentOutputDir);
			
			// copy the student's submissions into the src dir of master
			Path studentOutputDirSrc = pathAppend(studentOutputDir, "src");
	        int numStudentCopied = copyDirWithProjectTree(submissionDir.toPath(), studentOutputDirSrc);
	        
	        // change the name in the project file
	        updateProjectFileWithNewName(fullProjectFile, studentName, studentOutputDir);
	        output.printf("Copied %d master %d student to %s\n", numMasterCopied, numStudentCopied, studentOutputDir.toString());        
		}
		output.printf("Generate completed successfully.\n");
	}

	private static void updateProjectFileWithNewName(String fullProjectFile, String studentName, Path studentOutputDir)
			throws FileNotFoundException {
		Path studentOutputProjectFile = pathAppend(studentOutputDir, ".project");
		int indexOfName = fullProjectFile.indexOf("</name>");
		// there are actually several name fields, but the first one is what we want
		PrintWriter out = new PrintWriter(studentOutputProjectFile.toFile());
		out.print(fullProjectFile.substring(0, indexOfName));
		out.print("_");
		out.print(studentName);
		out.print(fullProjectFile.substring(indexOfName));
		out.close();
	}

	private static String getProjectFileContents(File masterDir, PrintStream output) throws IOException, FileNotFoundException {
		File masterProjectFile = pathAppend(masterDir.toPath(), ".project").toFile();
		if(!masterProjectFile.exists()) {
			throw new IOException("Can't find master project file: " + masterProjectFile.getAbsolutePath());
		}
		String fullProjectFile = new Scanner(masterProjectFile).useDelimiter("\\Z").next();
		output.println("Read project file: " + masterProjectFile.getAbsolutePath());
		return fullProjectFile;
	}

}
