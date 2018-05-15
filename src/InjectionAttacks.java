import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class InjectionAttacks {
	

	
	public static void main(String[] args) throws Exception {
		
		
		/*
		 * 
		 * Attacks Properties
		 * 
		 * 
		 */

		// Folder name
		String folderName = "TEST-USER-MEDIUM";

		// Selection of the stopwords strategies: 0 for Lucene default list and
		// 1 for SnowBall list
		Integer stopWords = 1;		

		// Removing numerical keywords (and 1-letter keywords): 1 to maintain and 0 to remove
		Integer removeNumbers = 1;
		
		// Number of Keywords (keywords space from which the queries are
		// sampled)
		Integer numberOfKeywords = 500;
		
		// Display adjustment: 1 to adjust query space to be equal to numberOfKeywords (this is required for less
		//frequent keywords as there are many that have the same selectivity equal to 1)
		Integer adjust = 1;

		// Frequency: determine whether the keyword space is populated from the
		// most or less frequent keywords: 0 for most frequent
		// and 1 for less frequent
		Integer frequency = 0;		
		
		// Number of Queries
		Integer numberOfQueries = 150;
		

		System.out.println("\n=====================================\n" + 
		"==== Coding Attack=====\n" + 
				"=====================================\n");
		Coding.attack(folderName, stopWords, frequency, numberOfKeywords, numberOfQueries, adjust, removeNumbers);
		
		System.out.println("\n=====================================\n" + 
		"==== Binary Attack=====\n" + 
				"=====================================\n");
		
		Binary.attack(folderName, stopWords, frequency, numberOfKeywords, numberOfQueries, adjust, removeNumbers);
	}
	


}
