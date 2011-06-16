package org.ektorp;

import java.util.*;

import org.ektorp.http.*;


/**
 * 
 * @author henrik lundgren
 *
 */
public interface CouchDbInstance {
	/**
	 * 
	 * @return the names of all databases residing in this instance.
	 */
	List<String> getAllDatabases();

	/**
	 * 
	 * @param db
	 * @return true if the database exists.
	 */
	boolean checkIfDbExists(DbPath db);

	void createDatabase(DbPath path);
	void createDatabase(String path);
	
	void deleteDatabase(String path);
	/**
	 * 
	 * @param path
	 * @param createIfNotExists
	 * @return
	 */
	CouchDbConnector createConnector(String path, boolean createIfNotExists);
	/**
	 * Convenience method for accessing the underlying HttpClient.
	 * Preferably used wrapped in a org.ektorp.http.RestTemplate.
	 * @return
	 */
	HttpClient getConnection();
	
	ReplicationStatus replicate(ReplicationCommand cmd);
}
