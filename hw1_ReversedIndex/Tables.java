package hw1_ReversedIndex;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;


/**
 * A class that stores the reversedIndex, an ArrayList of terms.
 * It also stores all documents as an ArrayList.
 * Class parses,creates, and answers all queries.
 * 
 * 
 * @author Nathan Roehl - CS744 - HW1
 *
 */
public class Tables {

	ArrayList<Term> reversedIndex = new ArrayList<>();						//holds the reversed index, a list of terms
	ArrayList<Document> documentLists = new ArrayList<>();					//holds all documents used to create reversedIndex
	String inputFilePath;
	String outputFilePath;

	public Tables() {
		inputFilePath = "";
		outputFilePath = "";
	}

	public Tables(String inputFilePath, String outputFilePath) {
		this.inputFilePath = inputFilePath;
		this.outputFilePath = outputFilePath;
	}

	public void addFilePaths(String inputFilePath, String outputFilePath) {
		this.inputFilePath = inputFilePath;
		this.outputFilePath = outputFilePath;
	}

	public ArrayList<Term> getAllTerms(){
		return this.reversedIndex;
	}

	public ArrayList<Document> getAllDocuments(){
		return this.documentLists;
	}


	/**
	 * No duplicate terms allowed.  If string passed in matches existing term, returns that term to adjust locations.
	 * If no term exists, returns null.
	 * Sequential search of reversedIndex.  Used during construction.
	 * 
	 * @param termStr
	 * @return
	 */
	private Term getTerm(String termStr) {

		for(Term t: reversedIndex) {
			if(t.getName().equals(termStr)) {
				return t;
			}
		}
		return null;
	}

	/**
	 * Binary search used during query phases.
	 * Can't be used during construction, since Terms are not ordered when added to reversedIndex
	 * @param termStr
	 * @return
	 */
	public Term search(String termStr) {
		Term toFind = null;
		Term midTerm;
		int mid, low = 0, hi = reversedIndex.size() - 1;

		while(low <= hi) {
			mid = (low + hi) / 2;
			midTerm = reversedIndex.get(mid);

			if(midTerm.equals(termStr)) {
				return midTerm;
			} else if(midTerm.getName().compareTo(termStr) > 0) {
				hi = mid - 1;
			} else {
				low = mid + 1;
			}


		}

		return toFind;
	}


	/**
	 * Opens files, parses out the words, and enters the terms into the reversedIndex.
	 * Also creates the Document objects needed to help map a term to its locations in a Document.
	 * 
	 * @param arrayOfFiles - is list of strings of files to open
	 * @param documentsList
	 * @throws FileNotFoundException 
	 */

	public void parseFiles(String[] arrayOfFiles) throws FileNotFoundException {
		String line;
		String[] lineSplit;

		//loops over each document to read
		for(int i = 0; i < arrayOfFiles.length; i++) {
			int indexCount = 0;
			int totalWords = 0;

			//creates and sets document name to number processed
			//also pass in actual document name to remember actual document, this does remove ".txt"
			Document doc = new Document((i+1) + "", arrayOfFiles[i].substring(0, arrayOfFiles[i].indexOf(".")));
			documentLists.add(doc);


			//start of reading files
			try(Scanner reader = new Scanner(new File(inputFilePath + arrayOfFiles[i]))){
				while(reader.hasNextLine()) {

					line = reader.nextLine();

					//split passed in line into individual strings
					lineSplit = line.split(" ");
					totalWords += lineSplit.length;

					for(int j = 0; j < lineSplit.length; j++) {

						if(!lineSplit[j].isEmpty()) {
							lineSplit[j] = lineSplit[j].toLowerCase().trim();
							lineSplit[j] = formatString(lineSplit[j]);	
						}

					}

					if(lineSplit != null && lineSplit.length >= 1) {
						//adds string to reversedIndex correctly as Terms
						fillTables(doc, lineSplit, indexCount);
						//keeps track of word location in document
						indexCount += lineSplit.length;
					}

				}

				doc.setTotalWords(totalWords);
			} catch(FileNotFoundException e) {
				
			}
		}

	}

