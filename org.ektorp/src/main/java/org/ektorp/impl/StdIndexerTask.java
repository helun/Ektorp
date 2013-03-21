package org.ektorp.impl;

import org.ektorp.IndexerTask;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StdIndexerTask extends StdActiveTask implements IndexerTask {

    private String databaseName;
    private long totalChanges;
    private long completedChanges;
    private String designDocumentId;

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    @JsonProperty(required = false, value = "database")
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @Override
    public long getTotalChanges() {
        return totalChanges;
    }

    @JsonProperty(required = false, value = "total_changes")
    public void setTotalChanges(long totalChanges) {
        this.totalChanges = totalChanges;
    }

    @Override
    public long getCompletedChanges() {
        return completedChanges;
    }

    @JsonProperty(required = false, value = "changes_done")
    public void setCompletedChanges(long completedChanges) {
        this.completedChanges = completedChanges;
    }

    @Override
    public String getDesignDocumentId() {
        return designDocumentId;
    }

    @JsonProperty(required = false, value = "design_document")
    public void setDesignDocumentId(String designDocumentId) {
        this.designDocumentId = designDocumentId;
    }
}
