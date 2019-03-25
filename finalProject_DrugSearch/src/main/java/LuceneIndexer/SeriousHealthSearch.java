package LuceneIndexer;

import java.util.LinkedList;

public class SeriousHealthSearch {
	
	private static String[] verySerious = {"severe","heart","stab","sharp","stabbing"};
	private static String[] serious = {"blood","vomit","confused","confusion","high","fever","rapid",
			"breathing","stool","weight loss","unexplained","breath","shortness","gasp","tar",
			"tarry","black","stool","poop","sudden","aggressive","aggression","behavior",
			"concentrate","vomit","vomiting","bright","spots","flashes","light",
			"bloody","bleed","bleeding","swelling","swollen","unexpected"};


	public static boolean areSeriousHealthIssues(String query) {
		boolean serious = false;
		int count = 0;
		
		if(query.contains("chest pain")) {
			return true;
		}
		
		String[] querySplit = query.split(" ");
		LinkedList<String> reducedQuery = new LinkedList<>();
		for(int i = 0; i < querySplit.length; i++) {
			querySplit[i] = querySplit[i].replaceAll("[,.!?]", "").toLowerCase().trim();
			if(!Searcher.stopWordSet.containsStopWord(querySplit[i])) {
				reducedQuery.add(querySplit[i]);
			}
		}
		for(String qstr: reducedQuery) {
			for(String str: SeriousHealthSearch.verySerious) {
				if(str.equals(qstr)) {
					return true;
				}
			}
		}

		
		for(String qstr: reducedQuery) {
			for(String str: SeriousHealthSearch.serious) {
				if(str.equals(qstr)) {
					count++;
				}
			}
		}

		if(count > reducedQuery.size()/2) {
			serious = true;
		}
		return serious;
	}
	
}
