import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;

/**
 * Searches a lucene index created by Indexer Class.
 * Will search for important medical terms and "boost" those terms to find better matching articles/questions.
 * 
 * @author Nathan Roehl
 *
 */

public class Searcher {

	public String indexDirectoryPath;
	public String inputFilePath;
	public String outputQueryFilePath;

	private static final String CONTENTS = "contents";
	private static final String FILE_NAME = "filename";
	private static final int MAX_COUNT = 100;
	private static MedSet medTermSet = new MedSet();
	private static StopWordList stopWordSet = new StopWordList();
	private final float boostNormal = 0.0f;
	private final float boostUp = 2.0f;


	public void setIndexDirPath(String path) {
		this.indexDirectoryPath = path;
	}

	public void setInputFilePath(String path) {
		this.inputFilePath = path;
	}

	public void setOutputQueryFilePath(String path) {
		this.outputQueryFilePath = path;
	}

	public String getIndexDirPath() {
		return this.indexDirectoryPath;
	}

	public String getInputFilePath() {
		return this.inputFilePath;
	}

	public String getOutPutFileLocation() {
		return this.outputQueryFilePath;
	}



	public String query(String queryFile) {
		try {
			String queryStr = getFileContents(queryFile);
			return answerQuery(queryStr, queryFile);
		} catch (IOException e) {
			return "Error opening file " + queryFile + " during query stage.  Please enter a new file to query.";
		}
	}

	/**
	 * Takes in query, breaks it apart into to smaller queries, applies boost effects,
	 * and outputs results to file and returns String of search results to output to GUI window.
	 * 
	 * @param queryStr
	 * @param queryFile
	 * @throws IOException
	 * @return String
	 * 
	 */
	private String answerQuery(String queryStr, String queryFile) throws IOException {

		Directory dir = FSDirectory.open(Paths.get(indexDirectoryPath));
		IndexReader indexReader = DirectoryReader.open(dir);
		IndexSearcher searcher = new IndexSearcher(indexReader);

		//Add more stop words to analyzer.
		//Is same analyzer used when building indexer.
		Analyzer analyzer = StopWordList.init();
		QueryBuilder builder = new QueryBuilder(analyzer);

		//Add all relevant terms to the boostQuery
		HashMap<String, BoostQuery> boostQuery = getBoostedQueries(queryStr, builder);

		//Used to hold all queries and boosted queries
		Builder bQuery = new BooleanQuery.Builder();
		BoostQuery b;


		//If file was found and boostQuery is full of query's, it will search the reversed index.
		//If it is empty, it will return a string saying error finding the specific file.
		if(!boostQuery.isEmpty()) {
			
			HashMap<String,BoostQuery> boostedTermsforOutput = new HashMap<>();
			
			for(String str: boostQuery.keySet()) {

				b = boostQuery.get(str);
				if(b.getBoost() > 1) {
					boostedTermsforOutput.put(str, b);
				}
				
				//I tried using Occur.MUST, but it never returned anything no matter what combination of terms I used
				bQuery.add(b, Occur.SHOULD);

			}

			//Add all queries to a BooleanQuery object to be searched over.
			BooleanQuery bq = bQuery.build();

			TopDocs topDocs = searcher.search(bq, MAX_COUNT);
			ScoreDoc[] hits = topDocs.scoreDocs;

			int docID, display = 0;
			Document d;
			String fileName = "", tabs = "\t\t";

			//Holds file names that have already been displayed, this helps reduce duplicate files from being returned
			HashSet<String> fileHits = new HashSet<String>();
			fileHits.add(queryFile);
			String result = "";


			//Find the average score of all documents.
			//I noticed hits[] will return counts for the same document sometimes, so I created a HashSet to make a file is not output more than one.
			double avg = 0;
			int totalDocs = 0;
			for(int i = 0; i < hits.length && hits[i].score > 0; i++) {

				docID = hits[i].doc;
				d = searcher.doc(docID);
				fileName = d.get(FILE_NAME);

				if(!fileHits.contains(fileName)) {
					fileHits.add(fileName);
					avg += hits[i].score;
					totalDocs++;
				}

			}

			avg /= totalDocs;
			fileHits = new HashSet<String>();
			fileHits.add(queryFile);

			//PrintWriter used to output results to file.
			try(PrintWriter pw = new PrintWriter(outputQueryFilePath + "\\" + queryFile + "_RESULTS.txt")){

				pw.println("Results for file: " + queryFile);
				pw.println("Please keep in mind, " + queryFile +" has been removed from the search results.");
				pw.println();
				pw.println("Boosted terms during query phase:");
				
				for(String str: boostedTermsforOutput.keySet()) {
					pw.print("(" + str + ")" + "^" + boostedTermsforOutput.get(str).getBoost() + "   ");
				}
				
				pw.println();
				pw.println();
				pw.println("File\t\t\tScore");
				int docCounter = 1;

				for(int i = 0; i < hits.length && hits[i].score > avg; i++) {

					docID = hits[i].doc;
					d = searcher.doc(docID);
					fileName = d.get(FILE_NAME);

					if(!fileHits.contains(fileName)) {

						//output file could have many results, but GUI display is capped at 10 documents
						//I did this to make the display a little cleaner
						if(display < 10)
							result += docCounter + ") File name: " + d.get(FILE_NAME) + ",   Score: " + hits[i].score + " and ";

						pw.println(docCounter++ + ") " + d.get(FILE_NAME) + tabs + hits[i].score);

						fileHits.add(fileName);
						display++;

					}
				}
				return result;
			}
		}
		return "Error creating boosted queries. File " + queryFile + " does not exist.";
	}


