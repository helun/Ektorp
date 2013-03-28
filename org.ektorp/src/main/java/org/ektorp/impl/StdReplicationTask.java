package org.ektorp.impl;

import org.ektorp.ReplicationTask;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StdReplicationTask extends StdActiveTask implements ReplicationTask {

    private String replicationId;
    private String replicationDocumentId;
    private boolean isContinuous;
    private long writeFailures;
    private long totalReads;
    private long totalWrites;
    private long totalMissingRevisions;
    private long totalRevisionsChecked;
    private String sourceDatabase;
    private String targetDatabase;
    private long sourceSequenceId;
    private long checkpointedSourceSequenceId;

    @Override
    public String getReplicationId() {
        return replicationId;
    }

    @JsonProperty(required = false, value = "replication_id")
    public void setReplicationId(String replicationId) {
        this.replicationId = replicationId;
    }

    @Override
    public String getReplicationDocumentId() {
        return replicationDocumentId;
    }

    @JsonProperty(required = false, value = "doc_id")
    public void setReplicationDocumentId(String replicationDocumentId) {
        this.replicationDocumentId = replicationDocumentId;
    }

    @Override
    public boolean isContinuous() {
        return isContinuous;
    }

    @JsonProperty(required = false, value = "continuous")
    public void setContinuous(boolean isContinuous) {
        this.isContinuous = isContinuous;
    }

    @Override
    public long getWriteFailures() {
        return writeFailures;
    }

    @JsonProperty(required = false, value = "doc_write_failures")
    public void setWriteFailures(long writeFailures) {
        this.writeFailures = writeFailures;
    }

    @Override
    public long getTotalReads() {
        return totalReads;
    }

    @JsonProperty(required = false, value = "docs_read")
    public void setTotalReads(long totalReads) {
        this.totalReads = totalReads;
    }

    @Override
    public long getTotalWrites() {
        return totalWrites;
    }

    @JsonProperty(required = false, value = "docs_written")
    public void setTotalWrites(long totalWrites) {
        this.totalWrites = totalWrites;
    }

    @Override
    public long getTotalMissingRevisions() {
        return totalMissingRevisions;
    }

    @JsonProperty(required = false, value = "missing_revisions_found")
    public void setTotalMissingRevisions(long totalMissingRevisions) {
        this.totalMissingRevisions = totalMissingRevisions;
    }

    @Override
    public long getTotalRevisionsChecked() {
        return totalRevisionsChecked;
    }

    @JsonProperty(required = false, value = "revisions_checked")
    public void setTotalRevisionsChecked(long totalRevisionsChecked) {
        this.totalRevisionsChecked = totalRevisionsChecked;
    }

    @Override
    public String getSourceDatabaseName() {
        return sourceDatabase;
    }

    @JsonProperty(required = false, value = "source")
    public void setSourceDatabase(String sourceDatabase) {
        this.sourceDatabase = sourceDatabase;
    }

    @Override
    public String getTargetDatabaseName() {
        return targetDatabase;
    }

    @JsonProperty(required = false, value = "target")
    public void setTargetDatabase(String targetDatabase) {
        this.targetDatabase = targetDatabase;
    }

    @Override
    public long getSourceSequenceId() {
        return sourceSequenceId;
    }

    @JsonProperty(required = false, value = "source_seq")
    public void setSourceSequenceId(long sourceSequenceId) {
        this.sourceSequenceId = sourceSequenceId;
    }

    @Override
    public long getCheckpointedSourceSequenceId() {
        return checkpointedSourceSequenceId;
    }

    @JsonProperty(required = false, value = "checkpointed_source_seq")
    public void setCheckpointedSourceSequenceId(long checkpointedSourceSequenceId) {
        this.checkpointedSourceSequenceId = checkpointedSourceSequenceId;
    }

}
