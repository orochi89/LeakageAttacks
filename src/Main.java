
public class Main {

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
		
		// Removing numerical keywords (and 1-letter keywords): 0 to maintain and 1 to remove
		Integer removeNumbers = 0;

		// Number of Queries
		Integer numberOfQueries = 150;

		// Number of Keywords (keywords space from which the queries are
		// sampled)
		Integer numberOfKeywords = 500;
		
		// Display adjustment: 1 to adjust query space to be equal to numberOfKeywords (this is required for less
		//frequent keywords as there are many that have the same selectivity equal to 1)
		Integer adjust = 0;

		// Frequency: determine whether the keyword space is populated from the
		// most or less frequent keywords: 0 for most frequent
		// and 1 for less frequent
		Integer frequency = 0;

		// In/Out: determine whether the queries are drawn from the keywords in
		// the entire user's dataset
		// or from the adversary's known dataset: 0 for partial and 1 for entire
		Integer inOut = 1;

		// Percentage of server knowledge
		Integer serverKnowledge = 90;

		// Run the Boolean Attack: 0 for no and 1 for yes
		Integer bool = 0;

		// Number of terms in the disjunction/conjunction
		Integer sizeBool = 2;

		// Number of times the attacks are executed
		Integer numberOfExecutions = 5;

		// Display keywords distribution: 1 to display
		Integer keywordDistribution = 0;


		// Running the attacks
		Evaluator.loadDataset(folderName, stopWords, frequency, numberOfKeywords, adjust,removeNumbers);
		Evaluator.evaluator(folderName, stopWords, numberOfQueries, numberOfKeywords, frequency, inOut, serverKnowledge,
				bool, sizeBool, numberOfExecutions, keywordDistribution, adjust,removeNumbers);
	}

}
