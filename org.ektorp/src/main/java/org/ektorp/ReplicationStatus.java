package org.ektorp;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Replication response doc is not very well documented in the CouchDB reference...
 *
 * @author henrik lundgren
 *
 */
public class ReplicationStatus extends Status implements Serializable {

	private static final long serialVersionUID = 6617269292660336903L;

	@JsonProperty("no_changes")
	boolean noChanges;

	@JsonProperty("session_id")
	String sessionId;
	
	@JsonProperty("_local_id")
	String id;

	@JsonProperty("source_last_seq")
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SE_BAD_FIELD")
	JsonNode sourceLastSequence;

	@JsonProperty("history")
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SE_BAD_FIELD")
	List<History> history;

	public boolean isNoChanges() {
		return noChanges;
	}

	public String getSessionId() {
		return sessionId;
	}
	
	public String getId() {
        return id;
    }

	public String getSourceLastSequence() {
		return sourceLastSequence != null ? sourceLastSequence.asText() : null;
	}

	public JsonNode getSourceLastSequenceAsNode() {
		return sourceLastSequence;
	}

	public List<History> getHistory() {
		return history;
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
		JsonNode startLastSeq;

		@JsonProperty("end_last_seq")
		JsonNode endLastSeq;

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
		JsonNode recordedSeq;

		public String getRecordedSeq() {
			return recordedSeq != null ? recordedSeq.asText() : null;
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
			return startLastSeq != null ? startLastSeq.asText() : null;
		}

		public JsonNode getStartLastSeqAsNode() {
			return startLastSeq;
		}

		public String getEndLastSeq() {
			return endLastSeq != null ? endLastSeq.asText() : null;
		}

		public JsonNode getEndLastSeqAsNode() {
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

        @Override
        public String toString() {
            return "Replication history: " + getSessionId() + ", " + getRecordedSeq();
        }
	}

}
