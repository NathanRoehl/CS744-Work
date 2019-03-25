package hw3_Weka;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * The Class Parser does most of the work.
 * It opens files, it parses the files, it outputs MI scores for a topic, it outputs train/test files,
 * and it creats .arff files for weka.
 * 
 * @author		Nathan Roehl
 * @topic 		CS744 - Fall 2018
 * @assignment	HW3-PartB
 *
 */

public class Parser {

	String inPath;
	String outPath;

	HashMap<String, HashMap<String, Set<Doc>>> N11 = new HashMap<>();
	//map (term to (topic,document))
	//keeps track of how many documents contain the topic and term together

	int totalDocs = 0;
	//keeps track of total number of documents in the whole corpus

	HashMap<String, Set<Doc>> topicToDoc = new HashMap<>();
	//map topic to Docs it is contained

	HashMap<String, Set<Doc>> termToDoc = new HashMap<>();
	//map term to document it is in

	HashMap<Doc, Set<String>> docToTerm = new HashMap<>();
	//map doc to terms

	ArrayList<Doc> allTrainDocs = new ArrayList<>();
	//holds all train documents
	ArrayList<Doc> allTestDocs = new ArrayList<>();
	//holds all test documents
	ArrayList<Doc> allUnknownDocs = new ArrayList<>();
	//holds all unknown documents

	HashSet<String> allTopics = new HashSet<>();
	//holds all topics, use hash set so duplicate topics are not allowed
	HashSet<String> allTrainTopics = new HashSet<>();
	//used to hold all train topics, helpful when searching if a topic exists in the training set
	HashSet<String> allTestTopics = new HashSet<>();
	//used to hold all test topics, helpful when searching if a topic exists in the test set
	HashSet<String> allUnknownTopics = new HashSet<>();
	//used to hold all unknown topics, helpful when searching if a topic exists in the unknown set

	ArrayList<String> topicFiles = new ArrayList<>();
	//used to store file names of topics that have been created already
	//useful for automatically computing arff files from existing train/test files

	HashMap<String, ArrayList<Tuple>> storedMIScores = new HashMap<>();
	//maps a topic to an arraylist containing all computed MI scores for the topic in sorted order
	
	/**
	 * Constructor that sets file paths for reading and writing.
	 * 
	 * @param inPath
	 * @param outPath
	 */
	public Parser(String inPath, String outPath) {
		this.inPath = inPath;
		this.outPath = outPath;
	}

	/**
	 * Method opens the set of files specified by the path and parsed out the document.
	 * Looks for markers, like "<Topic>" to determine which method to call to handle current case.
	 * 
	 */
	public void parse() {

		System.out.println("Starting process. Please wait till next prompt, this may take 10-20 seconds.");
		File filePath = new File(inPath);

		String line;
		File[] listings = filePath.listFiles();
		Doc doc = new Doc();
		for(File f: listings) {

			try(Scanner scr = new Scanner(f)){

				while(scr.hasNextLine()) {
					line = scr.nextLine().trim();

					int docStart = line.indexOf("<REUTERS");
					if(docStart != -1) {
						doc = new Doc();
						//gets mod and if has topic (yes/no)
						parseDoc(doc,line);
						totalDocs++;
					}

					int topicStart = line.indexOf("<TOPICS>");
					if(topicStart != -1) {
						//add all topics to current doc
						parseTopics(doc,line);
					}

					int titleStart = line.indexOf("<TITLE>");
					if(titleStart != -1) {
						parseTitle(doc,line);
					}

					//If <BODY> found, continue reading lines until </BODY> is found.
					//Will read each line pass each line to parseTerms
					int bodyStart = line.indexOf("<BODY>");
					if(bodyStart != -1) {

						line = line.substring(bodyStart+6).trim();

						while(line.indexOf("</BODY>") == -1) {
							doc.addStringToBody(line);
							parseTerms(doc,line);
							if(scr.hasNextLine()) {
								line = scr.nextLine().trim();
							} else {
								break;
							}
						}
						//Removes reuter from text so it doesn't get included
						doc.getBody().remove(doc.getBody().size()-1);
					}


				} //end while loop

			} catch (FileNotFoundException e) {
				System.out.println("Error in opening file.");
			}
		}

		//When done reading all files, print out sizes of train,test, and unknown sets.
		System.out.println("Train size: " + allTrainDocs.size());
		System.out.println("Test size: " + allTestDocs.size());
		System.out.println("Unknown size: " + allUnknownDocs.size());

		System.out.println("Finished parsing documents.");
	}

