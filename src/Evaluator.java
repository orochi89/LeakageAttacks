
/** * Copyright (C) 2016 Tarik Moataz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import javax.crypto.NoSuchPaddingException;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

public class Evaluator {


	static Multimap<Integer, String> selectivityKeywords = ArrayListMultimap.create();
	static List<String> queryKeywords = new ArrayList<String>();
	static Multimap<Integer, String> queryKeywordsBoolean = ArrayListMultimap.create();
	static List<String> keywordsFilter = new ArrayList<String>();
	static List<String> keywordsFilterTotal = new ArrayList<String>();
	static List<String> toSelectFrom = new ArrayList<String>();
	static Multimap<String, String> totaLP1 = ArrayListMultimap.create();
	static Multimap<String, String> totaLP2 = ArrayListMultimap.create();
	static HashMap<String, Long> totaLP3 = new HashMap<String, Long>();
	static Multimap<String, Long> totaLP4 = ArrayListMultimap.create();
	static Multimap<Long, String> totaLP5 = ArrayListMultimap.create();
	static Multimap<Long, String> volumeKeywords = ArrayListMultimap.create();
	public static Long filesSize = 0L;
	static double averageFileSize = 0;

	/**
	 * 
	 * @param folderName
	 * @param stopWords
	 * @param numberOfQueries
	 * @param numberOfKeywords
	 * @param frequency
	 * @param inOut
	 * @param serverKnowledge
	 * @param bool
	 * @param sizeBool
	 * @param numberOfExecutions
	 * @param keywordDistribution
	 * @throws Exception
	 */
	
	
	public static void evaluator(String folderName, Integer stopWords, Integer numberOfQueries,
			Integer numberOfKeywords, Integer frequency, Integer inOut, Integer serverKnowledge, Integer bool,
			Integer sizeBool, Integer numberOfExecutions, Integer keywordDistribution, Integer adjust, Integer remove) throws Exception {


		String filename = "Test-" + folderName + "-" + numberOfKeywords + "-" + frequency + "-" + inOut;

		FileWriter fileWriter = new FileWriter(filename);
		PrintWriter printWriter = new PrintWriter(fileWriter);

		TreeMultimap<Integer, Double> countOnlyAttack = TreeMultimap.create();
		TreeMultimap<Integer, Double> volumeOnlyAttack = TreeMultimap.create();
		TreeMultimap<Integer, Double> volumeCountAttack = TreeMultimap.create();
		TreeMultimap<Integer, Double> subgraphAPAttack = TreeMultimap.create();
		TreeMultimap<Integer, Double> subgraphVPAttack = TreeMultimap.create();
		TreeMultimap<Integer, Double> subgraphDisjAPAttack = TreeMultimap.create();
		TreeMultimap<Integer, Double> subgraphDisjVPAttack = TreeMultimap.create();
		TreeMultimap<Integer, Double> subgraphConjAPAttack = TreeMultimap.create();
		TreeMultimap<Integer, Double> subgraphConjVPAttack = TreeMultimap.create();

		int countOnlySuccessRate = 0;
		int volumeOnlySuccessRate = 0;
		int volumeCountSuccessRate = 0;
		int subgraphAPSuccessRate = 0;
		int subgraphVPSuccessRate = 0;

		int subgraphDisjAPSuccessRate = 0;
		int subgraphDisjVPSuccessRate = 0;

		int subgraphConjAPSuccessRate = 0;
		int subgraphConjVPSuccessRate = 0;


		ArrayList<File> listOfFile = new ArrayList<File>();
		TextProc.listf(folderName, listOfFile);
		int numberOfFiles = (int) (listOfFile.size() * serverKnowledge / 100);
		ArrayList<File> selectedFiles = new ArrayList<File>();
		TreeMap<Integer, File> tempFiles = new TreeMap<Integer, File>();
		for (int j = 0; j < numberOfFiles; j++) {
			int random = new Random().nextInt((int) Math.pow(2, 32));
			tempFiles.put(random, listOfFile.get(j));
			filesSize = 	filesSize +listOfFile.get(j).length();
		}

		int cnt = 0;
		for (Integer value : tempFiles.keySet()) {
			if (cnt >= numberOfFiles) {
				break;
			}
			selectedFiles.add(tempFiles.get(value));
			cnt++;
		}

		TextExtractPar.lp1 = ArrayListMultimap.create();
		TextExtractPar.lp2 = ArrayListMultimap.create();
		TextExtractPar.lp3 = new HashMap<String, Long>();
		TextExtractPar.lp4 = ArrayListMultimap.create();
		TextExtractPar.lp5 = ArrayListMultimap.create();
		TextExtractPar.extractTextPar(selectedFiles, stopWords, remove);

		
		averageFileSize = ((double) filesSize) / TextExtractPar.lp2.keySet().size();

		
		Integer totalNumberOfKeywords = TextExtractPar.lp1.keySet().size();

		HashMap<Integer, Integer> countHistogram = new HashMap<Integer, Integer>();

		for (String keyword : TextExtractPar.lp1.keySet()) {
			if (countHistogram.get(TextExtractPar.lp1.get(keyword).size()) == null) {
				countHistogram.put(TextExtractPar.lp1.get(keyword).size(), 1);
			} else {
				Integer tmp = countHistogram.get(TextExtractPar.lp1.get(keyword).size());
				countHistogram.put(TextExtractPar.lp1.get(keyword).size(), tmp + 1);
			}
		}

		TreeMap<Integer, Integer> sortedCountHistogram = new TreeMap<Integer, Integer>();
		for (Integer size : countHistogram.keySet()) {
			sortedCountHistogram.put(size, countHistogram.get(size));
		}

		if (keywordDistribution == 1) {
			int rank = 0;
			String filename2 = "Keyword-frequency-" + folderName + "-" + numberOfKeywords + "-" + frequency + "-"
					+ inOut;
			FileWriter fileWriter2 = new FileWriter(filename2);
			PrintWriter printWriter2 = new PrintWriter(fileWriter2);
			for (Integer size : sortedCountHistogram.descendingKeySet()) {
				for (int l = 0; l < sortedCountHistogram.get(size); l++) {
					printWriter2.printf(rank + "\t" + (double) size + "\n");
					rank++;
				}
			}
			printWriter2.close();
			return;
		}
		
		

		if (frequency == 0) {


			Integer filterSize = 0;
			Integer tmpSize = 0;
			for (Integer value : sortedCountHistogram.descendingKeySet()) {
				if (tmpSize >= numberOfKeywords) {
					filterSize = value+1;
					break;
				} else {
					tmpSize = tmpSize + sortedCountHistogram.get(value);
				}
			}
			if (filterSize == 0) {
				System.out.println("No filtering occured");
				printWriter.printf("No filtering occured");
				printWriter.close();
				return;
			}

			keywordsFilter = new ArrayList<String>();
			selectivityKeywords = ArrayListMultimap.create();
			for (String keyword : TextExtractPar.lp1.keySet()) {
				if (TextExtractPar.lp1.get(keyword).size() >= filterSize){
					keywordsFilter.add(keyword);
					selectivityKeywords.put(TextExtractPar.lp1.get(keyword).size(), keyword);
				}

			}

		} else if (frequency == 1) {

			Integer filterSize = 0;
			Integer tmpSize = 0;
			for (Integer value : sortedCountHistogram.keySet()) {
				if (tmpSize >= numberOfKeywords) {
					filterSize = value-1;
					break;
				} else {
					tmpSize = tmpSize + sortedCountHistogram.get(value);
				}

			}

			if (filterSize == 0) {
				System.out.println("No filtering occured for " + frequency);
				printWriter.printf("No filtering occured for " + frequency);
				printWriter.close();
				return;
			}
			

			keywordsFilter = new ArrayList<String>();
			selectivityKeywords = ArrayListMultimap.create();
			for (String keyword : TextExtractPar.lp1.keySet()) {
				if (TextExtractPar.lp1.get(keyword).size() <= filterSize){
					keywordsFilter.add(keyword);
					selectivityKeywords.put(TextExtractPar.lp1.get(keyword).size(), keyword);
				}

			}
		}

		if (adjust == 1){
		int toRemove = keywordsFilter.size() - numberOfKeywords;
			while (toRemove >0){
				int random = new Random().nextInt(keywordsFilter.size());
				keywordsFilter.remove(random);
				toRemove--;
			}
		}
		
		// We calculate the number of keywords with unique selectivities	
		countHistogram = new HashMap<Integer, Integer>();
		for (String keyword : keywordsFilter) {
			if (countHistogram.get(TextExtractPar.lp1.get(keyword).size()) == null) {
				countHistogram.put(TextExtractPar.lp1.get(keyword).size(), 1);
			} else {
				Integer tmp = countHistogram.get(TextExtractPar.lp1.get(keyword).size());
				countHistogram.put(TextExtractPar.lp1.get(keyword).size(), tmp + 1);
			}
		}
		Integer uniqueKeywordsRatio = 0;
		for (Integer value : countHistogram.keySet()) {
			if (countHistogram.get(value) == 1) {
				uniqueKeywordsRatio = uniqueKeywordsRatio + 1;
			}
		}

		// We calculate the number of keywords with unique volumes of file
		HashMap<Long, Integer> volumeHistogram = new HashMap<Long, Integer>();
		volumeKeywords = ArrayListMultimap.create();

		for (String keyword : keywordsFilter) {
			volumeKeywords.put(TextExtractPar.lp3.get(keyword), keyword);
			if (volumeHistogram.get(TextExtractPar.lp3.get(keyword)) == null) {
				volumeHistogram.put(TextExtractPar.lp3.get(keyword), 1);
			} else {
				Integer tmp = volumeHistogram.get(TextExtractPar.lp3.get(keyword));
				volumeHistogram.put(TextExtractPar.lp3.get(keyword), tmp + 1);
			}
		}

		List<String> uniqueKeywords = new ArrayList<String>();
		Integer uniqueKeywordsVolumeRatio = 0;
		for (Long value : volumeHistogram.keySet()) {
			if (volumeHistogram.get(value) == 1) {
				uniqueKeywordsVolumeRatio = uniqueKeywordsVolumeRatio + 1;
				uniqueKeywords.add(volumeKeywords.get(value).iterator().next());
			}
		}

		// We calculate the number of keywords with unique (selectivity,volume)
		Integer uniqueSelectivityVolumeRatio = uniqueKeywordsVolumeRatio;

		for (String keyword : keywordsFilter) {
			if (!uniqueKeywords.contains(keyword)) {
				Long volume = TextExtractPar.lp3.get(keyword);
				Integer selectivity = TextExtractPar.lp1.get(keyword).size();
				Set<String> volKey = new HashSet<String>(volumeKeywords.get(volume));
				Set<String> selKey = new HashSet<String>(selectivityKeywords.get(selectivity));
				selKey.retainAll(volKey);
				if (selKey.size() == 1) {
					uniqueSelectivityVolumeRatio = uniqueSelectivityVolumeRatio + 1;
					uniqueKeywords.add(keyword);
				}
			}
		}

		/*
		 * The print below summarizes the statistics about the known/entire data
		 */
		System.out.println("\n=====================================\n" + "==== Statistics of " + serverKnowledge
				+ " % of data =====\n" + "=====================================\n");

		printWriter.printf("\n=====================================\n" + "==== Statistics of %d of data =====\n"
				+ "=====================================\n", serverKnowledge);

		/*
		 * Number of keywords in the total knowledge
		 */
		System.out.println("\t*Number of (total) keywords " + totaLP1.keySet().size());
		printWriter.printf("\n\t*Number of (total) keywords " + totaLP1.keySet().size());
		
		System.out.println("\t*Number of (total) files " + totaLP2.keySet().size());
		printWriter.printf("\n\t*Number of (total) files " + totaLP2.keySet().size());

		System.out.println("\t*Number of (partial) keywords " + TextExtractPar.lp1.keySet().size());
		printWriter.printf("\n\t*Number of (partial) keywords " + TextExtractPar.lp1.keySet().size());

		System.out.println("\t*Number of (partial) files " + TextExtractPar.lp2.keySet().size());
		printWriter.printf("\n\t*Number of (partial) files " + TextExtractPar.lp2.keySet().size());

		System.out.println("\t*Keywords with unique selectivities (in known data) " + uniqueKeywordsRatio);
		printWriter.printf("\n\t*Keywords with unique selectivities (in known data) " + uniqueKeywordsRatio);

		System.out.println("\t*Keywords with unique volumes (in keyword space) " + uniqueKeywordsVolumeRatio);
		printWriter.printf("\n\t*Keywords with unique volumes (in keyword space) " + uniqueKeywordsVolumeRatio);

		System.out.println("\t*Volumes (in keyword space) " + volumeHistogram.keySet().size());
		printWriter.printf("\n\t*Volumes (in keyword space) " + volumeHistogram.keySet().size());
	
		System.out.println("\t*Unique (selectivity,volume) (in keyword space) " + uniqueSelectivityVolumeRatio);
		printWriter.printf("\n\t*Unique (selectivity,volume) (in keyword space) " + uniqueSelectivityVolumeRatio);

		System.out.println("\t*Total number of (most/less frequent) keywords " + keywordsFilter.size());
		printWriter.printf("\n\t*Total number of (most/less frequent) keywords " + keywordsFilter.size());

		for (int p = 0; p < numberOfExecutions; p++) {

			System.out.println("\n===================================\n" + "====  Attacks of " + serverKnowledge
					+ " % of data; iteration " + p + " =====\n" + "===================================\n");

			printWriter.printf("\n===================================\n"
					+ "====  Attacks of %d of data; iteration %d =====\n" + "===================================\n",
					serverKnowledge, p);

			/**
			 * Generating new query for every run with new randomness
			 * 
			 */
			if (inOut == 0) {
				toSelectFrom = keywordsFilter;
			} else if (inOut == 1) {
				toSelectFrom = keywordsFilterTotal;
			}

			randomQueriesGeneration(numberOfQueries, toSelectFrom);
			System.out.println("\nSelected query vector " + queryKeywords);
			printWriter.printf("\nSelected query vector " + queryKeywords);

			/*
			 * Single keyword SSE attacks
			 */

			countOnlySuccessRate = CountAttack.countOnly(serverKnowledge,numberOfQueries);
			System.out.println("\n" + "Round " + serverKnowledge + " Count Only Attack success rate "
					+ ((double) countOnlySuccessRate) / numberOfQueries);

			volumeOnlySuccessRate = VolumeAttack.volumeOnly(serverKnowledge,numberOfQueries);
			System.out.println("Round " + serverKnowledge + " Volume Only Attack success rate "
					+ ((double) volumeOnlySuccessRate) / numberOfQueries);

			volumeCountSuccessRate = VolumeAttack.volumeCount(serverKnowledge,numberOfQueries, 4);
			System.out.println("Round " + serverKnowledge + " Volume Count Attack success rate "
					+ ((double) volumeCountSuccessRate) / numberOfQueries);

			subgraphAPSuccessRate = SubgraphMatch.subgraphAP(serverKnowledge, numberOfQueries, 10);
			System.out.println("Round " + serverKnowledge + " Subgraph AP Attack success rate "
					+ ((double) subgraphAPSuccessRate) / numberOfQueries);

			
			subgraphVPSuccessRate = SubgraphMatch.subgraphVP(serverKnowledge, numberOfQueries, 10);
			System.out.println("Round " + serverKnowledge + " Subgraph VP Attack success rate "
					+ ((double) subgraphVPSuccessRate) / numberOfQueries);

			countOnlyAttack.put(serverKnowledge, ((double) countOnlySuccessRate) / numberOfQueries);
			volumeOnlyAttack.put(serverKnowledge, ((double) volumeOnlySuccessRate) / numberOfQueries);
			volumeCountAttack.put(serverKnowledge, ((double) volumeCountSuccessRate) / numberOfQueries);
			subgraphAPAttack.put(serverKnowledge, ((double) subgraphAPSuccessRate) / numberOfQueries);
			subgraphVPAttack.put(serverKnowledge, ((double) subgraphVPSuccessRate) / numberOfQueries);

			/*
			 * Boolean Queries Attacks
			 */

			if (bool == 1) {

				/**
				 * Generating new Boolean query for every run with new
				 * randomness
				 * 
				 */

				queryKeywordsBoolean = ArrayListMultimap.create();

				int counter = 0;
				while (counter < numberOfQueries) {
					for (int i = 0; i < sizeBool; i++) {
						queryKeywordsBoolean.put(counter, toSelectFrom.get(new Random().nextInt(toSelectFrom.size())));
					}
					boolean flag = true;
					for (int j = 0; j < counter; j++) {
						if (queryKeywordsBoolean.get(j).containsAll(queryKeywordsBoolean.get(counter))) {
							flag = false;
							break;
						}
					}
					if (flag == true) {
						counter++;
					}
				}

				System.out.println("\nSelected Boolean query " + queryKeywordsBoolean);
				printWriter.printf("\n\nSelected Boolean query " + queryKeywordsBoolean);

				subgraphDisjAPSuccessRate = SubgraphMatch.subgraphDisjAP(numberOfQueries, sizeBool);
				System.out.println("\nRound " + serverKnowledge + " and with a query length " + sizeBool
						+ " Subgraph Disj AP Attack success rate "
						+ ((double) subgraphDisjAPSuccessRate) / numberOfQueries);

				subgraphDisjVPSuccessRate = SubgraphMatch.subgraphDisjVP(numberOfQueries, sizeBool);
				System.out.println("Round " + serverKnowledge + " and with a query length " + sizeBool
						+ " Subgraph Disj VP Attack success rate "
						+ ((double) subgraphDisjVPSuccessRate) / numberOfQueries);

				subgraphConjAPSuccessRate = SubgraphMatch.subgraphConjAP2(numberOfQueries, sizeBool);
				System.out.println("Round " + serverKnowledge + " and with a query length " + sizeBool
						+ " Subgraph Conj AP Attack success rate "
						+ ((double) subgraphConjAPSuccessRate) / numberOfQueries);

				subgraphConjVPSuccessRate = SubgraphMatch.subgraphConjVP2(numberOfQueries, sizeBool);
				System.out.println("Round " + serverKnowledge + " and with a query length " + sizeBool
						+ " Subgraph Conj VP Attack success rate "
						+ ((double) subgraphConjVPSuccessRate) / numberOfQueries);
				subgraphDisjAPAttack.put(serverKnowledge, ((double) subgraphDisjAPSuccessRate) / numberOfQueries);
				subgraphDisjVPAttack.put(serverKnowledge, ((double) subgraphDisjVPSuccessRate) / numberOfQueries);
				subgraphConjAPAttack.put(serverKnowledge, ((double) subgraphConjAPSuccessRate) / numberOfQueries);
				subgraphConjVPAttack.put(serverKnowledge, ((double) subgraphConjVPSuccessRate) / numberOfQueries);

			}

		}

		System.out.println("\nCount-only Attack " + countOnlyAttack);
		printWriter.printf("\n\nCount-only Attack " + countOnlyAttack);

		System.out.println("Volume-Only Attack " + volumeOnlyAttack);
		printWriter.printf("\n\nVolume-Only Attack " + volumeOnlyAttack);

		System.out.println("Volume Count Attack " + volumeCountAttack);
		printWriter.printf("\n\nVolume Count Attack " + volumeCountAttack);

		System.out.println("Subgraph Access Pattern-based Attack " + subgraphAPAttack);
		printWriter.printf("\n\nSubgraph Access Pattern-based Attack " + subgraphAPAttack);

		System.out.println("Subgraph Volume Pattern-based Attack " + subgraphVPAttack);
		printWriter.printf("\n\nSubgraph Volume Pattern-based Attack " + subgraphVPAttack);

		System.out.println("Subgraph Disj Access Pattern-based Attack " + subgraphDisjAPAttack);
		printWriter.printf("\n\nSubgraph Disj Access Pattern-based Attack " + subgraphDisjAPAttack);

		System.out.println("Subgraph Disj Volume Pattern-based Attack " + subgraphDisjVPAttack);
		printWriter.printf("\n\nSubgraph Disj Volume Pattern-based Attack " + subgraphDisjVPAttack);

		System.out.println("Subgraph Conj Access Pattern-based Attack " + subgraphConjAPAttack);
		printWriter.printf("\n\nSubgraph Conj Access Pattern-based Attack " + subgraphConjAPAttack);

		System.out.println("Subgraph Conj Volume Pattern-based Attack " + subgraphConjVPAttack);
		printWriter.printf("\n\nSubgraph Conj Volume Pattern-based Attack " + subgraphConjVPAttack);

		printWriter.close();

	}

	public static void loadDataset(String folderName, Integer stopWords, Integer frequency, Integer numberOfKeywords, Integer adjust, Integer remove)
			throws IOException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
			NoSuchProviderException, NoSuchPaddingException, InvalidKeySpecException {

		ArrayList<File> listOfFile = new ArrayList<File>();
		TextProc.listf(folderName, listOfFile);
		TextProc.TextProc(false, stopWords, folderName, remove);

		totaLP1 = ArrayListMultimap.create(TextExtractPar.lp1);
		totaLP2 = ArrayListMultimap.create(TextExtractPar.lp2);
		totaLP3 = new HashMap<String, Long>(TextExtractPar.lp3);
		totaLP4 = ArrayListMultimap.create(TextExtractPar.lp4);
		totaLP5 = ArrayListMultimap.create(TextExtractPar.lp5);

		totalKeywordSpaceAssignment(frequency, numberOfKeywords, adjust);
	}
	
	
	private static void totalKeywordSpaceAssignment(Integer frequency, Integer numberOfKeywords, Integer adjust){
		HashMap<Integer, Integer> countHistogram = new HashMap<Integer, Integer>();

		for (String keyword : TextExtractPar.lp1.keySet()) {
			if (countHistogram.get(TextExtractPar.lp1.get(keyword).size()) == null) {
				countHistogram.put(TextExtractPar.lp1.get(keyword).size(), 1);
			} else {
				Integer tmp = countHistogram.get(TextExtractPar.lp1.get(keyword).size());
				countHistogram.put(TextExtractPar.lp1.get(keyword).size(), tmp + 1);
			}
		}

		// We sort the histogram based on its counters
		TreeMap<Integer, Integer> sortedCountHistogram = new TreeMap<Integer, Integer>();
		for (Integer size : countHistogram.keySet()) {
			sortedCountHistogram.put(size, countHistogram.get(size));
		}

		if (frequency == 0) {

			Integer filterSize = 0;
			Integer tmpSize = 0;
			for (Integer value : sortedCountHistogram.descendingKeySet()) {
				if (tmpSize >= numberOfKeywords) {
					filterSize = value+1;
					break;
				} else {
					tmpSize = tmpSize + sortedCountHistogram.get(value);
				}
			}
			if (filterSize == 0) {
				System.out.println("No filtering occured");
				return;
			}

			keywordsFilterTotal = new ArrayList<String>();
			for (String keyword : TextExtractPar.lp1.keySet()) {
				if (TextExtractPar.lp1.get(keyword).size() >= filterSize){ 
					keywordsFilterTotal.add(keyword);
				}

			}
		} else if (frequency == 1) {
			Integer filterSize = 0;
			Integer tmpSize = 0;
			for (Integer value : sortedCountHistogram.keySet()) {
				if (tmpSize >= numberOfKeywords) {
					filterSize = value-1;
					break;
				} else {
					tmpSize = tmpSize + sortedCountHistogram.get(value);
				}

			}
			if (filterSize == 0) {
				System.out.println("No filtering occured for " + frequency);
				return;
			}

			keywordsFilterTotal = new ArrayList<String>();
			for (String keyword : TextExtractPar.lp1.keySet()) {
				if (TextExtractPar.lp1.get(keyword).size() <= filterSize){
					keywordsFilterTotal.add(keyword);
					
				}

			}
		}
		
		if (adjust == 1){
		int toRemove = keywordsFilterTotal.size() - numberOfKeywords;
			while (toRemove >0){
				int random = new Random().nextInt(keywordsFilterTotal.size());
				keywordsFilterTotal.remove(random);
				toRemove--;
			}
		}
		
	}
	
	
	public static void randomQueriesGeneration(int numberOfQueries, List<String> toSelectFrom){
		queryKeywords = new ArrayList<String>();
		TreeMap<Integer, String> tempQuery = new TreeMap<Integer, String>();
		for (int i = 0; i < toSelectFrom.size(); i++) {
			int random = new Random().nextInt((int) Math.pow(2, 32));
			tempQuery.put(random, toSelectFrom.get(i));
		}

		int counter = 0;
		for (Integer value : tempQuery.keySet()) {
			if (counter >= numberOfQueries) {
				break;
			}
			queryKeywords.add(tempQuery.get(value));
			counter++;
		}
	}
}
