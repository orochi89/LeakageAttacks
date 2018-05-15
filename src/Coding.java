import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.crypto.NoSuchPaddingException;



public class Coding {
	static HashMap<String, Long> keywordVolumeMap = new HashMap<String, Long>();

	
	
	public static void attack(String folderName, int stopWords, int frequency, int numberOfKeywords,
			int numberOfQueries, int adjust, int removeNumbers) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeySpecException, IOException{
		Evaluator.loadDataset(folderName, stopWords, frequency, numberOfKeywords, adjust, removeNumbers);
		keywordVolumeMap = new HashMap<String, Long>(TextExtractPar.lp3);
		// determining delta for the Coding attack
		System.out.println("\n*Delta computation");

		Set<Long> volumeDiffs = new HashSet<Long>();
		for (String keyword1: Evaluator.keywordsFilterTotal){
			for (String keyword2 : Evaluator.keywordsFilterTotal ){
				long diff = Evaluator.totaLP3.get(keyword1) - Evaluator.totaLP3.get(keyword2);
				if (diff != 0){ volumeDiffs.add(Math.abs(diff));}
			}
		}
		
		
		System.out.println("\n*Size of Volume Diffs "+volumeDiffs.size());
		System.out.println("\n*Number of keywords "+Evaluator.keywordsFilterTotal.size());


		boolean flag = true;
		long delta  = 3;
		while (flag){
			Iterator<Long> itr = volumeDiffs.iterator();
			long tmp =delta;
			while(itr.hasNext()){
				long diff = itr.next();
				if ((diff % delta) == 0){
					delta++;
					break;
				}
			}
			if (tmp == delta){
				break;
			}
		}
		

		
		System.out.println("\n*Delta "+delta);
		
		//verification of delta
		System.out.println("\n*Verification");

		for (Long volume : volumeDiffs){
			if (volume % delta == 0){
				System.out.println("WTF");
			}
		}
		
		
		// update of the volume of keywords
		System.out.println("\n*Volume Updates");
		for (int i = 1; i<=Evaluator.keywordsFilterTotal.size(); i++){
			Set<String> output = truncatedSetofKeywords(i);
			updateSelectivities(delta, output);
		}
		
		//keywords recovery
			
		
		System.out.println("\n*Query generation");

		Evaluator.randomQueriesGeneration(numberOfQueries, Evaluator.keywordsFilterTotal);
		
		System.out.println("\n*Recovery");
		
		int falseMatches = 0;

		for (String keyword :  Evaluator.queryKeywords){
			long newVolume = keywordVolumeMap.get(keyword);
			int count =1;
			while (count < Evaluator.keywordsFilterTotal.size()){
				long tmp = newVolume - count*delta;
				int count2 = 0;
				String match= "";
				for (String keyword2 : Evaluator.keywordsFilterTotal){
					if (tmp == TextExtractPar.lp3.get(keyword2)){
						if (Evaluator.keywordsFilterTotal.get(Evaluator.keywordsFilterTotal.size()-count).equals(keyword2)) {
							match = keyword2;
							count2++;
						}
					}
				}
				
				if (count2 == 1){
					//System.out.println("Query "+keyword+" ; Match "+match);
					break;
				}
				else if (count2 >1){
					System.out.println("Attack failed");
					falseMatches++;
					break;
				}
				count++;
			}
		}
		
		System.out.println("\n*Recovery Rate "+((double) (Evaluator.queryKeywords.size() - falseMatches))/ Evaluator.queryKeywords.size());
		
	}
	
	
	
	private static void updateSelectivities(long delta, Set<String> keywordList){
		for(String keyword : keywordList){
			long value= keywordVolumeMap.get(keyword)+delta;
			keywordVolumeMap.put(keyword, value);
		}
		return;
	}

	private static Set<String> truncatedSetofKeywords(int k){
		Set<String> output =  new HashSet<String>();
		
		for (int i = 0; i <k; i++){
			output.add(Evaluator.keywordsFilterTotal.get(i));
		}
		return output;
	}


}

