package org.ektorp.http;

import java.io.*;

import org.apache.commons.io.*;
import org.codehaus.jackson.*;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.node.*;
import org.ektorp.*;

/**
 * 
 * @author henrik lundgren
 *
 * @param <T>
 */
public class StdResponseHandler<T> implements ResponseCallback<T> {
	
	private final static ObjectMapper MAPPER = new ObjectMapper();
	/**
	 * Creates an DbAccessException which specific type is determined by the response code in the http response.
	 * @param hr
	 * @return
	 */
	public static DbAccessException createDbAccessException(HttpResponse hr) {
		JsonNode responseBody;
		try {
			responseBody = responseBodyAsNode(IOUtils.toString(hr.getContent()));
		} catch (IOException e) {
			responseBody = NullNode.getInstance();
		}
		switch (hr.getCode()) {
		case HttpStatus.NOT_FOUND:
			return new DocumentNotFoundException(hr.getRequestURI(), responseBody);
		case HttpStatus.CONFLICT:
			return new UpdateConflictException();
		default:
			String body;
			try {
				body = toPrettyString(responseBody);
			} catch (IOException e) {
				body = "unavailable";
			}
			return new DbAccessException(hr.toString() + "\nURI: " + hr.getRequestURI() + "\nResponse Body: \n" + body);
		}
	}
	
	private static String toPrettyString(JsonNode n) throws IOException {
		return MAPPER.defaultPrettyPrintingWriter().writeValueAsString(n);
	}
	
	private static JsonNode responseBodyAsNode(String s) throws IOException {
		if (s == null || s.length() == 0) {
			return NullNode.getInstance();
		} else if (!s.startsWith("{")) {
			return NullNode.getInstance();
		}
		return MAPPER.readTree(s);
	}

	public T error(HttpResponse hr) {
		throw StdResponseHandler.createDbAccessException(hr);
	}
	
	public T success(HttpResponse hr) throws Exception {
		return null;
	}

}
