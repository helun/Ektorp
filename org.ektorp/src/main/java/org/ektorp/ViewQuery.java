package org.ektorp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.ektorp.http.URI;
import org.ektorp.impl.StdObjectMapperFactory;
import org.ektorp.util.Assert;
import org.ektorp.util.Exceptions;

/**
 *
 * @author henrik lundgren
 *
 */
public class ViewQuery {

	private class KeyOrRawKey {

		private String rawKey;

		private Object key;

		private boolean isRaw;

		public void setKey(Object value) {
			this.key = value;
			this.rawKey = null;
			isRaw = false;
		}

		public void setRawKey(String value) {
			this.key = null;
			this.rawKey = value;
			isRaw = true;
		}

		public boolean isNull() {
			if (isRaw) {
				return rawKey == null;
			} else {
				return key == null;
			}
		}

		public String asEncoded() {
			if (isRaw) {
				return rawKey;
			} else {
				try {
					return mapper.writeValueAsString(key);
				} catch (Exception e) {
					throw Exceptions.propagate(e);
				}
			}
		}

		public Object asDecoded() {
			if (isRaw) {
				return parseJson(rawKey);
			} else {
				return key;
			}
		}

		public void copyTo(KeyOrRawKey other) {
			if (isRaw) {
				other.setRawKey(rawKey);
			} else {
				other.setKey(key);
			}
		}
		
	}
	

	private final static ObjectMapper DEFAULT_MAPPER = new StdObjectMapperFactory().createObjectMapper();
	private final static String ALL_DOCS_VIEW_NAME = "_all_docs";
	private final static int NOT_SET = -1;

	private final Map<String, String> queryParams = new TreeMap<String, String>();

	private ObjectMapper mapper;

	private String dbPath;
	private String designDocId;
	private String viewName;
    private final KeyOrRawKey key = new KeyOrRawKey();
    private Keys keys;
	private final KeyOrRawKey startKey = new KeyOrRawKey();
	private String startDocId;
	private final KeyOrRawKey endKey = new KeyOrRawKey();
	private String endDocId;
	private int limit = NOT_SET;
	private String staleOk;
	private boolean descending;
	private int skip = NOT_SET;
	private boolean group;
	private int groupLevel = NOT_SET;
	private boolean reduce = true;
	private boolean includeDocs = false;
	private boolean inclusiveEnd = true;
	private boolean ignoreNotFound = false;
	private boolean updateSeq = false;

	private boolean cacheOk = false;

	private String cachedQuery;
	private String listName;

	public ViewQuery() {
		mapper = DEFAULT_MAPPER;
	}
	/**
	 * Bring your own ObjectMapper.
	 * The mapper is used when serializing keys when building the query.
	 * @param om
	 */
	public ViewQuery(ObjectMapper om) {
		Assert.notNull(om, "ObjectMapper may not be null");
		mapper = om;
	}

    public String getDbPath() {
        return dbPath;
    }

    public String getDesignDocId() {
        return designDocId;
    }

    public String getViewName() {
        return viewName;
    }

    public String getStartDocId() {
        return startDocId;
    }

    public String getEndDocId() {
        return endDocId;
    }

    public int getLimit() {
        return limit;
    }

    public boolean isStaleOk() {
        return staleOk != null && ("ok".equals(staleOk) || "update_after".equals(staleOk));
    }

    public boolean isDescending() {
        return descending;
    }

    public int getSkip() {
        return skip;
    }

    public boolean isGroup() {
        return group;
    }

    public int getGroupLevel() {
        return groupLevel;
    }

    public boolean isReduce() {
        return reduce;
    }

    public boolean isIncludeDocs() {
        return includeDocs;
    }

    public boolean isInclusiveEnd() {
        return inclusiveEnd;
    }

    public boolean isUpdateSeq() {
        return updateSeq;
    }

    public ViewQuery dbPath(String s) {
		reset();
		dbPath = s;
		return this;
	}

