package DrugParsing;

import java.util.LinkedList;

/**
 * Used to store individual drugs as objects.
 * 
 * @author Nathan Roehl and Nisreen Abdel Karim Ahmad Al Khun
 */
public class DrugDoc {

	private String name;
	private String category;
	private boolean isPrescription;
	private boolean isOpiod = false;
	private String description = "";
	private String url = "";
	private LinkedList<String> usage = new LinkedList<>();
	private LinkedList<String> warnings = new LinkedList<>();
	private LinkedList<String> directions = new LinkedList<>();

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public boolean isPrescription() {
		return isPrescription;
	}
	public void setPrescription(boolean isPrescription) {
		this.isPrescription = isPrescription;
	}
	public String getPrescription() {
		return isPrescription ? "Y":"N";
	}
	public String getUsage() {
		String val = "";
//		for(String str: usage) {
//			val = val + str + "\n";
//		}
		for(String str: usage) {
			val = val + " " + str;
		}
		return val;
	}
	public void setUsage(String use) {
		usage.add(use);
	}
	public String getWarnings() {
		String val = "";
		for(String str: warnings) {
			val = val + str + "\n";
		}
		return val;
	}
	public void setWarnings(String warn) {
		warnings.add(warn);
		if(warn.toLowerCase().contains("opioid")) {
			isOpiod = true;
		}
	}
	
	public String getIsOpioid() {
		return isOpiod ? "Y" : "N";
	}
	public String getDirections() {
		String val = "";
		for(String str: directions) {
			val = val + str + "\n";
		}
		return val;
	}
	public void setDirections(String dir) {
		directions.add(dir);
	}
	
	public void setDescription(String d) {
		this.description = d;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public void setURL(String u) {
		this.url = u;
	}
	
	public String getURL() {
		return this.url;
	}

	public String toString() {
		return "Name: " + name
				+"\nCategory: " + category
				+"\nPrescription: " + (isPrescription ? "Yes" : "No")
				+"\nUsage: " + (usage.isEmpty() ? "empty" : usage)
				+"\nWarnings: " + (warnings.isEmpty() ? "empty" : warnings)
				+"\nDirections: " + (directions.isEmpty() ? "empty" : directions);
	}

}