	/**
	 * Opens a file and reads all the contents to the a String which is then returned.
	 * 
	 * @param str
	 * @return
	 */
	private String getFileContents(String str) throws IOException {

		String documentText = "";

		try(Scanner scr = new Scanner(new File(inputFilePath + "\\" + str))){
			while(scr.hasNextLine()) {
				documentText += scr.nextLine();
			}
		}

		return documentText;

	}

	/**
	 * Cycles through query removing stop words and punctuation.
	 * 
	 * I wasn't sure if I created many smaller queries in lucene or if it would still remove these words when searching based on the analyzer,
	 * so I removed them first based on the stop word list I created.
	 * 
	 * Applies boost effect to popular medical terms to enhance searching.
	 * If a term shows up multiple times, it's boost is increased even further.
	 * Can also handle bi-words that show up in the medTermSet, meaning "heart disease" will be grouped together as one term and boosted.
	 * Uses TreeSet from Java Collections Framework to store many terms for fast retrieval.
	 * 
	 * @param str
	 * @param builder
	 * @return
	 */
	public HashMap <String, BoostQuery> getBoostedQueries(String str, QueryBuilder builder){

		HashMap <String, Integer> visitedTerms = new HashMap<>();
		HashMap <String, BoostQuery> boostedQueries = new HashMap<>();

		//split the file contents into individual strings
		String[] strTerms = str.split(" ");

		TermQuery t;
		BoostQuery q;
		Integer valBoost;
		boolean formatted = false;
		boolean biWord = false;
		String biWordCombined;

		for(int i = 0; i < strTerms.length; i++) {

			biWordCombined = null;
			strTerms[i] = strTerms[i].trim();

			if(strTerms[i].isEmpty()) {
				continue;
			}

			if(!formatted) {
				strTerms[i] = formatStringTerm(strTerms[i]);
			}

			formatted = false;

			String term = strTerms[i];

			if(isNumber(term)) {
				continue;
			}

			//check if term is located in important medical terms tree
			//if so, this term will get a boost specified by the user
			if(medTermSet.contains(term)) {

				//Check for biwords, need to make sure i does not go out of bounds
				if(i + 1 < strTerms.length) {
					strTerms[i+1] = formatStringTerm(strTerms[i+1]);
					//setting formatted to true tells next iteration that it does not need to format the next str in the array
					formatted = true;
					//if next string is a medical term, found a biword
					//advance i so not to include word again
					if(medTermSet.contains(strTerms[i+1])) {
						biWordCombined = term + " " + strTerms[i+1];
						biWord = true;
					}
				}

				//Check is string term has been added to list or not yet
				//If added already will boost term higher
				if(visitedTerms.containsKey(term)) {
					valBoost = visitedTerms.get(term);
					//Caps boost at 4096
					if(valBoost < 127)
						valBoost *= 2;
					visitedTerms.put(term, valBoost);
				} else {
					//If here, means first time seeing term, place it in the visited hashmap
					//valBoost set to 1 because it is boosted during BoostQuery creation
					visitedTerms.put(term, 2);
					valBoost = 1;
				}

				//If biword, boost higher.
				if(biWord) {
					valBoost *= 4;
					biWord = false;
				}

				t = new TermQuery(new Term(CONTENTS,term));
				//Can modify boostUp to modify how much terms are boosted in the end
				q = new BoostQuery(t, valBoost*boostUp);
				boostedQueries.put(term, q);
				
				if(biWordCombined != null) {
					if(visitedTerms.containsKey(biWordCombined)) {
						valBoost = visitedTerms.get(biWordCombined);
						if(valBoost < 2000)
							valBoost *= 2;
						visitedTerms.put(biWordCombined, valBoost);
					} else {
						visitedTerms.put(biWordCombined, 2);
						valBoost = 1;
					}
					
					t = new TermQuery(new Term(CONTENTS,biWordCombined));
					//Can modify boostUp to modify how much terms are boosted in the end
					q = new BoostQuery(t, valBoost*boostUp);
					boostedQueries.put(biWordCombined, q);
					
				}

			} 
			//if the term is not in the medical terms and not part of the stop words,
			//it is added to the list with a boost level specified by boostNormal.
			else if(!stopWordSet.containsStopWord(term)) {

				t = new TermQuery(new Term(CONTENTS,term));
				q = new BoostQuery(t, boostNormal);
				boostedQueries.put(term, q);

				
			}
		}

		//Return hashmap with boostedQueries
		return boostedQueries;

	}

	/**
	 * Tells if a string is a number or not.
	 * May have issues with "-" numbers.
	 * 
	 * @param str
	 * @return
	 */
	private static boolean isNumber(String str) {
		if(str == null || str.isEmpty()) {
			return false;
		}
		for(int i = 0; i < str.length(); i++) {
			if(!Character.isDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * A private helper method that removes any punctuation, trims the string, and converts it to lower case.
	 * 
	 * @param str
	 * @return
	 */
	private static String formatStringTerm(String str) {

		if(str == null || str.isEmpty()) {
			return "";
		}

		str = str.replaceAll("[.,!?()]", "").toLowerCase().trim();

		return str;

	}

}