	public ViewQuery designDocId(String s) {
		reset();
		designDocId = s;
		return this;
	}
	/**
	 * Will automatically set the query special _all_docs URI.
	 * In this case, setting designDocId will have no effect.
	 * @return
	 */
	public ViewQuery allDocs() {
		reset();
		viewName = ALL_DOCS_VIEW_NAME;
		return this;
	}

	public ViewQuery viewName(String s) {
		reset();
		viewName = s;
		return this;
	}

	public ViewQuery listName(String s) {
		reset();
		listName = s;
		return this;
	}
	/**
	 * If set to true, the view query result will be cached and subsequent queries
	 * (with cacheOk set) may be served from the cache instead of the db.
	 *
	 * Note that if the view changes, the cache will be invalidated.
	 *
	 * @param b
	 * @return
	 */
	public ViewQuery cacheOk(boolean b) {
		reset();
		cacheOk = b;
		return this;
	}

	public boolean isCacheOk() {
		return cacheOk;
	}
	/**
	 * @param Will be JSON-encoded.
	 * @return the view query for chained calls
	 */
	public ViewQuery key(String s) {
		reset();
		key.setKey(s);
		return this;
	}
	/**
	 * @param Will be parsed as JSON.
	 * @return the view query for chained calls
	 */
	public ViewQuery rawKey(String s) {
		reset();
		key.setRawKey(s);
		return this;
	}

	private JsonNode parseJson(String s) {
		try {
			return mapper.readTree(s);
		} catch (Exception e) {
			throw Exceptions.propagate(e);
		}
	}
	/**
	 * @param Will be JSON-encoded.
	 * @return the view query for chained calls
	 */
	public ViewQuery key(int i) {
		reset();
		key.setKey(i);
		return this;
	}
	/**
	 * @param Will be JSON-encoded.
	 * @return the view query for chained calls
	 */
	public ViewQuery key(long l) {
		reset();
		key.setKey(l);
		return this;
	}
	/**
	 * @param Will be JSON-encoded.
	 * @return the view query for chained calls
	 */
	public ViewQuery key(float f) {
		reset();
		key.setKey(f);
		return this;
	}
	/**
	 * @param Will be JSON-encoded.
	 * @return the view query for chained calls
	 */
	public ViewQuery key(double d) {
		reset();
		key.setKey(d);
		return this;
	}
	/**
	 * @param Will be JSON-encoded.
	 * @return the view query for chained calls
	 */
	public ViewQuery key(boolean b) {
		reset();
		key.setKey(b);
		return this;
	}
	/**
	 * @param Will be JSON-encoded.
	 * @return the view query for chained calls
	 */
	public ViewQuery key(Object o) {
		reset();
		key.setKey(o);
		return this;
	}
    /**
     * For multiple-key queries (as of CouchDB 0.9). Keys will be JSON-encoded.
     * @param keyList a list of Object, will be JSON encoded according to each element's type.
     * @return the view query for chained calls
     */
    public ViewQuery keys(Collection<?> keyList) {
        reset();
        keys = Keys.of(keyList);
        return this;
    }

    public Keys getKeys() {
        return keys;
    }

    public Collection<?> getKeysValues() {
        return keys.getValues();
    }

	/**
	 * @param Will be JSON-encoded.
	 * @return the view query for chained calls
	 */
	public ViewQuery startKey(String s) {
		reset();
		startKey.setKey(s);
		return this;
	}

	/**
	 * @param Will be parsed as json
	 * @return the view query for chained calls
	 */
	public ViewQuery rawStartKey(String s) {
		reset();
		startKey.setRawKey(s);
		return this;
	}
	/**
	 * @param Will be JSON-encoded.
	 * @return the view query for chained calls
	 */
	public ViewQuery startKey(int i) {
		reset();
		startKey.setKey(i);
		return this;
	}
	/**
	 * @param Will be JSON-encoded.
	 * @return the view query for chained calls
	 */
	public ViewQuery startKey(long l) {
		reset();
		startKey.setKey(l);
		return this;
	}
	/**
	 * @param Will be JSON-encoded.
	 * @return the view query for chained calls
	 */
	public ViewQuery startKey(float f) {
		reset();
		startKey.setKey(f);
		return this;
	}
	/**
	 * @param Will be JSON-encoded.
	 * @return the view query for chained calls
	 */
	public ViewQuery startKey(double d) {
		reset();
		startKey.setKey(d);
		return this;
	}
	/**
	 * @param Will be JSON-encoded.
	 * @return the view query for chained calls
	 */
	public ViewQuery startKey(boolean b) {
		reset();
		startKey.setKey(b);
		return this;
	}

