package org.ektorp.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.ektorp.DbAccessException;
import org.ektorp.ViewResultException;

public class QueryResultParser<T> {

	private static final String ROWS_FIELD_NAME = "rows";
	private static final String VALUE_FIELD_NAME = "value";
	private static final String ID_FIELD_NAME = "id";
	private static final String ERROR_FIELD_NAME = "error";
	private static final String KEY_FIELD_NAME = "key";
	private static final String INCLUDED_DOC_FIELD_NAME = "doc";
	private static final String TOTAL_ROWS_FIELD_NAME = "total_rows";
	private static final String OFFSET_FIELD_NAME = "offset";
	
	private int totalRows = -1;
	private int offset = -1;
	private List<T> rows;
	
	private String firstId;
	private JsonNode firstKey;
	
	private String lastId;
	private JsonNode lastKey;
	
	private final ObjectMapper mapper;
	private final Class<T> type;
	private boolean ignoreNotFound;
	
	public QueryResultParser(Class<T> type, ObjectMapper mapper) {
		this.type = type;
		this.mapper = mapper;
	}
	
	public void parseResult(InputStream json) throws JsonParseException, IOException {
		JsonParser jp = mapper.getJsonFactory().createJsonParser(json);
		
		if (jp.nextToken() != JsonToken.START_OBJECT) {
			throw new RuntimeException("Expected data to start with an Object");
		}

		Map<String, String> fields = readHeaderFields(jp);
		assertNoErrors(fields);

		if (fields.containsKey(OFFSET_FIELD_NAME)) {
			offset = Integer.parseInt(fields.get(OFFSET_FIELD_NAME));
		}
		if (fields.containsKey(TOTAL_ROWS_FIELD_NAME)) {
			totalRows = Integer.parseInt(fields.get(TOTAL_ROWS_FIELD_NAME));
			if (totalRows == 0) {
				rows = Collections.emptyList();
				return;
			} else {
				rows = new ArrayList<T>(totalRows);
			}
		} else {
			rows = new ArrayList<T>();
		}

		ParseState state = new ParseState();

		T first = parseFirstRow(jp, state);
		if (first == null) {
			rows = Collections.emptyList();
		} else {
			rows.add(first);
		}

		while (jp.getCurrentToken() != null) {
			skipToField(jp, state.docFieldName, state);
			lastId = state.lastId;
			lastKey = state.lastKey;
			if (atEndOfRows(jp)) {
				return;
			}
			rows.add(jp.readValueAs(type));
			endRow(jp, state);
		}
	}
	
	public int getTotalRows() {
		return totalRows;
	}

	public int getOffset() {
		return offset;
	}

	public List<T> getRows() {
		return rows;
	}

	public void setIgnoreNotFound(boolean b) {
		this.ignoreNotFound = b;
	}
	
	private void assertNoErrors(Map<String, String> fields) {
		if (fields.containsKey("error")) {
			JsonNode error = mapper.convertValue(fields, JsonNode.class);
			throw new DbAccessException(error.toString());
		}
	}

	private T parseFirstRow(JsonParser jp, ParseState state)
			throws JsonParseException, IOException, JsonProcessingException,
			JsonMappingException {
		
		skipToField(jp, VALUE_FIELD_NAME, state);
		firstId = state.lastId;
		firstKey = state.lastKey;
		JsonNode value = null;
		if (atObjectStart(jp)) {
			value = jp.readValueAsTree();
			jp.nextToken();
			if (isEndOfRow(jp)) {
				state.docFieldName = VALUE_FIELD_NAME;
				T doc = mapper.readValue(value, type);
				endRow(jp, state);
				return doc;
			}
		}
		skipToField(jp, INCLUDED_DOC_FIELD_NAME, state);
		if (atObjectStart(jp)) {
			state.docFieldName = INCLUDED_DOC_FIELD_NAME;
			T doc = jp.readValueAs(type);
			endRow(jp, state);
			return doc;
		}
		return null;
	}

	private boolean isEndOfRow(JsonParser jp) {
		return jp.getCurrentToken() == JsonToken.END_OBJECT;
	}

	private void endRow(JsonParser jp, ParseState state) throws IOException,
			JsonParseException {
		state.inRow = false;
		jp.nextToken();
	}

	private boolean atObjectStart(JsonParser jp) {
		return jp.getCurrentToken() == JsonToken.START_OBJECT;
	}

	private boolean atEndOfRows(JsonParser jp) {
		return jp.getCurrentToken() != JsonToken.START_OBJECT;
	}

	private void skipToField(JsonParser jp, String fieldName, ParseState state)
			throws JsonParseException, IOException {
		String lastFieldName = null;
		while (jp.getCurrentToken() != null) {
			switch (jp.getCurrentToken()) {
			case FIELD_NAME:
				lastFieldName = jp.getCurrentName();
				jp.nextToken();
				break;
			case START_OBJECT:
				if (!state.inRow) {
					state.inRow = true;
					jp.nextToken();
				} else {
					if (isInField(fieldName, lastFieldName)) {
						return;
					} else {
						jp.skipChildren();
					}
				}
				break;
			default:
				if (isInField(ID_FIELD_NAME, lastFieldName)) {
					state.lastId = jp.readValueAsTree().getTextValue();
				} else if (isInField(KEY_FIELD_NAME, lastFieldName)) {
					state.lastKey = jp.readValueAsTree();
				} else if (isInField(ERROR_FIELD_NAME, lastFieldName)) {
					JsonNode error = jp.readValueAsTree();
					if (ignoreNotFound
							&& error.getValueAsText().equals("not_found")) {
						break;
					}
					throw new ViewResultException(state.lastKey,
							error.getValueAsText());
				} else if (isInField(fieldName, lastFieldName)) {
					jp.nextToken();
					return;
				}
				jp.nextToken();
				break;
			}
		}
	}

	private boolean isInField(String fieldName, String lastFieldName) {
		return lastFieldName != null && lastFieldName.equals(fieldName);
	}

	private Map<String, String> readHeaderFields(JsonParser jp)
			throws JsonParseException, IOException {
		Map<String, String> map = new HashMap<String, String>();
		jp.nextToken();
		String nextFieldName = jp.getCurrentName();
		while (nextFieldName != null && !ROWS_FIELD_NAME.equals(nextFieldName)) {
			jp.nextToken();
			map.put(nextFieldName, jp.getText());
			jp.nextToken();
			nextFieldName = jp.getCurrentName();
		}
		return map;
	}

	private static class ParseState {
		public String lastId;
		boolean inRow;
		JsonNode lastKey;
		String docFieldName = "";
	}

	public String getFirstId() {
		return firstId;
	}
	
	public JsonNode getFirstKey() {
		return firstKey;
	}

	public String getLastId() {
		return lastId;
	}
	
	public JsonNode getLastKey() {
		return lastKey;
	}
}
