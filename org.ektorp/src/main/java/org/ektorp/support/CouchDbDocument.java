package org.ektorp.support;

import java.io.*;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.ektorp.*;
import org.ektorp.util.*;

import com.dw.couchdb.dto.CouchdbDocument;
import com.dw.couchdb.dto.Revisions;
/**
 *
 * @author henrik lundgren
 *
 */
@JsonInclude(Include.NON_NULL)
public class CouchDbDocument extends CouchdbDocument implements Serializable {

    public static final String ATTACHMENTS_NAME = "_attachments";

	private static final long serialVersionUID = 1L;
	private String id;
	private Map<String, Attachment> attachments;
	private List<String> conflicts;
	private Revisions revisions;

	@JsonProperty("_id")
	public String getId() {
		return id;
	}

	@JsonProperty("_id")
	public void setId(String s) {
		Assert.hasText(s, "id must have a value");
		if (id != null && id.equals(s)) {
		    return;
		}
	    if (id != null) {
			throw new IllegalStateException("cannot set id, id already set");
		}
		id = s;
	}


	@Deprecated
	@JsonProperty("_rev")
	public String getRevision() {
		return super.getRev();
	}

	@Deprecated
	@JsonProperty("_rev")
	public void setRevision(String s) {
		super.setRev(s);
	}
	@JsonIgnore
	public boolean isNew() {
		return super.getRev() == null;
	}
	
	@JsonProperty(ATTACHMENTS_NAME)
	public Map<String, Attachment> getAttachments() {
		return attachments;
	}
	
	@JsonProperty(ATTACHMENTS_NAME)
	void setAttachments(Map<String, Attachment> attachments) {
		this.attachments = attachments;
	}

	@JsonProperty("_conflicts")
	public void setConflicts(List<String> conflicts) {
		this.conflicts = conflicts;
	}

	/**
	 *
	 * @return a list of conflicting revisions. Note: Will only be populated if this document has been loaded through the CouchDbConnector.getWithConflicts method.
	 */
	@JsonIgnore
	public List<String> getConflicts() {
		return conflicts;
	}
	/**
	 *
	 * @return true if this document has a conflict. Note: Will only give a correct value if this document has been loaded through the CouchDbConnector.getWithConflicts method.
	 */
	public boolean hasConflict() {
		return conflicts != null && !conflicts.isEmpty();
	}

	protected void removeAttachment(String id) {
		Assert.hasText(id, "id may not be null or emtpy");
		if (attachments != null) {
			attachments.remove(id);
		}
	}

	protected void addInlineAttachment(Attachment a) {
		Assert.notNull(a, "attachment may not be null");
		if (attachments == null) {
			attachments = new HashMap<String, Attachment>();
		}
		attachments.put(a.getId(), a);
	}
	
	@JsonIgnore
	  public Revisions getRevisions() {
	    return revisions;
	  }

	  @JsonProperty("_revisions")
	  public void setRevisions(Revisions allRevisions) {
	    this.revisions = allRevisions;
	  }

}
