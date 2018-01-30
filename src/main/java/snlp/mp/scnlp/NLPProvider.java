package snlp.mp.scnlp;

import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.simple.Sentence;
/**
 * Class to handle the Basic Dependency Graph of a document.
 * @author Nikit
 *
 */
public class NLPProvider {

	private String document;
	private SemanticGraph depGraph;
	private static Properties props;
	static {
		props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, depparse, openie");
		props.setProperty("ner.useSUTime", "0");
	}

	public NLPProvider(String document) {
		super();
		this.document = document;
		this.buildDependencyGraph();
	}

	private void buildDependencyGraph() {

		Sentence sent = new Sentence(this.document, props);
		this.depGraph = sent.dependencyGraph();
	}
	/**
	 * Method to fetch the compound string for a particular node in a graph
	 * @param node
	 * @param sg - graph to look for other related compound nodes
	 * @return Compound String
	 */
	public static String getCompoundStr(IndexedWord node, SemanticGraph sg) {
		String res = node.originalText();
		IndexedWord curNode = node;
		int count;
		while(true) {
			count = 0;
			List<SemanticGraphEdge> outEdges = sg.getOutEdgesSorted(curNode);
			for(SemanticGraphEdge edge : outEdges) {
				if(edge.getRelation().toString().equalsIgnoreCase("compound")) {
					curNode = edge.getTarget();
					res = curNode.originalText()+" "+res;
					count++;
				}
			}
			if(count==0)
				break;
		}
		return res;
	}


	public String getDocument() {
		return document;
	}

	public void setDocument(String document) {
		this.document = document;
	}

	public SemanticGraph getDepGraph() {
		return depGraph;
	}

	public void setDepGraph(SemanticGraph depGraph) {
		this.depGraph = depGraph;
	}

}
