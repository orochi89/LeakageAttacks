import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.crypto.NoSuchPaddingException;



public class Binary {
	static HashMap<String, Long> keywordVolumeMap = new HashMap<String, Long>();
	static List<String> left = new ArrayList<String>();
	static List<String> right = new ArrayList<String>();

	
	
	public static void attack(String folderName, int stopWords, int frequency, int numberOfKeywords,
			int numberOfQueries, int adjust, int removeNumbers) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeySpecException, IOException{
		
		
		Evaluator.loadDataset(folderName, stopWords, frequency, numberOfKeywords, adjust, removeNumbers);
		keywordVolumeMap = new HashMap<String, Long>(TextExtractPar.lp3);
		

		
		//query selection (tentative example)
		Set<Long> volumeDiffs = new HashSet<Long>();
		String queryOfInterest = "";
		long volQueryOfInterest = 0;
		for (String keyword1: Evaluator.keywordsFilterTotal){
			volumeDiffs = new HashSet<Long>();
			boolean flag = true;
			for (String keyword2: Evaluator.keywordsFilterTotal){
				long diff = Evaluator.totaLP3.get(keyword1) - Evaluator.totaLP3.get(keyword2);
				if (!keyword1.equals(keyword2)){
					volumeDiffs.add(Math.abs(diff));
					if (diff == 0){	
						flag =false;
						break;
					}
				}


			}
			if (flag  == true){
				queryOfInterest = keyword1;
				volQueryOfInterest = Evaluator.totaLP3.get(keyword1) ;
				break;
			}

		}
		
		if (volumeDiffs.size() == 0){
			System.out.println("No possible query");
			return;
		}
		else{
			System.out.println("*Query: "+queryOfInterest);
			System.out.println("*Volume of Query: "+volQueryOfInterest);
			System.out.println("*Selectivity: "+Evaluator.totaLP1.get(queryOfInterest).size());
			System.out.println("*Size of Volume Diffs: "+volumeDiffs.size());
			System.out.println("*Number of keywords: "+Evaluator.keywordsFilterTotal.size());
		}
		
		
		
		// determining delta for the Coding attack
		System.out.println("\n*Delta computation and Recovery");
		
		long sumOfDeltas = 0;
		
		devide(Evaluator.keywordsFilterTotal);
		List<String> toDevide = new ArrayList<String>();
		while(true){
			long delta= findDelta(volumeDiffs);
			sumOfDeltas = delta+ sumOfDeltas;
			updateSelectivities(delta,right);	
			
			//pin-point
			for (String keyword: Evaluator.keywordsFilterTotal){
				if (keywordVolumeMap.get(keyword) - volQueryOfInterest == delta){
					volQueryOfInterest = keywordVolumeMap.get(keyword);
					toDevide = new ArrayList<String>(right);
					break;
				}
				else if  (keywordVolumeMap.get(keyword) - volQueryOfInterest == 0){
					toDevide = new ArrayList<String>(left);
					break;
				}
			}
			
			if (toDevide.size() == 1){
				break;
			}
			devide(toDevide);				
		}		
		System.out.println("\n*Total Injection: "+sumOfDeltas);	
	}
	
	private static long findDelta(Set<Long> volumeDiffs){
		long delta = 0;
		for (String keyword : right){
			delta = keyword.length() +delta +1;
		}
		
		boolean flag = true;
		//minimum size of the file to inject should be equal to the length of half keyword space size
		while (flag){
			Iterator<Long> itr = volumeDiffs.iterator();
			long tmp =delta;
			while(itr.hasNext()){
				long diff = itr.next();
				if ((diff - delta) == 0){
					delta++;
					break;
				}
			}
			if (tmp == delta){
				break;
			}
		}
		
		System.out.println("\nDelta to add "+delta);
		
		return delta;
	}
	
	
	private static void devide(List<String> toDevide){
		int half = toDevide.size() /2;
		left = new ArrayList<String>();
		right = new ArrayList<String>();

		for (int i = 0; i<half; i++){
			left.add(toDevide.get(i));
		}
		for (int i = half; i<toDevide.size(); i++){
			right.add(toDevide.get(i));
		}	
	}
	
	private static void updateSelectivities(long delta, List<String> keywordList){
		for(String keyword : keywordList){
			long value= keywordVolumeMap.get(keyword)+delta;
			keywordVolumeMap.put(keyword, value);
		}
		return;
	}




}

