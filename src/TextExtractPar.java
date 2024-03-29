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


import com.google.common.base.Charsets;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.xslf.extractor.XSLFPowerPointExtractor;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.xmlbeans.XmlException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.*;

public class TextExtractPar implements Serializable {

	public static int lengthStrings = 0;
	public static int totalNumberKeywords = 0;
	public static int maxTupleSize = 0;
	public static int threshold = 100;
	public static int removeNumbers = 0;
	public static ConcurrentHashMap<String,Long> idToVol = new ConcurrentHashMap<String,Long>();

	static CharArraySet noise = CharArraySet.copy(EnglishAnalyzer.getDefaultStopSet());
	
	
	// lookup1 stores a plaintext inverted index of the dataset, i.e., the
	// association between the keyword and documents that contain the keyword

	Multimap<String, String> lookup1 = ArrayListMultimap.create();
	static Multimap<String, String> lp1 = ArrayListMultimap.create();

	// lookup2 stores the document identifier (title) and the keywords contained
	// in this document

	Multimap<String, String> lookup2 = ArrayListMultimap.create();
	static Multimap<String, String> lp2 = ArrayListMultimap.create();

	// lookup3 & lp4 stores the keyword and the set of all documents lengths (volumes) containing the keyword
	// lp3 stores the keyword and the sum (in bits) of the documents lengths containing the keyword

	Multimap<String, Long> lookup3 = ArrayListMultimap.create();
	static HashMap<String, Long> lp3 = new HashMap<String, Long>();
	

	static Multimap<String, Long> lp4 = ArrayListMultimap.create();
	
	// lookup5 & lp5 stores the mapping between a volume and the keywords contained in documents with the same volume
	
	Multimap<Long, String> lookup5 = ArrayListMultimap.create();
	static Multimap<Long, String> lp5 = ArrayListMultimap.create();
	
	static int counter = 0;

	public TextExtractPar(Multimap<String, String> lookup1, Multimap<String, String> lookup2, 
			Multimap<String, Long> lookup3, Multimap<Long, String> lookup5) {
		this.lookup1 = lookup1;
		this.lookup2 = lookup2;
		this.lookup3 = lookup3;
		this.lookup5 = lookup5;

	}

	public Multimap<String, String> getL1() {
		return this.lookup1;
	}

	public Multimap<String, String> getL2() {
		return this.lookup2;
	}
	
	public Multimap<String, Long> getL3() {
		return this.lookup3;
	}
	
	public Multimap<Long, String> getL5() {
		return this.lookup5;
	}
	

