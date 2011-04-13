package org.ektorp.http;
/**
 * 
 * @author henrik lundgren
 *
 */
public final class HttpStatus {
	/**
	 * Request completed successfully.
	 */
	public final static int OK = 200;
	/**
	 * Document created successfully.
	 */
	public final static int CREATED = 201;
	/**
	 * Request for database compaction completed successfully.
	 */
	public final static int ACCEPTED = 202;
	/**
	 * Etag not modified since last update.
	 */
	public final static int NOT_MODIFIED = 304;
	/**
	 * Request given is not valid in some way.
	 */
	public final static int BAD_REQUEST = 400;
	/**
	 * Request for a document which doesn't exist.
	 */
	public final static int NOT_FOUND = 404;
	/**
	 * Request was accessing a non-existent URL. For example, if you have a malformed URL, or are using a third party library that is targeting a different version of CouchDB.
	 */
	public final static int RESOURCE_NOT_ALLOWED = 405;
	/**
	 * Request resulted in an update conflict.
	 */
	public final static int CONFLICT = 409;
	/**
	 * Request attempted to created database which already exists.
	 */
	public final static int PRECONDITION_FAILED = 412;
	/**
	 * Request contained invalid JSON, probably happens in other cases too.
	 */
	public final static int INTERNAL_SERVER_ERROR = 500;
	
}
