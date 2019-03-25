

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * A class that builds a reversed index using lucene.
 * Fairly straightforward class.
 * 
 * @author Nathan Roehl CS744 HW2
 *
 */

public class Indexer {

	public String indexDirectoryPath;
	public String inputFilePath;
	
	public static final String CONTENTS = "contents";
	public static final String FILE_NAME = "filename";
	public static final String FILE_PATH = "filepath";
	
	public void setIndexDirPath(String path) {
		this.indexDirectoryPath = path;
	}
	
	public void setInputFilePath(String path) {
		 this.inputFilePath = path;
	}
	
	public String getIndexDirPath() {
		return this.indexDirectoryPath;
	}
	
	public String getInputFilePath() {
		return this.inputFilePath;
	}
	
	public void createIndex() throws IOException {

		File files = new File(inputFilePath);

		File[] fileListings = files.listFiles();

		if(fileListings != null) {

			Arrays.sort(fileListings, new Comparator<File>() {
				
				public int compare(File o1, File o2) {
					String a = o1.getName().substring(0, o1.getName().indexOf(".txt"));
					String b = o2.getName().substring(0, o2.getName().indexOf(".txt"));
					return Integer.valueOf(a).compareTo(Integer.valueOf(b));
				}
			});

			Directory dir = FSDirectory.open(Paths.get(indexDirectoryPath));

			Analyzer analyzer = StopWordList.init();
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			IndexWriter writer = new IndexWriter(dir,config);

			Document doc;
			Field fileNameField;
			Field filePathField;
			Field contentField;

			for(File f: fileListings) {

				doc = new Document();

				contentField = new TextField(CONTENTS, new FileReader(f));
				fileNameField = new StoredField(FILE_NAME, f.getName());
				filePathField = new StoredField(FILE_PATH, f.getCanonicalPath());

				doc.add(contentField);
				doc.add(fileNameField);
				doc.add(filePathField);

				writer.addDocument(doc);
			}

			writer.close();

		} else {
			System.out.println("File path is invalid.");
		}
	}
	
}
