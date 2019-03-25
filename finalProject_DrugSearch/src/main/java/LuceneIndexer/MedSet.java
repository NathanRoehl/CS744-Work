package LuceneIndexer;
import java.util.HashSet;

public class MedSet {
	
	/**
	 * A class used to contain medical terms that may be useful when searching a query.
	 * Medical terms found can be "boosted" in lucene to improve the search results of that term.
	 * Using a hashset, provides immediate look up and doesn't allow duplicates.
	 * The no duplicates makes it easier for me, I just started thinking of terms I could add, so duplicates appear in the String array,
	 * but on in the hashset for searching.
	 * 
	 */

	private static String[] medicalTerms = { "benign", "malignant", "anti-inflammatory", "bmi", "body", "mass", "index", "biopsy", "hypotension", "hypertension",
			"lesion", "noninvasive", "outpatient","inpatient","remission", "membrane", "acute", "angina", "gastroesophageal", "disease",
			"cellulitis","epidermis","neutrophils","edema","emoblism","sutures","polyp","fracture", "cranial", "digiatl", "femoral",
			"gluteal", "inguinal","lumber","mammary","nasal","ventral", "abrasion", "aruption", "ambulatory", "cavity", "exacerbation", 
			"hematemesis", "reflux", "remission", "stimulus", "myocardial", "infraction", "ami","leukemia","cancer",
			 "ibs", "irritable", "bowl", "syndrome", "diabetes", "mds", "cell", "pe", "pulmonary", "embolism", "insulin",
			"sids","death", "acl", "failure", "adhd","adr","reaction", "drug", "urine", "adverse", "blood", "knee", 
			"cardiovascular", "disease", "coronar", "artery", "diagnosis", "dnr", "rususcitate", "thrombosis", "emergency", "hemoglobin",
			"examination", "headache", "hormone", "bowel", "inflammatory", "intensive", "care", "fibrosis", "joint", "potassium",
			"carcinoma", "lobular", "electrolytes", "milligrams", "milliters", "liters", "grams", "gram", "liter", "nausea", "vomit", "sodium",
			"nursing", "sinus", "sinuses", "parasite", "parasites", "eye", "eyeball", "internal", "pulse", "ligament", "pneumonia", "pupils", 
			"therapy", "physical", "thyroid", "ulcer", "peptic", "rheumatoid", "arthritis", "respiratory", "breath", "shortness", "temp",
			"temperature", "tonsils", "tonsillectomy", "abdominal", "hip", "urinary", "infection", "sinusitis", "common", "cold", "pressure",
			"vitals", "vital", "weight", "xray", "x-ray", "radiation", "migraine", "gluten", "anxiety","depression","knots","knot", "mental",
			"health", "abnormal", "dementia", "serotonin", "anorexia", "nervosa", "stem", "cells", "cell", "vision", "peripheral", "clinical",
			"trials", "trial","anorexia", "diagnosed","diagnose","genetic", "genetics", "disorders" , "disorder" ,"eating", "anorexic"
			,"deficiency", "pseudocholinesterase", "syndrome", "EDS", "test","ancestral","ancestry","heritage","inherit","inherited", "advanced",
			"treatments","treatment","cholesterol","gene","congenital","diaphragmatic","hernia","marriage","degenerate","degeneration",
			"cerebellar","lynch","retinoblastoma","studies","parkinson","parkinson's","study","anesthesia","record","records","herpes","immunity",
			"dosage","achondroplasia","bladder","tumor","BCG","cobalt","chormium","urine","feces","fecal","contagious","stem","gum","teeth",
			"glucosamine","groin","hip","bones","tension","sleep","clench","alzheimer","alzheimer's","mri","masturbation","tsd","congenital",
			"pheochromocytomas","government","doctor","lungs","lung","heart","hands","hand","cure","curable","abcess","cervical","lipids","damage",
			"thalassemia","neck","spine","jaundice","fragile","flatulence","astham","symptoms","physiotherapy","ankle","arthritis","urgent","brain",
			"ribs","breast","rib","stomach","tummy","suicide","assisted","lice","swollen","angelman","pancreas","pancreatitis","fluid","obese",
			"health","healthy","exercise","ferritin","penis","vagina","plantar","weight","hospital","specialist","vessel","inflame","throat","sick"
			,"eye","eyes","eyeball","finger","thumb","ring","dark","puss", "pressure","esophogus", "heart","pulse","foot","feet","bath","salt","iron",
			"cough","suppressant", "acl","surgery","nerve","blind","blindness","color","wart","mole","allergic","allergies","fat","obese","obesity",
			"anxiety","anxious","fear","bulemic","vomit","fever","puke","barf","gray","pale","green","yellow", "sugar","cramp", "flu","shot",
			"ill","tired","sleepy","sleep","sleeplessness","haze","daze","smell","rotten","pulmonary","oil","pills","antibiotics","antihistamine"};

	private HashSet <String> hset;

	public MedSet() {
		
		hset = new HashSet<String>();
		
		for(String str: medicalTerms) {
			hset.add(str);
		}
		
	}
	
	public boolean contains(String str) {
		return hset.contains(str);
	}

}
