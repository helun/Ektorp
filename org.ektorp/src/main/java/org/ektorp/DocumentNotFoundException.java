package org.ektorp;

import com.fasterxml.jackson.databind.*;
/**
 *
 * @author Henrik Lundgren
 * created 7 nov 2009
 *
 */
public class DocumentNotFoundException extends DbAccessException {

	/**
	 *
	 */
	private static final long serialVersionUID = -4130993962797968754L;

	private final String path;
	private final JsonNode body;

	public DocumentNotFoundException(String path, JsonNode responseBody) {
		super(String.format("nothing found on db path: %s, Response body: %s", path, responseBody));
		this.path = path;
		this.body = responseBody;
	}

	public DocumentNotFoundException(String path) {
		super(String.format("nothing found on db path: %s", path));
		this.path = path;
		this.body = null;
	}

	private boolean checkReason(String expect) {
		if (body == null) {
			return false;
		}
		JsonNode reason = body.findPath("reason");
		return !reason.isMissingNode() ? reason.textValue().equals(expect) : false;
	}

	public boolean isDocumentDeleted() {
		return checkReason("deleted");
	}

	public boolean isDatabaseDeleted() {
		return checkReason("no_db_file");
	}

	public JsonNode getBody() {
		return body;
	}

	public String getPath() {
		return path;
	}
}
