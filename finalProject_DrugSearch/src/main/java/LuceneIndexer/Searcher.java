package LuceneIndexer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.JOptionPane;

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
 * Uses prebuilt Lucene Index containing 200 drugs to answer user queries.
 * Handles symptom search and similar drug search.
 * Loads a prebuilt synonym map which is used for query expansion.
 * 
 * @author Nathan Roehl and Nisreen Abdel Karim Ahmad Al Khun
 *
 */
public class Searcher {

	public static final String INDEX_DIRECTORY_PATH = Indexer.INDEX_DIRECTORY_PATH;
	public static final String FILES_TO_READ_PATH = Indexer.FILES_TO_READ_PATH;

	private static final int MAX_COUNT = 100;
	private static MedSet medTermSet = new MedSet();
	public static final StopWordList stopWordSet = new StopWordList();
	private static final LinkedList<Document> allDrugs = new LinkedList<>();
	private static float boostNormal = 1.0f;
	private static float boostUp = 2.0f;
	private static CustomSynonymMap sMap;
	private Analyzer analyzer;
	private Directory dir;
	private IndexReader indexReader;
	private IndexSearcher searcher;
	private QueryBuilder builder;


	public Searcher() {

		sMap = new CustomSynonymMap();

		try {

			FileInputStream fi = new FileInputStream(new File("CustomSynonymMap.txt"));
			ObjectInputStream oi = new ObjectInputStream(fi);

			sMap = (CustomSynonymMap)oi.readObject();

			oi.close();
			fi.close();

		} catch (FileNotFoundException e) {
			System.out.println("File not found");
			JOptionPane.showMessageDialog(null, "File not found." , "Error", JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			System.out.println("Error initializing stream");
			JOptionPane.showMessageDialog(null, "Error initializing stream." , "Error", JOptionPane.ERROR_MESSAGE);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Class not found." , "Error", JOptionPane.ERROR_MESSAGE);
		}

		try {

			analyzer = StopWordList.init();
			dir = FSDirectory.open(Paths.get(INDEX_DIRECTORY_PATH));
			indexReader = DirectoryReader.open(dir);
			searcher = new IndexSearcher(indexReader);
			builder = new QueryBuilder(analyzer);

		} catch (Exception e) {
			System.out.println("Error opening lucene index.");
			JOptionPane.showMessageDialog(null, "Error open lucene index." , "Error", JOptionPane.ERROR_MESSAGE);
		}
		
		try {
			storeAllDrugs();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			JOptionPane.showMessageDialog(null, "Error storing all drugs." , "Error", JOptionPane.ERROR_MESSAGE);
		}


	}



	public LinkedList<Document> symptomQuery(String queryStr, boolean addSynonyms) throws IOException {

		//Add all relevant terms to the boostQuery
		ArrayList<BoostQuery> boostQuery = getBoostedQueries(queryStr.replaceAll("\n", " "),addSynonyms);

		//Used to hold all queries and boosted queries
		Builder bQuery = new BooleanQuery.Builder();

		if(!boostQuery.isEmpty()) {
			for(BoostQuery b: boostQuery) {
				bQuery.add(b, Occur.SHOULD);
			}
		}

		BooleanQuery bq = bQuery.build();

		TopDocs topDocs = searcher.search(bq, MAX_COUNT);
		ScoreDoc[] hits = topDocs.scoreDocs;
		
		int docID;
		Document d;

		LinkedList<Document> searchResults = new LinkedList<>();

		HashSet<String> seenDocs = new HashSet<>();
		String name = null, category;
		String category1 = null, category2 = null;

		for(int i = 0; i < hits.length; i++) {

			docID = hits[i].doc;
			d = searcher.doc(docID);
			name = d.get(Indexer.NAME);
			if(category1 == null) {
				category1 = d.get(Indexer.CATEGORY);
			} else if (category2 == null && !category1.equals(d.get(Indexer.CATEGORY))) {
				category2 = d.get(Indexer.CATEGORY);
				break;
			}
		}
		
		for(int i = 0; i < hits.length; i++) {

			docID = hits[i].doc;
			d = searcher.doc(docID);
			name = d.get(Indexer.NAME);
			category = d.get(Indexer.CATEGORY);
			if(seenDocs.contains(name)) {
				//do nothing
			} else if(category.equals(category1) || category.equals(category2)){
				searchResults.add(d);
				seenDocs.add(name);
			}
		}
		
		return searchResults;
	}


	/**
	 * Search for similar drugs.
	 * Only outputs drugs that match the category of the drug you are searching for.
	 * 
	 * @param queryStr
	 * @throws IOException
	 */
	public LinkedList<Document> similarDrugQuery(String queryDrug) throws IOException {

		//Find drug to search for similar results for
		Document doc = getDocument(queryDrug);
		
		if(doc == null) {
			return null;
		}

		//Convert usage of drug to boost query where synonyms are not added
		String drugUsage = doc.get(Indexer.USAGE);
		ArrayList<BoostQuery> boostQuery = getBoostedQueries(drugUsage,false);

		//Used to hold all queries and boosted queries
		Builder bQuery = new BooleanQuery.Builder();

		if(!boostQuery.isEmpty()) {
			for(BoostQuery b: boostQuery) {
				bQuery.add(b, Occur.SHOULD);
			}
		}

		BooleanQuery bq = bQuery.build();
		TopDocs topDocs = searcher.search(bq, MAX_COUNT);
		ScoreDoc[] hits = topDocs.scoreDocs;
		LinkedList<Document> searchResults = new LinkedList<>();
		
		HashSet<String> seenDocs = new HashSet<>();
		seenDocs.add(doc.get(Indexer.NAME));
				
		int docID;
		String tmpDrugName,tmpDrugCategory,drugCategory = doc.get(Indexer.CATEGORY);
		Document d;
		for(int i = 0; i < hits.length; i++) {
			docID = hits[i].doc;
			d = searcher.doc(docID);
			tmpDrugName = d.get(Indexer.NAME);
			tmpDrugCategory = d.get(Indexer.CATEGORY);
			if(seenDocs.contains(tmpDrugName) || !drugCategory.equals(tmpDrugCategory)) {
				//do nothing
			} else {
				searchResults.add(d);
				seenDocs.add(tmpDrugName);
			}
		}
		return searchResults;
	}
	
	
	public Document getDocument(String queryDrug) throws IOException {
		Document doc = null;
		queryDrug = queryDrug.toLowerCase().trim();
		String docName;
		for(int i = 0; i < indexReader.maxDoc(); i++) {
			doc = indexReader.document(i);
			docName = doc.get(Indexer.NAME).toLowerCase().trim();
			if(queryDrug.equals(docName)) {
				return doc;
			}
		}
		return null;
	}
	
	private void storeAllDrugs() throws IOException {
		Document doc = null;
		String name;
		HashSet<String> allDocNames = new HashSet<>();
		for(int i = 0; i < indexReader.maxDoc(); i++) {
			doc = indexReader.document(i);
			name = doc.get(Indexer.NAME);
			if(!allDocNames.contains(name)) {
				allDrugs.add(doc);
				allDocNames.add(name);
			}
		}
	}
	
	public LinkedList<Document> getAllDrugs(){
		return Searcher.allDrugs;
	}


	/**
	 * Cycles through query removing stop words and removes punctuation.
	 * Applies boost effect to popular medical terms to enhance searching.
	 * Boolean value indicates if search will add synonyms.
	 * 
	 * @param str
	 * @param builder
	 * @return
	 */
	public ArrayList<BoostQuery> getBoostedQueries(String str, boolean addSynonyms){

		ArrayList<BoostQuery> bs = new ArrayList<>();
		String[] strTerms = str.split(" ");
		String[] synonymArray;
		TermQuery t1;
		BoostQuery q;
		String term, termCombo;

		for(int i = 0; i < strTerms.length; i++) {

			term = strTerms[i] = formatStringTerm(strTerms[i]);
			if(term.isEmpty() || stopWordSet.containsStopWord(term)) {
				continue;
			}

			t1 = new TermQuery(new Term(Indexer.USAGE,term));
			if(medTermSet.contains(term)) {
				q = new BoostQuery(t1, boostUp);
			} else {
				q = new BoostQuery(t1, boostNormal);
			}

			bs.add(q);

			//Handles bi words that occur next to each other
			if(i < strTerms.length && i > 0) {
				if(!strTerms[i-1].isEmpty()) {
					if(medTermSet.contains(term) && medTermSet.contains(strTerms[i-1])) {
						termCombo = strTerms[i-1] + " " + term;
						t1 = new TermQuery(new Term(Indexer.USAGE,termCombo));
						q = new BoostQuery(t1, boostUp);
						bs.add(q);
					}
				}
			}

			if(addSynonyms && sMap.containsWord(term)) {
				synonymArray = sMap.getSynonyms(term).split(" ");
				for(int j = 0; j < synonymArray.length; j++) {
					synonymArray[j] = formatStringTerm(synonymArray[j]);
					if(!synonymArray[j].isEmpty()) {
						t1 = new TermQuery(new Term(Indexer.USAGE,synonymArray[j]));
						q = new BoostQuery(t1, boostNormal);
						bs.add(q);
					}
				}
			}

		}
		return bs;
	}

	private String formatStringTerm(String str) {

		if(str == null || str.isEmpty()) {
			return "";
		}

		return str.toLowerCase().trim().replaceAll("[.,!?]", "");
	}

}
