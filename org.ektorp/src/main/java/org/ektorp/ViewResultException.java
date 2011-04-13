package org.ektorp;

import org.codehaus.jackson.*;

public class ViewResultException extends DbAccessException {

	private static final long serialVersionUID = 8051194912659215094L;

	private final JsonNode key;
	private final String error;
	
	public ViewResultException(JsonNode key, String error) {
		super(String.format("key: %s error: \"%s\"", key, error));
		this.key = key;
		this.error = error;
	}
	
	public JsonNode getKey() {
		return key;
	}
	
	public String getError() {
		return error;
	}
}
