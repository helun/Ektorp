package org.ektorp;


public interface IndexerTask extends ActiveTask {
    String getDatabaseName();
    String getDesignDocumentId();
    long getCompletedChanges();
    long getTotalChanges();
}