	/**
	 * Parse out if has topics and whether train or test.
	 * doc passed in is updated accordingly.
	 * @param doc
	 * @param line
	 */
	private void parseDoc(Doc doc, String line) {

		int TOPICS = line.indexOf("TOPICS=");

		//<REUTERS TOPICS="YES" LEWISSPLIT="TRAIN" CGISPLIT="TRAINING-SET" OLDID="18419" NEWID="2001">
		if(TOPICS != -1) {
			String top = line.substring(TOPICS + 7, TOPICS + 12);
			if(top.contains("YES")) {
				doc.setHasTopics(true);
			} else {
				doc.setHasTopics(false);
			}
		}

		int LEWISSPLIT = line.indexOf("LEWISSPLIT=");
		if(LEWISSPLIT != -1) {
			String top = line.substring(LEWISSPLIT + 12, LEWISSPLIT + 19);
			if(top.contains("TEST") && doc.isHasTopics()) {
				doc.setModaptsplit(ModAptSplit.TEST);
				allTestDocs.add(doc);
			} else if (top.contains("TRAIN") && doc.isHasTopics()) {
				doc.setModaptsplit(ModAptSplit.TRAIN);
				allTrainDocs.add(doc);
			}else {
				doc.setModaptsplit(ModAptSplit.UNKNOWN);
				allUnknownDocs.add(doc);
			}
		}
	}

	/**
	 * Parse out all topics, some documents have many.
	 * doc passed in is updated accordingly.
	 * @param doc
	 * @param line
	 */
	private void parseTopics(Doc doc, String line) {

		int start = line.indexOf("<D>");
		int end;
		String topic;

		while(start != -1){
			end = line.indexOf("</D>", start + 2);

			topic = line.substring(start+3, end);

			if(!topic.isEmpty()) {
				doc.addTopic(topic);
				allTopics.add(topic);

				//fills correct list of documents based on train, test, or unknown
				if(doc.getModaptsplit() == ModAptSplit.TRAIN && doc.isHasTopics()) {
					allTrainTopics.add(topic);
				} else if(doc.getModaptsplit() == ModAptSplit.TEST && doc.isHasTopics()) {
					allTestTopics.add(topic);
				} else {
					allUnknownTopics.add(topic);
				}

				//maps a topic to a document
				//helps get total number of documents that contain topic
				if(topicToDoc.containsKey(topic)) {
					Set<Doc> tmpSet = topicToDoc.get(topic);
					tmpSet.add(doc);
					topicToDoc.put(topic, tmpSet);
				} else {
					Set<Doc> tmpSet = new HashSet<Doc>();
					tmpSet.add(doc);
					topicToDoc.put(topic, tmpSet);
				}
			}
			start = line.indexOf("<D>", end);
		}
	}


/**
	 * Parse out title.
	 * doc passed in is updated accordingly.
 * @param doc
 * @param line
 */
	private void parseTitle(Doc doc, String line) {

		int start = line.indexOf("<TITLE>");
		int end = line.indexOf("</TITLE>");

		if(start != -1 && end != -1) {
			String title = line.substring(start + 7, end);
			doc.setTitle(title);
		}

	}

