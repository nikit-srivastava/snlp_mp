package snlp.mp.main;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.mashape.unirest.http.exceptions.UnirestException;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.simple.Sentence;
import snlp.mp.dbps.DBPResource;
import snlp.mp.dbps.NERProvider;
import snlp.mp.dict.WordNetExpansion;
import snlp.mp.io.IOHandler;
import snlp.mp.misc.SNLPUtil;
import snlp.mp.scnlp.NLPEntity;
import snlp.mp.scnlp.NLPProvider;
import snlp.mp.scnlp.NLPTriple;
import snlp.mp.sparql.RelationHandler;

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
		/*
		 * Path path = Paths.get("C:\\Users\\Nikit\\Downloads\\test.tsv"); List<String>
		 * ttLines = new ArrayList<String>(); Files.lines(path).forEachOrdered(s ->
		 * printOneLine(s));
		 */
		/*
		 * BufferedReader br = new BufferedReader( new InputStreamReader(new
		 * FileInputStream("C:\\Users\\Nikit\\Downloads\\test.tsv"), "utf-8")); String s
		 * = br.readLine(); String s2 = br.readLine(); String data[] = s2.split("\t");
		 * Properties props = new Properties(); props.setProperty("annotators",
		 * "tokenize, ssplit, pos, lemma, ner, parse, depparse, openie");
		 * props.setProperty("ner.useSUTime", "0"); Sentence sent = new
		 * Sentence("Chris Brown (American entertainer) is Charlie Sheen's better half."
		 * , props); SemanticGraph sg = sent.dependencyGraph();
		 */
		printRootNodes();
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

	public static void printRootNodes() throws IOException {
		System.out.println("Process Started.");
		IOHandler ioHandler = null;
		Set<String> tags = new HashSet<>();
		try {

			ioHandler = new IOHandler("C:\\Users\\Nikit\\Downloads\\test.tsv",
					"C:\\Users\\Nikit\\Desktop\\outputNNP.tsv");
			while (true) {
				String[] doc = ioHandler.getNextDoc();
				if (doc == null)
					break;
				String id = doc[0];
				String fact = doc[1];
				getFactScore(id, fact, ioHandler);
			}
			// ioHandler.getWriter().write(tags.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (ioHandler != null)
				ioHandler.closeIO();
			System.out.println("Process Finished.");
		}
	}

	public static int getFactScore(String id, String fact, IOHandler ioHandler) throws IOException, UnirestException {
		int fScore = 0;
		// Run NER on the fact
		NERProvider nerProvider = new NERProvider(fact);
		Map<String, DBPResource> entityMap = nerProvider.getEntityMap();
		if(entityMap.isEmpty())
			return fScore;
		// Get dependencyGraph of the fact
		Sentence sent = new Sentence(fact, props);
		SemanticGraph sg = sent.dependencyGraph();
		// find subject relation and object by matching against entities
		String tempTag;
		NLPEntity subject = null;
		NLPEntity object = null;
		String relation = null;
		DBPResource tempRes;
		for (IndexedWord rootWord : sg.getRoots()) {
			tempTag = rootWord.tag();
			//NNP root handling
			if (tempTag.matches("NNP[S]?")) {
				// tags.add(tempTag);
				// save object
				tempRes = findMatchingRes(rootWord.originalText(), entityMap);
				if (tempRes == null) {
					// Find the compounded string
					object = new NLPEntity(NLPProvider.getCompoundStr(rootWord, sg), null, false);
				} else {
					object = new NLPEntity(tempRes.getSurfaceForm(), tempRes.getUri(), true);
				}
				for (SemanticGraphEdge edge : sg.getOutEdgesSorted(rootWord)) {
					if (edge.getRelation().toString().equalsIgnoreCase("nsubj")) {
						IndexedWord relNode = edge.getTarget();
						// save relation
						relation = NLPProvider.getCompoundStr(relNode, sg);
						// find subject
						for (SemanticGraphEdge subEdge : sg.getOutEdgesSorted(relNode)) {
							if (subEdge.getRelation().toString().equalsIgnoreCase("nmod:poss")) {
								// save subj
								IndexedWord subjNode = subEdge.getTarget();
								tempRes = findMatchingRes(subjNode.originalText(), entityMap);
								if (tempRes == null) {
									// Find the compounded string
									subject = new NLPEntity(NLPProvider.getCompoundStr(subjNode, sg), null, false);
								} else {
									subject = new NLPEntity(tempRes.getSurfaceForm(), tempRes.getUri(), true);
								}
							}
						}
					}
				}
			}
		}
		// Find the relation between entities on DBPedia
		NLPTriple nlpTriple = new NLPTriple(subject, relation, object);
		if (subject != null) {
			// Find all the relations of subject and object in case of failure
			RelationHandler relationHandler = new RelationHandler(nlpTriple);
			// Check if synonymous and check for negation
			Map<String, List<String>> sampleMap = relationHandler.getSampleMap();
			if(sampleMap.isEmpty())
				return 0;
			// synonym check to be done with wordnet
			WordNetExpansion wne = new WordNetExpansion(
					"C:\\Users\\Nikit\\Downloads\\SPARQL2NL-master\\resources\\wordnet\\dict");
			double similarity = wne.getExpandedJaccardSimilarityAdv(relation, sampleMap.get(relation));
			// save the ID and result in the output file
			System.out.println(similarity);

		}
		return fScore;
	}

	public static DBPResource findMatchingRes(String qVal, Map<String, DBPResource> entityMap) {
		DBPResource res = null;
		for (String key : entityMap.keySet()) {
			if (key.matches(".*" + qVal + ".*"))
				return entityMap.get(key);
		}
		return res;
	}

	/*
	 * ioHandler.getWriter().write(id + "\t" + rootWord + ":\t" + fact);
	 * ioHandler.getWriter().newLine();
	 */

}