	/**
	 * Private helper method to format a String.
	 * Returns $number, string as is, or string with punctuation removed.
	 * 
	 * @param x
	 * @return
	 */
	private String formatString(String x) {

		//if string passed in is a number, return $number
		if(Character.isDigit(x.charAt(0))){
			boolean isDigit = true;
			for(int i = 1; i < x.length() && isDigit; i++) {
				if(!Character.isDigit(x.charAt(i))) {
					isDigit = false;
				}
			}
			if(isDigit)
				return "$number";
		}

		String formatted = x.toLowerCase();

		int period = formatted.lastIndexOf(".");
		if(period != -1 && period == formatted.length()-1) {
			formatted = formatted.substring(0,period);
		}

		int comma = formatted.lastIndexOf(",");
		if (comma != -1 && comma == formatted.length()-1) {
			formatted = formatted.substring(0,comma);
		}

		int semicolon = formatted.lastIndexOf(":");
		if (semicolon != -1 && semicolon == formatted.length()-1) {
			formatted = formatted.substring(0,semicolon);
		}

		int questionMark = formatted.lastIndexOf("?");
		if (questionMark != -1 && questionMark == formatted.length()-1) {
			formatted = formatted.substring(0,questionMark);
		}

		int percentage = formatted.lastIndexOf("%");
		if(percentage != -1 && percentage == formatted.length()-1) {
			formatted = formatted.substring(0,percentage);
		}

		return formatted;
	}


	/**
	 * Private helper method to loop over a string array and add the terms to reversedInex
	 * 
	 * @param doc - Document term was contained in
	 * @param array - array of strings read from file
	 * @param indexCount
	 */
	private void fillTables(Document doc, String[] array, int indexCount) {
		int count = indexCount;
		for(int i = 0; i < array.length; i++) {
			if(!array[i].isEmpty()) {
				addTermAndDocument(doc, array[i], count + i);
			} else {
				count--;
			}
		}
	}


	/**
	 * Creates a term and links a term to the document it came from and its index in the document.
	 * No duplicate Terms are allowed in reversedIndex.
	 * 
	 * @param document - Document linked to term
	 * @param termStr - String parameter to convert to Term
	 * @param index = Stores index of Term in Document
	 */
	public void addTermAndDocument(Document document, String termStr, int index) {

		Term term = getTerm(termStr);					//if term does not exist, will be null.  Otherwise will be existing term.

		if(term == null) {								//if null need to create and add new term to reversed index
			term = new Term(termStr);
			reversedIndex.add(term);
		}

		term.addLocation(document, index);				//use methods of Term class to add location and document for term
		term.addDocument(document);
		document.addTerm(term);							//have document keep track of what terms are used in it

	}


	/**
	 * Creates files for terms, raw counts, and second postings assigned to graduate students.
	 * Initially had this set as three different methods, but thought it was wasteful to loop over reversedIndex 3 times.
	 * Can either call this one or call three other methods separately to create documents.
	 * 
	 * @param projectName - Used to print projectName in header of each file
	 */
	public void createTermsRawCountsSecondPostings(String projectName) throws FileNotFoundException {

		try(PrintWriter writeTerms = new PrintWriter(outputFilePath + projectName + "terms.txt");
				PrintWriter writeRawCounts = new PrintWriter(outputFilePath + projectName + "rawcounts.txt");
				PrintWriter writeSecondPostings = new PrintWriter(outputFilePath + projectName + "secondPostings.txt")){

			writeTerms.println(projectName + "terms: total = " + reversedIndex.size());
			writeRawCounts.println(projectName + "rawcounts: total ");
			writeSecondPostings.println(projectName + "secondposting: ");

			ArrayList<Document> documentsPerTerm;
			ArrayList<Integer> termLocationsPerDoc;
			Document currentDoc;
			int termUsagePerDoc;

			for(Term t: reversedIndex) {												//loop over all terms

				writeTerms.print("\t(" + t.getName() + " ");
				writeRawCounts.print("\t(" + t.getName() + " ");
				writeSecondPostings.print("\t(" + t.getName() + " ");

				documentsPerTerm = t.getDocumentsForTerm();								//get documents associated with current term
				int totalDocumentsPerTerm = documentsPerTerm.size();					//get total number of documents term is used in

				for(int i = 0; i < totalDocumentsPerTerm; i++) {

					currentDoc = documentsPerTerm.get(i);								//get document associated with term
					writeTerms.print(currentDoc.getName());								//write document name to output file terms

					termLocationsPerDoc = t.getLocationsForDocument(currentDoc);		//get all locations of term in document
					termUsagePerDoc = termLocationsPerDoc.size();						//get total number of times term used in document

					writeRawCounts.print("(" + currentDoc.getName() + " ");				//write document name to output file rawcount
					writeRawCounts.print(termUsagePerDoc + ")");						//write total number of times term used in document

					for(int j = 0; j < termUsagePerDoc; j++) {
						writeSecondPostings.print("(" + currentDoc.getName() + " ");	//write document name to output file second postings
						writeSecondPostings.print(termLocationsPerDoc.get(j) + ")");	//write location of term in document to output file second postings

						if(j != termUsagePerDoc - 1) {
							writeSecondPostings.print(" ");								//adds space between locations of term in document
						}
					}

					if(i != totalDocumentsPerTerm - 1) {
						writeTerms.print(" ");
						writeRawCounts.print(" ");
						writeSecondPostings.print(" ");
					}
				}

				writeTerms.println(")");
				writeRawCounts.println(")");
				writeSecondPostings.println(")");

			}

		}
	}

