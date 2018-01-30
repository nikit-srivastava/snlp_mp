package snlp.mp.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import snlp.mp.dbps.DBPResource;
import snlp.mp.dbps.DBPResponse;
/**
 * Class to store miscellaneous methods called in the project
 * @author Nikit
 *
 */
public class SNLPUtil {

	public static int limit = 10;
	/**
	 * Maps data from a JsonNode to DBPResponse class
	 * @param jsonNode - jsonNode object
	 * @return mapped instance of DBPResponse
	 */
	public static DBPResponse mapDBPSJson(JsonNode jsonNode) {
		DBPResponse resp = null;
		
		try {
			JSONObject jObj = jsonNode.getObject();
			JSONArray resJArr = jObj.getJSONArray(Consts.DBPS_RESOURCES);
			List<DBPResource> resources = new ArrayList<>();
			resp = new DBPResponse();
			resp.setResources(resources);
			DBPResource temp;
			JSONObject tempJObj;
			// Setting the List of Resources
			for (int i = 0; i < resJArr.length(); i++) {
				temp = new DBPResource();
				tempJObj = resJArr.getJSONObject(i);
				temp.setOffset(tempJObj.getString(Consts.DBPSR_OFFSET));
				temp.setPercentageOfSecondRank(tempJObj.getString(Consts.DBPSR_POSR));
				temp.setSimilarityScore(tempJObj.getString(Consts.DBPSR_SIMILARITYSCORE));
				temp.setSupport(tempJObj.getString(Consts.DBPSR_SUPPORT));
				temp.setSurfaceForm(tempJObj.getString(Consts.DBPSR_SURFACEFORM));
				temp.setTypes(tempJObj.getString(Consts.DBPSR_TYPES));
				temp.setUri(tempJObj.getString(Consts.DBPSR_URI));
				resources.add(temp);
			}
			// Setting other response attributes
			resp.setConfidence(jObj.getString(Consts.DBPS_CONFIDENCE));
			resp.setPolicy(jObj.getString(Consts.DBPS_POLICY));
			resp.setSparql(jObj.getString(Consts.DBPS_SPARQL));
			resp.setSupport(jObj.getString(Consts.DBPS_SUPPORT));
			resp.setText(jObj.getString(Consts.DBPS_TEXT));
			resp.setTypes(jObj.getString(Consts.DBPS_TYPES));
		} catch (Exception ex) {
			//TODO: log : NER failure
		}
		return resp;
	}

	// Method to get synonyms array from datamuse
	/**
	 * Method fetch a list of synonyms for a given phrase
	 * @param phrase - phrase to find synonyms of
	 * @return List of top synonyms based on the limit
	 * @throws UnirestException
	 */
	public static List<String> getDMSyn(String phrase) throws UnirestException {
		List<String> synList = new ArrayList<>();
		HttpResponse<JsonNode> jsonResponse = Unirest.get("https://api.datamuse.com/words").queryString("ml", phrase)
				.asJson();
		JSONArray jsonArr = jsonResponse.getBody().getArray();
		String temp;
		for (int i = 0; i < jsonArr.length(); i++) {
			if (i + 1 > limit)
				break;
			temp = jsonArr.getJSONObject(i).getString("word");
			synList.add(temp);
		}
		return synList;
	}
	/**
	 * Method to check if two strings are substring of each other
	 * @param val1 - first String
	 * @param val2 - second String
	 * @return - true if one string is substring of another
	 */
	public static boolean isSimilar(String val1, String val2) {
		boolean res = false;
		if (val2.matches(".*" + val1 + ".*") || val1.matches(".*" + val2 + ".*"))
			res = true;

		return res;
	}
	/**
	 * Method to retrieve the matching entity for a give String qVal
	 * @param qVal - String to match entities against
	 * @param entityMap - Map of entities and their labels
	 * @return DBResource object of the matching entity
	 */
	public static DBPResource findMatchingRes(String qVal, Map<String, DBPResource> entityMap) {
		DBPResource res = null;
		for (String key : entityMap.keySet()) {
			if (key.matches(".*" + qVal + ".*"))
				return entityMap.get(key);
		}
		return res;
	}
	/**
	 * Method to format factId and its result as a RDF triple
	 * @param factId
	 * @param result
	 * @return String formatted as a RDF triple
	 */
	public static String getFormattedOutput(String factId, int result) {
		StringBuilder outLine = new StringBuilder();
		double dRes = (double) result;
		outLine.append("<http://swc2017.aksw.org/task2/dataset/").append(factId).append("> ");
		outLine.append("<http://swc2017.aksw.org/hasTruthValue> ");
		outLine.append("\"").append(dRes).append("\"^^<http://www.w3.org/2001/XMLSchema#double> .");
		return outLine.toString();
	}
}
