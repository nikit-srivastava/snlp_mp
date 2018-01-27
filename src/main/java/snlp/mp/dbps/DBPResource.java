package snlp.mp.dbps;

public class DBPResource {
	private String uri;
	private String support;
	private String types;
	private String surfaceForm;
	private String offset;
	private String similarityScore;
	private String percentageOfSecondRank;

	public DBPResource() {
	}

	public DBPResource(String uri, String support, String types, String surfaceForm, String offset,
			String similarityScore, String percentageOfSecondRank) {
		super();
		this.uri = uri;
		this.support = support;
		this.types = types;
		this.surfaceForm = surfaceForm;
		this.offset = offset;
		this.similarityScore = similarityScore;
		this.percentageOfSecondRank = percentageOfSecondRank;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
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

	public String getSurfaceForm() {
		return surfaceForm;
	}

	public void setSurfaceForm(String surfaceForm) {
		this.surfaceForm = surfaceForm;
	}

	public String getOffset() {
		return offset;
	}

	public void setOffset(String offset) {
		this.offset = offset;
	}

	public String getSimilarityScore() {
		return similarityScore;
	}

	public void setSimilarityScore(String similarityScore) {
		this.similarityScore = similarityScore;
	}

	public String getPercentageOfSecondRank() {
		return percentageOfSecondRank;
	}

	public void setPercentageOfSecondRank(String percentageOfSecondRank) {
		this.percentageOfSecondRank = percentageOfSecondRank;
	}

}
