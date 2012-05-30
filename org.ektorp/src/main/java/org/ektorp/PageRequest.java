package org.ektorp;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
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
	private static final String PAGE_SIZE_FIELD_NAME = "s";
	private static final String BACK_FIELD_NAME = "b";
	private static final String PAGE_FIELD_NAME = "p";

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private final int pageSize;
	private final KeyIdPair nextKey;
	private final boolean back;
	private final int page;

	public static ViewQuery applyPagingParameters(ViewQuery q, PageRequest pr) {
		ViewQuery pagedQuery = q.clone();
		if (pr.page > 0) {
			if (pr.getStartKey() != null) {
				pagedQuery.startKey(pr.getStartKey());
			}
			if (pr.getStartKeyDocId() != null) {
				pagedQuery.startDocId(pr.getStartKeyDocId());
			}
			if (pr.back) {
				pagedQuery.descending(!pagedQuery.isDescending());
			}	
		}
		
		int offset = pr.back ? 1 : 1;
		pagedQuery.limit(pr.getPageSize() + offset);
		return pagedQuery;
	}

	public int getPageNo() {
		return page;
	}
	
	public static PageRequest firstPage(int pageSize) {
		return new Builder().pageSize(pageSize).build();
	}
	
	public PageRequest.Builder nextRequest(Object nextStartKey,
			String nextStartDocId) {
		try {
			JsonNode keyNode = MAPPER.readTree(MAPPER
					.writeValueAsString(nextStartKey));
			return new Builder(this)
						.nextKey(new KeyIdPair(keyNode, nextStartDocId));
		} catch (IOException e) {
			throw Exceptions.propagate(e);
		}
	}

	private PageRequest(Builder b) {
		this.back = b.back;
		this.page = b.page;
		this.nextKey = b.nextKey;
		this.pageSize = b.pageSize;
	}

	public static PageRequest fromLink(String link) {
		try {
			JsonNode n = MAPPER.readTree(new ByteArrayInputStream(Base64
					.decode(link, Base64.URL_SAFE)));

			KeyIdPair key = parseNextKey(n);
			int pageSize = n.get(PAGE_SIZE_FIELD_NAME).getIntValue();
			boolean back = n.get(BACK_FIELD_NAME).asInt() == 1;
			int page = n.get(PAGE_FIELD_NAME).asInt();
			return new Builder()
						.nextKey(key)
						.pageSize(pageSize)
						.back(back)
						.page(page)
						.build();
		} catch (Exception e) {
			throw Exceptions.propagate(e);
		}
	}

	private static KeyIdPair parseNextKey(JsonNode n) {
		return parseKey(NEXT_KEY_FIELD_NAME, n);
	}

	private static KeyIdPair parseKey(String fieldName, JsonNode n) {
		KeyIdPair key;
		JsonNode nextKey = n.get(fieldName);
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
		n.put(BACK_FIELD_NAME, back ? 1 : 0);
		n.put(PAGE_FIELD_NAME, page);
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
	 * 
	 * @param startKey
	 * @param startDocId
	 * @return
	 */
	public PageRequest getPreviousPageRequest() {
		return new Builder(this)
					.page(this.page-1)
					.build();
	}

	
	public boolean isBack() {
		return back;
	}

	@Override
	public String toString() {
		return asJson().toString();
	}

	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (back ? 1231 : 1237);
		result = prime * result + ((nextKey == null) ? 0 : nextKey.hashCode());
		result = prime * result + page;
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
		if (back != other.back)
			return false;
		if (nextKey == null) {
			if (other.nextKey != null)
				return false;
		} else if (!nextKey.equals(other.nextKey))
			return false;
		if (page != other.page)
			return false;
		if (pageSize != other.pageSize)
			return false;
		return true;
	}



	public static class Builder {
		
		private int pageSize;
		private KeyIdPair nextKey;
		private boolean back;
		private int page;
		
		public Builder() {
			
		}
		
		public Builder(PageRequest prototype) {
			this.back = prototype.back;
			this.nextKey = prototype.nextKey;
			this.page = prototype.page;
			this.pageSize = prototype.pageSize; 
		}
		
		public Builder pageSize(int i) {
			this.pageSize = i;
			return this;
		}
		
		public Builder page(int i) {
			this.page = i;
			return this;
		}
		
		public Builder back(boolean b) {
			this.back = b;
			return this;
		}
		
		public Builder nextKey(KeyIdPair k) {
			this.nextKey = k;
			return this;
		}
		
		public PageRequest build() {
			return new PageRequest(this);
		}

		public int getPageNo() {
			return page;
		}
		
		public int getNextPage() {
			return page + 1;
		}
		
		public int getPrevPage() {
			return page - 1;
		}
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
