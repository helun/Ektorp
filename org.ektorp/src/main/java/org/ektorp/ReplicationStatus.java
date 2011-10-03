package org.ektorp;

import java.io.*;
import java.util.*;

import org.codehaus.jackson.annotate.*;
/**
 * 
 * Replication response doc is not very well documented in the CouchDB reference...
 * 
 * @author henrik lundgren
 *
 */
public class ReplicationStatus implements Serializable {

	private static final long serialVersionUID = 6617269292660336903L;

	@JsonProperty("ok")
	boolean ok;
	
	@JsonProperty("no_changes")
	boolean noChanges;
	
	@JsonProperty("session_id")
	String sessionId;
	
	@JsonProperty("source_last_seq")
	String sourceLastSequence;
	
	@JsonProperty("history")
	List<History> history;
	
	private Map<String, Object> unknownFields;
	
	public boolean isOk() {
		return ok;
	}

	public boolean isNoChanges() {
		return noChanges;
	}

	public String getSessionId() {
		return sessionId;
	}

	public String getSourceLastSequence() {
		return sourceLastSequence;
	}

	public List<History> getHistory() {
		return history;
	}

	private Map<String, Object> unknown() {
		if (unknownFields == null) {
			unknownFields = new HashMap<String, Object>();
		}
		return unknownFields;
	}
	
	@JsonAnySetter
	public void setUnknown(String key, Object value) {
		unknown().put(key, value);
	}
	
	public Object getField(String key) {
		return unknown().get(key);
	}
	
	public static class History {
		
		private Map<String, Object> unknownFields;
	
		@JsonProperty("session_id")
		String sessionId;
		
		@JsonProperty("start_time")
		String startTime;
		
		@JsonProperty("end_time")
		String endTime;
		
		@JsonProperty("start_last_seq")
		String startLastSeq;
		
		@JsonProperty("end_last_seq")
		String endLastSeq;
		
		@JsonProperty("missing_checked")
		int missingChecked;
		
		@JsonProperty("missing_found")
		int missingFound;
		
		@JsonProperty("docs_read")
		int docsRead;
		
		@JsonProperty("docs_written")
		int docsWritten;
		
		@JsonProperty("doc_write_failures")
		int docWriteFailures;
		
		@JsonProperty("recorded_seq")
		int recordedSeq;
		
		public int getRecordedSeq() {
			return recordedSeq;
		}
		
		public String getSessionId() {
			return sessionId;
		}

		public String getStartTime() {
			return startTime;
		}

		public String getEndTime() {
			return endTime;
		}

		public String getStartLastSeq() {
			return startLastSeq;
		}

		public String getEndLastSeq() {
			return endLastSeq;
		}

		public int getMissingChecked() {
			return missingChecked;
		}

		public int getMissingFound() {
			return missingFound;
		}

		public int getDocsRead() {
			return docsRead;
		}

		public int getDocsWritten() {
			return docsWritten;
		}

		public int getDocWriteFailures() {
			return docWriteFailures;
		}

		private Map<String, Object> unknown() {
			if (unknownFields == null) {
				unknownFields = new HashMap<String, Object>();
			}
			return unknownFields;
		}
		
		@JsonAnySetter
		public void setUnknown(String key, Object value) {
			unknown().put(key, value);
		}
		
		public Object getField(String key) {
			return unknown().get(key);
		}		
	}
	
}
