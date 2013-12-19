package org.ektorp;

/**
 * Interface for retrieving data about an indexer task
 *
 * @author ed wagstaff
 */
public interface IndexerTask extends ActiveTask {

    /**
     * @return the database which the task is running against
     */
    String getDatabaseName();

    /**
     * @return the ID of the design document where the index is defined
     */
    String getDesignDocumentId();

    /**
     * @return the number of processed changes
     */
    long getCompletedChanges();

    /**
     * @return the total number of changes to process
     */
    long getTotalChanges();
}
