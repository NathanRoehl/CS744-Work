package hw1_ReversedIndex;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A class to represent a term, which is basically a word.
 * A Term stores all documents it occurs in.
 * A Term uses HashMaps to map a document to the location in the document, a document to term frequency, and a document to tfidf value.
 * 
 * @author Nathan Roehl - HW1 - CS744
 *
 */

public class Term {

	//Name of Term
	private String name;
	//Overall times term is used
	private int totalCount = 0;

	//String is Document Term belongs to
	//Arraylist stores location of Term in Document
	private HashMap <Document, ArrayList<Integer>> locations = new HashMap<Document, ArrayList<Integer>>();
	private HashMap <Document, Double> tfValueDocument = new HashMap<>();
	private HashMap <Document, Double> tfIDFValueDocument = new HashMap<>();

	private ArrayList<Document> documentList = new ArrayList<>();


	public Term(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public int getTotalCount() {
		return this.totalCount;
	}

	public int getTotalDocumentsPerTerm() {
		return documentList.size();
	}

	/**
	 * Duplicate documents not allowed.
	 * 
	 * @param document
	 */
	public void addDocument(Document document) {
		for(Document d: documentList) {
			if(d.equals(document)) {
				return;
			}
		}
		documentList.add(document);
	}


	public ArrayList<Document> getDocumentsForTerm(){

		return documentList;

	}

	/**
	 * Adds location of term to a document.
	 * If first term added, will initialize ArrayList to add location mapped to document
	 * 
	 * @param document
	 * @param index
	 */
	public void addLocation(Document document, int index) {

		totalCount++;
		ArrayList<Integer> tmp = locations.get(document);
		if(tmp == null) {
			tmp = new ArrayList<>();
		}
		tmp.add(index);
		locations.put(document, tmp);

	}

	public void addTermFrequency(Document document, double tfValue) {
		tfValueDocument.put(document, tfValue);
	}

	public double getTermFrequency(Document document) {
		return tfValueDocument.get(document);
	}

	public void addTermFrequencyIDF(Document document, double tfIDFValue) {
		tfIDFValueDocument.put(document, tfIDFValue);
	}

	public Double getTermFrequencyIDF(Document document) {
		return tfIDFValueDocument.get(document);
	}

	public ArrayList<Integer> getLocationsForDocument(Document document){

		return locations.get(document);

	}

	/**
	 * Sorts locations of term mapped to document if they are out of order for some reason.
	 * 
	 * @param document
	 */
	public void sortList(Document document) {
		ArrayList<Integer> tmp = locations.get(document);
		
		for(int i = 1; i < tmp.size(); i++) {
			int j = i;
			while(j > 0 && tmp.get(j-1) > tmp.get(j)) {
				int x = tmp.get(j-1);
				int y = tmp.get(j);
				tmp.set(j-1, y);
				tmp.set(j, x);
				j--;
			}
		}
		locations.put(document, tmp);
	}

	/**
	 * Sort document list using insertion sort.
	 * For some reason sorting many documents can lead to a few out of order elements.
	 * If there were 10 documents, it would sort 1, 10, 2, 3, 4, 5, 6, 7 , 8, 9
	 * Tables.compareStrings is helper method to hopefully avoid this.
	 * 
	 */
	public void sortDocuments() {
		for(int i = 1; i < documentList.size(); i++) {
			int j = i;

			while(j > 0 && Tables.compareStrings(documentList.get(j-1).getName(), documentList.get(j).getName())){

				Document x = documentList.get(j-1);
				Document y = documentList.get(j);
				documentList.set(j-1, y);
				documentList.set(j, x);

				j--;
			}
		}	
	}

	@Override
	public boolean equals(Object other) {
		if(other instanceof Term) {
			Term tmp = (Term) other;
			return this.name.equals(tmp.name);
		}
		return false;
	}

	/**
	 * Quicker way to tell if a string is the same as the term.
	 * 
	 * @param str
	 * @return
	 */
	public boolean equals(String str) {
		if(str == null)
			return false;
		return str.equals(name);
	}

	@Override
	public String toString() {
		return this.name + " , total count: " + this.totalCount +  locations;
	}

}

/**
 * Class meant to mimic a tuple(Document, value) for sorting TFIDF values.
 * 
 * @author Nathan Roehl
 *
 */
class DocumentTFIDF{
	Document d;
	double value;

	public DocumentTFIDF(Document d, double tfidf) {
		this.d = d;
		this.value = tfidf;
	}

	public double getValue() {
		return this.value;
	}

	public Document getDocument() {
		return this.d;
	}
}

/**
 * Class meant to mimic tuple(Document, count) for BiQuery search.
 * 
 * @author Nathan Roehl
 *
 */
class DocumentBiqueryCount{
	Document d;
	int count;

	public DocumentBiqueryCount(Document d, int count) {
		this.d = d;
		this.count = count;
	}

	public void incCount() {
		this.count++;
	}

	public Document getDoc() {
		return this.d;
	}

	public int getCount() {
		return this.count;
	}
}


