import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.io.FileUtils;

public class AutograderBuilder implements ActionListener {
	public static void main(String[] args) {
		new AutograderBuilder();
	}

	private JFrame frame;
	private JPanel configPanes;
	private JButton homeworkDirButton, templateDirButton, compileDirButton, startButton;
	private JTextField pointsInput;
	private JTextArea outputArea;
	private ArrayList<String> testClasses;
	private File[] homeworkSubDirectories;

	public AutograderBuilder() {
		frame = new JFrame("Autograder Builder");
		configPanes = new JPanel();
		configPanes.setLayout(new BoxLayout(configPanes, BoxLayout.PAGE_AXIS));
		homeworkDirButton = new JButton();
		addConfigButton("Original assignment project directory, from 220 repo", homeworkDirButton);
		templateDirButton = new JButton();
		addConfigButton("Template folder inside the gradescope-autograder repo", templateDirButton);
		templateDirButton.setEnabled(false);
		compileDirButton = new JButton();
		addConfigButton("Output directory", compileDirButton);
		compileDirButton.setEnabled(false);
		pointsInput = new JTextField(10);
		addConfigInput("Point value", pointsInput);

		JPanel bigButtonPanel = new JPanel();
		bigButtonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		startButton = new JButton("GENERATE AUTOGRADER");
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startGenerate();
			}
		});
		bigButtonPanel.add(startButton);
		configPanes.add(bigButtonPanel);
		outputArea = new JTextArea("Enter directories and then GENERATE");
		JScrollPane lowerPanel = new JScrollPane(outputArea);

		frame.add(configPanes, BorderLayout.NORTH);
		frame.add(lowerPanel, BorderLayout.CENTER);
		frame.setSize(700, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	private void addConfigButton(String description, JButton button) {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.setLayout(new BorderLayout());
		JLabel label = new JLabel(description);
		int labelHeight = label.getPreferredSize().height;
		label.setPreferredSize(new Dimension(400, labelHeight));
		panel.add(label, BorderLayout.WEST);
		button.setText("Select a directory");
		button.addActionListener(this);
		panel.add(button);
		configPanes.add(panel);
	}

	private void addConfigInput(String description, JTextField input) {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.setLayout(new BorderLayout());
		JLabel label = new JLabel(description);
		int labelHeight = label.getPreferredSize().height;
		label.setPreferredSize(new Dimension(400, labelHeight));
		panel.add(label, BorderLayout.WEST);
		panel.add(input);
		configPanes.add(panel);
	}

	private void startGenerate() {
		if (homeworkDirButton.getText().equals("Select a directory")) {
			outputArea.setText("You must choose a homework directory");
			return;
		}
		if (templateDirButton.getText().equals("Select a directory")) {
			outputArea.setText("You must choose the autograder template directory");
			return;
		}
		if (compileDirButton.getText().equals("Select a directory")) {
			outputArea.setText("You must choose the output directory");
			return;
		}
		if (pointsInput.getText().equals("") || !pointsInput.getText().matches("[0-9]+")) {
			outputArea.setText("You must specify a numeric point value");
			return;
		}

		outputArea.setText("Generating autograder...\n");
		startButton.setEnabled(false);
		copyFiles();
		startButton.setEnabled(true);
	}

	private void copyFiles() {
		File templateDir = new File(templateDirButton.getText());
		File compileDir = new File(compileDirButton.getText());

		copyTemplateDir(templateDir, compileDir);

		copyHomeworkTestFiles(homeworkSubDirectories, compileDir);

		updateRunnerFile(compileDir, testClasses);

		compressOutput(compileDir);

		outputArea.append("Done! Opening output directory...");
		try {
			Desktop.getDesktop().open(compileDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Copies the template directory to the output directory
	private void copyTemplateDir(File templateDir, File compileDir) {
		try {
			FileUtils.copyDirectory(templateDir, compileDir);
		} catch (Exception e) {
			outputArea.append("Error copying template directory: " + e.getMessage());
			return;
		}
		outputArea.append("Copied template directory\n");
	}

	// Copies the test files from the homework directory to the output directory
	private void copyHomeworkTestFiles(File[] homeworkSubDirectories, File compileDir) {
		homeworkSubDirectories = Arrays.stream(homeworkSubDirectories).filter(Objects::nonNull).toArray(File[]::new);
		System.out.println(Arrays.toString(homeworkSubDirectories));
		for (File dir : homeworkSubDirectories) {
			for (File f : dir.listFiles()) {
				if (f.toString().toLowerCase().contains("test") && !f.isDirectory()) {
					File out = new File(compileDir, "src/" + dir.getName() + '/' + f.getName());
					try {
						FileUtils.copyFile(f, out);
					} catch (Exception e) {
						outputArea.append("Error copying homework file: " + e.getMessage());
						return;
					}
					outputArea.append("Copied homework file " + f.getName() + "\n");
				}
			}
		}
	}

	// Updates the run.sh file in the output directory
	private void updateRunnerFile(File compileDir, ArrayList<String> testClasses) {
		File runnerFile = new File(compileDir, "run.sh");
		try {
			ArrayList<String> fileContent = new ArrayList<>(Files.readAllLines(runnerFile.toPath()));
			String testClassesString = testClasses.toString().replace("[", "").replace("]", "").replace(",", "");
			fileContent.set(2, fileContent.get(2).replace("\"\"", "\"" + testClassesString + "\""));
			fileContent.set(3, fileContent.get(3).replace("0", pointsInput.getText()));
			Files.write(runnerFile.toPath(), fileContent);
		} catch (Exception e) {
			outputArea.append("Error updating run.sh: " + e.getMessage());
			return;
		}
		outputArea.append("Updated runner file\n");
	}

	// Compresses the output directory into a zip file
	public void compressOutput(File compileDir) {
		Path folder = compileDir.toPath();
		Path zipFilePath = new File(compileDir, "autograder.zip").toPath();
		try {
			FileOutputStream fos = new FileOutputStream(zipFilePath.toFile());
			ZipOutputStream zos = new ZipOutputStream(fos);
			try {
				Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						if (file.toString().contains("autograder.zip"))
							return FileVisitResult.CONTINUE;
						zos.putNextEntry(new ZipEntry(folder.relativize(file).toString().replace("\\", "/")));
						Files.copy(file, zos);
						zos.closeEntry();
						return FileVisitResult.CONTINUE;
					}
				});
			} finally {
				zos.close();
				fos.close();
			}
		} catch (Exception e) {
			outputArea.append("Error compressing output: " + e.getMessage());
			return;
		}
		outputArea.append("Compressed output to autograder.zip\n");
	}

	// Prompts the user to select which test classes to include in the autograder
	private void prepareTestClassesList() {
		testClasses = new ArrayList<String>();
		File homeworkDir = new File(homeworkDirButton.getText() + "/src");
		homeworkSubDirectories = homeworkDir.listFiles((FileFilter) pathname -> pathname.isDirectory());
		for (File dir : homeworkSubDirectories)
			if (dir.isDirectory() && dir.getName().toLowerCase().contains("test"))
				testClasses.add(dir.getName());
		if (testClasses.size() == 0) {
			JOptionPane.showMessageDialog(null,
					"No test classes were found in the homework directory. Please make sure that the homework directory is correct.",
					"No Test Classes Found", JOptionPane.WARNING_MESSAGE);
			return;
		}
		JFrame classSelect = new JFrame("Select Test Classes");
		classSelect.setLayout(new GridLayout(testClasses.size() + 1, 1));
		JCheckBox[] boxes = new JCheckBox[testClasses.size()];
		for (int i = 0; i < testClasses.size(); i++) {
			boxes[i] = new JCheckBox(testClasses.get(i));
			boxes[i].setSelected(true);
			classSelect.add(boxes[i]);
		}
		JButton submit = new JButton("Submit");
		submit.addActionListener(e -> {
			for (int i = 0; i < boxes.length; i++)
				if (!boxes[i].isSelected()) {
					testClasses.remove(boxes[i].getText());
					for (int j = 0; j < homeworkSubDirectories.length; j++)
						if (homeworkSubDirectories[j] != null)
							if (homeworkSubDirectories[j].getName().contains(boxes[i].getText().replace("Test", "")))
								homeworkSubDirectories[j] = null;
				}
			classSelect.dispose();
			templateDirButton.setEnabled(true);
			compileDirButton.setEnabled(true);
		});
		classSelect.add(submit);
		classSelect.pack();
		classSelect.setMinimumSize(new Dimension(200, 100));
		classSelect.setLocationRelativeTo(frame);
		classSelect.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setDialogTitle("Choose a directory");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
			((JButton) e.getSource()).setText(chooser.getSelectedFile().getAbsolutePath());
			if (e.getSource().equals(homeworkDirButton))
				prepareTestClassesList();
		}
	}
}
