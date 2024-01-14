import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class FileToolGUI implements ActionListener, Runnable {
	private static final String DEFAULT_TEXT = "Select a directory...";
	private static final String DEFAULT_OUTPUT_TEXT =
			"<html>Defaults to <code>(submissions)/projects</code>.</html>";
	private JPanel configPanes;
	private JFrame frame;
	private JButton masterButton;
	private JButton studentButton;
	private JButton outputButton;
	private JTextArea outputArea;
	private JButton startButton;

	public FileToolGUI() {
		frame = new JFrame("CSSE220 File Tool");

		configPanes = new JPanel();
		configPanes.setLayout(new BoxLayout(configPanes, BoxLayout.PAGE_AXIS));

		masterButton = new JButton();
		studentButton = new JButton();
		outputButton = new JButton();

		addConfigButton(
				"<html>Original assignment project directory, from 220 repo.<br>If not provided, will only rename folders.</html>",
				masterButton);
		addConfigButton("Student submission directory, unzipped from Moodle or Gradescope",
				studentButton);
		addConfigButton("Workspace output directory", outputButton);
		outputButton.setText(DEFAULT_OUTPUT_TEXT);

		JPanel bigButtonPanel = new JPanel();
		bigButtonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		startButton = new JButton("GENERATE STUDENT PROJECTS FOR GRADING");
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

		frame.setSize(800, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void addConfigButton(String description, AbstractButton button) {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.setLayout(new BorderLayout());
		JLabel label = new JLabel(description);
		int labelHeight = label.getPreferredSize().height;
		label.setPreferredSize(new Dimension(400, labelHeight));
		panel.add(label, BorderLayout.WEST);
		button.setText(DEFAULT_TEXT);
		button.addActionListener(this);
		panel.add(button);
		configPanes.add(panel);
	}

	public void openGUI() {
		frame.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setDialogTitle("Choose a directory");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		// disable the "All files" option.
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
			((JButton) e.getSource()).setText(chooser.getSelectedFile().getAbsolutePath());
		}
	}

	private void startGenerate() {
		if (masterButton.getText().equals(DEFAULT_TEXT)) {
			outputArea.setText("Starting to generate files without using an assignment directory...");
		}
		if (studentButton.getText().equals(DEFAULT_TEXT)) {
			outputArea.setText("You must choose a student submission directory");
		}

		outputArea.setText("Starting file generation...");
		startButton.setEnabled(false);
		Thread t = new Thread(this);
		t.start();
	}

	@Override
	public void run() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(os);

		File master = new File(masterButton.getText());
		File student = new File(studentButton.getText());

		String outputText;

		if (outputButton.getText().equals(DEFAULT_OUTPUT_TEXT)) {
			outputText = Paths.get(studentButton.getText(), "projects").toString();
			ps.println("Using " + outputText + " as output dir");
		} else {
			outputText = outputButton.getText();
		}

		File output = new File(outputText);

		if (Files.exists(output.toPath()) && !output.toPath().equals(master.toPath())) {
			try {
				Files.walk(output.toPath()).sorted(Comparator.reverseOrder()).map(Path::toFile)
						.forEach(File::delete);
			} catch (IOException e) {
				e.printStackTrace(ps);
			}
			ps.println("Output directory exists, deleting contents...");
		}
		output.mkdirs();


		if (masterButton.getText().equals(DEFAULT_TEXT)) {
			try {
				Csse220FileTool.doRename(student, ps, output);
			} catch (Exception e) {
				e.printStackTrace(ps);
			}
		} else {
			try {
				Csse220FileTool.doGenerate(master, student, output, ps);
			} catch (Exception e) {
				e.printStackTrace(ps);
			}
		}
		outputArea.setText(os.toString());
		startButton.setEnabled(true);
	}
}
