package snlp.mp.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.mashape.unirest.http.exceptions.UnirestException;

import snlp.mp.io.IOHandler;
import snlp.mp.misc.SNLPUtil;

/**
 * Hello world!
 *
 */
public class App {
	private static Properties props;
	static {
		props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, depparse, openie");
		props.setProperty("ner.useSUTime", "0");
	}

	public static void main(String[] args) throws IOException, UnirestException {
		//runOnTrainData();
		//runOnTestData();
		//runOnDemoData();
		runOnTestDataCF();
	}

	public static void process(String id, String fact) {
		// Run NER on the fact
		// Get dependencyGraph of the fact
		// find subject relation and object by matching against entities
		// Find the relation between entities on DBPedia
		// Find all the relations of subject and object in case of failure
		// Check if synonymous and check for negation
		// synonym check to be done with wordnet
		// save the ID and result in the output file
	}

	public static void runOnTrainData() throws IOException {
		System.out.println("Process Started.");
		int correctCount = 0;
		int count = 0;
		IOHandler ioHandler = null;
		try {

			ioHandler = new IOHandler("C:\\Users\\Nikit\\Downloads\\train.tsv",
					"C:\\Users\\Nikit\\Desktop\\result.ttl");
			FactChecker checker;
			String tempLine;
			while (true) {
				String[] doc = ioHandler.getNextDoc();
				if (doc == null)
					break;
				String id = doc[0];
				String fact = doc[1];
				checker = new FactChecker(id, fact);
				boolean marker = Boolean.parseBoolean(doc[2]);
				int result = checker.checkFact();
				tempLine = SNLPUtil.getFormattedOutput(id, result);
				ioHandler.getWriter().write(tempLine);
				ioHandler.getWriter().newLine();
				if (result != 0) {
					if ((result == -1 && !marker) || (result == 1 && marker))
						correctCount++;
				}
				System.out.print("\rRecords Processed: " + (++count));
			}
			System.out.println("\nCorrectly Identified : " + correctCount + " facts.");
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (ioHandler != null)
				ioHandler.closeIO();
			System.out.println("Process Finished.");
		}
	}
	
	public static void runOnTestData() throws IOException {
		System.out.println("Process Started.");
		int count = 0;
		IOHandler ioHandler = null;
		try {

			ioHandler = new IOHandler("C:\\Users\\Nikit\\Downloads\\test.tsv",
					"C:\\Users\\Nikit\\Desktop\\resultTest3.ttl");
			FactChecker checker;
			String tempLine;
			while (true) {
				String[] doc = ioHandler.getNextDoc();
				if (doc == null)
					break;
				String id = doc[0];
				String fact = doc[1];
				checker = new FactChecker(id, fact);
				int result = checker.checkFact();
				tempLine = SNLPUtil.getFormattedOutput(id, result);
				ioHandler.getWriter().write(tempLine);
				ioHandler.getWriter().newLine();
				System.out.print("\rRecords Processed: " + (++count));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (ioHandler != null)
				ioHandler.closeIO();
			System.out.println("Process Finished.");
		}
	}
	
	public static void runOnTestDataCF() throws IOException {
		System.out.println("Process Started.");
		int count = 0;
		IOHandler ioHandler = null;
		List<Double> posScores = new ArrayList<>();
		List<String> negFactIds = new ArrayList<>();
		try {

			ioHandler = new IOHandler("C:\\Users\\Nikit\\Downloads\\test.tsv",
					"C:\\Users\\Nikit\\Desktop\\resultTestCF3.ttl");
			FactChecker checker;
			String tempLine;
			while (true) {
				String[] doc = ioHandler.getNextDoc();
				if (doc == null)
					break;
				String id = doc[0];
				String fact = doc[1];
				checker = new FactChecker(id, fact);
				double result = checker.checkFact();
				if(result>0) {
					result = checker.getSimilarity();
					posScores.add(result);
				}
				if(result>=0) {
					tempLine = SNLPUtil.getFormattedOutput(id, result);
					ioHandler.getWriter().write(tempLine);
					ioHandler.getWriter().newLine();
				}else {
					negFactIds.add(id);
				}
				System.out.print("\rRecords Processed: " + (++count));
			}
			double avg = SNLPUtil.getNegAvgVal(posScores);
			for(String negFactId : negFactIds) {
				tempLine = SNLPUtil.getFormattedOutput(negFactId, avg);
				ioHandler.getWriter().write(tempLine);
				ioHandler.getWriter().newLine();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (ioHandler != null)
				ioHandler.closeIO();
			System.out.println("Process Finished.");
		}
	}
	
	public static void runOnDemoData() throws IOException {
		System.out.println("Process Started.");
		int count = 0;
		IOHandler ioHandler = null;
		try {

			ioHandler = new IOHandler("C:\\Users\\Nikit\\Desktop\\undecidedOutput.tsv",
					"C:\\Users\\Nikit\\Desktop\\uoOut.ttl");
			FactChecker checker;
			String tempLine;
			while (true) {
				String[] doc = ioHandler.getNextDoc();
				if (doc == null)
					break;
				String id = doc[0];
				String fact = doc[1];
				checker = new FactChecker(id, fact);
				int result = checker.checkFact();
				tempLine = SNLPUtil.getFormattedOutput(id, result);
				ioHandler.getWriter().write(tempLine);
				ioHandler.getWriter().newLine();
				System.out.print("\rRecords Processed: " + (++count));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (ioHandler != null)
				ioHandler.closeIO();
			System.out.println("Process Finished.");
		}
	}

}
