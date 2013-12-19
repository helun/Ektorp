package org.ektorp;

import java.util.Date;

/**
 * Interface for retrieving data about an active task (e.g. compaction or replication)
 *
 * @author ed wagstaff
 */
public interface ActiveTask {

    /**
     * @return the process ID of the task
     */
    String getPid();

    /**
     * @return the percentage progress of the task
     */
    int getProgress();

    /**
     * @return the date and time that the task was started
     */
    Date getStartedOn();

    /**
     * @return the date and time that the information about the task was last updated
     */
    Date getUpdatedOn();
}
