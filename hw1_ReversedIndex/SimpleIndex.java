package hw1_ReversedIndex;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class SimpleIndex extends JFrame {

	/**
	 * Nathan Roehl - CS744 - HW1
	 * Main method of HW1, provides GUI interface and basic user input/output.
	 * This class also determines which type of query to call.
	 * 
	 */

	private static final long serialVersionUID = 1L;
	JLabel lb = new JLabel("Copy and paste an existing file path that contains the files to read from. Hit enter.");
	JLabel lb2 = new JLabel();
	JTextField jtf = new JTextField(50);
	Tables data = new Tables();

	String projectName = null;
	String inputFilePath = null;
	String outputFilePath = null;
	String filesToParse = null;

	public SimpleIndex() {

		setTitle("Window Parser - Nathan Roehl");
		setSize(600,115);
		setLayout(new FlowLayout());
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		createContents();
		setResizable(false);
		setVisible(true);

	}

	public void createContents() {

		jtf.addActionListener(new textFieldListener());
		add(lb);
		add(jtf);
		add(lb2);


	}

	class textFieldListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {

			if(inputFilePath == null) {

				inputFilePath = modifyFilePath(jtf.getText());
				jtf.setText("");
				lb.setText("Please copy and paste an existing file path to have location to write to. Hit enter.");

			} else if (outputFilePath == null) {

				outputFilePath = modifyFilePath(jtf.getText());
				jtf.setText("HW1 p1.txt p2.txt p3.txt p4.txt");
				data.addFilePaths(inputFilePath, outputFilePath);
				lb.setText("Please enter files to read from as specified in homework description.");
				add(lb2);

			} else if (filesToParse == null) {

				filesToParse = jtf.getText();
				jtf.setText("");
				lb.setText("Please enter word to search for. Enter \"$q\" to quit.");
				lb2.setText("Searching supports single, bigram, and logic query.  Logic query supports multiple terms, no parenthesis.");
				buildReversedIndex();

			} else {

				String input = jtf.getText();
				if(input.equals("$q")) {
					System.exit(0);
				}
				answerQuery(input);
				jtf.setText("");

			}

		}

	}


	/**
	 * After user enters file paths and file extendsion, build the reversedIndex.
	 */
	private void buildReversedIndex() {
		String[] filesArray = filesToParse.split(" ");
		String[] files = new String[filesArray.length - 1];
		this.projectName = filesArray[0].trim();

		for(int i = 1; i < filesArray.length; i++) {
			files[i-1] = filesArray[i].trim();
		}

		try {
			data.parseFiles(files);
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(this, e.getMessage() ,"Error opening file.", JOptionPane.ERROR_MESSAGE);
			lb.setText("Please restart the program and enter file paths again.");
			lb2.setText("");
			jtf.setEnabled(false);
		}

		data.sortTerms();
		data.sortDocuments();

		//data.createTerms(projectName);
		//data.createRawCounts(projectName);
		//data.createSecondPostings(projectName);
		try {
			data.createTermsRawCountsSecondPostings(projectName);
			data.createDocSizes(projectName);
			data.createCalcStats(projectName);
		} catch(FileNotFoundException e) {
			JOptionPane.showMessageDialog(this, e.getMessage() ,"Error creating file.", JOptionPane.ERROR_MESSAGE);
		}

	}

	/**
	 * Add extra '\' slashes to allow user input to work when specifing file path.
	 * 
	 * @param x
	 * @return
	 */
	private static String modifyFilePath(String x) {
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


	private void answerQuery(String input) {

		String[] query = input.split(" ");

		try {
			if(query.length == 0) {
				//Do nothing, simply reprompt
			} else if(query.length == 1) {
				data.singleQuery(projectName, input.trim());
			} else if(query.length == 2 && query[0].indexOf("$") == -1) {
				data.biGramQuery(projectName, query);
			} else if(input.indexOf("$") != -1) {
				data.logicQuery(projectName, query);
			}
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(this, e.getMessage() ,"Error processing query.", JOptionPane.ERROR_MESSAGE);
		}
		

	}

	public static void main(String[] args) {

		new SimpleIndex();

	}

}
