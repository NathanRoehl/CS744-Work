package LuceneIndexer;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import DrugParsing.DocumentParser;
import DrugParsing.DrugDoc;

/**
 * Builds a Lucene Index.
 * Calls DocumentParser class which returns a list of Drug objects.
 * If updating lucene index on a different computer will have to update FILES_TO_READ_PATH.
 * 
 * @author Nathan Roehl and Nisreen Abdel Karim Ahmad Al Khun
 */
public class Indexer {

	public static final String INDEX_DIRECTORY_PATH = "LuceneIndex";
	public static final String FILES_TO_READ_PATH = "C:\\Users\\natha\\Desktop\\CS744\\Group Project\\Drugs";
	public static final String NAME = "name";
	public static final String CATEGORY = "category";
	public static final String PRESCRIPTION = "prescription";
	public static final String USAGE = "usage";
	public static final String WARNINGS = "warnings";
	public static final String DIRECTIONS = "directions";
	public static final String URL = "url";
	public static final String DESCRIPTION = "description";
	public static final String OPIOID = "opioid";

	
	public static void main(String[] args){

		try {
			System.out.println("Creating index.");
			createIndex();
			System.out.println("Finished creating index.");
		} catch(IOException e) {
			System.out.println("Error in creating index.");
			System.out.println(e.getMessage());
		}

		System.out.println("Goodbye.");

	}


	public static void createIndex() throws IOException {

		List<DrugDoc> allDrugs = DocumentParser.getContent();
		System.out.println("Drug size: " + allDrugs.size());
		HashSet<String> allCats = new HashSet<>();

		if(!allDrugs.isEmpty()) {

			Directory dir = FSDirectory.open(Paths.get(INDEX_DIRECTORY_PATH));

			Analyzer analyzer = StopWordList.init();
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			IndexWriter writer = new IndexWriter(dir,config);

			Document doc;
			Field drugName, drugCategory, drugPrescription, drugUsage,
			drugWarnings, drugDirections,drugDescription,drugURL,drugOpioid;

			for(DrugParsing.DrugDoc d: allDrugs) {
				
				System.out.println("Indexing: " + d.getName());

				doc = new Document();

				drugName = new TextField(NAME, d.getName(), Field.Store.YES);
				drugCategory = new TextField(CATEGORY, d.getCategory(), Field.Store.YES);
				drugPrescription = new StoredField(PRESCRIPTION,d.getPrescription());
				drugUsage = new TextField(USAGE, d.getUsage(), Field.Store.YES);
				drugWarnings = new StoredField(WARNINGS, d.getWarnings());
				drugDirections = new StoredField(DIRECTIONS, d.getDirections());
				drugDescription = new TextField(DESCRIPTION, d.getDescription(), Field.Store.YES);
				drugURL = new TextField(URL, d.getURL(), Field.Store.YES);
				drugOpioid = new TextField(OPIOID, d.getIsOpioid(), Field.Store.YES);
				
				allCats.add(d.getCategory());
				
				doc.add(drugName);
				doc.add(drugCategory);
				doc.add(drugPrescription);
				doc.add(drugUsage);
				doc.add(drugWarnings);
				doc.add(drugDirections);
				doc.add(drugDescription);
				doc.add(drugURL);
				doc.add(drugOpioid);

				writer.addDocument(doc);
				System.out.println(doc);

			}

			writer.close();

		} else {
			System.out.println("File path is invalid.");
		}
		
		System.out.println(allCats);
	}
	
}
