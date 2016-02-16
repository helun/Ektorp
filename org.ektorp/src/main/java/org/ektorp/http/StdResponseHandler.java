package org.ektorp.http;

import java.io.*;

import org.apache.commons.io.*;
import org.ektorp.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;

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
            InputStream content = hr.getContent();
            if (content != null) {
                responseBody = responseBodyAsNode(IOUtils.toString(content));
            } else {
                responseBody = NullNode.getInstance();
            }
		} catch (Exception e) {
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
		return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(n);
	}

	private static JsonNode responseBodyAsNode(String s) throws IOException {
		if (s == null || s.length() == 0) {
			return NullNode.getInstance();
		} else if (!s.startsWith("{")) {
			return NullNode.getInstance();
		}
		return MAPPER.readTree(s);
	}

	protected static JsonNode responseBodyAsNode(InputStream inputStream, ObjectMapper mapper) throws IOException {
		return mapper.readTree(inputStream);
	}

	@edu.umd.cs.findbugs.annotations.SuppressWarnings(
		    value="NP_LOAD_OF_KNOWN_NULL_VALUE", 
		    justification="The response should be null only if it is not a valid OK message. "
		    		+ "In that case, it is safe to die. In any case, this method should be owerwritten if the class is extended.")
	protected static <T> T checkResponseBodyOkAndReturnDefaultValue(HttpResponse hr, T defaultValue, ObjectMapper mapper) throws IOException {
		InputStream content = null;
		try {
			content = hr.getContent();
			if(content == null){
				return defaultValue;
			}
			JsonNode body = responseBodyAsNode(content, MAPPER);
			JsonNode okNode = body.get("ok");
			if (okNode != null) {
				if (okNode.isBoolean()) {
					if (okNode.booleanValue()) {
						return defaultValue;
					}
				}
			}
			throw new DbAccessException("Unexpected response body content, expected {\"ok\":true}, got " + body.toString());
		} finally {
			IOUtils.closeQuietly(content);
		}
	}

	public T error(HttpResponse hr) {
		throw StdResponseHandler.createDbAccessException(hr);
	}
	
	public T success(HttpResponse hr) throws Exception {
		return checkResponseBodyOkAndReturnDefaultValue(hr, null, MAPPER);
	}

}
