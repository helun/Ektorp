package org.ektorp.changes;

import org.codehaus.jackson.*;
/**
 * Represents a document change within a database.
 * @author henrik lundgren
 *
 */
public interface DocumentChange {
	/**
	 * @return the database sequence number in which this change took place.
	 */
	int getSequence();
	/**
	 * 
	 * @return the id of the changed document.
	 */
	String getId();
	/**
	 * 
	 * @return the revision the document had at the time of change.
	 */
	String getRevision();
	/**
	 * 
	 * @return true if the changed document has been deleted.
	 */
	boolean isDeleted();
	/**
	 * If the feed has been setup to include docs, it will be available through this accessor.
	 * @return
	 */
	String getDoc();
	/**
	 * If the feed has been setup to include docs, it will be available through this accessor.
	 * @return
	 */
	JsonNode getDocAsNode();

}