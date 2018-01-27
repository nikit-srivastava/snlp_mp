package snlp.mp.dbps;

import java.util.List;

public class DBPResponse {

	private String text;
	private String confidence;
	private String support;
	private String types;
	private String sparql;
	private String policy;
	private List<DBPResource> resources;

	public DBPResponse() {
	}

	public DBPResponse(String text, String confidence, String support, String types, String sparql, String policy,
			List<DBPResource> resources) {
		super();
		this.text = text;
		this.confidence = confidence;
		this.support = support;
		this.types = types;
		this.sparql = sparql;
		this.policy = policy;
		this.resources = resources;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getConfidence() {
		return confidence;
	}

	public void setConfidence(String confidence) {
		this.confidence = confidence;
	}

	public String getSupport() {
		return support;
	}

	public void setSupport(String support) {
		this.support = support;
	}

	public String getTypes() {
		return types;
	}

	public void setTypes(String types) {
		this.types = types;
	}

	public String getSparql() {
		return sparql;
	}

	public void setSparql(String sparql) {
		this.sparql = sparql;
	}

	public String getPolicy() {
		return policy;
	}

	public void setPolicy(String policy) {
		this.policy = policy;
	}

	public List<DBPResource> getResources() {
		return resources;
	}

	public void setResources(List<DBPResource> resources) {
		this.resources = resources;
	}

}
