package org.ektorp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import org.ektorp.util.Assert;

/**
 *
 * @author henrik lundgren
 *
 */
public class ViewResult implements Iterable<ViewResult.Row>, Serializable {

	private static final String OFFSET_FIELD_NAME = "offset";
	private static final String TOTAL_ROWS_FIELD_NAME = "total_rows";
	private static final String UPDATE_SEQ = "update_seq";
	private static final long serialVersionUID = 4750290767933801714L;
	private int totalRows = -1;
	private int offset = -1;
	private String updateSeq;
	private List<Row> rows;

	public ViewResult(JsonNode resultNode, boolean ignoreNotFound) {
        Assert.notNull(resultNode, "resultNode may not be null");
		Assert.isTrue(resultNode.findPath("rows").isArray(), "result must contain 'rows' field of array type");
		if (resultNode.get(TOTAL_ROWS_FIELD_NAME) != null) {
			totalRows = resultNode.get(TOTAL_ROWS_FIELD_NAME).intValue();
		}
		if (resultNode.get(OFFSET_FIELD_NAME) != null) {
			offset = resultNode.get(OFFSET_FIELD_NAME).intValue();
		}
		if (resultNode.get(UPDATE_SEQ) != null) {
			updateSeq = resultNode.get(UPDATE_SEQ).textValue();
                        if(updateSeq == null) {
                                updateSeq = Long.toString(resultNode.get(UPDATE_SEQ).intValue());
                        }
		}
		JsonNode rowsNode = resultNode.get("rows");
		rows = new ArrayList<ViewResult.Row>(rowsNode.size());
		for (JsonNode n : rowsNode) {
		    if (!(ignoreNotFound && n.has(Row.ERROR_FIELD_NAME))) {
		        rows.add(new Row(n));
		    }
		}
	}

	public List<Row> getRows() {
		return rows;
	}

	public int getSize() {
		return rows.size();
	}
	/**
	 *
	 * @return -1 if result did not contain an offset field
	 */
	public int getOffset() {
		return offset;
	}

	@JsonProperty
	void setOffset(int offset) {
		this.offset = offset;
	}
	/**
	 *
	 * @return -1 if result did not contain a total_rows field
	 */
	public int getTotalRows() {
		return totalRows;
	}

	@JsonProperty(TOTAL_ROWS_FIELD_NAME)
	void setTotalRows(int i) {
		this.totalRows = i;
	}

	/**
	 * @return -1L if result did not contain an update_seq field
	 */
	public long getUpdateSeq() {
		if(updateSeq != null) {
			return Long.parseLong(updateSeq);
		}
		return -1L;
	}

	/**
	 * @return false if db is an Cloudant instance.
	 */
	public boolean isUpdateSeqNumeric() {
		return updateSeq != null && updateSeq.matches("^\\d*$");
	}

	/**
	 *
	 * @return null if result did not contain an update_seq field
	 */
	public String getUpdateSeqAsString() {
		return updateSeq;
	}

	@JsonProperty(UPDATE_SEQ)
	public void setUpdateSeq(String updateSeq) {
		this.updateSeq = updateSeq;
	}

	public Iterator<ViewResult.Row> iterator() {
		return rows.iterator();
	}

	public boolean isEmpty() {
		return rows.isEmpty();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("{\n\"total_rows\":");
		builder.append(totalRows);
		builder.append(",\n\"offset\":");
		builder.append(offset);
		builder.append(",\n\"rows\":");
		builder.append(rows.toString());
		builder.append("\n}");
		return builder.toString();
	}
	
	public static class Row {

		static final String VALUE_FIELD_NAME = "value";
		static final String ID_FIELD_NAME = "id";
		static final String KEY_FIELD_NAME = "key";
		static final String DOC_FIELD_NAME = "doc";
		static final String ERROR_FIELD_NAME = "error";
		private final JsonNode rowNode;

		@JsonCreator
		public Row(JsonNode rowNode) {
			Assert.notNull(rowNode, "row node may not be null");
			this.rowNode = rowNode;
			if (getError() != null) {
				throw new ViewResultException(getKeyAsNode(), getError());
			}
		}

		public String getId() {
			return rowNode.get(ID_FIELD_NAME).textValue();
		}

		public String getKey() {
			return nodeAsString(getKeyAsNode());
		}

		public JsonNode getKeyAsNode() {
			return rowNode.findPath(KEY_FIELD_NAME);
		}

		public String getValue() {
			return nodeAsString(getValueAsNode());
		}

		public int getValueAsInt() {
			return getValueAsNode().asInt(0);
		}

		public JsonNode getValueAsNode() {
			return rowNode.findPath(VALUE_FIELD_NAME);
		}

		public String getDoc() {
			return nodeAsString(rowNode.findValue(DOC_FIELD_NAME));
		}

		public JsonNode getDocAsNode() {
			return rowNode.findPath(DOC_FIELD_NAME);
		}

		private String getError() {
			return nodeAsString(rowNode.findValue(ERROR_FIELD_NAME));
		}

		private String nodeAsString(JsonNode node) {
			if (isNull(node)) return null;
			return node.isContainerNode() ? node.toString() : node.asText();
		}

		private boolean isNull(JsonNode node) {
			return node == null || node.isNull() || node.isMissingNode();
		}

		@Override
		public String toString() {
			return rowNode.toString();
		}
	}

}
