package org.ektorp;

import java.io.ByteArrayInputStream;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonValue;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.node.ObjectNode;
import org.ektorp.util.Base64;
import org.ektorp.util.Exceptions;

/**
 * 
 * @author henrik lundgren
 * 
 */
@JsonSerialize(include = Inclusion.NON_NULL)
public class PageRequest {

	private final static ObjectMapper MAPPER = new ObjectMapper();

	private final int pageSize;
	private final Object startKey;
	private final String startKeyDocId;

	
	public static ViewQuery fromLink(ViewQuery q, String link) {
		PageRequest pp = PageRequest.fromLink(link);
		return q.startKey(pp.getStartKey()).startDocId(pp.getStartKeyDocId())
				.limit(pp.getPageSize() + 1);
	}

	public static ViewQuery firstPage(String designDoc, String viewName,
			int pageSize) {
		return new ViewQuery().designDocId(designDoc).viewName(viewName)
				.limit(pageSize + 1);

	}

	@JsonCreator
	public PageRequest(@JsonProperty("k") Object startKey,
			@JsonProperty("i") String startKeyDocId,
			@JsonProperty("s") int pageSize) {
		this.startKey = startKey;
		this.startKeyDocId = startKeyDocId;
		this.pageSize = pageSize;
	}

	public PageRequest(int startKey, String startKeyDocId, int pageSize) {
		this.startKey = startKey;
		this.startKeyDocId = startKeyDocId;
		this.pageSize = pageSize;
	}

	public PageRequest(double startKey, String startKeyDocId, int pageSize) {
		this.startKey = startKey;
		this.startKeyDocId = startKeyDocId;
		this.pageSize = pageSize;
	}

	public PageRequest(float startKey, String startKeyDocId, int pageSize) {
		this.startKey = startKey;
		this.startKeyDocId = startKeyDocId;
		this.pageSize = pageSize;
	}

	public PageRequest(boolean startKey, String startKeyDocId, int pageSize) {
		this.startKey = startKey;
		this.startKeyDocId = startKeyDocId;
		this.pageSize = pageSize;
	}

	public static PageRequest fromLink(String link) {
		try {
			return MAPPER.readValue(
					new ByteArrayInputStream(Base64.decode(link,
							Base64.URL_SAFE)), PageRequest.class);
		} catch (Exception e) {
			throw Exceptions.propagate(e);
		}
	}

	public String asLink() {
		try {
			return Base64.encodeBytes(MAPPER.writeValueAsBytes(this),
					Base64.URL_SAFE);
		} catch (Exception e) {
			throw Exceptions.propagate(e);
		}
	}

	@JsonValue
	public JsonNode asJson() {
		ObjectNode n = MAPPER.createObjectNode();
		if (startKey instanceof String) {
			n.put("k", (String) startKey);
		} else {
			n.putPOJO("k", startKey);
		}

		n.put("i", startKeyDocId);
		n.put("s", pageSize);
		return n;
	}

	public int getPageSize() {
		return pageSize;
	}

	public Object getStartKey() {
		return startKey;
	}

	public String getStartKeyDocId() {
		return startKeyDocId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + pageSize;
		result = prime * result
				+ ((startKey == null) ? 0 : startKey.hashCode());
		result = prime * result
				+ ((startKeyDocId == null) ? 0 : startKeyDocId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PageRequest other = (PageRequest) obj;
		if (pageSize != other.pageSize)
			return false;
		if (startKey == null) {
			if (other.startKey != null)
				return false;
		} else if (!startKey.equals(other.startKey))
			return false;
		if (startKeyDocId == null) {
			if (other.startKeyDocId != null)
				return false;
		} else if (!startKeyDocId.equals(other.startKeyDocId))
			return false;
		return true;
	}

}