	public static void extractTextPar(ArrayList<File> listOfFile, Integer stop, Integer remove)
			throws InterruptedException, ExecutionException, IOException {
		
		
		if(stop == 0){
			noise = EnglishAnalyzer.getDefaultStopSet();

		}
		else if (stop == 1){	    
			Scanner sc2 = null;
		    try {
		        sc2 = new Scanner(new File("/Users/tarikmoataz/Documents/workspace/ORAMAttacks/stopWordsSonwBallLucene.txt"));
		    } catch (FileNotFoundException e) {
		        e.printStackTrace();  
		    }
		    while (sc2.hasNextLine()) {
		            Scanner s2 = new Scanner(sc2.nextLine());
		        while (s2.hasNext()) {
		            String s = s2.next();
		            noise.add(s);
		        }
		    }
		}
		
		removeNumbers = remove;
		
		
		int threads = 0;
		if (Runtime.getRuntime().availableProcessors() > listOfFile.size()) {
			threads = listOfFile.size();
		} else {
			threads = Runtime.getRuntime().availableProcessors();
		}

		ExecutorService service = Executors.newFixedThreadPool(threads);
		ArrayList<File[]> inputs = new ArrayList<File[]>(threads);


		for (int i = 0; i < threads; i++) {
			File[] tmp;
			if (i == threads - 1) {
				tmp = new File[listOfFile.size() / threads + listOfFile.size() % threads];
				for (int j = 0; j < listOfFile.size() / threads + listOfFile.size() % threads; j++) {
					tmp[j] = listOfFile.get((listOfFile.size() / threads) * i + j);
				}
			} else {
				tmp = new File[listOfFile.size() / threads];
				for (int j = 0; j < listOfFile.size() / threads; j++) {

					tmp[j] = listOfFile.get((listOfFile.size() / threads) * i + j);
				}
			}
			inputs.add(i, tmp);
		}

		List<Future<TextExtractPar>> futures = new ArrayList<Future<TextExtractPar>>();
		for (final File[] input : inputs) {
			Callable<TextExtractPar> callable = new Callable<TextExtractPar>() {
				public TextExtractPar call() throws Exception {
					TextExtractPar output = extractOneDoc(input);

					return output;
				}
			};
			futures.add(service.submit(callable));
		}

		service.shutdown();
		
		service.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

		

		for (Future<TextExtractPar> future : futures) {
			Set<String> keywordSet1 = future.get().getL1().keySet();
			Set<String> keywordSet2 = future.get().getL2().keySet();
			Set<Long> volumeSet = future.get().getL5().keySet();

			for (String key : keywordSet1) {
				lp1.putAll(key, future.get().getL1().get(key));
				lp4.putAll(key, future.get().getL3().get(key));

				Collection<Long> sizeFiles = future.get().getL3().get(key);
				Long tempSize = 0L;
				if (lp3.get(key) != null){
					tempSize = lp3.get(key);
				}

				for (Long size : sizeFiles){
					tempSize = tempSize+size;
				}
				lp3.put(key, tempSize);

				if (lp1.get(key).size()>maxTupleSize){
					maxTupleSize= lp1.get(key).size();
				}
			}
			for (String key : keywordSet2) {
				lp2.putAll(key, future.get().getL2().get(key));
			}
			
			for (Long volume : volumeSet) {
				lp5.putAll(volume, future.get().getL5().get(volume));
			}
		}

	}

