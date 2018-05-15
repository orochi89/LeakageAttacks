import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class SubgraphMatch {

	public static int subgraphAP(int iteration, int numberOfQueries, int error) {

		HashMap<Integer, String> attackMap = new HashMap<Integer, String>();
		Multimap<Integer, String> windowAttackMap = ArrayListMultimap.create();

		Multimap<Integer, String> tokenVector = ArrayListMultimap.create();
		int counter1 = 0;
		for (String keyword : Evaluator.queryKeywords) {
			tokenVector.putAll(counter1, Evaluator.totaLP1.get(keyword));
			counter1++;
		}

		Set<String> uniqueSet = new HashSet<String>();
		Multimap<Integer, String> tokenVectorTMP = ArrayListMultimap.create();

		for (int i = 0; i < numberOfQueries; i++) {

			// STEP 1: filtering the identifiers set to only contain the
			// identifiers in the partial knowledge
			for (String id : tokenVector.get(i)) {
				if (TextExtractPar.lp2.get(id).size() > 0) {
					tokenVectorTMP.put(i, id);
				}
			}
			// STEP 2: full inclusion
			for (String keyword : Evaluator.keywordsFilter) {
				if ((TextExtractPar.lp1.get(keyword)
						.size() >= Math.ceil(
								iteration * Evaluator.totaLP1.get(Evaluator.queryKeywords.get(i)).size() / 100) - error)
						&& (TextExtractPar.lp1.get(keyword).size() <= Evaluator.totaLP1
								.get(Evaluator.queryKeywords.get(i)).size())) {

					int size = TextExtractPar.lp1.get(keyword).size();
					List<String> initialList = new ArrayList<String>(TextExtractPar.lp1.get(keyword));
					initialList.retainAll(tokenVectorTMP.get(i));
					if (initialList.size() == size) {
						windowAttackMap.put(i, keyword);
					}
				}
			}
			if (windowAttackMap.get(i).size() == 1) {
				String match = windowAttackMap.get(i).iterator().next();
				attackMap.put(i, match);
				uniqueSet.add(match);
				windowAttackMap.removeAll(i);
			}

		}

		Set<Integer> keySet = new HashSet<Integer>(windowAttackMap.keySet());
		for (Integer index : keySet) {
			Set<String> initialSet = new HashSet<String>(windowAttackMap.get(index));
			for (String id : tokenVectorTMP.get(index)) {
				Set<String> tempSet = new HashSet<String>();
				for (String keyword : TextExtractPar.lp2.get(id)) {
					if ((TextExtractPar.lp1.get(keyword)
							.size() >= Math.ceil(
									iteration * Evaluator.totaLP1.get(Evaluator.queryKeywords.get(index)).size() / 100)
									- error)
							&& (TextExtractPar.lp1.get(keyword).size() <= Evaluator.totaLP1
									.get(Evaluator.queryKeywords.get(index)).size())
							&& (Evaluator.keywordsFilter.contains(keyword)))
						tempSet.add(keyword);
				}
				initialSet.retainAll(tempSet);
			}

			if (initialSet.size() == 1) {
				String match = initialSet.iterator().next();
				uniqueSet.add(match);
				attackMap.put(index, match);
			} else {
				windowAttackMap.removeAll(index);
				windowAttackMap.putAll(index, initialSet);
			}
		}

		int count = -1;
		while (true) {
			int initialCount = uniqueSet.size();
			if (initialCount > count) {
				for (Integer index : windowAttackMap.keySet()) {
					Set<String> tempSet = new HashSet<String>(windowAttackMap.get(index));
					tempSet.removeAll(uniqueSet);
					if (tempSet.size() == 1) {
						String match = tempSet.iterator().next();
						uniqueSet.add(match);
						attackMap.put(index, match);
						break;
					}
				}
			} else {
				break;
			}
			count = initialCount;
		}

		int subgraphAPSuccessRateTMP = 0;
		for (Integer value : attackMap.keySet()) {
			if (attackMap.get(value).equals(Evaluator.queryKeywords.get(value))) {
				subgraphAPSuccessRateTMP++;
			}
		}

		return subgraphAPSuccessRateTMP;
	}

	public static int subgraphVP(int iteration, int numberOfQueries, int error) {

		HashMap<Integer, String> attackMap = new HashMap<Integer, String>();
		Multimap<Integer, String> windowAttackMap = ArrayListMultimap.create();

		Multimap<Integer, Long> tokenVector = ArrayListMultimap.create();
		int counter1 = 0;

		for (String keyword : Evaluator.queryKeywords) {
			tokenVector.putAll(counter1, Evaluator.totaLP4.get(keyword));
			counter1++;
		}

		Set<String> uniqueSet = new HashSet<String>();
		Multimap<Integer, Long> tokenVectorTMP = ArrayListMultimap.create();

		for (int i = 0; i < numberOfQueries; i++) {

			// STEP 1: filtering the volume set to only contain the volumes in
			// the partial knowledge
			for (Long volume : tokenVector.get(i)) {
				if (TextExtractPar.lp5.get(volume).size() > 0) {
					tokenVectorTMP.put(i, volume);
				}
			}

			// STEP 2: full inclusion
			for (String keyword : Evaluator.keywordsFilter) {
				if ((TextExtractPar.lp1.get(keyword)
						.size() >= Math.ceil(
								iteration * Evaluator.totaLP1.get(Evaluator.queryKeywords.get(i)).size() / 100) - error)
						&& (TextExtractPar.lp1.get(keyword).size() <= Evaluator.totaLP1
								.get(Evaluator.queryKeywords.get(i)).size())) {
					int size = TextExtractPar.lp1.get(keyword).size();
					List<Long> initialList = new ArrayList<Long>(TextExtractPar.lp4.get(keyword));
					initialList.retainAll(tokenVectorTMP.get(i));
					if (initialList.size() == size) {
						windowAttackMap.put(i, keyword);
					}

				}
			}
			if (windowAttackMap.get(i).size() == 1) {
				String match = windowAttackMap.get(i).iterator().next();
				attackMap.put(i, match);
				uniqueSet.add(match);
				windowAttackMap.removeAll(i);
			}
		}

		int count = -1;
		while (true) {
			int initialCount = uniqueSet.size();
			if (initialCount > count) {
				for (Integer index : windowAttackMap.keySet()) {

					Set<String> tempSet = new HashSet<String>(windowAttackMap.get(index));
					tempSet.removeAll(uniqueSet);

					if (tempSet.size() == 1) {

						String match = tempSet.iterator().next();
						uniqueSet.add(match);
						attackMap.put(index, match);
						windowAttackMap.removeAll(index);
						break;
					}
				}
			} else {
				break;
			}
			count = initialCount;
		}

		int subgraphVPSuccessRateTMP = 0;
		for (Integer value : attackMap.keySet()) {
			if (attackMap.get(value).equals(Evaluator.queryKeywords.get(value))) {
				subgraphVPSuccessRateTMP++;
			}
		}

		return subgraphVPSuccessRateTMP;

	}

	/*
	 * Disjunction AP
	 */

	public static int subgraphDisjAP(int numberOfQueries, int sizeDisj) {

		Multimap<Integer, String> attackMap = ArrayListMultimap.create();
		Multimap<Integer, String> windowAttackMap = ArrayListMultimap.create();

		Multimap<Integer, String> tokenVector = HashMultimap.create();
		for (Integer index : Evaluator.queryKeywordsBoolean.keySet()) {
			Iterator<String> iterator = Evaluator.queryKeywordsBoolean.get(index).iterator();
			for (int i = 0; i < sizeDisj; i++) {
				String keyword = iterator.next();
				tokenVector.putAll(index, Evaluator.totaLP1.get(keyword));
			}
		}

		Multimap<Integer, String> tokenVectorTMP = HashMultimap.create();

		for (int i = 0; i < numberOfQueries; i++) {

			// STEP 1: filtering the identifiers set to only contain the
			// identifiers in the partial knowledge
			for (String id : tokenVector.get(i)) {
				if (TextExtractPar.lp2.get(id).size() > 0) {
					tokenVectorTMP.put(i, id);
				}
			}

			// STEP 2: full inclusion
			for (String keyword : Evaluator.keywordsFilter) {
				int size = TextExtractPar.lp1.get(keyword).size();
				List<String> initialList = new ArrayList<String>(TextExtractPar.lp1.get(keyword));
				initialList.retainAll(tokenVectorTMP.get(i));
				if (initialList.size() == size) {
					windowAttackMap.put(i, keyword);
				}
			}
			if (windowAttackMap.get(i).size() == sizeDisj) {
				Iterator<String> iterator = windowAttackMap.get(i).iterator();
				for (int j = 0; j < sizeDisj; j++) {
					String match = iterator.next();
					attackMap.put(i, match);
				}
				windowAttackMap.removeAll(i);
			}
		}

		Set<Integer> keySet = new HashSet<Integer>(windowAttackMap.keySet());
		for (Integer index : keySet) {
			HashMap<Integer, Integer> structure = new HashMap<Integer, Integer>();
			HashMap<Integer, String> initialKeywords = new HashMap<Integer, String>();

			Set<String> removeKeywords = new HashSet<String>();
			int lambda = windowAttackMap.get(index).size();

			int counter = 0;
			Iterator<String> itr = windowAttackMap.get(index).iterator();
			while (counter < lambda) {
				initialKeywords.put(counter, itr.next());
				counter++;
			}

			for (int i = 0; i < sizeDisj; i++) {
				structure.put(i, 0);
			}

			int possibleCombinations = (int) Math.pow(lambda, sizeDisj);

			int entry = 0;

			for (int i = 0; i < possibleCombinations; i++) {
				for (int j = 0; j < sizeDisj; j++) {
					if ((i % Math.pow(lambda, sizeDisj - j - 1) == 0) && (i > 0)) {
						int tmp = structure.get(j);
						if (tmp == lambda - 1) {
							tmp = 0;
						} else {
							tmp++;
						}
						structure.put(j, tmp);
					}
				}

				Set<String> finalResult = new HashSet<String>();

				for (int j = 0; j < sizeDisj; j++) {
					finalResult.addAll(TextExtractPar.lp1.get(initialKeywords.get(structure.get(j))));
				}

				if (finalResult.size() == tokenVectorTMP.get(index).size()) {
					double value = ((double) i) / (Math.pow(lambda, sizeDisj - 1));
					i = (int) (((int) Math.floor(value)) * (Math.pow(lambda, sizeDisj - 1))
							+ Math.pow(lambda, sizeDisj - 1) - 1);
					entry++;
				} else if (((i + 1) % Math.pow(lambda, sizeDisj - 1)) == 0) {
					double value = ((double) i) / Math.pow(lambda, sizeDisj - 1);
					removeKeywords.add(initialKeywords.get((int) Math.floor(value)));

				}

				int value1 = (int) (i / Math.pow(lambda, sizeDisj - 1));
				if (value1 > removeKeywords.size() + sizeDisj) {
					break;
				}

				if (entry > sizeDisj) {
					break;
				}
			}

			if (removeKeywords.size() == (windowAttackMap.get(index).size() - sizeDisj)) {
				Set<String> result = new HashSet<String>(windowAttackMap.get(index));
				result.removeAll(removeKeywords);
				attackMap.putAll(index, result);
			}

		}

		int subgraphDisjAPSuccessRateTMP = 0;
		for (int i = 0; i < numberOfQueries; i++) {
			if ((attackMap.get(i).containsAll(Evaluator.queryKeywordsBoolean.get(i)))
					&& (attackMap.get(i).size() == sizeDisj)) {
				subgraphDisjAPSuccessRateTMP++;
			}
		}

		return subgraphDisjAPSuccessRateTMP;
	}

	/*
	 * Disjunction VP
	 */

	public static int subgraphDisjVP(int numberOfQueries, int sizeDisj) {

		Multimap<Integer, String> attackMap = ArrayListMultimap.create();
		Multimap<Integer, String> windowAttackMap = ArrayListMultimap.create();

		Multimap<Integer, Long> tokenVector = HashMultimap.create();
		for (Integer index : Evaluator.queryKeywordsBoolean.keySet()) {
			Iterator<String> iterator = Evaluator.queryKeywordsBoolean.get(index).iterator();
			for (int i = 0; i < sizeDisj; i++) {
				String keyword = iterator.next();
				tokenVector.putAll(index, Evaluator.totaLP4.get(keyword));
			}
		}

		Multimap<Integer, Long> tokenVectorTMP = HashMultimap.create();

		for (int i = 0; i < numberOfQueries; i++) {

			// STEP 1: filtering the identifiers set to only contain the
			// identifiers in the partial knowledge
			for (Long volume : tokenVector.get(i)) {
				if (TextExtractPar.lp5.get(volume).size() > 0) {
					tokenVectorTMP.put(i, volume);
				}
			}

			// STEP 2: full inclusion
			for (String keyword : Evaluator.keywordsFilter) {
				int size = TextExtractPar.lp1.get(keyword).size();
				List<Long> initialList = new ArrayList<Long>(TextExtractPar.lp4.get(keyword));
				initialList.retainAll(tokenVectorTMP.get(i));
				if (initialList.size() == size) {
					windowAttackMap.put(i, keyword);
				}
			}
			if (windowAttackMap.get(i).size() == sizeDisj) {
				Iterator<String> iterator = windowAttackMap.get(i).iterator();
				for (int j = 0; j < sizeDisj; j++) {
					String match = iterator.next();
					attackMap.put(i, match);
				}
				windowAttackMap.removeAll(i);
			}
		}

		Set<Integer> keySet = new HashSet<Integer>(windowAttackMap.keySet());
		for (Integer index : keySet) {
			HashMap<Integer, Integer> structure = new HashMap<Integer, Integer>();
			HashMap<Integer, String> initialKeywords = new HashMap<Integer, String>();

			Set<String> removeKeywords = new HashSet<String>();
			int lambda = windowAttackMap.get(index).size();

			int counter = 0;
			Iterator<String> itr = windowAttackMap.get(index).iterator();
			while (counter < lambda) {
				initialKeywords.put(counter, itr.next());
				counter++;
			}

			for (int i = 0; i < sizeDisj; i++) {
				structure.put(i, 0);
			}

			int possibleCombinations = (int) Math.pow(lambda, sizeDisj);

			int entry = 0;
			for (int i = 0; i < possibleCombinations; i++) {
				for (int j = 0; j < sizeDisj; j++) {
					if ((i % Math.pow(lambda, sizeDisj - j - 1) == 0) && (i > 0)) {
						int tmp = structure.get(j);
						if (tmp == lambda - 1) {
							tmp = 0;
						} else {
							tmp++;
						}
						structure.put(j, tmp);
					}
				}

				Set<Long> finalResult = new HashSet<Long>();

				for (int j = 0; j < sizeDisj; j++) {
					finalResult.addAll(TextExtractPar.lp4.get(initialKeywords.get(structure.get(j))));
				}

				if (finalResult.size() == tokenVectorTMP.get(index).size()) {
					double value = ((double) i) / (Math.pow(lambda, sizeDisj - 1));
					i = (int) (((int) Math.floor(value)) * (Math.pow(lambda, sizeDisj - 1))
							+ Math.pow(lambda, sizeDisj - 1) - 1);
					entry++;
				} else if (((i + 1) % Math.pow(lambda, sizeDisj - 1)) == 0) {
					double value = ((double) i) / Math.pow(lambda, sizeDisj - 1);
					removeKeywords.add(initialKeywords.get((int) Math.floor(value)));

				}

				int value1 = (int) (i / Math.pow(lambda, sizeDisj - 1));

				if (value1 > removeKeywords.size() + sizeDisj) {
					break;
				}
				if (entry > sizeDisj) {
					break;
				}
			}

			if (removeKeywords.size() == (windowAttackMap.get(index).size() - sizeDisj)) {
				Set<String> result = new HashSet<String>(windowAttackMap.get(index));
				result.removeAll(removeKeywords);
				attackMap.putAll(index, result);
			}

		}

		int subgraphDisjVPSuccessRateTMP = 0;
		for (int i = 0; i < numberOfQueries; i++) {
			if ((attackMap.get(i).containsAll(Evaluator.queryKeywordsBoolean.get(i)))
					&& (attackMap.get(i).size() == sizeDisj)) {
				subgraphDisjVPSuccessRateTMP++;
			}
		}

		return subgraphDisjVPSuccessRateTMP;
	}

	/*
	 * Conjunction AP
	 */

	public static int subgraphConjAP(int numberOfQueries, int sizeConj) {

		Multimap<Integer, String> attackMap = ArrayListMultimap.create();
		Multimap<Integer, String> windowAttackMap = ArrayListMultimap.create();

		Multimap<Integer, String> tokenVector = HashMultimap.create();
		for (Integer index : Evaluator.queryKeywordsBoolean.keySet()) {
			Iterator<String> iterator = Evaluator.queryKeywordsBoolean.get(index).iterator();
			for (int i = 0; i < sizeConj; i++) {
				String keyword = iterator.next();
				if (i == 0) {
					tokenVector.putAll(index, Evaluator.totaLP1.get(keyword));
				} else {
					tokenVector.get(index).retainAll(Evaluator.totaLP1.get(keyword));
				}
			}

		}

		Multimap<Integer, String> tokenVectorTMP = HashMultimap.create();

		for (int i = 0; i < numberOfQueries; i++) {

			// STEP 1: filtering the identifiers set to only contain the
			// identifiers in the partial knowledge
			for (String id : tokenVector.get(i)) {
				if (TextExtractPar.lp2.get(id).size() > 0) {
					tokenVectorTMP.put(i, id);
				}
			}

			// STEP 2: full inclusion
			for (String keyword : Evaluator.keywordsFilter) {
				List<String> initialList = new ArrayList<String>(TextExtractPar.lp1.get(keyword));
				if (initialList.containsAll(tokenVectorTMP.get(i))) {
					windowAttackMap.put(i, keyword);
				}
			}

			if (windowAttackMap.get(i).size() == sizeConj) {
				attackMap.putAll(i, windowAttackMap.get(i));
				windowAttackMap.removeAll(i);
			}
		}

		Set<Integer> keySet = new HashSet<Integer>(windowAttackMap.keySet());
		for (Integer index : keySet) {
			if (tokenVectorTMP.get(index).size() > 0) {
				HashMap<Integer, Integer> structure = new HashMap<Integer, Integer>();
				HashMap<Integer, String> initialKeywords = new HashMap<Integer, String>();
				Set<String> removeKeywords = new HashSet<String>();
				int lambda = windowAttackMap.get(index).size();

				int counter = 0;
				Iterator<String> itr = windowAttackMap.get(index).iterator();
				while (counter < lambda) {
					initialKeywords.put(counter, itr.next());
					counter++;
				}

				for (int i = 0; i < sizeConj; i++) {
					structure.put(i, 0);
				}

				int possibleCombinations = (int) Math.pow(lambda, sizeConj);

				int entry = 0;

				for (int i = 0; i < possibleCombinations; i++) {
					for (int j = 0; j < sizeConj; j++) {
						if ((i % Math.pow(lambda, sizeConj - j - 1) == 0) && (i > 0)) {
							int tmp = structure.get(j);
							if (tmp == lambda - 1) {
								tmp = 0;
							} else {
								tmp++;
							}
							structure.put(j, tmp);
						}
					}

					Set<String> finalResult = new HashSet<String>();

					for (int j = 0; j < sizeConj; j++) {
						if (j == 0) {
							Set<String> temp = new HashSet<String>(
									TextExtractPar.lp1.get(initialKeywords.get(structure.get(j))));
							finalResult.addAll(temp);
						} else {
							Set<String> temp = new HashSet<String>(
									TextExtractPar.lp1.get(initialKeywords.get(structure.get(j))));
							finalResult.retainAll(temp);
						}
					}

					if (finalResult.size() == tokenVectorTMP.get(index).size()) {
						double value = ((double) i) / (Math.pow(lambda, sizeConj - 1));
						i = (int) (((int) Math.floor(value)) * (Math.pow(lambda, sizeConj - 1))
								+ Math.pow(lambda, sizeConj - 1) - 1);
						entry++;
					} else if (((i + 1) % Math.pow(lambda, sizeConj - 1)) == 0) {
						double value = ((double) i) / Math.pow(lambda, sizeConj - 1);
						removeKeywords.add(initialKeywords.get((int) Math.floor(value)));

					}

					int value1 = (int) (i / (Math.pow(lambda, sizeConj - 1)));
					if (value1 > removeKeywords.size() + sizeConj) {
						break;
					}

					if (entry > sizeConj) {
						break;
					}
				}

				if (removeKeywords.size() == (windowAttackMap.get(index).size() - sizeConj)) {
					Set<String> result = new HashSet<String>(windowAttackMap.get(index));
					result.removeAll(removeKeywords);
					attackMap.putAll(index, result);

				}

			}
		}

		int subgraphConjAPSuccessRateTMP = 0;
		for (int i = 0; i < numberOfQueries; i++) {
			if ((attackMap.get(i).containsAll(Evaluator.queryKeywordsBoolean.get(i)))
					&& (attackMap.get(i).size() == sizeConj)) {
				subgraphConjAPSuccessRateTMP++;
			}
		}

		return subgraphConjAPSuccessRateTMP;

	}

	/*
	 * Conjunction AP
	 */

	public static int subgraphConjAP2(int numberOfQueries, int sizeConj) {

		Multimap<Integer, String> attackMap = ArrayListMultimap.create();
		Multimap<Integer, String> windowAttackMap = ArrayListMultimap.create();

		Multimap<Integer, String> tokenVector = HashMultimap.create();
		for (Integer index : Evaluator.queryKeywordsBoolean.keySet()) {
			Iterator<String> iterator = Evaluator.queryKeywordsBoolean.get(index).iterator();
			for (int i = 0; i < sizeConj; i++) {
				String keyword = iterator.next();
				if (i == 0) {
					Set<String> temp = new HashSet<String>(Evaluator.totaLP1.get(keyword));
					tokenVector.putAll(index, temp);
				} else {
					Set<String> temp = new HashSet<String>(Evaluator.totaLP1.get(keyword));
					tokenVector.get(index).retainAll(temp);
				}
			}

		}

		Multimap<Integer, String> tokenVectorTMP = HashMultimap.create();

		for (int i = 0; i < numberOfQueries; i++) {

			// STEP 1: filtering the identifiers set to only contain the
			// identifiers in the partial knowledge
			for (String id : tokenVector.get(i)) {
				if (TextExtractPar.lp2.get(id).size() > 0) {
					tokenVectorTMP.put(i, id);
				}
			}

			// STEP 2: full inclusion
			for (String keyword : Evaluator.keywordsFilter) {
				List<String> initialList = new ArrayList<String>(TextExtractPar.lp1.get(keyword));
				if (initialList.containsAll(tokenVectorTMP.get(i))) {
					windowAttackMap.put(i, keyword);
				}
			}

			if (windowAttackMap.get(i).size() == sizeConj) {
				attackMap.putAll(i, windowAttackMap.get(i));
				windowAttackMap.removeAll(i);
			}
		}

		Set<Integer> keyset = new HashSet<Integer>(windowAttackMap.keySet());
		for (Integer index : keyset) {

			Set<String> tempResult = new HashSet<String>();

			if (tokenVectorTMP.get(index).size() > 0) {
				Set<String> temporaryKeywords = new HashSet<String>(windowAttackMap.get(index));

				for (String keyword1 : windowAttackMap.get(index)) {
					boolean flag = false;
					for (String keyword2 : temporaryKeywords) {
						Set<String> temp = new HashSet<String>(TextExtractPar.lp1.get(keyword1));

						Set<String> temp1 = new HashSet<String>(TextExtractPar.lp1.get(keyword2));
						temp.retainAll(temp1);

						if (temp.size() == tokenVectorTMP.get(index).size()) {

							flag = true;
							tempResult.add(keyword1);
							tempResult.add(keyword2);
							break;
						}
					}
					if (flag == false) {
						temporaryKeywords.remove(keyword1);
					}

					if (tempResult.size() > sizeConj) {
						break;
					}
				}
			}

			if (tempResult.size() == 2) {
				attackMap.putAll(index, tempResult);

			}

		}

		int subgraphConjAPSuccessRateTMP = 0;
		for (int i = 0; i < numberOfQueries; i++) {
			if ((attackMap.get(i).containsAll(Evaluator.queryKeywordsBoolean.get(i)))
					&& (attackMap.get(i).size() == sizeConj)) {
				subgraphConjAPSuccessRateTMP++;
			}
		}

		return subgraphConjAPSuccessRateTMP;

	}

	/*
	 * Conjunction VP
	 */

	public static int subgraphConjVP2(int numberOfQueries, int sizeConj) {

		Multimap<Integer, String> attackMap = ArrayListMultimap.create();
		Multimap<Integer, String> windowAttackMap = ArrayListMultimap.create();

		// A Step to get the correct leakage
		Multimap<Integer, Long> tokenVector = ArrayListMultimap.create();
		Multimap<Integer, String> tokenVectorID = HashMultimap.create();
		for (Integer index : Evaluator.queryKeywordsBoolean.keySet()) {
			Iterator<String> iterator = Evaluator.queryKeywordsBoolean.get(index).iterator();
			for (int i = 0; i < sizeConj; i++) {
				String keyword = iterator.next();
				if (i == 0) {
					Set<String> temp = new HashSet<String>(Evaluator.totaLP1.get(keyword));
					tokenVectorID.putAll(index, temp);
				} else {
					Set<String> temp = new HashSet<String>(Evaluator.totaLP1.get(keyword));
					tokenVectorID.get(index).retainAll(temp);
				}
			}
		}

		for (Integer index : Evaluator.queryKeywordsBoolean.keySet()) {
			for (String id : tokenVectorID.get(index)) {
				tokenVector.put(index, TextExtractPar.idToVol.get(id));
			}
		}

		Multimap<Integer, Long> tokenVectorTMP = ArrayListMultimap.create();
		for (int i = 0; i < numberOfQueries; i++) {

			// STEP 1: filtering the identifiers set to only contain the
			// identifiers in the partial knowledge
			for (Long volume : tokenVector.get(i)) {
				if (TextExtractPar.lp5.get(volume).size() > 0) {
					tokenVectorTMP.put(i, volume);
				}
			}

			// STEP 2: full inclusion
			for (String keyword : Evaluator.keywordsFilter) {
				List<Long> initialList = new ArrayList<Long>(TextExtractPar.lp4.get(keyword));
				if (initialList.containsAll(tokenVectorTMP.get(i))) {
					windowAttackMap.put(i, keyword);
				}
			}

			if (windowAttackMap.get(i).size() == sizeConj) {
				attackMap.putAll(i, windowAttackMap.get(i));
				windowAttackMap.removeAll(i);
			}
		}

		Set<Integer> keyset = new HashSet<Integer>(windowAttackMap.keySet());
		for (Integer index : keyset) {

			Set<String> tempResult = new HashSet<String>();

			if (tokenVectorTMP.get(index).size() > 0) {
				Set<String> temporaryKeywords = new HashSet<String>(windowAttackMap.get(index));

				for (String keyword1 : windowAttackMap.get(index)) {
					boolean flag = false;
					for (String keyword2 : temporaryKeywords) {
						Set<String> temp = new HashSet<String>(TextExtractPar.lp1.get(keyword1));

						Set<String> temp1 = new HashSet<String>(TextExtractPar.lp1.get(keyword2));

						temp.retainAll(temp1);

						// implement an intersection that preserves multiplities

						if (temp.size() == tokenVectorTMP.get(index).size()) {

							flag = true;
							tempResult.add(keyword1);
							tempResult.add(keyword2);
							break;
						}
					}
					if (flag == false) {
						temporaryKeywords.remove(keyword1);
					}

					if (tempResult.size() > sizeConj) {
						break;
					}
				}
			}

			if (tempResult.size() == 2) {
				attackMap.putAll(index, tempResult);

			}

		}

		int subgraphConjVPSuccessRateTMP = 0;
		for (int i = 0; i < numberOfQueries; i++) {
			if ((attackMap.get(i).containsAll(Evaluator.queryKeywordsBoolean.get(i)))
					&& (attackMap.get(i).size() == sizeConj)) {
				subgraphConjVPSuccessRateTMP++;
			}
		}

		return subgraphConjVPSuccessRateTMP;

	}

	public static int subgraphConjVP(int numberOfQueries, int sizeConj) {

		Multimap<Integer, String> attackMap = ArrayListMultimap.create();
		Multimap<Integer, String> windowAttackMap = ArrayListMultimap.create();

		// A Step to get the correct leakage
		Multimap<Integer, Long> tokenVector = ArrayListMultimap.create();
		Multimap<Integer, String> tokenVectorID = HashMultimap.create();
		for (Integer index : Evaluator.queryKeywordsBoolean.keySet()) {
			Iterator<String> iterator = Evaluator.queryKeywordsBoolean.get(index).iterator();
			for (int i = 0; i < sizeConj; i++) {
				String keyword = iterator.next();
				if (i == 0) {
					Set<String> temp = new HashSet<String>(Evaluator.totaLP1.get(keyword));
					tokenVectorID.putAll(index, temp);
				} else {
					Set<String> temp = new HashSet<String>(Evaluator.totaLP1.get(keyword));
					tokenVectorID.get(index).retainAll(temp);
				}
			}
		}

		for (Integer index : Evaluator.queryKeywordsBoolean.keySet()) {
			for (String id : tokenVectorID.get(index)) {
				tokenVector.put(index, TextExtractPar.idToVol.get(id));
			}
		}

		Multimap<Integer, Long> tokenVectorTMP = HashMultimap.create();

		for (int i = 0; i < numberOfQueries; i++) {

			// STEP 1: filtering the identifiers set to only contain the
			// identifiers in the partial knowledge
			for (Long volume : tokenVector.get(i)) {
				if (TextExtractPar.lp5.get(volume).size() > 0) {
					tokenVectorTMP.put(i, volume);
				}
			}

			// STEP 2: full inclusion
			for (String keyword : Evaluator.keywordsFilter) {
				List<Long> initialList = new ArrayList<Long>(TextExtractPar.lp4.get(keyword));
				if (initialList.containsAll(tokenVectorTMP.get(i))) {
					windowAttackMap.put(i, keyword);
				}
			}

			if (windowAttackMap.get(i).size() == sizeConj) {
				attackMap.putAll(i, windowAttackMap.get(i));
				windowAttackMap.removeAll(i);
			}
		}

		Set<Integer> keySet = new HashSet<Integer>(windowAttackMap.keySet());
		for (Integer index : keySet) {
			if (tokenVectorTMP.get(index).size() > 0) {
				HashMap<Integer, Integer> structure = new HashMap<Integer, Integer>();
				HashMap<Integer, String> initialKeywords = new HashMap<Integer, String>();
				Set<String> removeKeywords = new HashSet<String>();
				int lambda = windowAttackMap.get(index).size();

				int counter = 0;
				Iterator<String> itr = windowAttackMap.get(index).iterator();
				while (counter < lambda) {
					initialKeywords.put(counter, itr.next());
					counter++;
				}

				for (int i = 0; i < sizeConj; i++) {
					structure.put(i, 0);
				}

				int possibleCombinations = (int) Math.pow(lambda, sizeConj);

				int entry = 0;

				for (int i = 0; i < possibleCombinations; i++) {
					for (int j = 0; j < sizeConj; j++) {
						if ((i % Math.pow(lambda, sizeConj - j - 1) == 0) && (i > 0)) {
							int tmp = structure.get(j);
							if (tmp == lambda - 1) {
								tmp = 0;
							} else {
								tmp++;
							}
							structure.put(j, tmp);
						}
					}

					Set<Long> finalResult = new HashSet<Long>();

					for (int j = 0; j < sizeConj; j++) {
						if (j == 0) {

							Set<Long> temp = new HashSet<Long>(
									TextExtractPar.lp4.get(initialKeywords.get(structure.get(j))));
							finalResult.addAll(temp);
						} else {
							Set<Long> temp = new HashSet<Long>(
									TextExtractPar.lp4.get(initialKeywords.get(structure.get(j))));
							finalResult.retainAll(temp);
						}
					}

					if (finalResult.size() == tokenVectorTMP.get(index).size()) {
						double value = ((double) i) / (Math.pow(lambda, sizeConj - 1));
						i = (int) (((int) Math.floor(value)) * (Math.pow(lambda, sizeConj - 1))
								+ Math.pow(lambda, sizeConj - 1) - 1);
						entry++;
					} else if (((i + 1) % Math.pow(lambda, sizeConj - 1)) == 0) {
						double value = ((double) i) / Math.pow(lambda, sizeConj - 1);
						removeKeywords.add(initialKeywords.get((int) Math.floor(value)));

					}

					int value1 = (int) (i / (Math.pow(lambda, sizeConj - 1)));
					if (value1 > removeKeywords.size() + sizeConj) {
						break;
					}

					if (entry > sizeConj) {
						break;
					}
				}

				if (removeKeywords.size() == (windowAttackMap.get(index).size() - sizeConj)) {
					Set<String> result = new HashSet<String>(windowAttackMap.get(index));
					result.removeAll(removeKeywords);
					attackMap.putAll(index, result);

				}

			}
		}

		int subgraphConjVPSuccessRateTMP = 0;
		for (int i = 0; i < numberOfQueries; i++) {
			if ((attackMap.get(i).containsAll(Evaluator.queryKeywordsBoolean.get(i)))
					&& (attackMap.get(i).size() == sizeConj)) {
				subgraphConjVPSuccessRateTMP++;
			}
		}

		return subgraphConjVPSuccessRateTMP;

	}
}