	/**
	 * Creates file for terms.
	 * 
	 * @param projectName
	 */
	public void createTerms(String projectName) throws FileNotFoundException {

		try(PrintWriter writeTerms = new PrintWriter(outputFilePath + projectName + "terms.txt")){

			writeTerms.println(projectName + "terms: total = " + reversedIndex.size());

			ArrayList<Document> documentsPerTerm;
			Document currentDoc;


			for(Term t: reversedIndex) {												//loop over all terms

				writeTerms.print("\t(" + t.getName() + " ");

				documentsPerTerm = t.getDocumentsForTerm();								//get documents associated with current term
				int totalDocumentsPerTerm = documentsPerTerm.size();					//get total number of documents term is used in

				for(int i = 0; i < totalDocumentsPerTerm; i++) {

					currentDoc = documentsPerTerm.get(i);								//get document associated with term
					writeTerms.print(currentDoc.getName());								//write document name to output file terms


					if(i != totalDocumentsPerTerm - 1) {
						writeTerms.print(" ");
					}
				}

				writeTerms.println(")");

			}

		}
	}

	/**
	 * Creates file for raw counts.
	 * 
	 * @param projectName
	 */
	public void createRawCounts(String projectName) throws FileNotFoundException {

		try(PrintWriter writeRawCounts = new PrintWriter(outputFilePath + projectName + "rawcounts.txt")){


			writeRawCounts.println(projectName + "rawcounts: ");
			
			ArrayList<Document> documentsPerTerm;
			ArrayList<Integer> termLocationsPerDoc;
			Document currentDoc;
			int termUsagePerDoc;

			for(Term t: reversedIndex) {												//loop over all terms

				writeRawCounts.print("\t(" + t.getName() + " ");

				documentsPerTerm = t.getDocumentsForTerm();								//get documents associated with current term
				int totalDocumentsPerTerm = documentsPerTerm.size();					//get total number of documents term is used in

				for(int i = 0; i < totalDocumentsPerTerm; i++) {

					currentDoc = documentsPerTerm.get(i);								//get document associated with term


					termLocationsPerDoc = t.getLocationsForDocument(currentDoc);		//get all locations of term in document
					termUsagePerDoc = termLocationsPerDoc.size();						//get total number of times term used in document

					writeRawCounts.print("(" + currentDoc.getName() + " ");				//write document name to output file rawcount
					writeRawCounts.print(termUsagePerDoc + ")");						//write total number of times term used in document


					if(i != totalDocumentsPerTerm - 1) {
						writeRawCounts.print(" ");
					}
				}

				writeRawCounts.println(")");

			}

		}
	}

	public  void createDocSizes(String projectName) throws FileNotFoundException {

		try(PrintWriter writeDocSizes = new PrintWriter(outputFilePath + projectName + "docsize.txt")){
			writeDocSizes.println(projectName + "docsize: total = " + documentLists.size());

			for(Document d : documentLists) {
				writeDocSizes.println("\t(" + d.getName() + " " + d.getTotalWords() +")");
			}


		} catch(FileNotFoundException e) {
			System.out.println("Error writing file.");
		}
	}

