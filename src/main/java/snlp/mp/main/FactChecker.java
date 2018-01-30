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
		IndexedWord tempWord;
		// save relation
		relation = NLPProvider.getCompoundStr(rootWord, sg);
		for (SemanticGraphEdge edge : sg.getOutEdgesSorted(rootWord)) {
			tempWord = edge.getTarget();
			// find nsubj
			if (edgeMatchRel(edge, "nsubj")) {
				// save subject
				subject = getEntityObj(tempWord);
			}
			// find dobj on relation
			if (edgeMatchRel(edge, "dobj")) {
				// save object
				object = getEntityObj(tempWord);
			}
		}
	}

	private void processVBGRoot(IndexedWord rootWord) {
		IndexedWord tempWord;
		// save object
		object = getEntityObj(rootWord);
		for (SemanticGraphEdge edge : sg.getOutEdgesSorted(rootWord)) {
			tempWord = edge.getTarget();
			// find nsubj
			if (edgeMatchRel(edge, "nsubj")) {
				// save subject
				subject = getEntityObj(tempWord);
			}
			// find dobj on object
			if (edgeMatchRel(edge, "dobj")) {
				// save relation
				relation = NLPProvider.getCompoundStr(tempWord, sg);
			}
		}

	}

	private void processVBRoot(IndexedWord rootWord) {
		IndexedWord tempWord;
		IndexedWord subTempWord;
		String subTag;
		// save subject
		subject = getEntityObj(rootWord);
		
		for (SemanticGraphEdge edge : sg.getOutEdgesSorted(rootWord)) {
			tempWord = edge.getTarget();
			// find dobj
			if (edgeMatchRel(edge, "dobj")) {
				// save object
				object = getEntityObj(tempWord);
				for (SemanticGraphEdge subEdge : sg.getOutEdgesSorted(tempWord)) {
					subTempWord = subEdge.getTarget();
					subTag = subTempWord.tag();
					// find edge leading to other than NNP or DET on object
					if (!(subTag.equalsIgnoreCase("NNP") || subTag.equalsIgnoreCase("DET"))) {
						// save relation
						relation = NLPProvider.getCompoundStr(subTempWord, sg);
					}
				}
			}
		}
	}

	private void processNNRoot(IndexedWord rootWord) {

		IndexedWord tempWord;
		// save relation
		relation = NLPProvider.getCompoundStr(rootWord, sg);
		for (SemanticGraphEdge edge : sg.getOutEdgesSorted(rootWord)) {
			tempWord = edge.getTarget();
			// find nsubj
			if (edgeMatchRel(edge, "nsubj")) {
				// save object
				object = getEntityObj(tempWord);
			}
			// find nmod:poss on relation
			if (edgeMatchRel(edge, "nmod:poss")) {
				// save subject
				subject = getEntityObj(tempWord);
			}
		}

	}

	private void processNNSRoot(IndexedWord rootWord) {

		IndexedWord tempWord;
		// save object
		object = getEntityObj(rootWord);
		for (SemanticGraphEdge edge : sg.getOutEdgesSorted(rootWord)) {
			tempWord = edge.getTarget();
			// find nsubj
			if (edgeMatchRel(edge, "nsubj")) {
				// save relation
				relation = NLPProvider.getCompoundStr(tempWord, sg);
				// find nmod:poss on relation
				for (SemanticGraphEdge subEdge : sg.getOutEdgesSorted(tempWord)) {
					if (edgeMatchRel(subEdge, "nmod:poss")) {
						// save subj
						IndexedWord subjNode = subEdge.getTarget();
						subject = getEntityObj(subjNode);
					}
				}
			}
		}

	}

	private void processNNPRoot(IndexedWord rootWord) {
		// save object
		object = getEntityObj(rootWord);
		for (SemanticGraphEdge edge : sg.getOutEdgesSorted(rootWord)) {
			if (edgeMatchRel(edge, "nsubj")) {
				IndexedWord relNode = edge.getTarget();
				// save relation
				relation = NLPProvider.getCompoundStr(relNode, sg);
				// find subject
				for (SemanticGraphEdge subEdge : sg.getOutEdgesSorted(relNode)) {
					if (edgeMatchRel(subEdge, "nmod:poss")) {
						// save subj
						IndexedWord subjNode = subEdge.getTarget();
						subject = getEntityObj(subjNode);
					}
				}
			}
		}

	}

	private static boolean edgeMatchRel(SemanticGraphEdge edge, String rel) {
		return edge.getRelation().toString().equalsIgnoreCase(rel);
	}

	private NLPEntity getEntityObj(IndexedWord tempWord) {
		NLPEntity resObj;
		DBPResource tempRes = SNLPUtil.findMatchingRes(tempWord.originalText(), entityMap);
		if (tempRes == null) {
			// Find the compounded string
			resObj = new NLPEntity(NLPProvider.getCompoundStr(tempWord, sg), null, false);
		} else {
			resObj = new NLPEntity(tempRes.getSurfaceForm(), tempRes.getUri(), true);
		}
		return resObj;
	}

}
