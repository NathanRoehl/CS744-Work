package LuceneIndexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Creates and stores a synonym map based a document found online.
 * Document contained thousands of synonyms.
 * 
 * @author Nathan Roehl and Nisreen Abdel Karim Ahmad Al Khun
 */
public class CreateSynonymMap {

	public static void main(String[] args) {

		CustomSynonymMap sm = new CustomSynonymMap();
		HashMap<String,List<String>> hm = new HashMap<>();
		List<String> tmpList;
		
		try(Scanner scr = new Scanner(new File("C:\\Users\\natha\\Desktop\\CS744\\Group Project\\SynonymList.txt"))){
			String line,word,synonyms;
			int wordIndex,synonymIndex,antIndex, equalsIndex;
			while(scr.hasNextLine()) {
				line = scr.nextLine();
				wordIndex = line.indexOf("KEY:");
				if(wordIndex != -1) {
					String[] split = line.split(" ");
					word = split[1].replaceAll("[.:]", "").toLowerCase().trim();
					if(word.isEmpty()) {
						continue;
					}
					if(line.contains("[See")) {
						line = line.substring(line.indexOf("[See") + 4);
						line = line.replace("]", "");
						split = line.split(" ");
						for(int i = 0; i < split.length; i++) {
							split[i] = split[i].replaceAll("[.:]", "").toLowerCase().trim();
							if(split[i].isEmpty()) {
								continue;
							} else if (split[i].equals("and")) {
								continue;
							} else {
								if(hm.containsKey(word)) {
									tmpList = hm.get(word);
									tmpList.add(split[i]);
									hm.put(word, tmpList);
								} else {
									tmpList = new LinkedList<>();
									tmpList.add(split[i]);
									hm.put(word, tmpList);
								}
							}
						}
					}
					line = scr.nextLine();
					synonymIndex = line.indexOf("SYN: ");
					if(synonymIndex != -1) {
						synonyms = line.substring(synonymIndex + 4);
						line = scr.nextLine();
						antIndex = line.indexOf("ANT: ");
						equalsIndex = line.indexOf("=");
						while(antIndex == -1 && equalsIndex == -1) {
							synonyms = synonyms + " " + line;
							line = scr.nextLine();
							antIndex = line.indexOf("ANT: ");
							equalsIndex = line.indexOf("=");
						}
						sm.add(word, synonyms);
					}
				}
			}

		} catch (FileNotFoundException e) {
			System.out.println("Error with file.");
		}
		
		List<String> list;
		String currentStr;
		for(String str: hm.keySet()) {
			list = hm.get(str);
			currentStr = sm.getSynonyms(str);
			for(String s: list) {
				currentStr = currentStr + " " + sm.getSynonyms(s);
			}
			sm.add(str, currentStr);
		}
		
		try {
			FileOutputStream f = new FileOutputStream(new File("CustomSynonymMap.txt"));
			ObjectOutputStream o = new ObjectOutputStream(f);

			// Write objects to file
			o.writeObject(sm);
			o.close();

		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			System.out.println("Error initializing stream");
		}
	
	}
	

}
