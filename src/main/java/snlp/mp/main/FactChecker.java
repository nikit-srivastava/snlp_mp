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
import snlp.mp.dict.WordNetExpansionAdv;
import snlp.mp.misc.Consts;
import snlp.mp.misc.SNLPUtil;
import snlp.mp.scnlp.NLPEntity;
import snlp.mp.scnlp.NLPProvider;
import snlp.mp.scnlp.NLPTriple;
import snlp.mp.sparql.RelationHandler;

/**
 * Class to process a fact and provide the Truth value for it
 * 
 * @author Nikit
 *
 */
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
	/**
	 * Method to analyze the fact and return a truth value
	 * 
	 * @return -1 for false, 0 for neutral, +1 for true facts
	 * @throws UnirestException
	 */
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
			WordNetExpansionAdv wne = new WordNetExpansionAdv(Consts.WN_DPATH);
			double similarity = wne.getExpandedJaccardSimilarityAdv(relation, sampleMap.get(relation));
			// save the ID and result in the output file
			if (similarity > 0)
				cValue = 1;
			else
				cValue = -1;
		}
		return cValue;
	}

	/**
	 * Method to process the SemanticGraph of Basic Dependencies in order to find
	 * the triple of (Subject verb object)
	 */
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

	/**
	 * Method to process rootNodes having VBZ POS tag
	 * 
	 * @param rootWord
	 */
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

	/**
	 * Method to process rootNodes having VBG POS tag
	 * 
	 * @param rootWord
	 */
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

	/**
	 * Method to process rootNodes having VB POS tag
	 * 
	 * @param rootWord
	 */
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

	/**
	 * Method to process rootNodes having NN POS tag
	 * 
	 * @param rootWord
	 */
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

	/**
	 * Method to process rootNodes having NNS POS tag
	 * 
	 * @param rootWord
	 */
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

	/**
	 * Method to process rootNodes having NNP POS tag
	 * 
	 * @param rootWord
	 */
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

	/**
	 * Method to match relation of an edge
	 * 
	 * @param edge
	 *            - edge between two nodes
	 * @param rel
	 *            - relation to be compared with that of the edge
	 * @return true if relation is found
	 */
	private static boolean edgeMatchRel(SemanticGraphEdge edge, String rel) {
		return edge.getRelation().toString().equalsIgnoreCase(rel);
	}

	/**
	 * Method to find the given node as an entity in the entityMap
	 * 
	 * @param tempWord
	 *            - node to be searched for
	 * @return entity represented by the node
	 */
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

	// Setter and Getters
	public NLPEntity getSubject() {
		return subject;
	}

	public void setSubject(NLPEntity subject) {
		this.subject = subject;
	}

	public NLPEntity getObject() {
		return object;
	}

	public void setObject(NLPEntity object) {
		this.object = object;
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}

	public Map<String, DBPResource> getEntityMap() {
		return entityMap;
	}

	public void setEntityMap(Map<String, DBPResource> entityMap) {
		this.entityMap = entityMap;
	}

	public SemanticGraph getSg() {
		return sg;
	}

	public void setSg(SemanticGraph sg) {
		this.sg = sg;
	}

	public String getFactId() {
		return factId;
	}

	public void setFactId(String factId) {
		this.factId = factId;
	}

	public String getFact() {
		return fact;
	}

	public void setFact(String fact) {
		this.fact = fact;
	}

}
