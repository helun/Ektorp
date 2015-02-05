package org.ektorp;

/**
 * Interface for retrieving data about a replication task
 *
 * @author ed wagstaff
 */
public interface ReplicationTask extends ActiveTask {

    /**
     * @return a unique ID for this replication task which can be used e.g. to cancel an ongoing
     * replication (see <a href="http://wiki.apache.org/couchdb/Replication#from_1.2.0_onward">http://wiki.apache.org/couchdb/Replication#from_1.2.0_onward</a>)
     */
    String getReplicationId();

    /**
     * @return the ID of the document which defines the replication rule being processed by this task
     */
    String getReplicationDocumentId();

    /**
     * @return a boolean indicating whether or not the replication rule being processed is continuous
     */
    boolean isContinuous();

    /**
     * @return the total number of document writes which have failed so far in this task
     */
    long getWriteFailures();

    /**
     * @return the total number of document reads so far for this task
     */
    long getTotalReads();

    /**
     * @return the total number of successful document writes so far for this task
     */
    long getTotalWrites();

    /**
     * @return the number of documents in the target DB which have been found to be either missing or out of date. This
     * number can be incremented more than once for the same document: for example if a document is updated while a
     * continuous replication rule is active. It won't however be increased by more than 1 if a document is out of
     * date by more than one revision: for example if document X is at revision 3 in Source and revision 1 in Target,
     * this number only increases by 1 when document X is checked.
     */
    long getTotalMissingRevisions();

    /**
     * @return the number of documents from the source DB whose presence and revision number in the target DB have been
     * checked. This number can be incremented more than once for the same document: for example if a document is
     * updated while a continuous replication rule is active, it will be checked again. It won't however count
     * multiple revisions of the same document each time it's checked: for example if a document is at revision 3 the
     * first time it's checked, this count will still only increase by 1.
     */
    long getTotalRevisionsChecked();

    /**
     * @return the name of the source database for this task
     */
    String getSourceDatabaseName();

    /**
     * @return the name of the target database for this task
     */
    String getTargetDatabaseName();

    /**
     * @return the sequence number of the source database
     */
    long getSourceSequenceId();

    /**
     * @return the latest sequence number of the source database which has been processed by this task
     */
    long getCheckpointedSourceSequenceId();

	/**
	 * Defines replication checkpoint interval in milliseconds. Replicator will requests from the Source database at the specified interval
	 */
	Long getCheckpointInterval();

}