	/**
	 * Creates file for second postings assigned to graduate students.
	 * 
	 * @param projectName
	 */
	public void createSecondPostings(String projectName) throws FileNotFoundException {

		try(PrintWriter writeSecondPostings = new PrintWriter(outputFilePath + projectName + "secondPostings.txt")){

			writeSecondPostings.println(projectName + "secondposting: ");

			ArrayList<Document> documentsPerTerm;
			ArrayList<Integer> termLocationsPerDoc;
			Document currentDoc;
			int termUsagePerDoc;

			for(Term t: reversedIndex) {												//loop over all terms

				writeSecondPostings.print("\t(" + t.getName() + " ");

				documentsPerTerm = t.getDocumentsForTerm();								//get documents associated with current term
				int totalDocumentsPerTerm = documentsPerTerm.size();					//get total number of documents term is used in

				for(int i = 0; i < totalDocumentsPerTerm; i++) {

					currentDoc = documentsPerTerm.get(i);								//get document associated with term

					termLocationsPerDoc = t.getLocationsForDocument(currentDoc);		//get all locations of term in document
					termUsagePerDoc = termLocationsPerDoc.size();						//get total number of times term used in document

					for(int j = 0; j < termUsagePerDoc; j++) {
						writeSecondPostings.print("(" + currentDoc.getName() + " ");	//write document name to output file second postings
						writeSecondPostings.print(termLocationsPerDoc.get(j) + ")");	//write location of term in document to output file second postings

						if(j != termUsagePerDoc - 1) {
							writeSecondPostings.print(" ");								//adds space between locations of term in document
						}
					}

					if(i != totalDocumentsPerTerm - 1) {
						writeSecondPostings.print(" ");
					}
				}

				writeSecondPostings.println(")");

			}

		}
	}

	public void createCalcStats(String projectName) throws FileNotFoundException {

		try(PrintWriter writeStats = new PrintWriter(outputFilePath + projectName + "stats.txt")){

			writeStats.println(projectName + "stats: ");

			ArrayList<Document> documentsPerTerm;
			ArrayList<Integer> termLocationsPerDoc;
			String tfvalue;
			Document currentDoc;

			for(Term t: reversedIndex) {

				writeStats.print("\t(" + t.getName() + " ");
				documentsPerTerm = t.getDocumentsForTerm();						//get list of documents associated with term
				int totalDocumentsPerTerm = documentsPerTerm.size();			//Get total number of times term shows up in document
				writeStats.print(totalDocumentsPerTerm + " ");

				for(int i = 0; i < totalDocumentsPerTerm; i++) {
					currentDoc = documentsPerTerm.get(i);
					termLocationsPerDoc = t.getLocationsForDocument(currentDoc);
					writeStats.print("(" + currentDoc.getName() + " ");

					double x = termLocationsPerDoc.size();						//size and totalTerms are ints, must convert to doubles to get correct division
					double y = currentDoc.getTotalTerms();
					double z = x/y;

					t.addTermFrequency(currentDoc, z);							//store term frequency in current term for reuse if needed

					tfvalue = z + "";											//convert term frequency to string to output to file

					if(tfvalue.length() > 8) {									//truncates decimal if too large
						tfvalue = tfvalue.substring(0, 8);
					}

					//writeStats.print((int)x + "/" + (int)y + " = " + tfvalue + ")");
					writeStats.print( tfvalue + ")");


					if(i != totalDocumentsPerTerm - 1) {
						writeStats.print(" ");
					}

				}

				writeStats.println(")");

			}

		}
	}

	
	/**
	 * Handlse single query questions.
	 * 
	 * @param projectName
	 * @param queryStr
	 */
	public void singleQuery(String projectName, String queryStr) throws FileNotFoundException {

		try(PrintWriter writeQuery = new PrintWriter(outputFilePath + projectName + "." + queryStr + ".results.txt"))
		{
			writeQuery.println(queryStr + "." + projectName + ".results");

			Term queryTerm = search(formatString(queryStr));

			if(queryTerm == null) {												//if term does not exit, return
				writeQuery.println("\tTerm " + queryStr + " was not found.");
				return;
			}

			Double tf, idf, tfidf;												//Use Double (not double) because ArrayLists only store objects
			queryTerm.sortDocuments();
			ArrayList<Document> documents = queryTerm.getDocumentsForTerm();
			ArrayList<DocumentTFIDF> tfidfContainer = new ArrayList<>();		//Used to store (document, tfidf) for sorting and outputting to file

			for(Document doc: documents) {

				tfidf = queryTerm.getTermFrequencyIDF(doc);						//retrieve pre-calculated idf value if there

				if(tfidf == null) {												//if null, need to calculate value.
					tf = queryTerm.getTermFrequency(doc);
					idf = Math.log(((double)(documentLists.size()))/(queryTerm.getTotalDocumentsPerTerm()));

					if(idf == 0) {												//if idf = 1, log(1) = 0
						tfidf = 1.0;											//set default value for number to division by zero does not occur
					} else {
						tfidf = tf/idf;
					}

					queryTerm.addTermFrequencyIDF(doc, tfidf);					//store tfidf in the term with document for future use
				}

				tfidfContainer.add(new DocumentTFIDF(doc,tfidf));				//Store (document, tfidf) together for sorting after done searching.
																				//Tuples are not a class in java, so I made my own.
			}

			sortDocumentTFIDF(tfidfContainer);									//Sort documents based on tfidf value

			for(DocumentTFIDF docT : tfidfContainer) {							//after sorting, output to file
				String val = docT.getValue() + "";
				if(val.length() >= 8) {
					val = val.substring(0, 8);
				}
				writeQuery.println("\t(" + queryTerm.getName() + " (" + docT.getDocument().getName() + " " + val + "))");
			}

		}
	}


