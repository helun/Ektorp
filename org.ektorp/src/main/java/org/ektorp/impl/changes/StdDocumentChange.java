package org.ektorp.impl.changes;

import com.fasterxml.jackson.databind.JsonNode;
import org.ektorp.changes.*;
import org.ektorp.util.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private static final String CHANGES_FIELD_NAME = "changes";

    private final JsonNode node;

    public StdDocumentChange(JsonNode node) {
		Assert.notNull(node, "node may not be null");
		this.node = node;
	}

	public int getSequence() {
		return node.get(SEQ_FIELD_NAME).intValue();
	}

	public String getId() {
		return node.get(ID_FIELD_NAME).textValue();
	}

	public boolean isDeleted() {
		JsonNode deleted = node.findPath(DELETED_FIELD_NAME);
		return !deleted.isMissingNode() && deleted.booleanValue();
	}

	public String getDoc() {
		return nodeAsString(node.findValue(DOC_FIELD_NAME));
	}

	public JsonNode getDocAsNode() {
		return node.findPath(DOC_FIELD_NAME);
	}

	private String nodeAsString(JsonNode node) {
		if (isNull(node)) return null;
		return node.isContainerNode() ? node.toString() : node.asText();
	}

	private boolean isNull(JsonNode node) {
		return node == null || node.isNull() || node.isMissingNode();
	}

	public String getRevision() {
        return nodeAsString(node.findValue(REV_FIELD_NAME));
	}

    public List<String> getRevisions() {
        List<String> revisions = new ArrayList<String>();
        for (JsonNode changesNode : node.get(CHANGES_FIELD_NAME)) {
            revisions.add(nodeAsString(changesNode.get(REV_FIELD_NAME)));
        }
        return Collections.unmodifiableList(revisions);
    }

	@Override
	public String toString() {
		return node.toString();
	}
}
