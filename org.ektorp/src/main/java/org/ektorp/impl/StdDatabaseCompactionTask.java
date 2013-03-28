package org.ektorp.impl;

import org.ektorp.DatabaseCompactionTask;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StdDatabaseCompactionTask extends StdActiveTask implements DatabaseCompactionTask {

    private String databaseName;
    private long totalChanges;
    private long completedChanges;

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
}