	/**
	 * Takes two terms to search for and outputs if any documents contain these two terms combined next to each other.
	 * 
	 * @param projectName
	 * @param query
	 */
	public void biGramQuery(String projectName, String[] query) throws FileNotFoundException {

		if(query.length != 2 || query[0] == null || query[1] == null) {
			return;
		}

		String combinedQuery = query[0] + " " + query[1];

		try(PrintWriter writeQuery = new PrintWriter(outputFilePath + projectName + "." + query[0] + "-" + query[1] + ".results.txt"))
		{
			writeQuery.println(combinedQuery + "." + projectName + ".results");

			Term queryTerm1 = search(formatString(query[0]));
			Term queryTerm2 = search(formatString(query[1]));

			if(queryTerm1 == null || queryTerm2 == null) {								//if term does not exist, return
				writeQuery.println("\tTerm \"" + combinedQuery + "\" was not found.");
				return;
			}



			ArrayList<Document> sharedDocs = new ArrayList<>();							

			for(Document d1: queryTerm1.getDocumentsForTerm()) {
				for(Document d2: queryTerm2.getDocumentsForTerm()) {
					if(d1.equals(d2)) {
						sharedDocs.add(d1);												//if terms share a document, add to sharedDocs
						break;
					}
				}
			}

			if(sharedDocs.size() == 0) {												//if no shared documents, return
				writeQuery.println("\tTerm " + combinedQuery + " not found.");
				return;
			}

			ArrayList<Integer> arrayList1;
			ArrayList<Integer> arrayList2;
			DocumentBiqueryCount tmpDocCount;											//used to store Document with raw count, again a type of tuple to help with sorting
			ArrayList<DocumentBiqueryCount> finalDocList = new ArrayList<>();

			for(Document d: sharedDocs) {												//search each shared document
				tmpDocCount = new DocumentBiqueryCount(d,0);
				arrayList1 = queryTerm1.getLocationsForDocument(d);						//store locations of term1 in arrayList1
				arrayList2 = queryTerm2.getLocationsForDocument(d);						//store locations of term2 in arrayList2

				int m = 0;
				int n = 0;

				while (m < arrayList1.size() && n < arrayList2.size()) {				//compare locations of each term in shared document
					int loc1 = arrayList1.get(m);
					int loc2 = arrayList2.get(n);

					if(loc1 == loc2 - 1) {												//if locations match, found a location of combined query
						tmpDocCount.incCount();
						m++;
						n++;
					} else if (loc1 > loc2) {											//advance location of term2
						n++;
					} else {															//advance location of term1
						m++;
					}
				}
				if(tmpDocCount.getCount() > 0) {										//only add to final combined list if documents contains combined query
					finalDocList.add(tmpDocCount);
				}
			}


			if(finalDocList.size() == 0) {												//If no combined match, return
				writeQuery.println("\tTerm \"" + combinedQuery + "\" was not found.");
				return;
			}

			sortDocumentBiquery(finalDocList);											//sort occurrences by raw count
			
			writeQuery.print("(" + combinedQuery + " ");
			for(int i = 0; i < finalDocList.size(); i++) {

				DocumentBiqueryCount d = finalDocList.get(i);
				if(d.getCount() > 0) {
					writeQuery.print("(" + d.getDoc().getName() + " " + d.getCount() + ")");

					if(i != finalDocList.size() - 1) {
						writeQuery.print(" ");
					}
				}
			}

			writeQuery.print(")");


		}
	}

/**
 * Logic query can handle as many $and or $or or $not statements as you wish.
 * But, they must be in correct format.  Meaning "biomedical $and $or test" will not work.
 * Efficiency is not taken in to account, meaning it does not scan the query and search for the best terms to start with.
 * It merely picks the first two queries, if there are two, then combines them accordingly.
 * If there are more terms to search for, this new term is merged with previous queries until complete.
 * Terms must be separated by logical operators for this method to work.
 * 
 * @param projectName
 * @param query
 */
	public void logicQuery(String projectName, String[] query) throws FileNotFoundException {

		String queryStr = String.join(" ", query);

		try(PrintWriter writeLogicQuery = new PrintWriter(outputFilePath + projectName + "." + queryStr + ".results.txt"))
		{
			writeLogicQuery.println(queryStr + ":" + projectName + ".results");
			if(query == null || query.length == 0) {
				//if query is empty, simple output message and return.
				writeLogicQuery.println("\tIncorrect format. No input entered.");
				return;
			}

			if("$and".equals(query[0].trim())) {
				writeLogicQuery.println("\tIncorrect format. Can't start with $and");
				return;
			}
			
			if("$or".equals(query[0].trim())) {
				writeLogicQuery.println("\tIncorrect format. Can't start with $or");
				return;
			}

			int[] processIndex = new int[query.length];

			//if "or" follows "and" or "and" follows "or" will output error
			//fill processIndex: 0 = normal word, 1 = and, 2 = or, 3 = not
			for(int i = 0; i < query.length - 1; i++) {
				if(query[i].equals("$and")) {
					if(query[i+1].equals("$or")) {
						writeLogicQuery.println("\tIncorrect format. Cannot have $and followed by $or.");
						return;
					}
					processIndex[i] = 1;

				} else if (query[i].equals("$or")) {
					if(query[i+1].equals("$and")) {
						writeLogicQuery.println("\tIncorrect format. Cannot have $or followed by $and.");
						return;
					}
					processIndex[i] = 2;

				} else if (query[i].equals("$not")){
					processIndex[i] = 3;
				} else {
					processIndex[i] = 0;
				}
			}

			int i = 0;

			boolean notBeginning = false;
			boolean andStatement = false;

			if(processIndex[i] == 3) {
				i++;
				notBeginning = true;
			}

			Term q1 = search(query[i]);
			ArrayList<Document> tmp;

			if(notBeginning) {

				if(q1 == null) {

					q1 = new Term(query[i]);
					for(Document d1: documentLists) {		//term does not exist but has "not" infront of it, so add all documents to q1
						q1.addDocument(d1);
					}

				} else {
					tmp = q1.getDocumentsForTerm();			//take "not" of document


					q1 = new Term(query[i]);				//re-create q1 so we do not add more Documents to actual term found
					for(Document d1: documentLists) {
						if(!tmp.contains(d1)) {
							q1.addDocument(d1);
						}
					}
				}

			} else {
				if(q1 == null) {
					q1 = new Term(query[i]);				//q1 does not exist in the document, so leave documents list empty
				}
			}


			Term q2 = null;
			if((q1 != null) && (i+1 < query.length) && (processIndex[i+1] == 1 || processIndex[i+1] == 2)) {	//process rest of query if more, only handles two statements


				if(processIndex[i+1] == 1) {
					andStatement = true;
				}

				if(i+2 < query.length && processIndex[i+2] == 3 && (i+3) < query.length) {			//process not statement

					q2 = search(query[i+3]);

					if(q2 == null) {
						q2 = new Term(query[i+3]);							//term doesn't exist, add all documents
						for(Document d1: documentLists) {
							q2.addDocument(d1);
						}
					} else {
						tmp = q2.getDocumentsForTerm();						//term exists, add not of existing term
						q2 = new Term(query[i+3]);							//reset q2 to not modify actual term
						for(Document d1: documentLists) {
							if(!tmp.contains(d1)) {
								q2.addDocument(d1);
							}
						}
					}

					i+=4;													//advance i to point at "not" character for next round

				} else {
					q2 = search(query[i+2]);
					if(q2 == null) {
						q2 = new Term(query[i+2]);
					}
					i+=3;													//advance i to point to next character
				}
			}

			ArrayList<Document> queryList;

			if(query.length >= 3) {				//if original query contains 3 or more terms combine document lists from 2 files
				if(andStatement){
					queryList = mergeDocuments(q1.getDocumentsForTerm(), q2.getDocumentsForTerm(), true);
				} else {
					queryList = mergeDocuments(q1.getDocumentsForTerm(), q2.getDocumentsForTerm(), false);
				}
				sortDocuments(queryList);					//sort Document list after merging
			} else {
				queryList = q1.getDocumentsForTerm();		//if lest then 3, means was not of some term, simply get the documents
				i+=3;
			}

			

			//after first terms are merged, merge every term afterwards with new list
			//algoritm does not take into account grouping or best way to process terms
			while(i < query.length) {

				//set boolean to say if "and" statement or not
				if(processIndex[i] == 1) {
					andStatement = true;
				} else {
					andStatement = false;
				}

				//advance "i" to read next word
				i++;

				//if "not" variable, set boolean and advance pointer
				//otherwise reset boolean variable
				if(processIndex[i] == 3) {
					i++;
					notBeginning = true;
				} else {
					notBeginning = false;
				}

				q1 = search(query[i]);

				if(notBeginning) {

					if(q1 == null) {

						//re-create q1 so we do not add more Documents to actual term found
						q1 = new Term(query[i]);
						//term does not exist but has "not" infront of it, so add all documents to q1
						for(Document d1: documentLists) {
							q1.addDocument(d1);
						}

					} else {
						//take "not" of document
						tmp = q1.getDocumentsForTerm();			

						//re-create q1 so we do not add more Documents to actual term found
						q1 = new Term(query[i]);
						for(Document d1: documentLists) {
							if(!tmp.contains(d1)) {
								q1.addDocument(d1);
							}
						}
					}

				} else {
					if(q1 == null) {

						//q1 does not exist in the document, so leave documents list empty
						q1 = new Term(query[i]);
					}
				}

				if(andStatement){
					queryList = mergeDocuments(queryList, q1.getDocumentsForTerm(), true);
				} else {
					queryList = mergeDocuments(queryList, q1.getDocumentsForTerm(), false);
				}

				//sort Document list after merging
				sortDocuments(queryList);

				//advance "i" to point at next "or" or "and" statement
				i++;

			}

			writeLogicQuery.print("\t(" + queryStr + " (");
			Document currentDoc;

			//if empty, simply say list is empty
			if(queryList.isEmpty()) {
				writeLogicQuery.print("~Empty List~");
			} else {

				for(int j = 0; j < queryList.size(); j++) {
					//get document associated with term
					currentDoc = queryList.get(j);
					//write document name to output file terms
					writeLogicQuery.print(currentDoc.getName());				


					if(j != queryList.size() - 1) {
						writeLogicQuery.print(" ");
					}
				}
			}

			writeLogicQuery.print("))");


		}
	}

	
	/**
	 * Helper method to combine two lists.
	 * If and == true, perform "and" join.
	 * If and == false, perform "or" join.
	 * @param d1
	 * @param d2
	 * @param and
	 * @return
	 */
	private ArrayList<Document> mergeDocuments(ArrayList<Document> d1, ArrayList<Document> d2, boolean and){
		ArrayList<Document> combinedList = new ArrayList<>();
		ArrayList<Document> shortList;
		ArrayList<Document> longList;

		if(d1.size() <= d2.size()) {		//find shorter of two lists, can make anding two documents go faster
			shortList = d1;
			longList = d2;
		} else {
			shortList = d2;
			longList = d1;
		}

		if(and) {							//if anding two document lists, merge accordingly

			for(Document d: shortList) {
				if(longList.contains(d)) {
					combinedList.add(d);
				}
			}

		} else {							//if oring, merge accordingly
			for(Document d: shortList) {
				combinedList.add(d);
			}
			for(Document d: longList) {
				if(!combinedList.contains(d)) {
					combinedList.add(d);
				}
			}
		}

		return combinedList;
	}


