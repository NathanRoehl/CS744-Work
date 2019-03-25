import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * Program that creates a reversed index using lucene.
 * Then takes in a text file and searches for matching text files using lucene.
 * Then outputs top results that are higher than the average for matching documents.
 * 
 * @author Nathan Roehl - CS744 - HW2 PartB
 */

public class GuiWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private JLabel headerLabel = new JLabel("Copy and paste an existing file path that contains the files to read from.");
	private JLabel resultsLabel = new JLabel("");
	private JTextField jtf = new JTextField(50);

	private String filesToRead = null;
	private String productDirPath = null;
	private String outputQueryPath = null;

	Searcher search = new Searcher();
	Indexer index = new Indexer();

	public GuiWindow() {
		setTitle("Window Parser - Nathan Roehl");
		setSize(620,270);
		setLayout(new FlowLayout());
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		createContents();
		setResizable(false);
		setVisible(true);
	}

	public void createContents() {
		jtf.addActionListener(new textFieldListener());
		add(headerLabel);
		add(jtf);

	}

	/**
	 * ActionListener for text field to read user input.
	 * Will take in file paths and file to query.
	 * Part of code that calls Class Indexer to create the index.
	 * 
	 * @author Nathan Roehl
	 *
	 */
	class textFieldListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {

			if(filesToRead == null) {

				filesToRead = modifyFilePath(jtf.getText());
				
				Path path = Paths.get(filesToRead);
				
				if(!filesToRead.isEmpty() && Files.exists(path)) {
					
					index.setInputFilePath(filesToRead);
					search.setInputFilePath(filesToRead);

					jtf.setText("");
					headerLabel.setText("Enter file path to store lucene index files.");
					
				} else {
					
					filesToRead = null;
					jtf.setText("");
					headerLabel.setText("Error with file path. Please re-enter file path that contains the files to read from.");
					
				}
				
			} else if (productDirPath == null) {

				add(resultsLabel);
				productDirPath = modifyFilePath(jtf.getText()).trim();
				
				Path path = Paths.get(productDirPath);
				
				if(!productDirPath.isEmpty() && Files.exists(path)) {
					
					jtf.setText("");

					index.setIndexDirPath(productDirPath);
					search.setIndexDirPath(productDirPath);

					try {
						
						index.createIndex();
						resultsLabel.setText("");
						headerLabel.setText("Please enter an output file path for query results.");
						
					} catch (IOException e) {
						
						jtf.setText("");
						productDirPath = null;
						headerLabel.setText("Error with file path. Please re-enter file path to store the lucene index.");
						
					}
					
				} else {
					
					jtf.setText("");
					productDirPath = null;
					headerLabel.setText("Error with file path. Please re-enter file path to store the lucene index.");

				}
				
			} else if (outputQueryPath == null) {

				outputQueryPath = modifyFilePath(jtf.getText()).trim();
				
				Path path = Paths.get(outputQueryPath);
				
				if(!outputQueryPath.isEmpty() && Files.exists(path)) {
					
					search.setOutputQueryFilePath(outputQueryPath);
					headerLabel.setText("Please enter your text file name to parse.");
					jtf.setText("");
					add(resultsLabel);
					
				} else {
					
					outputQueryPath = null;
					jtf.setText("");
					headerLabel.setText("Error with file path.  Please re-enter file path to store output query files.");
					
				}
				
			} else {

				String input = jtf.getText();
				
				if(!input.isEmpty()) {
					headerLabel.setText("Please enter your text file name to parse.");
					String x = search.query(input);
					resultsLabel.setText(formatOutput(x));
					jtf.setText("");
				} else {
					headerLabel.setText("You didn't enter anything. Please enter your text file to query.");
					jtf.setText("");
				}
			}
		}
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

	/**
	 * Adds break lines to output to screen.
	 * JLabels can't read new line characters ("\n") apparently.
	 * 
	 * @param str
	 * @return String with html code so JLabel can print new lines.
	 */
	private static String formatOutput(String str) {
		String[] splitStr = str.split(" and ");
		String result = "<html>";

		for(int i = 0; i < splitStr.length; i++) {
			if(i != splitStr.length -1) {
				result = result + " " + splitStr[i] + "<br/>";
			} else {
				result = result + " " + splitStr[i] + "</html>";
			}
		}

		return result;
	}

	public static void main(String [] args) {
		new GuiWindow();
	}

}
