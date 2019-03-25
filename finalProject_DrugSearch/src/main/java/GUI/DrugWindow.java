package GUI;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import org.apache.lucene.document.Document;

import LuceneIndexer.Indexer;
import LuceneIndexer.Searcher;
import LuceneIndexer.SeriousHealthSearch;

/**
 * Graphical User Interface that uses a prebuilt lucene index containing 200 drugs.
 * GUI uses methods of the Searcher class to answer queries and output information for different drugs.
 * 
 * @author Nathan Roehl and Nisreen Abdel Karim Ahmad Al Khun
 *
 */
public class DrugWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel leftSide, rightSide, leftButtons, rightButtons, topLabelLeft, topLabelRight, resultArea;
	private JLabel introLabel,resultsLabel,searchType1, searchType2, searchType3;
	private JButton okButton, clearButtonSearch, displayAllDrugs, clearButtonResults,previousSearch,nextSearch;
	private JTextArea queryArea;
	private JRadioButton findSymptomsRadio, findSimilarRadio, includeSynonyms;
	private JScrollPane leftScroll, rightScroll, allDrugs;
	private Searcher search;
	private LinkedList<JPanel> previousSearches = new LinkedList<>();
	private int panelSearchIndex;
//	private JMenuBar topMenu;
//	private JMenu mainFile;
//	private JMenuItem loadIndex,exportFiles;
	private static JPopupMenu menu;
	private static JMenuItem openWeb;
	private static JMenuItem openJPain;
	private static String url;
	private static Document doc;
	private static JButton[] buttons = new JButton[2];
	private String warning1 = "Warning: This drug is classified as an opiod.";
	private String warning2 = "Opioids may cause psychological and physical dependence.";
	private JFrame secondFrame;
	private static final String expansionUsed = " (Query expansion used)";

	public DrugWindow() {
		setTitle("Drug Search");
		setSize(850,450);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		this.setLayout(new GridLayout(1,2));
		setResizable(false);

		//sets tool tip to display much longer
		int dismissDelay = ToolTipManager.sharedInstance().getDismissDelay();
		dismissDelay = Integer.MAX_VALUE;
		ToolTipManager.sharedInstance().setDismissDelay(dismissDelay);

		//addTopMenu();
		loadLuceneIndex();
		addAllDrugsToScrollPane();
		createLeft();
		createRight(null);
		setVisible(true);
	}

