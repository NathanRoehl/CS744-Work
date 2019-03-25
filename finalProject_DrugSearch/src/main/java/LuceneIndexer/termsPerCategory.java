package LuceneIndexer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import DrugParsing.DocumentParser;
import DrugParsing.DrugDoc;

/**
 * Used to see what common terms were kept after applying stop word list.
 * Gives frequency of terms used per drug category and synonyms added to each word.
 * This class is not used during program execution.
 * It is used for information gain for each individual word.
 * 
 * @author Nathan Roehl and Nisreen Abdel Karim Ahmad Al Khun
 */
public class termsPerCategory {

	public static void main(String[] args) {
		

		CustomSynonymMap sMap = new CustomSynonymMap();
		
		try {

			FileInputStream fi = new FileInputStream(new File("CustomSynonymMap.txt"));
			ObjectInputStream oi = new ObjectInputStream(fi);

			sMap = (CustomSynonymMap)oi.readObject();
			
			oi.close();
			fi.close();

		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			System.out.println("Error initializing stream");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		List<DrugDoc> allDrugs = DocumentParser.getContent();
		System.out.println(allDrugs.size());
		HashMap<String,HashMap<String,Integer>> megaHashMap = new HashMap<>();
		HashSet<String> categories = new HashSet<>();
		HashSet<String> allWords = new HashSet<>();
		StopWordList swl = new StopWordList();
		
		//map Category to (word,count)
		String[] split;
		String category,simpleWord;
		HashMap<String,Integer> counting;
		Integer val;
		
		for (DrugDoc drug: allDrugs) {
			category = drug.getCategory();
			categories.add(category);
			
			if(megaHashMap.containsKey(category)) {
				counting = megaHashMap.get(category);
			} else {
				counting = new HashMap<>();
			}
			
			split = drug.getUsage().split(" ");
			for(int i = 0; i < split.length; i++) {
				split[i] = split[i].replaceAll("[():,.!?]", "").toLowerCase().trim();
				split[i] = split[i].replace("[", "");
				split[i] = split[i].replace("]", "");
				simpleWord = split[i];
				if(simpleWord.isEmpty() || swl.containsStopWord(simpleWord)) {
					continue;
				}
				allWords.add(simpleWord);
				if(counting.containsKey(simpleWord)) {
					val = counting.get(split[i]) + 1;
					counting.put(simpleWord, val);
				} else {
					counting.put(simpleWord, 1);
				}
			}
			megaHashMap.put(category, counting);
			
		}
		
		ArrayList<String> allWordsSorted = new ArrayList<>();
		for(String str: allWords) {
			allWordsSorted.add(str);
		}
		
		Collections.sort(allWordsSorted);
		
//		for(String str: allWordsSorted) {
//			System.out.println(str);
//		}
		
		ArrayList<String> al = new ArrayList<>();
		
		for(String cat: categories) {
			counting = megaHashMap.get(cat);
			for(String word: counting.keySet()) {
				al.add(word);
			}
			Collections.sort(al);
			System.out.println("<<<<<<<<<<" + cat + ">>>>>>>>>>>");
			for(String word: al) {
				System.out.println(word + " : " + counting.get(word) + " " + sMap.getSynonyms(word));
			}
			al = new ArrayList<>();
		}
		
	}

}