	/**
	 * Takes a string from the body of a document and breaks it down in to individual words.
	 * Lots of information updated here.
	 * @param doc
	 * @param line
	 */
	private void parseTerms(Doc doc, String line) {

		//Split passed in line into individual terms
		String[] lineSplit = line.split(" ");
		String term;
		HashMap<String, Set<Doc>> hm;
		Set<Doc> s;

		//loop over all terms, remove all punctuation, and map term to many things.
		for(String str: lineSplit) {
			term = str.toLowerCase().replaceAll("[\",.!?()']", "");
			term = term.replaceAll("[-]", " ").trim();

			if(term.isEmpty())
				continue;

			//link term to set of docs it occurs in, a reversed index
			if(termToDoc.containsKey(term)) {
				Set<Doc> tmpSet = termToDoc.get(term);
				tmpSet.add(doc);
				termToDoc.put(term, tmpSet);
			} else {
				Set<Doc> tmpSet = new HashSet<Doc>();
				tmpSet.add(doc);
				termToDoc.put(term, tmpSet);
			}

			//link doc to all terms it contains
			//useful for computing MI scores
			//makes it so duplicate terms are not computed several times
			if(docToTerm.containsKey(doc)) {
				Set<String> tmpSet = docToTerm.get(doc);
				tmpSet.add(term);
				docToTerm.put(doc, tmpSet);
			} else {
				Set<String> tmpSet = new HashSet<String>();
				tmpSet.add(term);
				docToTerm.put(doc, tmpSet);
			}

			//HashMap<Term, HashMap<Topic, Set<Doc>>> N11 = new HashMap<>();
			//Links a term and topic to a set of documents.
			if(N11.containsKey(term)) {
				hm = N11.get(term);
			} else {
				hm = new HashMap<String, Set<Doc>>();
			}

			//HashMap<Topic, Set<Doc>>>
			for(String topic: doc.getTopics()) {
				if(hm.containsKey(topic)) {
					s = hm.get(topic);
				} else {
					s = new HashSet<Doc>();
				}
				s.add(doc);
				hm.put(topic, s);
			}

			N11.put(term, hm);
		}
	}

	
	/**
	 * Compute MI scores for passed in topic.
	 * If topic is empty or does not exist nothing will happen.
	 * Train boolean value helps determine if making MI scores for train or test set.
	 * I never computed MI features for test set of a topic, but if you were curious enough to do so it is possible.
	 * 
	 * @param topic
	 * @param train
	 * @return
	 */
	public String MI_Features(String topic, boolean train) {

		if(topic.trim().isEmpty()) {
			return "You did not enter a anything.";
		} else if(!allTrainTopics.contains(topic)) {
			return "\"" + topic + "\" is not in collection of documents.";
		} else {
			System.out.println("Computing MI for \"" + topic + "\". Please wait.");
			Set<Doc> setDocs = topicToDoc.get(topic);
			ArrayList<Doc> myDocs = new ArrayList<>();
			for(Doc d: setDocs) {
				//will only search train sets
				if(d.isHasTopics() && d.getModaptsplit() == ModAptSplit.TRAIN && train) {
					myDocs.add(d);
				//will only search test sets
				} else if(d.isHasTopics() && d.getModaptsplit() == ModAptSplit.TEST && !train) {
					myDocs.add(d);
				}
			}

			Set<String> found = new HashSet<String>();
			Set<String> termsInDoc;
			HashMap<String,Set<Doc>> termTopicDoc;
			Set<Doc> topicDoc;
			int n11,n10,n01,n00;
			ArrayList<Tuple> scores = new ArrayList<>();
			Tuple tuple;

			//for each document related to the topic, create MI scores for all terms in all these documents
			for(Doc d: myDocs) {
				termsInDoc = docToTerm.get(d);
				if(termsInDoc == null) {
					continue;
				}
				for(String term: termsInDoc) {

					//make sure duplicate terms are not computed
					if(found.contains(term))
						continue;

					//found also used to avoid duplicate computations of terms
					//if "the" shows up 5o times, it only gets computed once
					found.add(term);

					//N11 holds total number of documents that the term and topic occur in together
					termTopicDoc = N11.get(term);
					topicDoc = termTopicDoc.get(topic);
					n11 = topicDoc.size();
					
					//Holds document count where term is not in document but document is in class of train set
					n01 = setDocs.size() - n11;

					//Holds document count where term is in document but not in class of train set
					n10 = termToDoc.get(term).size();
					
					//Holds document count where term and document do not occur in class of train set
					n00 = totalDocs - n11 - n01 - n10;

					//Calculate MI score of term and topic
					double MI = computeMI(n11,n01,n10,n00);
					
					//create tuple object with score and term
					//useful for sorting and creating output file
					tuple = new Tuple(term,MI);
					scores.add(tuple);
				}
			}

			//Sort list of tuples
			Collections.sort(scores, new sortTuple());

			try(PrintWriter pw = new PrintWriter(outPath + topic + ".scores.txt")){

				//write scores to output file for topic
				for(Tuple t: scores) {
					pw.println(t.getTerm() + ": " + t.getScore());
				}

			} catch (FileNotFoundException e) {

			}

			//If topic has not been computed, store it
			if(!storedMIScores.containsKey(topic)) {
				storedMIScores.put(topic, scores);
			}

			return "Mutual information for \"" + topic + "\" has been created.";
		}

	}

