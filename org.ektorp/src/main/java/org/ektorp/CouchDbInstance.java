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
	boolean checkIfDbExists(String path);

	void createDatabase(DbPath path);
	void createDatabase(String path);
	boolean createDatabaseIfNotExists(DbPath path);
	boolean createDatabaseIfNotExists(String path);

	void deleteDatabase(String path);
	/**
	 * 
	 * @param path
	 * @param createIfNotExists
	 * @return
	 */
	CouchDbConnector createConnector(String path, boolean createIfNotExists);

    /**
     * Returns the Couch _replicator database
     * @return CouchDbConnector a connector to the replicator database
     */
    CouchDbConnector getReplicatorConnector();

	/**
	 * Convenience method for accessing the underlying HttpClient.
	 * Preferably used wrapped in a org.ektorp.http.RestTemplate.
	 * @return
	 */
	HttpClient getConnection();
	
	ReplicationStatus replicate(ReplicationCommand cmd);

    /**
     * Get the full configuration of this instance
     * @param c the type to return the configuration in (Map, JsonNode, POJO)
     * @return
     */
    <T> T getConfiguration(final Class<T> c);

    /**
     * Get the configuration of this instance within the specified section
     * @param c the type to return the configuration in (Map, JsonNode, POJO)
     * @param section
     * @return
     */
    <T> T getConfiguration(final Class<T> c, String section);

    /**
     * Get the configuration of this instance for this specific section and key
     *
     * @param c the type to return the configuration in (Map, JsonNode, POJO)
     * @param section
     * @param key
     * @return
     */
    <T> T getConfiguration(final Class<T> c, String section, String key);

    /**
     * Convenience method to get specific configuration item
     * @param section
     * @param key
     * @return the configuration value for the specified key in the specified section
     */
    String getConfiguration(String section, String key);

    /**
     * Update the configuration key in the specified section with the specified value
     * @param section
     * @param key
     * @param value the value to set (all config values are Strings in CouchDB)
     * @return the previous value for this key
     */
    String setConfiguration(String section, String key, String value);

    /**
     * Delete the configuration key in the specified section
     * @param section
     * @param key
     * @return the previous value for this key
     */
    String deleteConfiguration(String section, String key);

    /**
     * @return all active tasks
     */
    Collection<ActiveTask> getActiveTasks();
}
