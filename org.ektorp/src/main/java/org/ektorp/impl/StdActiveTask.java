package org.ektorp.impl;

import org.ektorp.ActiveTask;
import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

@JsonTypeInfo(
   use = JsonTypeInfo.Id.NAME,
   include = JsonTypeInfo.As.PROPERTY,
   property = "type")
@JsonSubTypes({
   @Type(value = StdReplicationTask.class, name = "replication"),
   @Type(value = StdIndexerTask.class, name = "indexer"),
   @Type(value = StdDatabaseCompactionTask.class, name = "database_compaction"),
   @Type(value = StdViewCompactionTask.class, name = "view_compaction") })
public abstract class StdActiveTask implements ActiveTask {

    private String pid;
    private int progress;
    private DateTime startedOn;
    private DateTime updatedOn;

    @Override
    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    @Override
    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    @Override
    public DateTime getStartedOn() {
        return startedOn;
    }

    @JsonProperty(value = "started_on")
    public void setStartedOn(DateTime startedOn) {
        this.startedOn = startedOn;
    }

    @Override
    public DateTime getUpdatedOn() {
        return updatedOn;
    }

    @JsonProperty(value = "updated_on")
    public void setUpdatedOn(DateTime updatedOn) {
        this.updatedOn = updatedOn;
    }
}
