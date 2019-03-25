package hw3_Weka;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A simple GUI to parse files, compute MI scores, create Train/Test text files, and create Train/Test arff files.
 * 
 * @author		Nathan Roehl
 * @topic 		CS744 - Fall 2018
 * @assignment	HW3-PartB
 */

public class GuiWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel jp = new JPanel();
	private JLabel headerLabel = new JLabel("Copy and paste an existing file path that contains the SGM files to read from.");
	private JLabel resultsLabel = new JLabel(" ");
	private JLabel results2 = new JLabel(" ");
	private JLabel results3 = new JLabel(" ");
	private JLabel results4 = new JLabel(" ");

	private JTextField jtf = new JTextField(60);

	private String filesToRead = null;
	private String outputQueryPath = null;
	private String create;
	private boolean computeMI = false;
	private boolean createTrainTest = false;
	private boolean createArffFiles = false;

	Parser parser;

	public GuiWindow() {
		setTitle("Window Parser - Nathan Roehl");
		setSize(800,140);
		//setLayout(new FlowLayout());
		//new BoxLayout(this,BoxLayout.PAGE_AXIS)
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		createContents();
		setResizable(false);
		setVisible(true);
	}

	public void createContents() {
		jp.setLayout(new BoxLayout(jp,BoxLayout.PAGE_AXIS));
		jtf.addActionListener(new textFieldListener());
		jp.add(headerLabel);
		jp.add(jtf);
		jp.add(resultsLabel);
		jp.add(results2);
		jp.add(results3);
		jp.add(results4);
		add(jp);
	}

	/**
	 * ActionListener for text field to read user input.
	 * 
	 * @author Nathan Roehl
	 *
	 */
	class textFieldListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {

			if(filesToRead == null) {

				filesToRead = modifyFilePath(jtf.getText().trim());

				Path path = Paths.get(filesToRead);

				if(!filesToRead.isEmpty() && Files.exists(path)) {
					jtf.setText("");
					headerLabel.setText("Enter file path to store output files.");
					resultsLabel.setText("This may take a moment to compute once entered.");

				} else {
					filesToRead = null;
					jtf.setText("");
					headerLabel.setText("Error with file path. Please re-enter file path that contains the files to read from.");
				}

			} else if (outputQueryPath == null) {

				outputQueryPath = modifyFilePath(jtf.getText()).trim();
				Path path = Paths.get(outputQueryPath);

				if(!outputQueryPath.isEmpty() && Files.exists(path)) {
					jtf.setText("");
					parser = new Parser(filesToRead,outputQueryPath);					
					parser.parse();
					resultsLabel.setText(" ");
					results2.setText("Train size: " + parser.allTrainDocs.size());
					results3.setText("Test size: " + parser.allTestDocs.size());
					results4.setText("Unknown size: " + parser.allUnknownDocs.size());

					computeMI = true;
					headerLabel.setText("Please enter a topic. Entering \"quit\" will stop building MI features.");
				} else {
					jtf.setText("");
					outputQueryPath = null;
					headerLabel.setText("Error with file path. Please re-enter file path to store output files.");
				}

			} else if (computeMI) {

				String topic = jtf.getText();

				if(topic.equals("quit")) {
					computeMI = false;
					createTrainTest = true;
					jtf.setText("");
					resultsLabel.setText(" ");
					headerLabel.setText("Enter in a topic to create train/test sets or \"quit\" to stop making train/test sets.");
					resultsLabel.setText("This may take a moment to create the train/test files.");
				} else {
					create = parser.MI_Features(topic,true);
					resultsLabel.setText(create);
					jtf.setText("");
				}
			} else if (createTrainTest) {
				String[] topics = jtf.getText().split(" ");
				if(topics.length != 0 && topics[0].equals("quit")) {
					createTrainTest = false;
					createArffFiles = true;
					jtf.setText("");
					parser.createARFFfiles(null);
					headerLabel.setText("Would you like to create arff files from new topics? Type \"quit\" to end program.");
					resultsLabel.setText("This may take a moment to create the arff files.");
				}
				else if(checkEntry(parser,topics)){
					jtf.setText("");
					parser.Train_Test_Split(topics);
					resultsLabel.setText("This may take a moment to create more train/test files.");
				} else {
					resultsLabel.setText("The topic does not exist. Please try again.");
					jtf.setText("");
				}

			} else if(createArffFiles) {
				String[] topics = jtf.getText().split(" ");
				if(topics.length != 0 && topics[0].equals("quit")) {
					System.exit(0);
				}
				else if(checkEntry(parser,topics)){
					jtf.setText("");
					parser.Train_Test_Split(topics);
					for(String topic: topics) {
						if(!topic.isEmpty())
							parser.createARFFfiles(topic);
					}
					resultsLabel.setText("This may take a moment to create more train/test files.");
				} else {
					resultsLabel.setText("The topic does not exist. Please try again.");
					jtf.setText("");
				}
			}
		}
	}

/**
 * Makes sure all topics passed in are valid topics.
 * 
 * @param p
 * @param topics
 * @return
 */
	public static boolean checkEntry(Parser p, String[] topics) {
		for(String str: topics) {
			if(!p.allTopics.contains(str)) {
				return false;
			}
		}
		return true;
	}


	/**
	 * Modify passed in file path by adding in \'s.
	 * 
	 * @param x
	 * @return filepath as String
	 */
	private static String modifyFilePath(String x) {

		x = x.trim();

		if(x == null || x.isEmpty()) {
			return "";
		}

		String path = "";
		char c;

		for(int i = 0; i < x.length(); i++) {
			c = x.charAt(i);
			if(c == '\\') {
				path += "\\\\";
			} else {
				path += c;
			}

		}

		return path + "\\";
	}



	public static void main(String [] args) {
		new GuiWindow();
	}

}
