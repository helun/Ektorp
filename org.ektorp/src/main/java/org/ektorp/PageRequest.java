package org.ektorp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.NullNode;
import org.codehaus.jackson.node.ObjectNode;
import org.ektorp.util.Base64;
import org.ektorp.util.Exceptions;

/**
 * 
 * @author henrik lundgren
 * 
 */
public class PageRequest {

	private static final String NEXT_KEY_FIELD_NAME = "k";
	private static final String KEY_HISTORY_FIELD_NAME = "h";
	private static final String PAGE_SIZE_FIELD_NAME = "s";

	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final KeyIdPair FIRST_PAGE_NEXT_KEY_PLACEHOLDER = new KeyIdPair(NullNode.instance, "_ektorp_1k");

	private final int pageSize;
	private final KeyIdPair nextKey;
	private final Deque<KeyIdPair> keyHistory;

	public static ViewQuery applyPagingParameters(ViewQuery q, PageRequest pr) {
		if (pr.getStartKey() != null) {
			q.startKey(pr.getStartKey());
		}
		if (pr.getStartKeyDocId() != null) {
			q.startDocId(pr.getStartKeyDocId());
		}
		return q.limit(pr.getPageSize() + 1);
	}

	public static PageRequest firstPage(int pageSize) {
		return new PageRequest(null, pageSize, new LinkedList<KeyIdPair>());
	}

	private PageRequest(KeyIdPair nextKey, int pageSize, Deque<KeyIdPair> keyHistory) {
		this.nextKey = nextKey;
		this.pageSize = pageSize;
		this.keyHistory = keyHistory;
	}

	public static PageRequest fromLink(String link) {
		try {
			JsonNode n = MAPPER.readTree(new ByteArrayInputStream(Base64
					.decode(link, Base64.URL_SAFE)));

			KeyIdPair key = parseNextKey(n);
			Deque<KeyIdPair> keyHistory = parseKeyHistory(n);
			int pageSize = n.get(PAGE_SIZE_FIELD_NAME).getIntValue();
			
			return new PageRequest(key, pageSize, keyHistory);
		} catch (Exception e) {
			throw Exceptions.propagate(e);
		}
	}

	private static Deque<KeyIdPair> parseKeyHistory(JsonNode n) {
		Deque<KeyIdPair> keyHistory = new LinkedList<KeyIdPair>();
		ArrayNode h = (ArrayNode) n.get(KEY_HISTORY_FIELD_NAME);
		if (h != null) {
			for (JsonNode hn : h) {
				String docId = hn.getFieldNames().next();
				keyHistory.addFirst(new KeyIdPair(hn.get(docId), docId));
			}

		}
		return keyHistory;
	}

	private static KeyIdPair parseNextKey(JsonNode n) {
		KeyIdPair key;
		JsonNode nextKey = n.get(NEXT_KEY_FIELD_NAME);
		if (nextKey != null) {
			String docId = nextKey.getFieldNames().next();
			key = new KeyIdPair(nextKey.get(docId), docId);
		} else {
			key = null;
		}
		return key;
	}

	public String asLink() {
		try {
			return Base64.encodeBytes(MAPPER.writeValueAsBytes(asJson()),
					Base64.URL_SAFE);
		} catch (Exception e) {
			throw Exceptions.propagate(e);
		}
	}

	public JsonNode asJson() {
		ObjectNode n = MAPPER.createObjectNode();
		if (nextKey != null) {
			n.putObject(NEXT_KEY_FIELD_NAME).put(nextKey.docId, nextKey.key);
		}
		n.put(PAGE_SIZE_FIELD_NAME, pageSize);
		if (!keyHistory.isEmpty()) {
			ArrayNode h = n.putArray(KEY_HISTORY_FIELD_NAME);
			for (KeyIdPair k : keyHistory) {
				h.addObject().put(k.docId, k.key);
			}
		}
		return n;
	}

	public int getPageSize() {
		return pageSize;
	}

	public Object getStartKey() {
		return nextKey != null ? nextKey.key : null;
	}

	public String getStartKeyDocId() {
		return nextKey != null ? nextKey.docId : null;
	}
	/**
	 * @return the previous PageRequest or null if no previous page exists
	 */
	public PageRequest getPreviousPageRequest() {
		Deque<KeyIdPair> d = new LinkedList<KeyIdPair>(keyHistory);
		KeyIdPair previousKey = d.pollFirst();
		if (previousKey == null) {
			return null;
		}
		if (FIRST_PAGE_NEXT_KEY_PLACEHOLDER.equals(previousKey)) {
			d.clear();
			previousKey = null;
		}
		return new PageRequest(previousKey, pageSize, d);
	}

	public PageRequest getNextPageRequest(Object nextStartKey,
			String nextStartDocId) {
		try {
			JsonNode keyNode = MAPPER.readTree(MAPPER
					.writeValueAsString(nextStartKey));
			
			Deque<KeyIdPair> d = new LinkedList<KeyIdPair>(keyHistory);
			d.addFirst(this.nextKey != null ? this.nextKey : FIRST_PAGE_NEXT_KEY_PLACEHOLDER);

			return new PageRequest(new KeyIdPair(keyNode, nextStartDocId), pageSize, d);
		} catch (IOException e) {
			throw Exceptions.propagate(e);
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((keyHistory == null) ? 0 : keyHistory.size());
		result = prime * result + ((nextKey == null) ? 0 : nextKey.hashCode());
		result = prime * result + pageSize;
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
		if (keyHistory == null) {
			if (other.keyHistory != null)
				return false;
		} else if (keyHistory.size() != other.keyHistory.size())
			return false;
		if (nextKey == null) {
			if (other.nextKey != null)
				return false;
		} else if (!nextKey.equals(other.nextKey))
			return false;
		if (pageSize != other.pageSize)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return asJson().toString();
	}

	private static final class KeyIdPair {
		final JsonNode key;
		final String docId;

		KeyIdPair(JsonNode key, String docId) {
			this.key = key;
			this.docId = docId;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((docId == null) ? 0 : docId.hashCode());
			result = prime * result + ((key == null) ? 0 : key.toString().hashCode());
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
			KeyIdPair other = (KeyIdPair) obj;
			if (docId == null) {
				if (other.docId != null)
					return false;
			} else if (!docId.equals(other.docId))
				return false;
			if (key == null) {
				if (other.key != null)
					return false;
			} else if (!key.toString().equals(other.key.toString()))
				return false;
			return true;
		}
		
		
	}

}
