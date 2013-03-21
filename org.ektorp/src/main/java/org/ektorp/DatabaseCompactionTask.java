package org.ektorp;


public interface DatabaseCompactionTask extends ActiveTask {
    String getDatabaseName();
    long getTotalChanges();
    long getCompletedChanges();
}
