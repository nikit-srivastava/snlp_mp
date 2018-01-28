package snlp.mp.scnlp;

import java.util.Properties;

import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.simple.Sentence;

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
