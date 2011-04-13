package org.ektorp;

import java.io.*;

import org.codehaus.jackson.annotate.*;
/**
 * Represents a result of a document operation.
 * @author henrik lundgren
 *
 */
@JsonIgnoreProperties("ok")
public class DocumentOperationResult implements Serializable {

	private static final long serialVersionUID = -5107130332464837673L;
	
	private String id;
	private String rev;
	private String error;
	private String reason;
	
	public static DocumentOperationResult newInstance(String id, String error, String reason) {
		DocumentOperationResult r = new DocumentOperationResult();
		r.setId(id);
		r.setError(error);
		r.setReason(reason);
		return r;
	}
	
	public static DocumentOperationResult newInstance(String id, String revision) {
		DocumentOperationResult r = new DocumentOperationResult();
		r.setId(id);
		r.setRev(revision);
		return r;
	}
	
	@JsonProperty
	void setError(String error) {
		this.error = error;
	}
	
	@JsonProperty
	void setReason(String reason) {
		this.reason = reason;
	}
	
	public String getId() {
		return id;
	}
	
	@JsonProperty("id")
	void setId(String id) {
		this.id = id;
	}
	
	public String getRevision() {
		return rev;
	}
	@JsonProperty("rev")
	void setRev(String rev) {
		this.rev = rev;
	}
	
	public String getError() {
		return error;
	}
	
	public String getReason() {
		return reason;
	}
	
	public boolean isErroneous() {
		return error != null;
	}

	@Override
	public String toString() {
		return "DocumentOperationResult [id=" + id + ", rev=" + rev
				+ ", error=" + error + ", reason=" + reason + "]";
	}


}
