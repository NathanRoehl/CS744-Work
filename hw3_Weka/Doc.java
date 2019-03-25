package hw3_Weka;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Class to mimic documents read.
 * Initially I stored more information, like places and people, but these have been removed.
 * topics holds a list of all (or none) of the topics of a document
 * hasTopics is true if the document has a topic ("YES") or false ("NO")
 * title is the title of the document
 * modaptsplit holds an Enum of TRAIN,TEST,UNKNOWN
 * body holds each String of document when parsed as a single line.
 * 
 * @author		Nathan Roehl
 * @topic 		CS744 - Fall 2018
 * @assignment	HW3-PartB
 *
 */

public class Doc {

	private HashSet<String> topics = new HashSet<>();
	private boolean hasTopics = false;
	private String title = "";
	private ModAptSplit modaptsplit;
	private ArrayList<String> body = new ArrayList<>();
	
	public HashSet<String> getTopics() {
		return topics;
	}
	public void addTopic(String t) {
		this.topics.add(t);
	}
	public boolean isHasTopics() {
		return hasTopics;
	}
	public void setHasTopics(boolean hasTopics) {
		this.hasTopics = hasTopics;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public ModAptSplit getModaptsplit() {
		return modaptsplit;
	}
	public void setModaptsplit(ModAptSplit modaptsplit) {
		this.modaptsplit = modaptsplit;
	}
	public ArrayList<String> getBody() {
		return body;
	}
	public void addStringToBody(String str) {
		body.add(str);
	}
	
	public String hasTopic(String topic) {
		return topics.contains(topic) ? "Y" : "N";
	}
	
	public String printDoc() {
		return "Title: " + title + "\n" +
				"Has Topic: " + convertHasTopics() + "\n" +
				"ModAptSplit: " + convertModInt() + "\n" +
				"Topic(s): " + topics + "\n";
	}
	
	public String toString() {
		return title;
	}
	
	public String convertHasTopics() {
		return hasTopics ? "Y" : "N";
	}
	
	public String convertModInt() {
		if(modaptsplit == ModAptSplit.TEST) {
			return "TEST";
		}
		if(modaptsplit == ModAptSplit.TRAIN) {
			return "TRAIN";
		}
		return "NOT USED";
	}
	
	public boolean equals(Object other) {
		
		if(other == this)
			return true;
		
		if(other instanceof Doc) {
			Doc tmp = (Doc) other;
			return tmp.title.equals(this.title) && tmp.hasTopics == this.hasTopics && tmp.topics.size() == this.topics.size()
					&& tmp.modaptsplit == this.modaptsplit && tmp.body.size() == this.body.size();
		}
		
		return false;
	}

}
