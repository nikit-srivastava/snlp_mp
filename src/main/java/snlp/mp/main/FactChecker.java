package snlp.mp.main;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.mashape.unirest.http.exceptions.UnirestException;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.simple.Sentence;
import snlp.mp.dbps.DBPResource;
import snlp.mp.dbps.NERProvider;
import snlp.mp.dict.WordNetExpansion;
import snlp.mp.misc.Consts;
import snlp.mp.misc.SNLPUtil;
import snlp.mp.scnlp.NLPEntity;
import snlp.mp.scnlp.NLPProvider;
import snlp.mp.scnlp.NLPTriple;
import snlp.mp.sparql.RelationHandler;

public class FactChecker {

	private static Properties props;
	static {
		props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, depparse, openie");
		props.setProperty("ner.useSUTime", "0");
	}
	private NLPEntity subject;
	private NLPEntity object;
	private String relation;
	private Map<String, DBPResource> entityMap;
	private SemanticGraph sg;

	private String factId;
	private String fact;

	public FactChecker(String factId, String fact) {
		super();
		this.factId = factId;
		this.fact = fact;
	}

	// Method that returns confidence value
	public int checkFact() throws UnirestException {
		int cValue = 0;
		// Run NER on the fact
		NERProvider nerProvider = new NERProvider(fact);
		entityMap = nerProvider.getEntityMap();
		if (entityMap.isEmpty())
			return cValue;
		// Get dependencyGraph of the fact
		Sentence sent = new Sentence(fact, props);
		sg = sent.dependencyGraph();
		// find subject relation and object by matching against entities
		processSG();
		NLPTriple nlpTriple = new NLPTriple(subject, relation, object);
		if (subject != null && relation != null && object != null) {
			// Find all the relations of subject and object in case of failure
			RelationHandler relationHandler = new RelationHandler(nlpTriple);
			// Check if synonymous and check for negation
			Map<String, List<String>> sampleMap = relationHandler.getSampleMap();
			if (sampleMap.isEmpty())
				return 0;
			// synonym check to be done with wordnet
			WordNetExpansion wne = new WordNetExpansion(Consts.WN_DPATH);
			double similarity = wne.getExpandedJaccardSimilarityAdv(relation, sampleMap.get(relation));
			// save the ID and result in the output file
			if (similarity > 0)
				cValue = 1;
			else
				cValue = -1;
		}
		return cValue;
	}

	private void processSG() {
		String tempTag;
		for (IndexedWord rootWord : sg.getRoots()) {
			tempTag = rootWord.tag();
			// NNP root handling
			if (tempTag.matches("NNP[S]?")) {
				processNNPRoot(rootWord);
			}
			// VBZ root handling
			else if (tempTag.equalsIgnoreCase("VBZ")) {
				processVBZRoot(rootWord);
			}
			// VBG root handling
			else if (tempTag.equalsIgnoreCase("VBG")) {
				processVBGRoot(rootWord);
			}
			// VB root handling
			else if (tempTag.equalsIgnoreCase("VB")) {
				processVBRoot(rootWord);
			}
			// NN root handling
			else if (tempTag.equalsIgnoreCase("NN")) {
				processNNRoot(rootWord);
			}
			// NNS root handling
			else if (tempTag.equalsIgnoreCase("NNS")) {
				processNNSRoot(rootWord);
			}
		}
	}

	private void processVBZRoot(IndexedWord rootWord) {
		DBPResource tempRes;
		// save relation
		//find nsubj
		//save subject
		//find dobj on relation and save object
	}

	private void processVBGRoot(IndexedWord rootWord) {
		DBPResource tempRes;
		// save relation
		//find nsubj
		//save subject
		//find dobj on relation and save object

	}

	private void processVBRoot(IndexedWord rootWord) {
		DBPResource tempRes;
		// save relation
		//find nsubj
		//save subject
		//find dobj on relation and save object

	}

	private void processNNRoot(IndexedWord rootWord) {
		DBPResource tempRes;
		// save relation
		//find nsubj
		//save subject
		//find dobj on relation and save object

	}

	private void processNNSRoot(IndexedWord rootWord) {
		DBPResource tempRes;
		// save relation
		//find nsubj
		//save subject
		//find dobj on relation and save object

	}

	private void processNNPRoot(IndexedWord rootWord) {
		DBPResource tempRes;
		// save object
		tempRes = SNLPUtil.findMatchingRes(rootWord.originalText(), entityMap);
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
						tempRes = SNLPUtil.findMatchingRes(subjNode.originalText(), entityMap);
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