	/**
	 * @param Will be JSON-encoded.
	 * @return the view query for chained calls
	 */
	public ViewQuery startKey(Object o) {
		reset();
		startKey.setKey(o);
		return this;
	}

	public ViewQuery startDocId(String s) {
		reset();
		startDocId = s;
		return this;
	}
	/**
	 * @param will be JSON-encoded.
     * @return the view query for chained calls
	 */
	public ViewQuery endKey(String s) {
		reset();
		endKey.setKey(s);
		return this;
	}
	/**
	 * @param will be parsed as JSON.
     * @return the view query for chained calls
	 */
	public ViewQuery rawEndKey(String s) {
		reset();
		endKey.setRawKey(s);
		return this;
	}
	/**
	 * @param Will be JSON-encoded.
	 * @return the view query for chained calls
	 */
	public ViewQuery endKey(int i) {
		reset();
		endKey.setKey(i);
		return this;
	}
	/**
	 * @param Will be JSON-encoded.
	 * @return the view query for chained calls
	 */
	public ViewQuery endKey(long l) {
		reset();
		endKey.setKey(l);
		return this;
	}
	/**
	 * @param Will be JSON-encoded.
	 * @return the view query for chained calls
	 */
	public ViewQuery endKey(float f) {
		reset();
		endKey.setKey(f);
		return this;
	}
	/**
	 * @param Will be JSON-encoded.
	 * @return the view query for chained calls
	 */
	public ViewQuery endKey(double d) {
		reset();
		endKey.setKey(d);
		return this;
	}
	/**
	 * @param Will be JSON-encoded.
	 * @return the view query for chained calls
	 */
	public ViewQuery endKey(boolean b) {
		reset();
		endKey.setKey(b);
		return this;
	}

	/**
	 * @param Will be JSON-encoded.
	 * @return the view query for chained calls
	 */
	public ViewQuery endKey(Object o) {
		reset();
		endKey.setKey(o);
		return this;
	}

	public ViewQuery endDocId(String s) {
		reset();
		endDocId = s;
		return this;
	}
	/**
	 * limit=0 you don't get any data, but all meta-data for this View. The number of documents in this View for example.
	 * @param i the limit
     * @return the view query for chained calls
	 */
	public ViewQuery limit(int i) {
		reset();
		limit = i;
		return this;
	}
	/**
	 * The stale option can be used for higher performance at the cost of possibly not seeing the all latest data. If you set the stale option to ok, CouchDB may not perform any refreshing on the view that may be necessary.
	 * @param b the staleOk flag
     * @return the view query for chained calls
	 */
	public ViewQuery staleOk(boolean b) {
		reset();
		staleOk = b ? "ok" : null;
		return this;
	}
	/**
	 * Same as staleOk(true) but will also trigger a rebuild of the view index after the results of the view have been retrieved.
	 * (since CouchDB 1.1.0)
	 * @return
	 */
	public ViewQuery staleOkUpdateAfter() {
		reset();
		staleOk = "update_after";
		return this;
	}
	/**
	 * View rows are sorted by the key; specifying descending=true will reverse their order. Note that the descending option is applied before any key filtering, so you may need to swap the values of the startkey and endkey options to get the expected results.
	 * @param b the descending flag
     * @return the view query for chained calls
	 */
	public ViewQuery descending(boolean b) {
		reset();
		descending = b;
		return this;
	}
	/**
	 * The skip option should only be used with small values, as skipping a large range of documents this way is inefficient (it scans the index from the startkey and then skips N elements, but still needs to read all the index values to do that). For efficient paging you'll need to use startkey and limit. If you expect to have multiple documents emit identical keys, you'll need to use startkey_docid in addition to startkey to paginate correctly. The reason is that startkey alone will no longer be sufficient to uniquely identify a row.
	 * @param i the skip count
     * @return the view query for chained calls
	 */
	public ViewQuery skip(int i) {
		reset();
		skip = i;
		return this;
	}
	/**
	 * The group option controls whether the reduce function reduces to a set of distinct keys or to a single result row.
	 * @param b the group flag
     * @return the view query for chained calls
	 */
	public ViewQuery group(boolean b) {
		reset();
		group = b;
		return this;
	}

