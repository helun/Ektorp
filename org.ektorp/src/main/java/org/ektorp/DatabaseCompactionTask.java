package org.ektorp;

/**
 * Interface for retrieving data about a database compaction task
 *
 * @author ed wagstaff
 */
public interface DatabaseCompactionTask extends ActiveTask {

    /**
     * @return the database which the task is running against
     */
    String getDatabaseName();

    /**
     * @return the total number of changes to process
     */
    long getTotalChanges();

    /**
     * @return the number of processed changes
     */
    long getCompletedChanges();
}
