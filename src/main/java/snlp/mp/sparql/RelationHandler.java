package snlp.mp.sparql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

import snlp.mp.misc.SNLPUtil;
import snlp.mp.scnlp.NLPTriple;

public class RelationHandler {

	private NLPTriple nlpTriple;
	private Map<String, List<String>> sampleMap;
	private static int uriLim = 3;

	public RelationHandler(NLPTriple nlpTriple) {
		super();
		this.nlpTriple = nlpTriple;
		this.sampleMap = new HashMap<>();
		boolean isSubjURI = nlpTriple.getSubject().isURI();
		boolean isObjURI = nlpTriple.getObject().isURI();
		// find the appropriate action set for above triple
		// process the triple and set the sampleMap
		if (isSubjURI && isObjURI) {
			// getRelQuery
			processRelQ();
		} else if (isSubjURI && !isObjURI) {
			// getSubjRelQuery
			processSubjRelQ();
		} else if (!isSubjURI && isObjURI) {
			// getObjRelQuery
			processObjRelQ();
		}

	}

	private void processRelQ() {
		// get the subject and object uri list
		String subjURI = nlpTriple.getSubject().getUri();
		String objURI = nlpTriple.getObject().getUri();
		List<String> subjURIList = getSameAsURIList(subjURI);
		List<String> objURIList = getSameAsURIList(objURI);
		// construct the query
		String queryStr = getRelQuery(subjURIList, objURIList, nlpTriple.getObject().getLabel());
		// run the query
		Query query = QueryFactory.create(queryStr);
		List<String> relationList = new ArrayList<>();
		// Remote execution.
		try (QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query)) {
			// Set the DBpedia specific timeout.
			((QueryEngineHTTP) qexec).addParam("timeout", "10000");

			// Execute.
			ResultSet rs = qexec.execSelect();
			RDFNode tempNode;
			while (rs.hasNext()) {
				tempNode = rs.next().get("relName");
				relationList.add(tempNode.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Set the sampleMap
		this.sampleMap.put(this.nlpTriple.getRelation(), relationList);
	}

	private void processSubjRelQ() {
		// get the subject uri list
		String subjURI = nlpTriple.getSubject().getUri();
		List<String> subjURIList = getSameAsURIList(subjURI);
		String objName = nlpTriple.getObject().getLabel();
		// construct the query
		String queryStr = getSubjRelQuery(subjURIList);
		// run the query
		Query query = QueryFactory.create(queryStr);
		List<String> relationList = new ArrayList<>();
		// Remote execution.
		try (QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query)) {
			// Set the DBpedia specific timeout.
			((QueryEngineHTTP) qexec).addParam("timeout", "10000");

			// Execute.
			ResultSet rs = qexec.execSelect();
			RDFNode tempNode;
			RDFNode tempNodeObj;
			// Match the existing object label with all the objects and fetch the relations
			QuerySolution tempQSol;
			while (rs.hasNext()) {
				tempQSol = rs.next(); 
				tempNode = tempQSol.get("relName");
				tempNodeObj = tempQSol.get("objName");
				if (SNLPUtil.isSimilar(objName, tempNodeObj.toString()))
					relationList.add(tempNode.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Set the sampleMap
		this.sampleMap.put(this.nlpTriple.getRelation(), relationList);
	}

	private void processObjRelQ() {
		// get the object uri list
		String objURI = nlpTriple.getObject().getUri();
		List<String> objURIList = getSameAsURIList(objURI);
		String subjName = nlpTriple.getSubject().getLabel();
		// construct the query
		String queryStr = getObjRelQuery(objURIList,nlpTriple.getObject().getLabel());
		// run the query
		Query query = QueryFactory.create(queryStr);
		List<String> relationList = new ArrayList<>();
		// Remote execution.
		try (QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query)) {
			// Set the DBpedia specific timeout.
			((QueryEngineHTTP) qexec).addParam("timeout", "10000");

			// Execute.
			ResultSet rs = qexec.execSelect();
			RDFNode tempNode;
			RDFNode tempNodeSubj;
			// Match the existing subject label with all the subjects and fetch the
			// relations
			QuerySolution tempQSol;
			while (rs.hasNext()) {
				tempQSol = rs.next(); 
				tempNode = tempQSol.get("relName");
				tempNodeSubj = tempQSol.get("subjName");
				if (SNLPUtil.isSimilar(subjName, tempNodeSubj.toString()))
					relationList.add(tempNode.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Set the sampleMap
		this.sampleMap.put(this.nlpTriple.getRelation(), relationList);
	}

	private List<String> getSameAsURIList(String uri) {
		List<String> sameAsUriList = new ArrayList<>();
		StringBuilder queryStr = new StringBuilder();
		queryStr.append("PREFIX owl: <http://www.w3.org/2002/07/owl#> ");
		queryStr.append("SELECT distinct ?same ");
		queryStr.append("WHERE { ");
		queryStr.append(" <").append(uri).append("> (owl:sameAs|^owl:sameAs)* ?same. ");
		queryStr.append("} LIMIT ").append(uriLim);

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
			//e.printStackTrace();
			//TODO: Put logger here
		}

		return sameAsUriList;
	}

	private String getRelQuery(List<String> subjList, List<String> objList, String objLabel) {
		StringBuilder queryStr = new StringBuilder();
		queryStr.append("PREFIX dbo: <http://swrc.ontoware.org/ontology/> ");
		queryStr.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns/> ");
		queryStr.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> ");
		queryStr.append("SELECT distinct ( STR( ?relNameUF ) as ?relName ) ");
		queryStr.append("WHERE { ");
		for (int i = 0; i < subjList.size(); i++) {
			for (int j = 0; j < objList.size(); j++) {
				queryStr.append(" { <").append(subjList.get(i)).append("> ?rel <").append(objList.get(j))
						.append("> . } ");
				queryStr.append(" UNION { <").append(subjList.get(i)).append("> ?rel ?obj . ")
				.append(" FILTER regex(?obj, \".*").append(objLabel).append(".*\", \"i\") } ");
				if (j < objList.size() - 1)
					queryStr.append(" UNION ");
			}
			if (i < subjList.size() - 1)
				queryStr.append(" UNION ");
		}
		queryStr.append("?rel rdfs:label ?relNameUF . ");
		queryStr.append("FILTER langMatches( lang( ?relNameUF ) , 'en' ) . ");
		queryStr.append("}");
		return queryStr.toString();
	}

	private String getSubjRelQuery(List<String> subjList) {
		String tempSubj;
		StringBuilder queryStr = new StringBuilder();
		queryStr.append("PREFIX dbo: <http://swrc.ontoware.org/ontology/> ");
		queryStr.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns/> ");
		queryStr.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> ");
		queryStr.append("SELECT ?relName ?objName ");
		queryStr.append("WHERE { ");
		for (int i = 0; i < subjList.size(); i++) {
			tempSubj = subjList.get(i);
			queryStr.append(" { <").append(tempSubj).append("> ?rel ?obj . } ");
			if (i < subjList.size() - 1)
				queryStr.append(" UNION ");
		}
		queryStr.append("?obj rdfs:label ?objNameUF . ");
		queryStr.append("?rel rdfs:label ?relNameUF . ");
		queryStr.append("FILTER langMatches( lang( ?relNameUF ) , 'en' ) . ");
		queryStr.append("FILTER langMatches( lang( ?objNameUF ) , 'en' ) . ");
		queryStr.append(" BIND( STR( ?relNameUF ) as ?relName ) . ");
		queryStr.append(" BIND( STR( ?objNameUF ) as ?objName ) ");
		queryStr.append("}");
		return queryStr.toString();
	}

	private String getObjRelQuery(List<String> objList, String objLabel) {
		String tempObj;
		StringBuilder queryStr = new StringBuilder();
		queryStr.append("PREFIX dbo: <http://swrc.ontoware.org/ontology/> ");
		queryStr.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns/> ");
		queryStr.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> ");
		queryStr.append("SELECT ?relName ?subjName ");
		queryStr.append("WHERE { ");
		for (int i = 0; i < objList.size(); i++) {
			tempObj = objList.get(i);
			queryStr.append(" { ?subj ?rel <").append(tempObj).append(">  . } ");
			queryStr.append(" UNION { ?subj ?rel ?obj . ")
			.append(" FILTER regex(?obj, \".*").append(objLabel).append(".*\", \"i\") } ");
			if (i < objList.size() - 1)
				queryStr.append(" UNION ");
		}
		queryStr.append("?subj rdfs:label ?subjNameUF . ");
		queryStr.append("?rel rdfs:label ?relNameUF . ");
		queryStr.append("FILTER langMatches( lang( ?relNameUF ) , 'en' ) . ");
		queryStr.append("FILTER langMatches( lang( ?subjNameUF ) , 'en' ) . ");
		queryStr.append(" BIND( STR( ?relNameUF ) as ?relName ) . ");
		queryStr.append(" BIND( STR( ?subjNameUF ) as ?subjName ) ");
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