	private static TextExtractPar extractOneDoc(File[] listOfFile) throws IOException {

		Multimap<String, String> lookup1 = ArrayListMultimap.create();
		Multimap<String, String> lookup2 = ArrayListMultimap.create();
		Multimap<String, Long> lookup3 = ArrayListMultimap.create();
		Multimap<Long, String> lookup5 = ArrayListMultimap.create();

		for (File file : listOfFile) {

			for (int j = 0; j < 100; j++) {

				if (counter == (int) ((j + 1) * listOfFile.length / 100)) {
					System.out.println("Number of files read equals " + j + " %");
					break;
				}
			}

			List<String> lines = new ArrayList<String>();
			counter++;
			FileInputStream fis = new FileInputStream(file);

			// ***********************************************************************************************//

			///////////////////// .docx /////////////////////////////

			// ***********************************************************************************************//

			if (file.getCanonicalPath().endsWith(".docx")) {
				XWPFDocument doc;
				try {

					doc = new XWPFDocument(fis);
					XWPFWordExtractor ex = new XWPFWordExtractor(doc);
					lines.add(ex.getText());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("File not read: " + file.getCanonicalPath());
				}

			}

			// ***********************************************************************************************//

			///////////////////// .pptx /////////////////////////////

			// ***********************************************************************************************//

			else if (file.getCanonicalPath().endsWith(".pptx")) {

				OPCPackage ppt;
				try {

					ppt = OPCPackage.open(fis);
					XSLFPowerPointExtractor xw = new XSLFPowerPointExtractor(ppt);
					lines.add(xw.getText());
				} catch (XmlException e) {
					// TODO Auto-generated catch block
					System.out.println("File not read: " + file.getCanonicalPath());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("File not read: " + file.getCanonicalPath());
				} catch (OpenXML4JException e) {
					System.out.println("File not read: " + file.getCanonicalPath());
				}

			}

			// ***********************************************************************************************//

			///////////////////// .xlsx /////////////////////////////

			// ***********************************************************************************************//

			else if (file.getCanonicalPath().endsWith(".xlsx")) {

				OPCPackage xls;
				try {

					xls = OPCPackage.open(fis);
					XSSFExcelExtractor xe = new XSSFExcelExtractor(xls);
					lines.add(xe.getText());
				} catch (InvalidFormatException e) {
					// TODO Auto-generated catch block
					System.out.println("File not read: " + file.getCanonicalPath());
				} catch (IOException e) {
					System.out.println("File not read: " + file.getCanonicalPath());

				} catch (XmlException e) {
					// TODO Auto-generated catch block
					System.out.println("File not read: " + file.getCanonicalPath());
				} catch (OpenXML4JException e) {
					System.out.println("File not read: " + file.getCanonicalPath());
				}

			}

			// ***********************************************************************************************//

			///////////////////// .doc /////////////////////////////

			// ***********************************************************************************************//

			else if (file.getCanonicalPath().endsWith(".doc")) {

				NPOIFSFileSystem fs;
				try {

					fs = new NPOIFSFileSystem(file);
					WordExtractor extractor = new WordExtractor(fs.getRoot());
					for (String rawText : extractor.getParagraphText()) {
						lines.add(extractor.stripFields(rawText));
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("File not read: " + file.getCanonicalPath());
				}

			}

			// ***********************************************************************************************//

			///////////////////// .pdf /////////////////////////////

			// ***********************************************************************************************//

			else if (file.getCanonicalPath().endsWith(".pdf")) {

				PDFParser parser;
				try {

					parser = new PDFParser(fis);
					parser.parse();
					COSDocument cd = parser.getDocument();
					PDFTextStripper stripper = new PDFTextStripper();
					lines.add(stripper.getText(new PDDocument(cd)));

				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("File not read: " + file.getCanonicalPath());
				}

			}

			// ***********************************************************************************************//

			///////////////////// Media Files such as gif, jpeg, .wmv, .mpeg,
			///////////////////// .mp4 /////////////////////////////

			// ***********************************************************************************************//

			else if (file.getCanonicalPath().endsWith(".gif") && file.getCanonicalPath().endsWith(".jpeg")
					&& file.getCanonicalPath().endsWith(".wmv") && file.getCanonicalPath().endsWith(".mpeg")
					&& file.getCanonicalPath().endsWith(".mp4")) {

				lines.add(file.getName());

			}

			// ***********************************************************************************************//

			///////////////////// raw text extensions
			///////////////////// /////////////////////////////

			// ***********************************************************************************************//

			else {
				try {

					lines = Files.readLines(file, Charsets.UTF_8);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("File not read: " + file.getCanonicalPath());
				} finally {
					try {
						fis.close();
					} catch (IOException ioex) {
						// omitted.
					}
				}
			}

			// ***********************************************************************************************//

			///////////////////// Begin word extraction
			///////////////////// /////////////////////////////

			// ***********************************************************************************************//

			int temporaryCounter = 0;
			
			//Add the volume information
			
			idToVol.put(file.getCanonicalPath(), file.length());
			

			// Filter threshold
			int counterDoc = 0;
			for (int i = 0; i < lines.size(); i++) {


				Analyzer analyzer = new StandardAnalyzer(noise);
				List<String> token0 = Tokenizer.tokenizeString(analyzer, lines.get(i));
				List<String> token = new ArrayList<String>();
				//removing numbers/1-letter keywords
				if (removeNumbers == 1){
					for (int j=0; j<token0.size();j++){
						if ((!token0.get(j).matches(".*\\d+.*")
								&&
								(token0.get(j)).length() >1)){
							token.add(token0.get(j));
						}
					}
				}
				else{
					token =token0;
				}
				
				temporaryCounter = temporaryCounter + token.size();
				for (int j = 0; j < token.size(); j++) {

					// Avoid counting occurrences of words in the same file
					if (!lookup2.get(file.getCanonicalPath()).contains(token.get(j))) {
						lookup2.put(file.getCanonicalPath(), token.get(j));
						lookup5.put(file.length(), token.get(j));
					}

					// Avoid counting occurrences of words in the same file
					if (!lookup1.get(token.get(j)).contains(file.getCanonicalPath())) {
						lookup1.put(token.get(j), file.getCanonicalPath());
						lookup3.put(token.get(j), file.length());
					}

				}

			}

		}

		return new TextExtractPar(lookup1, lookup2, lookup3, lookup5);

	}

}
