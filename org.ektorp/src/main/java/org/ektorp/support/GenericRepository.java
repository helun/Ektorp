package org.ektorp.support;

import java.util.*;

import org.ektorp.*;

/**
 * 
 * @author henrik lundgren
 *
 * @param <T>
 */
public interface GenericRepository<T> {
	/**
	 * 
	 * @param entity
	 * @throws UpdateConflictException if there was an update conflict.
	 */
	void add(T entity);
	/**
	 * 
	 * @param entity
	 * @throws UpdateConflictException if there was an update conflict.
	 */
	void update(T entity);
	/**
	 * 
	 * @param entity
	 * @throws UpdateConflictException if there was an update conflict.
	 */
	void remove(T entity);
	/**
	 * 
	 * @param id
	 * @return
	 * @throws DocumentNotFoundException if the document was not found.
	 */
	T get(String id);
	/**
	 * 
	 * @param id
	 * @param rev
	 * @return
	 * @throws DocumentNotFoundException if the document was not found.
	 */
	T get(String id, String rev);
	/**
	 * 
	 * @return empty list if nothing was found.
	 */
	List<T> getAll();
	/**
	 * 
	 * @param docId
	 * @return true if a document with the specified id exists in the database.
	 */
	boolean contains(String docId);
}
