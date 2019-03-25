package LuceneIndexer;
import java.io.Serializable;
import java.util.TreeMap;

/**
 * Represents a collection of synonyms.
 * 
 * @author Nathan Roehl and Nisreen Abdel Karim Ahmad Al Khun
 */
public class CustomSynonymMap implements Serializable {

	private static final long serialVersionUID = 1L;
	private TreeMap<String,String> synonyms = new TreeMap<>();
	
	public void add(String k, String v) {
		if(synonyms.containsKey(k)) {
			v = synonyms.get(k) + ", " + v;
		}
		v = v.replaceAll("[.:]", "").toLowerCase().trim();
		synonyms.put(k, v);
	}
	
	public String getSynonyms(String word) {
		word = word.replaceAll("[,.]", "").toLowerCase().trim();
		if(synonyms.containsKey(word)) {
			return synonyms.get(word);
		}
		return "";
	}
	
	public boolean containsWord(String word) {
		return synonyms.containsKey(word);
	}
	
}