	/**
	 * Helper method to compute MI score
	 * NaN check makes sure MI score does not turn to NaN.
	 * 
	 * @param n11
	 * @param n01
	 * @param n10
	 * @param n00
	 * @return
	 */
	private double computeMI(int n11, int n01, int n10, int n00) {

		double N = n11 + n01 + n10 + n00;
		double N1_ = n11 + n10;
		double N_1 = n01 + n11;
		double N0_ = n01 + n00;
		double N_0 = n00 + n10;

		double MI = 0;
		double calc	= (n11/N) * (Math.log((N * n11)/(N1_*N_1))/Math.log(2));

		if(Double.isNaN(calc)) {
			calc = 0;
		}
		MI+=calc;

		calc = (n01/N) * (Math.log((N * n01)/(N0_*N_1))/Math.log(2));
		if(Double.isNaN(calc)) {
			calc = 0;
		}
		MI+=calc;

		calc = (n10/N) * (Math.log((N * n10)/(N1_*N_0))/Math.log(2));

		if(Double.isNaN(calc)) {
			calc = 0;
		}
		MI+=calc;

		calc = (n00/N) * (Math.log((N * n00)/(N0_*N_0))/Math.log(2));

		if(Double.isNaN(calc)) {
			calc = 0;
		}
		MI+=calc;

		return MI;
	}