	public ViewQuery groupLevel(int i) {
		reset();
		groupLevel = i;
		return this;
	}
	/**
	 * If a view contains both a map and reduce function, querying that view will by default return the result of the reduce function. The result of the map function only may be retrieved by passing reduce=false as a query parameter.
     * @param b the reduce flag
     * @return the view query for chained calls
	 */
	public ViewQuery reduce(boolean b) {
		reset();
		reduce = b;
		return this;
	}
	/**
	 * The include_docs option will include the associated document. Although, the user should keep in mind that there is a race condition when using this option. It is possible that between reading the view data and fetching the corresponding document that the document has changed. If you want to alleviate such concerns you should emit an object with a _rev attribute as in emit(key, {"_rev": doc._rev}). This alleviates the race condition but leaves the possiblity that the returned document has been deleted (in which case, it includes the "_deleted": true attribute).
	 * @param b the includeDocs flag
     * @return the view query for chained calls
	 */
	public ViewQuery includeDocs(boolean b) {
		reset();
		includeDocs = b;
		return this;
	}
	/**
	 * The inclusive_end option controls whether the endkey is included in the result. It defaults to true.
	 * @param b the inclusiveEnd flag
     * @return the view query for chained calls
	 */
	public ViewQuery inclusiveEnd(boolean b) {
		reset();
		inclusiveEnd = b;
		return this;
	}

	/**
	 * The update_seq option adds a field to the result indicating the update_seq the view reflects.  It defaults to false.
	 * @param b the updateSeq flag
	 * @return the view query for chained calls
	 */
	public ViewQuery updateSeq(boolean b) {
		reset();
		updateSeq = b;
		return this;
	}

	public ViewQuery queryParam(String name, String value) {
		queryParams.put(name, value);
		return this;
	}

	/**
	 * Resets internal state so this builder can be used again.
	 */
	public void reset() {
		cachedQuery = null;
	}

	public Object getKey() {
		return key.asDecoded();
	}

    public boolean hasMultipleKeys() {
    	return keys != null;
    }

    public String getKeysAsJson() {
    	if (keys == null) {
    		return "{\"keys\":[]}";
    	}
        return keys.toJson(mapper);
    }


    public Object getStartKey() {
		return startKey.asDecoded();
	}

	public Object getEndKey() {
		return endKey.asDecoded();
	}

	public String buildQuery() {
		if (cachedQuery != null) {
			return cachedQuery;
		}

        URI query = buildQueryURI();

		cachedQuery = query.toString();
		return cachedQuery;
	}

    public URI buildQueryURI() {
		URI query = buildViewPath();

		if (!key.isNull()) {
			query.param("key", key.asEncoded());
		}

		if (!startKey.isNull()) {
			query.param("startkey", startKey.asEncoded());
		}

		if (!endKey.isNull()) {
			query.param("endkey", endKey.asEncoded());
		}

		if (isNotEmpty(startDocId)) {
			query.param("startkey_docid", startDocId);
		}

		if (isNotEmpty(endDocId)) {
			query.param("endkey_docid", endDocId);
		}

		if (hasValue(limit)) {
			query.param("limit", limit);
		}

		if (staleOk != null) {
			query.param("stale", staleOk);
		}

		if (descending) {
			query.param("descending", "true");
		}

		if (!inclusiveEnd) {
			query.param("inclusive_end", "false");
		}

		if (!reduce) {
			query.param("reduce", "false");
		}

		if (hasValue(skip)) {
			query.param("skip", skip);
		}

		if (includeDocs) {
			query.param("include_docs", "true");
		}

		if (group) {
			query.param("group", "true");
		}

		if (hasValue(groupLevel)) {
			query.param("group_level", groupLevel);
		}

		if (!queryParams.isEmpty()) {
			appendQueryParams(query);
		}

		if(updateSeq) {
			query.param("update_seq", "true");
		}
		return query;
	}

