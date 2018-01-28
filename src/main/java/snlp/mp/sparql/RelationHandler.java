package snlp.mp.sparql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

import snlp.mp.scnlp.NLPTriple;

public class RelationHandler {

	private NLPTriple nlpTriple;
	private Map<String, List<String>> sampleMap;

	public RelationHandler(NLPTriple nlpTriple) {
		super();
		this.nlpTriple = nlpTriple;
		boolean isSubjURI = nlpTriple.getSubject().isURI();
		boolean isObjURI = nlpTriple.getObject().isURI();
		// find the appropriate action set for above triple
		// process the triple and set the sampleMap
		if (isSubjURI && isObjURI) {
			// getRelQuery
		} else if (isSubjURI && !isObjURI) {
			// getSubjRelQuery
		} else if (!isSubjURI && isObjURI) {
			// getObjRelQuery
		}

	}

	private List<String> getSameAsURIList(String uri) {
		List<String> sameAsUriList = new ArrayList<>();
		StringBuilder queryStr = new StringBuilder();
		queryStr.append("PREFIX owl: <http://www.w3.org/2002/07/owl#> ");
		queryStr.append("SELECT distinct ?same ");
		queryStr.append("WHERE { ");
		queryStr.append(" <").append("uri").append("> (owl:sameAs|^owl:sameAs)* ?same. ");
		queryStr.append("}");

		Query query = QueryFactory.create(queryStr.toString());

		// Remote execution.
		try (QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query)) {
			// Set the DBpedia specific timeout.
			((QueryEngineHTTP) qexec).addParam("timeout", "10000");

			// Execute.
			ResultSet rs = qexec.execSelect();
			RDFNode tempNode;
			while (rs.hasNext()) {
				tempNode = rs.next().get("same");
				if (tempNode.isURIResource())
					sameAsUriList.add(tempNode.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return sameAsUriList;
	}

	private String getRelQuery() {
		List<String> subjList = null;
		List<String> objList = null;
		StringBuilder queryStr = new StringBuilder();
		queryStr.append("PREFIX dbo: <http://swrc.ontoware.org/ontology/> ");
		queryStr.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns/> ");
		queryStr.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema> ");
		queryStr.append("SELECT distinct (STR(?relName) as ?relName) ");
		queryStr.append("WHERE { ");
		for (int i = 0; i < subjList.size(); i++) {
			for (int j = 0; j < objList.size(); j++) {
				queryStr.append(" { <").append(subjList.get(i)).append("> ?rel <").append(objList.get(j))
						.append("> . } ");
				if (j < objList.size() - 1)
					queryStr.append(" UNION ");
			}
			if (i < subjList.size() - 1)
				queryStr.append(" UNION ");
		}
		queryStr.append("?rel rdfs:label ?relName . ");
		queryStr.append("FILTER langMatches(lang(?relName),'en') ");
		queryStr.append("}");
		return queryStr.toString();
	}

	private String getSubjRelQuery() {
		List<String> subjList = null;
		String tempSubj;
		StringBuilder queryStr = new StringBuilder();
		queryStr.append("PREFIX dbo: <http://swrc.ontoware.org/ontology/> ");
		queryStr.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns/> ");
		queryStr.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema> ");
		queryStr.append("SELECT ?relName, ?objName ");
		queryStr.append("WHERE { ");
		for (int i = 0; i < subjList.size(); i++) {
			tempSubj = subjList.get(i);
			queryStr.append(" { <").append(tempSubj).append("> ?rel ?obj . } ");
			if (i < subjList.size() - 1)
				queryStr.append(" UNION ");
		}
		queryStr.append("?obj rdfs:label ?objName . ");
		queryStr.append("?rel rdfs:label ?relName . ");
		queryStr.append("FILTER langMatches(lang(?relName),'en') ");
		queryStr.append("}");
		return queryStr.toString();
	}

	private String getObjRelQuery() {
		List<String> objList = null;
		String tempObj;
		StringBuilder queryStr = new StringBuilder();
		queryStr.append("PREFIX dbo: <http://swrc.ontoware.org/ontology/> ");
		queryStr.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns/> ");
		queryStr.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema> ");
		queryStr.append("SELECT ?relName, ?subjName");
		queryStr.append("WHERE { ");
		for (int i = 0; i < objList.size(); i++) {
			tempObj = objList.get(i);
			queryStr.append(" { ?subj ?rel <").append(tempObj).append(">  . } ");
			if (i < objList.size() - 1)
				queryStr.append(" UNION ");
		}
		queryStr.append("?subj rdfs:label ?subjName . ");
		queryStr.append("?rel rdfs:label ?relName . ");
		queryStr.append("FILTER langMatches(lang(?relName),'en') ");
		queryStr.append("}");
		return queryStr.toString();
	}

	// Getter and Setter
	public NLPTriple getNlpTriple() {
		return nlpTriple;
	}

	public void setNlpTriple(NLPTriple nlpTriple) {
		this.nlpTriple = nlpTriple;
	}

	public Map<String, List<String>> getSampleMap() {
		return sampleMap;
	}

	public void setSampleMap(Map<String, List<String>> sampleMap) {
		this.sampleMap = sampleMap;
	}

}
