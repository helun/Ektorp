package org.ektorp;

import org.codehaus.jackson.*;

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
	
	public boolean isDocumentDeleted() {
		if (body == null) {
			return false;
		}
		JsonNode reason = body.findPath("reason"); 
		return !reason.isMissingNode() ? reason.getTextValue().equals("deleted") : false;
	}
	
	public String getPath() {
		return path;
	}
}
