package org.ektorp;

/**
 * Interface for retrieving data about an indexer task
 *
 * @author ed wagstaff
 */
public interface ViewCompactionTask extends ActiveTask {

    /**
     * @return the database which the task is running against
     */
    String getDatabaseName();

    /**
     * @return the ID of the design document which the compaction task is running for
     */
    String getDesignDocumentId();
}
