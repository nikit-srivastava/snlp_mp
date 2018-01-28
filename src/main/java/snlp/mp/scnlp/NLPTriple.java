package snlp.mp.scnlp;

public class NLPTriple {
	private NLPEntity subject;
	private String relation;
	private NLPEntity object;
	public NLPEntity getSubject() {
		return subject;
	}
	public void setSubject(NLPEntity subject) {
		this.subject = subject;
	}
	public String getRelation() {
		return relation;
	}
	public void setRelation(String relation) {
		this.relation = relation;
	}
	public NLPEntity getObject() {
		return object;
	}
	public void setObject(NLPEntity object) {
		this.object = object;
	}	
}
