import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class CountAttack {
	
	/**
	 * 
	 * @param iteration
	 * @return
	 */
	
	public static int countOnly(int iteration, int numberOfQueries){
		int countOnlySuccessRateTMP = 0;

		HashMap<Integer, String> attackMap = new HashMap<Integer, String>();
		
		Multimap<Integer, String> tokenVector  = ArrayListMultimap.create();
		int counter1 = 0;
		for (String keyword : Evaluator.queryKeywords){
			tokenVector.putAll(counter1, Evaluator.totaLP1.get(keyword));
			counter1++;
		}
		// determining the tokens with unique counts
		for (int i = 0; i<numberOfQueries; i++){
			int selectivity = tokenVector.get(i).size();
			List<Integer> tempList = new ArrayList<Integer>();
			for (int  h =selectivity; h>=Math.ceil(iteration*selectivity/100);h--){
				if (!Evaluator.selectivityKeywords.get(h).isEmpty()){
					tempList.add(h);
					break;
				}
			}

			if (tempList.size() > 0){
				attackMap.put(i, Evaluator.selectivityKeywords.get(tempList.get(0)).iterator().next());
				tokenVector.removeAll(i);

			}
		}
		
		// Computation success rate of count-only attack
		for (Integer value : attackMap.keySet()){
			if (attackMap.get(value).equals(Evaluator.queryKeywords.get(value))){
				countOnlySuccessRateTMP++;
			}
		}
		return countOnlySuccessRateTMP;
	}
	
	
	/**
	 * 
	 * @param iteration
	 * @param error
	 * @return
	 */
	
	/*
	 * The two attacks below are tentative implementations for the partial knowledge variant of the Count attack by Cash et al. (CCS '15).
	 * They have not been fully tested+completed 
	 */
	
	public static int countOccurence(int iteration, int numberOfQueries, int error, int filter_factor){
		HashMap<Integer, String> attackMap = new HashMap<Integer, String>();
		
		Multimap<Integer, String> windowAttackMap = ArrayListMultimap.create();
		
		Multimap<Integer, String> tokenVector  = ArrayListMultimap.create();
		int counter1 = 0;
		for (String keyword : Evaluator.queryKeywords){
			tokenVector.putAll(counter1, Evaluator.totaLP1.get(keyword));
			counter1++;
		}
		
		
		HashMap<String, Integer> coOccurenceQuery = new HashMap<String,Integer>();
		for (int i =0; i<numberOfQueries; i++){
			for (int j =0;j<numberOfQueries; j++){
				Set<String> query1Sel = new HashSet<String>(tokenVector.get(i));
				Set<String> query2Sel = new HashSet<String>(tokenVector.get(j));
				query1Sel.retainAll(query2Sel);	
				coOccurenceQuery.put(i+" "+j, query1Sel.size());
			}
		}
		
		HashMap<String, Integer> coOccurenceIndex = new HashMap<String,Integer>();
		for (String keyword1 : Evaluator.keywordsFilter){
			for (String keyword2 : Evaluator.keywordsFilter){
				Set<String> keyword1Sel = new HashSet<String>(TextExtractPar.lp1.get(keyword1));
				Set<String> keyword2Sel = new HashSet<String>(TextExtractPar.lp1.get(keyword2));
				keyword1Sel.retainAll(keyword2Sel);	
				coOccurenceIndex.put(keyword1+" "+keyword2, keyword1Sel.size());
			}
		}
		
		// determining the tokens with unique counts
		for (int i = 0; i<numberOfQueries; i++){
			int selectivity = tokenVector.get(i).size();

			for (int  h =selectivity; h>=Math.ceil(iteration*selectivity/100) -error;h--){

				if (!Evaluator.selectivityKeywords.get(h).isEmpty()){
					windowAttackMap.putAll(i, Evaluator.selectivityKeywords.get(h));
				}
			}
		}	
		
		for (int i =0; i< numberOfQueries; i++){
			System.out.println("\nKeyword "+i+": "+windowAttackMap.get(i).contains(Evaluator.queryKeywords.get(i)));
		}
		
		// Filtering of the windowAttackmap so it only have a small number of possible mappings
		int minimum=1000000000;
		int chosen_index = 0;
		Set<String> temp_Set = null;
		for (int i =0; i< numberOfQueries; i++){
			if (minimum >= windowAttackMap.get(i).size()){
				minimum =windowAttackMap.get(i).size();
				chosen_index = i;
				temp_Set = new HashSet<String>(windowAttackMap.get(i));
			}
			if (windowAttackMap.get(i).size()>filter_factor){
				windowAttackMap.removeAll(i);				
			}
			
		}
		if (windowAttackMap.keySet().size() == 0){
			windowAttackMap.putAll(chosen_index, temp_Set);
		}

		System.out.println("KeySet Window Attack Map"+windowAttackMap.keySet());

		
		int count = -1; 
		while (true){
			int count1 = windowAttackMap.keySet().size();
			System.out.println("Count1: "+count1);
			if (count1 > count){
				for (Integer value1 : tokenVector.keySet()){
					int selectivity = tokenVector.get(value1).size(); 
					Set<String> select = new HashSet<String>();
					Set<String> select2 = new HashSet<String>();

					for (int  h =selectivity; h>=Math.ceil(iteration*selectivity/100) -error;h--){
						if (!Evaluator.selectivityKeywords.get(h).isEmpty()){
							select.addAll(Evaluator.selectivityKeywords.get(h));
							select2.addAll(Evaluator.selectivityKeywords.get(h));
						}
					}
					
				

					String match = "";
					Multimap<Integer, String> tempAttackMap = ArrayListMultimap.create();

					for (String keyword : select2){
						for (Integer value2 : windowAttackMap.keySet()){
							int flag = 0;
							Multimap<Integer, String> tempHold = ArrayListMultimap.create();

							for (String keyword2 : windowAttackMap.get(value2)){
								if (
										(coOccurenceIndex.get(keyword+" "+keyword2) < 
										coOccurenceQuery.get(value1+" "+value2) * (iteration/100))
										&&
										(coOccurenceIndex.get(keyword+" "+keyword2) > 
												coOccurenceQuery.get(value1+" "+value2))
												){
									flag++;
								}
								else{
									tempHold.put(value2, keyword2);
								}
							}

							if(flag == windowAttackMap.get(value2).size()){
								select.remove(keyword);
								break;
							}
							else{
								for (Integer in : tempHold.keySet()){
									tempAttackMap.putAll(in, tempHold.get(in));
								}
								
							}
						}
					}
					
					for (Integer val : tempAttackMap.keySet()){
						HashMap<String, Integer> tempAssignment = new HashMap<String,Integer>();
						for (String w : tempAttackMap.get(val)){
							if (tempAssignment.get(w) == null){
								tempAssignment.put(w, 1);
							}
							else{
								int tmp = tempAssignment.get(w);
								tempAssignment.put(w, tmp+1);
							}
						}


						int max = 0;
						String match2 = "";
						for (String w : tempAssignment.keySet()){
							if (tempAssignment.get(w) > max){
								max = tempAssignment.get(w);
								match2 = w;
							}
						}
						attackMap.put(val, match2);						

					}
					


					if (select.size() == 1){
						match = select.iterator().next();
						attackMap.put(value1, match);
						tokenVector.removeAll(value1);
						break;
					}
				}
			}
			else {
				break;
			}
			count = count1;
		}

		
		int countSuccessRate = 0;
		for (Integer value : attackMap.keySet()){
			if (attackMap.get(value).equals(Evaluator.queryKeywords.get(value))){
				countSuccessRate++;
			}
		}
		
		return countSuccessRate;
	}
	
	
	
	
	
	
	public static int countOccurencePartial(int iteration, int numberOfQueries, int error){
		
		
		HashMap<Integer, String> attackMap = new HashMap<Integer, String>();	
		Multimap<Integer, String> windowAttackMap = ArrayListMultimap.create();
		
		Multimap<Integer, String> tokenVector  = ArrayListMultimap.create();
		int counter1 = 0;
		for (String keyword : Evaluator.queryKeywords){
			tokenVector.putAll(counter1, Evaluator.totaLP1.get(keyword));
			counter1++;
		}
		
		
		HashMap<String, Integer> coOccurenceQuery = new HashMap<String,Integer>();
		for (int i =0; i<numberOfQueries; i++){
			for (int j =0;j<numberOfQueries; j++){
				Set<String> query1Sel = new HashSet<String>(tokenVector.get(i));
				Set<String> query2Sel = new HashSet<String>(tokenVector.get(j));
				query1Sel.retainAll(query2Sel);	
				coOccurenceQuery.put(i+" "+j, query1Sel.size());
			}
		}
		
		HashMap<String, Integer> coOccurenceIndex = new HashMap<String,Integer>();
		for (String keyword1 : Evaluator.keywordsFilter){
			for (String keyword2 : Evaluator.keywordsFilter){
				Set<String> keyword1Sel = new HashSet<String>(TextExtractPar.lp1.get(keyword1));
				Set<String> keyword2Sel = new HashSet<String>(TextExtractPar.lp1.get(keyword2));
				keyword1Sel.retainAll(keyword2Sel);	
				coOccurenceIndex.put(keyword1+" "+keyword2, keyword1Sel.size());
			}
		}
		
		
		int count = -1; 
		while (true){
			int count1 = windowAttackMap.keySet().size();
			if (count1 > count){
	
				System.out.println("Step 1: filtring out the keywords that will never match");
				for (Integer value1 : tokenVector.keySet()){
					int selectivity = tokenVector.get(value1).size(); 
					Set<String> select = new HashSet<String>();
					Set<String> select2 = new HashSet<String>();

					for (int  h =selectivity; h>=Math.ceil(iteration*selectivity/100) -error;h--){
						if (!Evaluator.selectivityKeywords.get(h).isEmpty()){
							select.addAll(Evaluator.selectivityKeywords.get(h));
							select2.addAll(Evaluator.selectivityKeywords.get(h));
						}
					}
					
					

					Set<String> temp_Set2 = new HashSet<String>();

					for (String keyword : select){
						Set<String> temp_Set3 = new HashSet<String>();

						for (int i =0; i<numberOfQueries;i++){
							boolean flag = false;
							for (String keyword1 : Evaluator.keywordsFilter){
								if(
										(coOccurenceIndex.get(keyword+" "+keyword1) >= 
									     Math.floor(coOccurenceQuery.get(value1+" "+i) * (iteration/100)))
										&&
										(coOccurenceIndex.get(keyword+" "+keyword1) <= 
										coOccurenceQuery.get(value1+" "+i))										
										&&
										(TextExtractPar.lp1.get(keyword1).size()>= 
										 Math.ceil(iteration*tokenVector.get(i).size()/100)-error)
									    &&
									    (TextExtractPar.lp1.get(keyword1).size()<=
									    		tokenVector.get(i).size())
								    
									)
								{
									temp_Set3.add(keyword1);
									flag = true;
								}
								else{
									if ((keyword.equals(Evaluator.queryKeywords.get(value1)))
											&&
											(keyword1.equals(Evaluator.queryKeywords.get(i)))
											){
										System.out.println("\n1 "+coOccurenceIndex.get(keyword+" "+keyword1)+" >= "+coOccurenceQuery.get(value1+" "+i) * (iteration/100)+" "+(coOccurenceIndex.get(keyword+" "+keyword1) >= 
												coOccurenceQuery.get(value1+" "+i) * (iteration/100))	);
										System.out.println("2 "+coOccurenceIndex.get(keyword+" "+keyword1)+" <= "+coOccurenceQuery.get(value1+" "+i)+" "+(coOccurenceIndex.get(keyword+" "+keyword1) <= 
												coOccurenceQuery.get(value1+" "+i))	);
										System.out.println("3 "+TextExtractPar.lp1.get(keyword1).size()+" >= "+(Math.ceil(iteration*tokenVector.get(i).size()/100)-error)+" "+(TextExtractPar.lp1.get(keyword1).size()>= 
												 Math.ceil(iteration*tokenVector.get(i).size()/100)-error));
										System.out.println("4 "+TextExtractPar.lp1.get(keyword1).size()+" <= "+tokenVector.get(i).size()+" "+(TextExtractPar.lp1.get(keyword1).size()<=
									    		tokenVector.get(i).size()));
										System.out.println("5 "+!temp_Set3.contains(keyword1));	
										System.out.println("6 "+keyword+" "+Evaluator.queryKeywords.get(value1));	
										System.out.println("6 "+keyword1+" "+Evaluator.queryKeywords.get(i));	

									}
								}
							}
							if (flag == false){
								temp_Set3 = new HashSet<String>();
							}
			
						}

						temp_Set2.addAll(temp_Set3);

					}
					System.out.println("Keyword "+Evaluator.queryKeywords.get(value1)+" : Set Size "+temp_Set2.size()+" CONTAINS: "+
							temp_Set2.contains(Evaluator.queryKeywords.get(value1)));

				}
			}
			else{
				break;
			}
			count= count1;
		}
						
		
		int countSuccessRate = 0;
		for (Integer value : attackMap.keySet()){
			if (attackMap.get(value).equals(Evaluator.queryKeywords.get(value))){
				countSuccessRate++;
			}
		}
		
		return countSuccessRate;
	}

	
}
