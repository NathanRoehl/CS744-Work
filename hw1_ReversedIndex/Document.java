package hw1_ReversedIndex;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


/**
 * 
 * A simple class that stores a document ID and the document's actual name associated with the ID.
 * Each document also stores all terms inside itself.
 * 
 * @author Nathan Roehl - CS744 - HW1
 *
 */
public class Document {

	private String name;
	private String actualName;
	private int totalWordCount;
	private ArrayList<Term> listOfTerms = new ArrayList<Term>();

	public Document(String name, String actualName) {
		this.name = name;
		this.actualName = actualName;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getActualDocumentName() {
		return this.actualName;
	}
	
	public int getTotalWords() {
		return this.totalWordCount;
	}
	
	public int getTotalTerms() {
		return listOfTerms.size();
	}
	
	public void setTotalWords(int count) {
		this.totalWordCount = count;
	}

	public boolean addTerm(Term term) {
		for(Term t: listOfTerms) {
			if(t.equals(term)) {
				return false;
			}
		}
		listOfTerms.add(term);
		return true;
	}

	
	/**
	 * Sorts the terms of the document lexicographically using java provided sorting algorithm.
	 */
	public void sortTerms() {
		Collections.sort(listOfTerms, new sortByTerm());
	}
	

	public boolean equals(Object other) {
		if(other instanceof Document) {
			Document tmp = (Document) other;

			return name.equals(tmp.name) && listOfTerms.size() == tmp.listOfTerms.size();

		}
		return false;
	}
	
	@Override
	public String toString() {
		return this.name + " , total count: " + this.listOfTerms.size();
	}

}

class sortByTerm implements Comparator<Term>{

	@Override
	public int compare(Term t1, Term t2) {
		return t1.getName().compareTo(t2.getName());
	}
}
