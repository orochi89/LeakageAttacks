import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VolumeAttack {

	public static int volumeOnly(int iteration, Integer numberOfQueries) {
		HashMap<Integer, Long> volumeVector = new HashMap<Integer, Long>();
		int counter2 = 0;
		for (String keyword : Evaluator.queryKeywords) {
			volumeVector.put(counter2, Evaluator.totaLP3.get(keyword));
			counter2++;
		}

		HashMap<Integer, String> volumeAttackMap = new HashMap<Integer, String>();

		for (int i = 0; i < numberOfQueries; i++) {
			int volume = volumeVector.get(i).intValue();
			List<Long> tempList = new ArrayList<Long>();
			for (int h = volume; h >= Math.ceil(iteration * volume / 100); h--) {
				Long tempVolume = (long) h;
				if (!Evaluator.volumeKeywords.get(tempVolume).isEmpty()) {
					tempList.add(tempVolume);
				}
			}

			if (tempList.size() == 0) {
				Long tempVolume = (long) Math.ceil(iteration * volume / 100);
				while (tempList.isEmpty()) {
					if (!Evaluator.volumeKeywords.get(tempVolume).isEmpty()) {
						tempList.add(tempVolume);
					} else if (tempVolume < 0) {
						break;
					}
					tempVolume--;
				}
			}

			if (tempList.size() > 0) {
				volumeAttackMap.put(i, Evaluator.volumeKeywords.get(tempList.get(0)).iterator().next());
			}
		}

		int volumeSuccessRateTMP = 0;
		for (Integer value : volumeAttackMap.keySet()) {
			if (volumeAttackMap.get(value).equals(Evaluator.queryKeywords.get(value))) {
				volumeSuccessRateTMP++;
			}
		}
		return volumeSuccessRateTMP;
	}

	public static int volumeCount(int iteration, int numberOfQueries, int rate) {

		HashMap<Integer, String> volumeCountAttackMap = new HashMap<Integer, String>();

		HashMap<Integer, Integer> countVector = new HashMap<Integer, Integer>();
		int counter3 = 0;
		for (String keyword : Evaluator.queryKeywords) {
			countVector.put(counter3, Evaluator.totaLP1.get(keyword).size());
			counter3++;
		}

		HashMap<Integer, Long> volumeVector = new HashMap<Integer, Long>();
		int counter2 = 0;
		for (String keyword : Evaluator.queryKeywords) {
			volumeVector.put(counter2, Evaluator.totaLP3.get(keyword));
			counter2++;
		}

		for (int i = 0; i < numberOfQueries; i++) {
			int volume = volumeVector.get(i).intValue();
			List<String> tempList1 = new ArrayList<String>();
			for (int h = volume; h >= Math.ceil(iteration * volume / 100); h--) {
				Long tempVolume = (long) h;
				if (!Evaluator.volumeKeywords.get(tempVolume).isEmpty()) {
					tempList1.addAll(Evaluator.volumeKeywords.get(tempVolume));
				}
			}

			if (tempList1.size() == 0) {
				Long tempVolume = (long) Math.ceil(iteration * volume / 100);
				while (tempList1.isEmpty()) {
					if (!Evaluator.volumeKeywords.get(tempVolume).isEmpty()) {
						tempList1.addAll(Evaluator.volumeKeywords.get(tempVolume));
					} else if (tempVolume < 0) {
						break;
					}
					tempVolume--;
				}
			}

			int selectivity = countVector.get(i);

			List<String> tempList2 = new ArrayList<String>();

			for (String keyword : tempList1) {
				Long volumeDifference = Evaluator.totaLP3.get(Evaluator.queryKeywords.get(i))
						- TextExtractPar.lp3.get(keyword);
				Integer estimatedSelectivity = selectivity - (int) (volumeDifference / Evaluator.averageFileSize);
				Integer tempSelectivity = TextExtractPar.lp1.get(keyword).size();
				double epsilon = (1 - iteration / 100) * selectivity / rate;
				if ((tempSelectivity <= selectivity) && (tempSelectivity >= estimatedSelectivity - epsilon)) {

					tempList2.add(keyword);

				}
			}

			if (tempList2.size() > 0) {
				volumeCountAttackMap.put(i, tempList2.get(0));
			}
		}

		int volumeCountSuccessRateTMP = 0;
		for (Integer value : volumeCountAttackMap.keySet()) {
			if (volumeCountAttackMap.get(value).equals(Evaluator.queryKeywords.get(value))) {
				volumeCountSuccessRateTMP++;
			}
		}
		return volumeCountSuccessRateTMP;

	}

}
