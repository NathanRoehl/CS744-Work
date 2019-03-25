package hw3_Weka;

/**
 * Simple class to mimic a Tuple.
 * Pairs a term with a score when copmuting MI.
 * 
 * @author		Nathan Roehl
 * @topic 		CS744 - Fall 2018
 * @assignment	HW3-PartB
 *
 */

public class Tuple {
	
	private String term;
	private double score;
	
	public Tuple(String t, double s) {
		term = t;
		score = s;
	}
	
	public String getTerm() {
		return term;
	}
	
	public double getScore() {
		return score;
	}

}