    @edu.umd.cs.findbugs.annotations.SuppressWarnings({"SA_FIELD_SELF_ASSIGNMENT", "CN_IMPLEMENTS_CLONE_BUT_NOT_CLONEABLE"})
	public ViewQuery clone() {
		ViewQuery copy = new ViewQuery();
		copy.mapper = mapper;
		copy.cacheOk = cacheOk;
		copy.dbPath = dbPath;
		copy.descending = descending;
		copy.designDocId = designDocId;
		copy.endDocId = endDocId;
		endKey.copyTo(copy.endKey);
		copy.group = group;
		copy.groupLevel = groupLevel;
		copy.ignoreNotFound = ignoreNotFound;
		copy.includeDocs = includeDocs;
		copy.inclusiveEnd = inclusiveEnd;
		key.copyTo(copy.key);
		if (keys != null) {
			copy.keys = keys.clone();
		}
		copy.limit = limit;
		copy.listName = listName;
		copy.queryParams.putAll(queryParams);
		copy.reduce = reduce;
		copy.skip = skip;
		copy.staleOk = staleOk;
		copy.startDocId = startDocId;
		startKey.copyTo(copy.startKey);
		copy.updateSeq = updateSeq;
		copy.viewName = viewName;
		return copy;
	}
	
	private void appendQueryParams(URI query) {
		for (Map.Entry<String, String> param : queryParams.entrySet()) {
			query.param(param.getKey(), param.getValue());
		}
	}

	private URI buildViewPath() {
		assertHasText(dbPath, "dbPath");
		assertHasText(viewName, "viewName");

		URI uri = URI.of(dbPath);
		if (isNotEmpty(listName)) {
			uri.append(designDocId).append("_list").append(listName).append(viewName);
		} else if (ALL_DOCS_VIEW_NAME.equals(viewName)) {
			uri.append(viewName);
		} else {
			assertHasText(designDocId, "designDocId");
			uri.append(designDocId).append("_view").append(viewName);
		}
		return uri;
	}

	private void assertHasText(String s, String fieldName) {
		if (s == null || s.length() == 0) {
			throw new IllegalStateException(String.format("%s must have a value", fieldName));
		}
	}

	private boolean hasValue(int i) {
		return i != NOT_SET;
	}

