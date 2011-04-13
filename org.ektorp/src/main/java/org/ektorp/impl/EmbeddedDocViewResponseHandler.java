package org.ektorp.impl;

import java.io.*;
import java.util.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.map.*;
import org.ektorp.*;
import org.ektorp.http.*;
import org.ektorp.util.*;
/**
 * Reads view result and extracts documents and maps them to the specified type.
 * 
 * Threadsafe.
 * 
 * @author henrik lundgren
 *
 * @param <T>
 */
public class EmbeddedDocViewResponseHandler<T> extends
		StdResponseHandler<List<T>> {

	private static final String ROWS_FIELD_NAME = "rows";
	private static final String VALUE_FIELD_NAME = "value";
	private static final String INCLUDED_DOC_FIELD_NAME = "doc";
	private static final String TOTAL_ROWS_FIELD_NAME = "total_rows";

	private final ObjectMapper mapper;
	private final Class<T> type;
	private final boolean ignoreNotFound;

	public EmbeddedDocViewResponseHandler(Class<T> docType, ObjectMapper om) {
		Assert.notNull(om, "ObjectMapper may not be null");
		Assert.notNull(docType, "docType may not be null");
		mapper = om;
		type = docType;
		ignoreNotFound = false;
	}

	public EmbeddedDocViewResponseHandler(Class<T> docType, ObjectMapper om,
			boolean ignoreNotFound) {
		Assert.notNull(om, "ObjectMapper may not be null");
		Assert.notNull(docType, "docType may not be null");
		mapper = om;
		type = docType;
		this.ignoreNotFound = ignoreNotFound;
	}

	@Override
	public List<T> success(HttpResponse hr) throws Exception {
		JsonParser jp = mapper.getJsonFactory().createJsonParser(
				hr.getContent());
		if (jp.nextToken() != JsonToken.START_OBJECT) {
			throw new RuntimeException("Expected data to start with an Object");
		}

		Map<String, String> fields = readHeaderFields(jp);
		assertNoErrors(fields);

		List<T> result;
		if (fields.containsKey(TOTAL_ROWS_FIELD_NAME)) {
			int totalRows = Integer.parseInt(fields.get(TOTAL_ROWS_FIELD_NAME));
			if (totalRows == 0) {
				return Collections.emptyList();
			}
			result = new ArrayList<T>(totalRows);
		} else {
			result = new ArrayList<T>();
		}

		ParseState state = new ParseState();

		T first = parseFirstRow(jp, state);
		if (first == null) {
			return Collections.emptyList();
		} else {
			result.add(first);
		}

		while (jp.getCurrentToken() != null) {
			skipToField(jp, state.docFieldName, state);
			if (atEndOfRows(jp)) {
				return result;
			}
			result.add(jp.readValueAs(type));
			endRow(jp, state);
		}
		return result;
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
				if (isInField("key", lastFieldName)) {
					state.lastKey = jp.readValueAsTree();
				} else if (isInField("error", lastFieldName)) {
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
		boolean inRow;
		JsonNode lastKey;
		String docFieldName = "";
	}
}