	/**
	 * Sorts the terms of the reversedindex lexicographicall using java provided sorting algorithm.
	 */
	public void sortTerms() {
		Collections.sort(reversedIndex, new sortByTerm());
	}

	/**
	 * Sorts the terms of the Documents lexicographically.
	 * If document total went over 10, sorting would not work correctly.
	 * would sort 1, 10, 2, 3, . . . 9
	 * Call self made compareString to ensure 10 comes after.
	 */
	public void sortDocuments() {
		for(int i = 1; i < documentLists.size(); i++) {
			int j = i;

			while(j > 0 && Tables.compareStrings(documentLists.get(j-1).getName(), documentLists.get(j).getName())){

				Document x = documentLists.get(j-1);
				Document y = documentLists.get(j);
				documentLists.set(j-1, y);
				documentLists.set(j, x);

				j--;
			}
		}	
	}


	/**
	 * Private helper method to sort Document Lists Internally.
	 * Used when merging two lists.
	 * @param docList
	 */
	private void sortDocuments(ArrayList<Document> docList) {
		for(int i = 1; i < docList.size(); i++) {
			int j = i;

			while(j > 0 && Tables.compareStrings(docList.get(j-1).getName(), docList.get(j).getName())){

				Document x = docList.get(j-1);
				Document y = docList.get(j);
				docList.set(j-1, y);
				docList.set(j, x);

				j--;
			}
		}	
	}