	private boolean isNotEmpty(Object s) {
		return s != null;
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((cachedQuery == null) ? 0 : cachedQuery.hashCode());
		result = prime * result + ((dbPath == null) ? 0 : dbPath.hashCode());
		result = prime * result + (descending ? 1231 : 1237);
		result = prime * result
				+ ((designDocId == null) ? 0 : designDocId.hashCode());
		result = prime * result
				+ ((endDocId == null) ? 0 : endDocId.hashCode());
		result = prime * result + ((endKey == null) ? 0 : endKey.hashCode());
		result = prime * result + (group ? 1231 : 1237);
		result = prime * result + groupLevel;
		result = prime * result + (ignoreNotFound ? 1231 : 1237);
		result = prime * result + (includeDocs ? 1231 : 1237);
		result = prime * result + (inclusiveEnd ? 1231 : 1237);
		result = prime * result + (updateSeq ? 1231 : 1237);
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + limit;
		result = prime * result
				+ ((listName == null) ? 0 : listName.hashCode());
		result = prime * result
				+ ((queryParams == null) ? 0 : queryParams.hashCode());
		result = prime * result + (reduce ? 1231 : 1237);
		result = prime * result + skip;
		result = prime * result + ((staleOk == null) ? 0 : staleOk.hashCode());
		result = prime * result
				+ ((startDocId == null) ? 0 : startDocId.hashCode());
		result = prime * result
				+ ((startKey == null) ? 0 : startKey.hashCode());
		result = prime * result
				+ ((viewName == null) ? 0 : viewName.hashCode());
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
		ViewQuery other = (ViewQuery) obj;
		if (cachedQuery == null) {
			if (other.cachedQuery != null)
				return false;
		} else if (!cachedQuery.equals(other.cachedQuery))
			return false;
		if (dbPath == null) {
			if (other.dbPath != null)
				return false;
		} else if (!dbPath.equals(other.dbPath))
			return false;
		if (descending != other.descending)
			return false;
		if (designDocId == null) {
			if (other.designDocId != null)
				return false;
		} else if (!designDocId.equals(other.designDocId))
			return false;
		if (endDocId == null) {
			if (other.endDocId != null)
				return false;
		} else if (!endDocId.equals(other.endDocId))
			return false;
		if (endKey == null) {
			if (other.endKey != null)
				return false;
		} else if (!endKey.equals(other.endKey))
			return false;
		if (group != other.group)
			return false;
		if (groupLevel != other.groupLevel)
			return false;
		if (ignoreNotFound != other.ignoreNotFound)
			return false;
		if (includeDocs != other.includeDocs)
			return false;
		if (inclusiveEnd != other.inclusiveEnd)
			return false;
		if (updateSeq != other.updateSeq)
			return false;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (limit != other.limit)
			return false;
		if (listName == null) {
			if (other.listName != null)
				return false;
		} else if (!listName.equals(other.listName))
			return false;
		if (queryParams == null) {
			if (other.queryParams != null)
				return false;
		} else if (!queryParams.equals(other.queryParams))
			return false;
		if (reduce != other.reduce)
			return false;
		if (skip != other.skip)
			return false;
		if (staleOk == null) {
			if (other.staleOk != null)
				return false;
		} else if (!staleOk.equals(other.staleOk))
			return false;
		if (startDocId == null) {
			if (other.startDocId != null)
				return false;
		} else if (!startDocId.equals(other.startDocId))
			return false;
		if (startKey == null) {
			if (other.startKey != null)
				return false;
		} else if (!startKey.equals(other.startKey))
			return false;
		if (viewName == null) {
			if (other.viewName != null)
				return false;
		} else if (!viewName.equals(other.viewName))
			return false;
		return true;
	}
	public void setIgnoreNotFound(boolean ignoreNotFound) {
		this.ignoreNotFound = ignoreNotFound;
	}

	public boolean isIgnoreNotFound() {
		return ignoreNotFound;
	}
	
	@Override
	public String toString() {
		return buildQuery();
	}

	public static class Keys {

		private final List<?> keys;

		public static Keys of(Collection<?> keys) {
			return new Keys(keys.toArray());
		}

		public static Keys of(Object... keys) {
			return new Keys(keys);
		}

		private Keys(Collection<?> keys) {
			this.keys = new ArrayList<Object>(keys);
		}

		private Keys(Object[] keys) {
			this.keys = Arrays.asList(keys);
		}

        public List<?> getValues() {
            return Collections.unmodifiableList(keys);
		}
		
        @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="CN_IMPLEMENTS_CLONE_BUT_NOT_CLONEABLE")
		public Keys clone() {
			return new Keys(keys);
		}

		public String toJson() {
			return toJson(DEFAULT_MAPPER);
		}

		public String toJson(ObjectMapper mapper) {
			ObjectNode rootNode = mapper.createObjectNode();
			ArrayNode keysNode = rootNode.putArray("keys");
			for (Object key : keys) {
				keysNode.addPOJO(key);
			}
			try {
				return mapper.writeValueAsString(rootNode);
			} catch (Exception e) {
				throw Exceptions.propagate(e);
			}
		}
	}



}
