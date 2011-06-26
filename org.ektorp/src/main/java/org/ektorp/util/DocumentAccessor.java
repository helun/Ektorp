package org.ektorp.util;
/**
 * Interface for accessing id and rev fields in a document of unknown type. 
 * @author henrik lundgren
 *
 */
public interface DocumentAccessor {
	/**
	 * @return true if document type's id field can be mutated.
	 */
	boolean hasIdMutator();

	String getId(Object o);

	void setId(Object o, String id);

	String getRevision(Object o);

	void setRevision(Object o, String rev);

}