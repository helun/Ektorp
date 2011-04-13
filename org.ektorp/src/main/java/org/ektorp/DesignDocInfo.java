package org.ektorp;

import org.codehaus.jackson.annotate.JsonProperty;
import org.ektorp.support.OpenCouchDbDocument;

/**
 * 
 * @author EronenP
 * 
 */
public class DesignDocInfo extends OpenCouchDbDocument {

	private static final long serialVersionUID = 4030630616850588285L;

	public static class ViewIndex extends OpenCouchDbDocument {

		private static final long serialVersionUID = 1164231233089979199L;

		@JsonProperty("compact_running")
		private boolean compactRunning;
		@JsonProperty("updater_running")
		private boolean updaterRunning;
		@JsonProperty
		private String language;
		@JsonProperty("purge_seq")
		private long purgeSeq;
		@JsonProperty("waiting_commit")
		private boolean waitingCommit;
		@JsonProperty("waiting_clients")
		private int waitingClients;
		@JsonProperty
		private String signature;
		@JsonProperty("update_seq")
		private long updateSeq;
		@JsonProperty("disk_size")
		private long diskSize;
		/**
		 * Indicates whether a compaction routine is currently running on the view
		 * @return
		 */
		public boolean isCompactRunning() {
			return compactRunning;
		}
		/**
		 * Indicates if the view is currently being updated.
		 * 
		 * @return
		 */
		public boolean isUpdaterRunning() {
			return updaterRunning;
		}
		/**
		 * Language for the defined views.
		 * @return
		 */
		public String getLanguage() {
			return language;
		}
		/**
		 * The purge sequence that has been processed.
		 * @return
		 */
		public long getPurgeSeq() {
			return purgeSeq;
		}
		/**
		 * Indicates if there are outstanding commits to the underlying database that need to processed.
		 * @return
		 */
		public boolean isWaitingCommit() {
			return waitingCommit;
		}
		/**
		 * Number of clients waiting on views from this design document.
		 * @return
		 */
		public int getWaitingClients() {
			return waitingClients;
		}
		/**
		 * MD5 signature of the views for the design document
		 * @return
		 */
		public String getSignature() {
			return signature;
		}
		/**
		 * The update sequence of the corresponding database that has been indexed.
		 * 
		 * @return
		 */
		public long getUpdateSeq() {
			return updateSeq;
		}
		/**
		 * Size in bytes of the view as stored on disk.
		 * @return
		 */
		public long getDiskSize() {
			return diskSize;
		}
	}

	@JsonProperty
	private String name;

	@JsonProperty("view_index")
	private ViewIndex viewIndex;

	public String getName() {
		return name;
	}

	public ViewIndex getViewIndex() {
		return viewIndex;
	}
}