	/**
	 * Build train/test files based on list of topics passed in.
	 * 
	 * @param topics
	 */
	public void Train_Test_Split(String[] topics) {

		ArrayList<ArrayList<Tuple>> allMI = new ArrayList<>();

		//If a topic is passed in that has not been computed, compute the score
		//Otherwise get the list of scores for the topic
		//Was meant to reduce duplicate computations of MI of a term and makes sure
		//each topic has a list of MI scores associated with it
		for(String str: topics) {
			if(!storedMIScores.containsKey(str)) {
				MI_Features(str,true);
			}
			allMI.add(storedMIScores.get(str));	//gets train set
			topicFiles.add(str);				//stores topic name to automatically create arff files for topic
		}

		//loop over each topic that was passed in and create train/test files for each
		for(int n = 0; n < topics.length; n++) {
			String topic = topics[n];
			ArrayList<Tuple> MI_Scores = allMI.get(n);

			try(PrintWriter pTrain50 = new PrintWriter(outPath+topic+"50.Train.txt");
					PrintWriter pTrain100 = new PrintWriter(outPath+topic+"100.Train.txt");
					PrintWriter pTrain300 = new PrintWriter(outPath+topic+"300.Train.txt");
					PrintWriter pTrain400 = new PrintWriter(outPath+topic+"400.Train.txt");
					PrintWriter pTest50 = new PrintWriter(outPath+topic+"50.Test.txt");
					PrintWriter pTest100 = new PrintWriter(outPath+topic+"100.Test.txt");
					PrintWriter pTest300 = new PrintWriter(outPath+topic+"300.Test.txt");
					PrintWriter pTest400 = new PrintWriter(outPath+topic+"400.Test.txt")){

				ArrayList<String> body;
				String inClass;
				
				//creates set of modified test docs based on MI scores
				for(Doc d: allTrainDocs) {

					inClass = d.hasTopic(topic);

					pTrain50.print("(" + inClass + " 50 (");
					pTrain100.print("(" + inClass + " 100 (");
					pTrain300.print("(" + inClass + " 300 (");
					pTrain400.print("(" + inClass + " 400 (");

					body = d.getBody();

					for(String str: body) {
						String term;
						int j;
						//list of if/else statements makes it so only the correct number of MI words are used when writing to a file
						for(int i = 0; i < 400 && i < MI_Scores.size(); i++) {
							
							//get term related to position i from MI score
							term = MI_Scores.get(i).getTerm();
							//finds term in document body (if there) and writes to correct output file
							j = str.indexOf(" " + term);
							if(i < 50) {
								while(j >= 0) {

									pTrain50.print(" "+term);
									pTrain100.print(" "+term);
									pTrain300.print(" "+term);
									pTrain400.print(" "+term);

									j = str.indexOf(" " + term, j+1);

								}
							} else if(i < 100) {
								while(j >= 0) {

									pTrain100.print(" "+term);
									pTrain300.print(" "+term);
									pTrain400.print(" "+term);

									j = str.indexOf(" " + term, j+1);

								}
							} else if(i < 300) {
								while(j >= 0) {

									pTrain300.print(" "+term);
									pTrain400.print(" "+term);

									j = str.indexOf(" " + term, j+1);

								}
							} else {
								while(j >= 0) {

									pTrain400.print(" "+term);

									j = str.indexOf(" " + term, j+1);

								}
							}
						}
					}
					//place final lines for each modified document
					pTrain50.println(" ))");
					pTrain100.println(" ))");
					pTrain300.println(" ))");
					pTrain400.println(" ))");
				}
				
				//creates set of modified train docs based on MI scores
				for(Doc d: allTestDocs) {

					inClass = d.hasTopic(topic);

					pTest50.print("(" + inClass + " 50 (");
					pTest100.print("(" + inClass + " 100 (");
					pTest300.print("(" + inClass + " 300 (");
					pTest400.print("(" + inClass + " 400 (");

					body = d.getBody();

					for(String str: body) {
						String term;
						int j;
						for(int i = 0; i < 400 && i < MI_Scores.size(); i++) {
							term = MI_Scores.get(i).getTerm();
							j = str.indexOf(" " + term);
							if(i < 50) {
								while(j >= 0) {

									pTest50.print(" "+term);
									pTest100.print(" "+term);
									pTest300.print(" "+term);
									pTest400.print(" "+term);

									j = str.indexOf(" " + term, j+1);

								}
							} else if(i < 100) {
								while(j >= 0) {

									pTest100.print(" "+term);
									pTest300.print(" "+term);
									pTest400.print(" "+term);

									j = str.indexOf(" " + term, j+1);

								}
							} else if(i < 300) {
								while(j >= 0) {

									pTest300.print(" "+term);
									pTest400.print(" "+term);

									j = str.indexOf(" " + term, j+1);

								}
							} else {
								while(j >= 0) {

									pTest400.print(" "+term);

									j = str.indexOf(" " + term, j+1);

								}
							}
						}
					}
					pTest50.println(" ))");
					pTest100.println(" ))");
					pTest300.println(" ))");
					pTest400.println(" ))");
				}

			} catch(FileNotFoundException e) {
				System.out.println("Error in creating train/test files.");
				System.out.println(e.getMessage());
			}
		}
	}

