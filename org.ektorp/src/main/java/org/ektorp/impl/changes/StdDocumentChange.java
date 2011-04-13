package org.ektorp.impl.changes;

import org.codehaus.jackson.*;
import org.ektorp.changes.*;
import org.ektorp.util.*;
/**
 * 
 * @author henrik lundgren
 *
 */
public class StdDocumentChange implements DocumentChange {

	private static final String REV_FIELD_NAME = "rev";
	private static final String SEQ_FIELD_NAME = "seq";
	private static final String ID_FIELD_NAME = "id";
	private static final String DOC_FIELD_NAME = "doc";
	private static final String DELETED_FIELD_NAME = "deleted";
	
	private final JsonNode node;
	
	public StdDocumentChange(JsonNode node) {
		Assert.notNull(node, "node may not be null");
		this.node = node;
	}
	
	public int getSequence() {
		return node.get(SEQ_FIELD_NAME).getIntValue();
	}
	
	public String getId() {
		return node.get(ID_FIELD_NAME).getTextValue();
	}
	
	public boolean isDeleted() {
		JsonNode deleted = node.findPath(DELETED_FIELD_NAME); 
		return !deleted.isMissingNode() && deleted.getBooleanValue();
	}
	
	public String getDoc() {
		return nodeAsString(node.findValue(DOC_FIELD_NAME));
	}
	
	public JsonNode getDocAsNode() {
		return node.findPath(DOC_FIELD_NAME);
	}
	
	private String nodeAsString(JsonNode node) {
		if (isNull(node)) return null;
		return node.isContainerNode() ? node.toString() : node.getValueAsText();
	}

	private boolean isNull(JsonNode node) {
		return node == null || node.isNull() || node.isMissingNode();
	}

	public String getRevision() {
		return nodeAsString(node.findPath(REV_FIELD_NAME));
	}
	
	@Override
	public String toString() {
		return node.toString();
	}
}
