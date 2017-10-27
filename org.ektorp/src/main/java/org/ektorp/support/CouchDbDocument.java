package org.ektorp.support;

import java.io.*;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.ektorp.*;
import org.ektorp.util.*;

/**
 *
 * @author henrik lundgren
 *
 */
@JsonInclude(Include.NON_NULL)
public class CouchDbDocument implements Serializable {

    public static final String ATTACHMENTS_NAME = "_attachments";

	private static final long serialVersionUID = 1L;
	private String id;
	private String revision;
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


	@JsonProperty("_rev")
	public String getRevision() {
		return revision;
	}

	@JsonProperty("_rev")
	public void setRevision(String s) {
		// no empty strings thanks
		if (s != null && s.length() == 0) {
			return;
		}
		this.revision = s;
	}
	@JsonIgnore
	public boolean isNew() {
		return revision == null;
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
	void setConflicts(List<String> conflicts) {
		this.conflicts = conflicts;
	}

	@JsonProperty("_revisions")
	void setRevisions(Revisions r) {
		this.revisions = r;
	}

	/**
	 * Note: Will only be populated if this document has been loaded with the revisions option = true.
	 * @return
	 */
	@JsonIgnore
	public Revisions getRevisions() {
		return revisions;
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

}
