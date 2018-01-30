package snlp.mp.scnlp;

/**
 * Class that stores basic information of an entity
 * 
 * @author Nikit
 *
 */
public class NLPEntity {

	private String label;
	private String uri;
	private boolean isURI;

	public NLPEntity(String label, String uri, boolean isURI) {
		super();
		this.label = label;
		this.uri = uri;
		this.isURI = isURI;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public boolean isURI() {
		return isURI;
	}

	public void setURI(boolean isURI) {
		this.isURI = isURI;
	}

}
