package DrugParsing;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Opens each drug as text file, stores each drug as a drug object, and returns the list of drug objects.
 * 
 * @author Nathan Roehl and Nisreen Abdel Karim Ahmad Al Khun
 */
public class DocumentParser {

	public static List<DrugDoc> getContent() {
		
		File directory = new File("C:\\Users\\natha\\Desktop\\CS744\\Group Project\\Drugs");
		File[] allFiles = directory.listFiles();
		LinkedList <DrugDoc> docList = new LinkedList<>();
		String line;
		DrugDoc doc;
		for(File f: allFiles) {
			
			doc = new DrugDoc();
			docList.add(doc);
			
			try(Scanner scr = new Scanner(f)){
				
				while(scr.hasNextLine()) {
					
					line = scr.nextLine();
					if(line.contains("<NAME>")) {
						doc.setName(scr.nextLine().trim());
					}
					if(line.contains("<CATEGORY>")) {
						line = scr.nextLine().toLowerCase().trim();
						line = Character.toUpperCase(line.charAt(0)) + line.substring(1);
						doc.setCategory(line);
					}
					if(line.contains("<DESCRIPTION>")) {
						line = punctuateLine(scr.nextLine().trim());
						doc.setDescription(line);
					}
					if(line.contains("<URL>")) {
						doc.setURL(scr.nextLine().trim());
					}
					if(line.contains("<PRESCRIPTION>")) {
						line = scr.nextLine().trim();
						if(line.toLowerCase().equals("y")) {
							doc.setPrescription(true);
						} else {
							doc.setPrescription(false);
						}
					}
					if(line.contains("<USAGE>")) {
						line = scr.nextLine().trim();
						while(!line.contains("</USAGE>")) {
							doc.setUsage(line);
							line = scr.nextLine().trim();
						}
					}
					if(line.contains("<WARNINGS>")) {
						line = scr.nextLine().trim();
						while(!line.contains("</WARNINGS>")) {
							doc.setWarnings(line);
							line = scr.nextLine().trim();
						}
					}
					if(line.contains("<DIRECTIONS>")) {
						line = scr.nextLine().trim();
						while(!line.contains("</DIRECTIONS>")) {
							doc.setDirections(line);
							line = scr.nextLine().trim();
						}
					}
					
					
				}
				
			} catch(FileNotFoundException e) {
				
			} catch(NoSuchElementException e) {
				System.out.println(doc.getName());
				e.printStackTrace();
			}
			
		}
		
//		try {
//			FileOutputStream f = new FileOutputStream(new File("DrugDocs.txt"));
//			ObjectOutputStream o = new ObjectOutputStream(f);
//
//			// Write objects to file
//			o.writeObject(docList);
//			o.close();
//
//		} catch (FileNotFoundException e) {
//			System.out.println("File not found");
//		} catch (IOException e) {
//			System.out.println("Error initializing stream");
//		}
		
		return docList;
		
	}
	
	public static String punctuateLine(String line) {
		if(line.charAt(line.length()-1) != '.') {
			line += ".";
		}
		return Character.toUpperCase(line.charAt(0)) + line.substring(1);
	}
	
}