//	public void addTopMenu() {
//		topMenu = new JMenuBar();
//		mainFile = new JMenu("File");
//		loadIndex = new JMenuItem("Set Up File Paths");
//		exportFiles = new JMenuItem("Export Results");
//		
//		setMenuFont(mainFile);
//		setMenuFont(loadIndex);
//		setMenuFont(exportFiles);
//
//		mainFile.add(loadIndex);
//		mainFile.add(exportFiles);
//		topMenu.add(mainFile);
//		this.setJMenuBar(topMenu);
//	}
	
	/**
	 * Create Searcher object, which is used to answer queries.
	 */
	public void loadLuceneIndex() {
		try {
			search = new Searcher();
		} catch (Exception e) {
			
		}
	}

	/**
	 * Create variable to store all drugs in the index as a JScrollPane.
	 * Used for showing all drugs to user.
	 */
	private void addAllDrugsToScrollPane() {
		JPanel drugs = createPanel(false,true,search.getAllDrugs());
		allDrugs = new JScrollPane(drugs);
		allDrugs.setPreferredSize(new Dimension(400,800));
	}

	/**
	 * Creates JOptionPane that will display all drugs in the index.
	 */
	private void showAllDrugs() {
		secondFrame = new JFrame();
		secondFrame.add(allDrugs);
		secondFrame.setTitle("List of all drugs.");
		secondFrame.setSize(400,700);
		//secondFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		secondFrame.setLocationRelativeTo(null);
		secondFrame.setVisible(true);
		//JOptionPane.showMessageDialog(null, allDrugs , "All Drugs", JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Creates left panel of main frame.
	 * Holds some JLabels, the text area to enter the query, some buttons, and some radio buttons.
	 */
	public void createLeft() {

		leftSide = new JPanel();
		leftSide.setLayout(new BoxLayout(leftSide, BoxLayout.PAGE_AXIS));
		leftSide.setAlignmentX(LEFT_ALIGNMENT);

		topLabelLeft = new JPanel();
		introLabel = new JLabel("Please enter your symptoms.");
		setLabelFont(introLabel);
		introLabel.setAlignmentX(LEFT_ALIGNMENT);
		topLabelLeft.add(introLabel);
		topLabelLeft.setAlignmentX(LEFT_ALIGNMENT);

		queryArea = new JTextArea();
		queryArea.setAlignmentX(LEFT_ALIGNMENT);
		queryArea.setLineWrap(true);
		setClickableFont(queryArea);
		leftScroll = new JScrollPane(queryArea);
		leftScroll.setPreferredSize(new Dimension(100,180));

		leftButtons = new JPanel();
		leftButtons.setAlignmentX(LEFT_ALIGNMENT);
		okButton = new JButton("  Search  ");
		setClickableFont(okButton);
		okButton.addActionListener(new buttonListener());
		clearButtonSearch = new JButton("Clear Search");
		setClickableFont(clearButtonSearch);
		clearButtonSearch.addActionListener(new buttonListener());
		displayAllDrugs = new JButton("All Drugs");
		displayAllDrugs.setToolTipText("Click to display all drugs in the program.");
		setClickableFont(displayAllDrugs);
		displayAllDrugs.addActionListener(new DisplayAllDrugs());
		leftButtons.add(okButton);
		leftButtons.add(new JLabel("     "));
		leftButtons.add(clearButtonSearch);
		leftButtons.add(new JLabel("     "));
		leftButtons.add(displayAllDrugs);

		searchType1 = new JLabel("Select \"Symptoms\" button to search by symptoms.");
		setLabelFont(searchType1);
		searchType1.setAlignmentX(LEFT_ALIGNMENT);
		searchType2 = new JLabel("Select \"Similar\" button to search for similar drugs.");
		setLabelFont(searchType2);
		searchType2.setAlignmentX(LEFT_ALIGNMENT);
		searchType3 = new JLabel("Select \"Synonyms\" button to use query expansion.");
		setLabelFont(searchType3);
		searchType3.setAlignmentX(LEFT_ALIGNMENT);

		findSymptomsRadio = new JRadioButton("Symptoms",true);
		setClickableFont(findSymptomsRadio);
		findSymptomsRadio.addActionListener(new RadioListener());
		findSymptomsRadio.setAlignmentX(LEFT_ALIGNMENT);
		findSimilarRadio = new JRadioButton("Similar");
		setClickableFont(findSimilarRadio);
		findSimilarRadio.addActionListener(new RadioListener());
		findSimilarRadio.setAlignmentX(LEFT_ALIGNMENT);
		includeSynonyms = new JRadioButton("Synonyms");
		setClickableFont(includeSynonyms);
		includeSynonyms.setAlignmentX(LEFT_ALIGNMENT);
		includeSynonyms.setToolTipText("<html>When checked will add more terms to your query.<br/>May increase or decrease relevance of search results.<br/>Is disabled when searching for similar drugs.</html>");

		
		leftSide.add(topLabelLeft);
		leftSide.add(new JLabel(" "));
		leftSide.add(leftScroll);
		leftSide.add(new JLabel(" "));
		leftSide.add(leftButtons);
		leftSide.add(new JLabel(" "));
		leftSide.add(searchType1);
		leftSide.add(searchType3);
		leftSide.add(searchType2);
		leftSide.add(new JLabel(" "));
		leftSide.add(findSymptomsRadio);
		leftSide.add(includeSynonyms);
		leftSide.add(findSimilarRadio);
		leftSide.setBorder(BorderFactory.createEmptyBorder(15,15,15,7));
		add(leftSide);

	}

	/**
	 * Creates left side of main window which displays the results.
	 * Passed in parameter used for answer queries.
	 * If actionPanel is null, create a blank right side panel.
	 * If actionPanel is not null, it will display the contents of actionPanel.
	 * 
	 * @param actionPanel
	 */
	public void createRight(JPanel actionPanel) {

		rightSide = new JPanel();
		rightSide.setLayout(new BoxLayout(rightSide, BoxLayout.PAGE_AXIS));
		rightSide.setAlignmentX(CENTER_ALIGNMENT);

		topLabelRight = new JPanel();
		resultsLabel = new JLabel("Results");
		setLabelFont(resultsLabel);
		topLabelRight.add(resultsLabel);
		topLabelRight.setAlignmentX(CENTER_ALIGNMENT);

		if(actionPanel == null) {
			resultArea = new JPanel();
		} else {
			resultArea = actionPanel;
		}
		resultArea.setAlignmentX(CENTER_ALIGNMENT);
		rightScroll = new JScrollPane(resultArea);
		rightScroll.setWheelScrollingEnabled(true);
		rightScroll.setPreferredSize(new Dimension(100,400));

		previousSearch = new JButton("<");
		setClickableFont(previousSearch);
		previousSearch.setToolTipText("Click to view previous search.");
		previousSearch.addActionListener(new PreviousSearches());
		clearButtonResults = new JButton("Clear Results");
		clearButtonResults.setToolTipText("Will clear all previous search results.");
		setClickableFont(clearButtonResults);
		nextSearch = new JButton(">");
		nextSearch.setToolTipText("Click to view next search.");
		setClickableFont(nextSearch);
		nextSearch.addActionListener(new PreviousSearches());

		rightButtons = new JPanel();
		rightButtons.add(previousSearch);
		rightButtons.add(new JLabel("    "));
		rightButtons.add(clearButtonResults);
		rightButtons.add(new JLabel("    "));
		rightButtons.add(nextSearch);

		clearButtonResults.addActionListener(new buttonListener());
		clearButtonResults.setAlignmentX(CENTER_ALIGNMENT);

		rightSide.add(topLabelRight);
		rightSide.add(new JLabel(" "));
		rightSide.add(rightScroll);
		rightSide.add(new JLabel(" "));
		rightSide.add(rightButtons);
		rightSide.setBorder(BorderFactory.createEmptyBorder(15,8,15,15));
		add(rightSide);

	}

	/**
	 * ActionListener for text field to read user input.
	 * Handles both types of searches.
	 */
	class buttonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {
			JButton pushed = (JButton) event.getSource();
			if(pushed == okButton) {
				String query = queryArea.getText();
				boolean warning = SeriousHealthSearch.areSeriousHealthIssues(query);
				if(warning) {
					String message = "Warning, some of the terms used could be very serious.  Do not try to treat these yourself, seek medical attention if very severe.";
					JOptionPane.showMessageDialog(null, message , "Warning", JOptionPane.WARNING_MESSAGE);
				}
				LinkedList<Document> docs;
				try {
					if(findSymptomsRadio.isSelected()) {
						docs = search.symptomQuery(query,includeSynonyms.isSelected());
					} else {
						docs = search.similarDrugQuery(query);
					}
					JPanel panel;

					//if result returned an empty linked list, means no matching result
					if(docs == null || docs.isEmpty()) {
						panel = new JPanel();
						panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
						JLabel searchType = new JLabel("Symptoms entered.");
						setLabelFont(searchType);
						panel.add(searchType);
						JLabel field = new JLabel(query);
						setResultFont(field);
						panel.add(field);
						JLabel noResult = new JLabel("Sorry, no results were found.");
						setResultFont(noResult);
						panel.add(noResult);
					} else {

						panel = new JPanel();
						panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
						JLabel searchType;
						LinkedList<Document> queryDoc = new LinkedList<>();

						//If looking for similar drugs adds a new label to top of panel
						if(findSimilarRadio.isSelected()) {
							searchType = new JLabel("Drug entered");
							setLabelFont(searchType);
							panel.add(searchType);
							Document d = search.getDocument(query);
							queryDoc.add(d);
							panel.add(new JScrollPane(createPanel("y".equalsIgnoreCase(d.get(Indexer.PRESCRIPTION)) ? true:false,true,queryDoc)));
						} else {
							searchType = new JLabel("Symptoms entered.");
							setLabelFont(searchType);
							panel.add(searchType);
							//TODO - added stuff here
							if(warning)
								query = query + " (Warning)";
							if(includeSynonyms.isSelected()) {
								query = query + expansionUsed;
							}
							//TODO - added stuff here
							//JTextField field = new JTextField(query);
							JTextArea field = new JTextArea(query);
							field.setBackground(searchType.getBackground());
							field.setLineWrap(true);
							field.setEditable(false);
							setResultFont(field);
							field.setEditable(false);
							panel.add(new JScrollPane(field));
						}

						JLabel noPre = new JLabel("Non Prescription");
						setLabelFont(noPre);
						panel.add(noPre);
						panel.add(new JScrollPane(createPanel(false,false,docs)));
						JLabel pre = new JLabel("Prescription");
						setLabelFont(pre);
						panel.add(pre);
						panel.add(new JScrollPane(createPanel(true,false,docs)));
					}
					//Remove current right side panel, recreate right side, revalidate and repaint
					previousSearches.add(panel);
					panelSearchIndex = previousSearches.size()-1;
					remove(rightSide);
					createRight(panel);
					revalidate();
					repaint();

				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, "Error during search." , "Error", JOptionPane.ERROR_MESSAGE);
				}
			} else if (pushed == okButton && findSimilarRadio.isSelected()) {
				queryArea.setText("");
			} else if (pushed == clearButtonSearch) {
				queryArea.setText("");
			} else if (pushed == clearButtonResults){
				previousSearches = new LinkedList<>();
				panelSearchIndex = 0;
				remove(rightSide);
				createRight(null);
				revalidate();
				repaint();
			}
		}
	}

	/**
	 * Creates the result panel to be displayed after a query.
	 * If isPre == true, means only add Prescription drugs.
	 * If ignorePre == true, means add all drugs passed.
	 * LinkedList docs holds all possible drugs to search for.
	 * @param isPre
	 * @param buildAllDrugs
	 * @param docs
	 * @return
	 */
	public JPanel createPanel(boolean isPre , boolean buildAllDrugs, LinkedList<Document> docs) {
		String p = "n";
		if(isPre) {
			p = "y";
		}
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
		JTextField field;
		Document drug;
		String prescription;
		int added = 0;
		for(int i = 0; i < docs.size(); i++) {
			drug = docs.get(i);
			prescription = drug.get(Indexer.PRESCRIPTION);
			if(buildAllDrugs) {
				field = fillTextFieldAllDrugs(drug);
				panel.add(field);
				added++;
			} else if(p.equals(prescription.toLowerCase())){
				field = fillTextFieldQuery(drug);
				panel.add(field);
				added++;
				if(added >= 5) {
					break;
				}
			}
		}
		//If nothing was added because of Non prescription or prescription, say so.
		if(added == 0) {
			field = new JTextField("Sorry, no results were found for this category.");
			setResultFont(field);
			field.setEditable(false);
			panel.add(field);
		}
		return panel;
	}

	public JTextField fillTextFieldQuery(Document drug) {
		String name = drug.get(Indexer.NAME);
		JTextField field = new JTextField(name);
		setResultFont(field);
		field.setEditable(false);
		String description = drug.get(Indexer.DESCRIPTION);
		String opioid = drug.get(Indexer.OPIOID);
		if("Y".equals(opioid)) {
			field.setToolTipText("<html>"+ warning1 +"<br/>" + warning2 + "<br/>" + description + "<br/>Right click drug for more options.</html>");
		} else {
			field.setToolTipText("<html>" + description + "<br/>Right click drug for more options.</html>");
		}
		field.addMouseListener(new ClickLabel());

		return field;
	}

	public JTextField fillTextFieldAllDrugs(Document drug) {
		String name = drug.get(Indexer.NAME);
		JTextField field = new JTextField(name);
		setResultFont(field);
		field.setEditable(false);
		String description = drug.get(Indexer.DESCRIPTION);
		String opioid = drug.get(Indexer.OPIOID);
		String prescription = drug.get(Indexer.PRESCRIPTION);
		if("Y".equals(opioid)) {
			field.setToolTipText("<html>"+ warning1 +"<br/>" + warning2 + "<br/>" + description + "<br/>Prescription: " + prescription + "<br/>Right click drug for more options.</html>");
		} else {
			field.setToolTipText("<html>" + description + "<br/>Prescription: " + prescription + "<br/>Right click drug for more options.</html>");
		}
		field.addMouseListener(new ClickLabel());

		return field;
	}


	/**
	 * Used to with method below to open webpage stored for each drug.
	 * @param url
	 * @return
	 */
	public boolean openWebpage(URL url) {
		try {
			return openWebpage(url.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean openWebpage(URI uri) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(uri);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Mouse listener for handling right clicks.
	 * By not adding funtionality to left click allows user to copy and paste drug information.
	 */
	public class ClickLabel implements MouseListener{

		@Override
		public void mouseClicked(MouseEvent e) {

			JTextField jl = (JTextField) e.getSource();
			String text = jl.getText().toLowerCase().trim();

			//get document associated with text
			doc = null;
			try {
				doc = search.getDocument(text);
			} catch (IOException e2) {
				e2.printStackTrace();
			}

			if(doc == null)
				return;

			//if right click, creates a pop up menu which lets a user get more basic information
			//or open a website
			if(SwingUtilities.isRightMouseButton(e)) {

				menu = new JPopupMenu();
				openWeb = new JMenuItem("Open Webpage");
				menu.add(openWeb);
				openJPain = new JMenuItem("More Information");
				menu.add(openJPain);
				url = doc.get(Indexer.URL);
				if(url.isEmpty()) {
					return;
				}

				//Opens webpage from right click if selected
				openWeb.addActionListener(new OpenWebPage());

				//Creates custom JOptionPane
				//Displays more information and lets a user open the website
				openJPain.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						String name = doc.get(Indexer.NAME);
						String descrip = doc.get(Indexer.DESCRIPTION);
						String category = doc.get(Indexer.CATEGORY);
						String prescription = doc.get(Indexer.PRESCRIPTION).equals("Y") ? "Yes":"No";

						//String url = d.getURL();
						String message = "Name: " + name + "\nPrescription: " + prescription + "\nCategory: " + category + "\n" +
								"Description: " + descrip;
						JTextArea jta = new JTextArea();
						setResultFont(jta);
						jta.setText(message);
						jta.setWrapStyleWord(true);
						jta.setEditable(false);
						JScrollPane jp = new JScrollPane(jta);
						buttons[0] = new JButton("Open Website");
						buttons[0].addActionListener(new OpenWebPage());
						buttons[1] = new JButton("Exit");
						setClickableFont(buttons[0]);
						setClickableFont(buttons[1]);

						//Since adding custom buttons, need to add listener to custom exit button
						buttons[1].addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								Window w = SwingUtilities.getWindowAncestor(buttons[1]);
								if(w != null) {
									w.dispose();
								}
							}
						});
						JOptionPane.showOptionDialog(null, jp, "Drug information" , JOptionPane.PLAIN_MESSAGE, 0 ,null, buttons,buttons[0]);
					}
				});
				menu.show(jl, e.getX(), e.getY());
			}
		}

		//Must provide extra methods when implement MouseListener.
		//All other methods perform no task.
		@Override
		public void mousePressed(MouseEvent e) {}

		@Override
		public void mouseReleased(MouseEvent e) {}

		@Override
		public void mouseEntered(MouseEvent e) {}

		@Override
		public void mouseExited(MouseEvent e) {}
	}

	/**
	 * Actionlistener that opens a webpage.
	 * Uses static variable url, which should be changed everytime a button
	 * or text field is selected to run this code.
	 */
	class OpenWebPage implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				URL u = new URL(url);
				openWebpage(u);
			} catch (MalformedURLException e1) {
				System.out.println("No url");
				e1.printStackTrace();
			}
		}
	}

	class DisplayAllDrugs implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			showAllDrugs();
		}

	}

	/**
	 *	Handles swapping radio buttons to tell whether searching by symptoms
	 *	or searching for similar drugs.
	 */
	class RadioListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {
			JRadioButton selected = (JRadioButton) event.getSource();
			if(selected == findSymptomsRadio) {
				findSymptomsRadio.setSelected(true);
				findSimilarRadio.setSelected(false);
				includeSynonyms.setEnabled(true);
				includeSynonyms.setToolTipText("<html>When checked will add more terms to your query.<br/>May increase or decrease relevance of search results.<br/>Is disabled when searching for similar drugs.</html>");
				introLabel.setText("Please enter your symptoms.");
			} else {
				findSymptomsRadio.setSelected(false);
				findSimilarRadio.setSelected(true);
				includeSynonyms.setSelected(false);
				includeSynonyms.setEnabled(false);
				includeSynonyms.setToolTipText("Disabled for drug search.");
				introLabel.setText("Please enter a drug you wish to find similar drugs for.");
			}
		}
	}

	class PreviousSearches implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {
			JButton jb = (JButton)event.getSource();
			JPanel panel;
			if(jb == previousSearch) {
				if(panelSearchIndex > 0) {
					panel = previousSearches.get(--panelSearchIndex);
					remove(rightSide);
					createRight(panel);
					revalidate();
					repaint();
				}
			} else {
				if(panelSearchIndex < previousSearches.size()-1) {
					panel = previousSearches.get(++panelSearchIndex);
					remove(rightSide);
					createRight(panel);
					revalidate();
					repaint();
				}
			}
		}
	}

	/**
	 *	Helper methods to set fonts for each type of display.
	 */
	private void setLabelFont(Component c) {
		c.setFont(new Font("Dialog", Font.BOLD,14));
	}

	private void setResultFont(Component c) {
		c.setFont(new Font("Dialog", Font.ITALIC,14));
	}

	private void setClickableFont(Component c) {
		c.setFont(new Font("Dialog",Font.PLAIN,14));
	}
	
//	private void setMenuFont(Component c) {
//		c.setFont(new Font("Dialog",Font.PLAIN,14));
//	}

	public static void main(String[] args) {

		new DrugWindow();

	}

}