	/**
	 * Opens existing .txt files of train and test documents based on topic and creates arff file for it.
	 * 
	 * @param top
	 * @return
	 */
	public String createARFFfiles(String top) {

		//Makes sure non topics are not computed
		if(top != null && !allTopics.contains(top)) {
			return "\"" + top + "\" is not in collection of documents.";
		}

		ArrayList<String> topicsToConvert = new ArrayList<>();

		//if null, means creating arff files from existing train/test files
		//if not null, means top passed in is to be converted to arff file
		//if not null, train/test files should exist, could crash otherwise
		if(top == null) {
			topicsToConvert = topicFiles;
		} else {
			topicsToConvert.add(top);
		}

		int[] sizes = {50,100,300,400};
		String trainFile,testFile,trainArff,testArff,line, textBody, classYN;
		int bodyStart;

		for(String topic: topicsToConvert) {

			for(int i = 0; i < 4; i++) {
				trainFile = outPath + topic + sizes[i]+".Train.txt";
				testFile = outPath + topic + sizes[i]+".Test.txt";
				trainArff = outPath + topic + sizes[i]+"Train.arff";
				testArff = outPath + topic + sizes[i]+"Test.arff";

				try(Scanner trainRead = new Scanner(new File(trainFile));
						Scanner testRead = new Scanner(new File(testFile));
						PrintWriter trainPrint = new PrintWriter(trainArff);
						PrintWriter testPrint = new PrintWriter(testArff)){

					trainPrint.println("% Title: Training set for " + topic + sizes[i] + ".");
					trainPrint.println("%");
					trainPrint.println("% Author: Nathan Roehl");
					trainPrint.println("%");
					trainPrint.println("@RELATION " + topic);
					trainPrint.println();
					trainPrint.println("@ATTRIBUTE   textBody   STRING");
					trainPrint.println("@ATTRIBUTE   class      {Y,N}");
					trainPrint.println();
					trainPrint.println("@DATA");

					while(trainRead.hasNextLine()) {
						line = trainRead.nextLine();
						classYN = (line.substring(1, 2)).equals("Y") ? "Y":"N";
						bodyStart = line.indexOf("(",4) + 1;
						textBody = line.substring(bodyStart,line.indexOf("))")).trim();
						if(textBody.isEmpty()) {
							trainPrint.println("?," + classYN);
						}else {
							trainPrint.println("\'" + textBody + "\', " + classYN);
						}
					}

					testPrint.println("% Title: Training set for " + topic + sizes[i] + ".");
					testPrint.println("%");
					testPrint.println("% Author: Nathan Roehl");
					testPrint.println("%");
					testPrint.println("@RELATION " + topic);
					testPrint.println();
					testPrint.println("@ATTRIBUTE   textBody   STRING");
					testPrint.println("@ATTRIBUTE   class      {Y,N}");
					testPrint.println();
					testPrint.println("@DATA");

					while(testRead.hasNextLine()) {
						line = testRead.nextLine();
						classYN = (line.substring(1, 2)).equals("Y") ? "Y":"N";
						textBody = line.substring(7,line.indexOf("))")).trim();
						if(textBody.isEmpty()) {
							testPrint.println("?," + classYN);
						}else {
							testPrint.println("\'" + textBody + "\', " + classYN);
						}
					}
				} catch (FileNotFoundException e) {
					System.out.println("Error in creating arff files.");
					System.out.println(e.getMessage());
				}
			}
		}
		//return "The term(s) " + topicsToConvert + " have been converted to arff files.";
		return (top != null ? (top + " ") : "");
	}

}

/**
 * Used to sort list of Tuples for computing MI scores.
 * @author		Nathan Roehl
 * @topic 		CS744 - Fall 2018
 * @assignment	HW3-PartB
 *
 */
class sortTuple implements Comparator<Tuple>{

	@Override
	public int compare(Tuple t1, Tuple t2) {
		return Double.compare(t2.getScore(), t1.getScore());
	}
}
