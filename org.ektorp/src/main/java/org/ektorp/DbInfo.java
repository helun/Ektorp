package org.ektorp;

import java.io.*;
import java.util.*;

import org.codehaus.jackson.annotate.*;
import org.ektorp.util.*;
/**
 * 
 * @author henrik lundgren
 *
 */
public class DbInfo implements Serializable {

	private static final long serialVersionUID = -6511885014968791685L;

	private final String dbName;
	
	@JsonProperty("compact_running")
	boolean compactRunning;	 
	@JsonProperty("disk_format_version")
    int diskFormatVersion;
	@JsonProperty("disk_size")
	long diskSize;
	@JsonProperty("doc_count")
	long docCount;
	@JsonProperty("doc_del_count")
	int docDelCount;
	@JsonProperty("instance_start_time")
	long instanceStartTime;
	@JsonProperty("purge_seq")
	int purgeSeq;
	@JsonProperty("update_seq")
	String updateSeq;
	/**
	 * Used to future proof this class, if new fields are added by CouchDb they will be found here.
	 */
	private Map<String, Object> unknownFields;
	
	public boolean isCompactRunning() {
		return compactRunning;
	}
	/**
	 * @return Name of the database
	 */
	public String getDbName() {
		return dbName;
	}
	/**
	 * @return Current version of the internal database format on disk
	 */
	public int getDiskFormatVersion() {
		return diskFormatVersion;
	}
	/**
	 * @return Current size in Bytes of the database (Note: Size of views indexes on disk are not included)
	 */
	public long getDiskSize() {
		return diskSize;
	}
	/**
	 * @return Number of documents (including design documents) in the database.
	 */
	public long getDocCount() {
		return docCount;
	}
	/**
	 * @return
	 */
	public int getDocDelCount() {
		return docDelCount;
	}
	/**
	 * @return Timestamp of CouchDBs start time (ms)
	 */
	public long getInstanceStartTime() {
		return instanceStartTime;
	}
	/**
	 * @return Number of purge operations
	 */
	public int getPurgeSeq() {
		return purgeSeq;
	}
	/**
	 * @return Current number of updates to the database
	 */
	public long getUpdateSeq() {
		return Long.parseLong(updateSeq);
	}
	/**
	 * Cloudant uses generated strings for update sequence.
	 * @return
	 */
	public String getUpdateSeqAsString() {
		return updateSeq;
	}
	/**
	 * @return false if db is an Cloudant instance.
	 */
	public boolean isUpdateSeqNumeric() {
		return updateSeq != null && updateSeq.matches("^\\d*$");
	}
	
	@JsonCreator
	public DbInfo(@JsonProperty("db_name") String dbName) {
		Assert.hasText(dbName, "dbName may not be null or empty");
		this.dbName = dbName;
	}
	
	@JsonAnySetter
	public void setUnknown(String key, Object value) {
		unknownFields().put(key, value);
	}
	
	public Map<String, Object> getUnknownFields() {
		return unknownFields();
	}
	
	private Map<String, Object> unknownFields() {
		if (unknownFields == null) {
			unknownFields = new HashMap<String, Object>();
		}
		return unknownFields;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o instanceof DbInfo) {
			DbInfo dbi = (DbInfo) o;
			return dbName.equals(dbi.dbName);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return dbName.hashCode();
	}
	
}
