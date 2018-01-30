package snlp.mp.dbps;

import java.util.HashMap;
import java.util.Map;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import snlp.mp.misc.Consts;
import snlp.mp.misc.SNLPUtil;
/**
 * Class to process a document for Named Entity Recognition
 * @author Nikit
 *
 */
public class NERProvider {
	private String document;
	private Map<String, DBPResource> entityMap;

	public NERProvider(String document) throws UnirestException {
		super();
		this.document = document;
		this.entityMap = new HashMap<>();
		processNER();
	}

	// Method to create NER tags for the entities
	/**
	 * Method to process the document and fill the entityMap
	 * @throws UnirestException
	 */
	public void processNER() throws UnirestException {
		// Send the request to dbpedia server to annotate String
		DBPResponse response = sendNERReq();
		// Set the entityMap through response
		if(response!=null)
			fillEntityMap(response);
	}
	/**
	 * Method to send the request to DBPedia Spotlight server to run NER on the fact
	 * @return - response object from the DBPedia server
	 * @throws UnirestException
	 */
	private DBPResponse sendNERReq() throws UnirestException {
		HttpResponse<JsonNode> jsonResponse = Unirest.post(Consts.DBPWS_URI).header("accept", "application/json")
				.field("text", this.document).field("confidence", Consts.DBPWS_CONFIDENCE).asJson();

		DBPResponse response = SNLPUtil.mapDBPSJson(jsonResponse.getBody());
		return response;
	}
	/**
	 * Method to fill the entityMap based on the response from the DBPedia Server
	 * @param response - response from the DBPedia Server
	 */
	private void fillEntityMap(DBPResponse response) {
		
		for(DBPResource resource : response.getResources()) {
			this.entityMap.put(resource.getSurfaceForm(), resource);
		}
	}

	// Getter and Setter
	public String getDocument() {
		return document;
	}

	public void setDocument(String document) {
		this.document = document;
	}

	public Map<String, DBPResource> getEntityMap() {
		return entityMap;
	}

	public void setEntityMap(Map<String, DBPResource> entityMap) {
		this.entityMap = entityMap;
	}

}
