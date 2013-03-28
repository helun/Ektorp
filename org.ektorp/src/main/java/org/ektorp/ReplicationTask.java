package org.ektorp;


public interface ReplicationTask extends ActiveTask {
    String getReplicationId();
    String getReplicationDocumentId();
    boolean isContinuous();
    long getWriteFailures();
    long getTotalReads();
    long getTotalWrites();
    long getTotalMissingRevisions();
    long getTotalRevisionsChecked();
    String getSourceDatabaseName();
    String getTargetDatabaseName();
    long getSourceSequenceId();
    long getCheckpointedSourceSequenceId();
}