	/**
	 * Sort documents based on tf-idf from largest to smallest..
	 * @param arrList
	 */
	public void sortDocumentTFIDF(ArrayList<DocumentTFIDF> arrList) {
		for(int i = 1; i < arrList.size(); i++) {
			int j = i;

			while(j > 0 && arrList.get(j - 1).getValue() < arrList.get(j).getValue()) {

				DocumentTFIDF x = arrList.get(j-1);
				DocumentTFIDF y = arrList.get(j);
				arrList.set(j-1, y);
				arrList.set(j, x);

				j--;

			}
		}	
	}

	/**
	 * Used to sort documents from largest to smalled when using BiQuery
	 * @param arrList
	 */
	public void sortDocumentBiquery(ArrayList<DocumentBiqueryCount> arrList) {
		Collections.sort(arrList, new sortByDocBiQuery());
	}

	/**
	 * Helper function to determine if two strings are out of order.
	 * If method returns true, means a and b need to swap.
	 * 
	 * @param a
	 * @param b
	 * @return
	 *
	 */
	public static boolean compareStrings(String a, String b) {

		if(b.length() > a.length())
			return false;

		if(a.length() > b.length())
			return true;

		int shortest = a.length() <= b.length() ? a.length() : b.length();
		char x,y;

		for(int i = 0; i < shortest; i++) {

			x = Character.toLowerCase(a.charAt(i));
			y = Character.toLowerCase(b.charAt(i));

			if(x == y) {
				continue;
			} else if (x > y) {
				return true;
			} else {
				return false;
			}

		}

		if(shortest == a.length() && shortest == b.length()) {
			//means same
			return false;
		} else if(shortest == a.length()) {
			//means a is shorter
			return false;
		} else {
			//b is shorter, so swap
			return true;
		}

	}

	class sortByDocuments implements Comparator<Document>{

		@Override
		public int compare(Document doc1, Document doc2) {
			return doc1.getName().compareTo(doc2.getName());
		}
	}


	class sortByTerm implements Comparator<Term>{

		@Override
		public int compare(Term t1, Term t2) {
			return t1.getName().compareTo(t2.getName());
		}
	}

	class sortByDocBiQuery implements Comparator<DocumentBiqueryCount>{

		@Override
		public int compare(DocumentBiqueryCount doc1, DocumentBiqueryCount doc2) {
			return doc1.getCount() - doc2.getCount();
		}
	}

}
