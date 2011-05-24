package org.ektorp.util;
/**
 * Interface for accessing id and rev fields in a document of unknown type. 
 * @author henrik lundgren
 * @author Pascal G√©linas (issue 99)
 *
 */
public interface DocumentAccessor<T> {
	/**
	 * @return true if document type's id field can be mutated.
	 */
	boolean hasIdMutator();

	String getId(T o);

	void setId(T o, String id);

	String getRevision(T o);

	void setRevision(T o, String rev);

}