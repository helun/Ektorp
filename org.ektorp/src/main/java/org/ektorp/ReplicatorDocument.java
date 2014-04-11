package org.ektorp;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.ektorp.support.CouchDbDocument;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents Couch documents used by the Couch _replicator database.
 */
@JsonInclude(Include.NON_NULL)
public class ReplicatorDocument extends CouchDbDocument
{
	@JsonInclude(Include.NON_NULL)
    public static class UserContext
    {
        private String name;
        private Collection<String> roles;

        private Map<String, Object> unknownFields;

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public Collection<String> getRoles()
        {
            return roles;
        }

        public void setRoles(Collection<String> roles)
        {
            this.roles = roles;
        }

        @JsonAnySetter
        public void setUnknown(String key, Object value) {
            unknownFields().put(key, value);
        }

        @JsonIgnore
        public Map<String, Object> getUnknownFields() {
            return unknownFields();
        }

        private Map<String, Object> unknownFields() {
            if (unknownFields == null) {
                unknownFields = new HashMap<String, Object>();
            }
            return unknownFields;
        }
    }

    private String source;
    private String target;
    private Boolean createTarget;
    private Boolean continuous;
    private Collection<String> documentIds;
    private String filter;
    private Object queryParameters;

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SE_BAD_FIELD")
    private UserContext userContext;

    private String replicationId;
    private String replicationState;
    private Calendar replicationStateTime;

    private Map<String, Object> unknownFields;

    public String getSource()
    {
        return source;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    public String getTarget()
    {
        return target;
    }

    public void setTarget(String target)
    {
        this.target = target;
    }

    @JsonProperty("create_target")
    public Boolean getCreateTarget()
    {
        return createTarget;
    }

    @JsonProperty("create_target")
    public void setCreateTarget(Boolean createTarget)
    {
        this.createTarget = createTarget;
    }

    public Boolean getContinuous()
    {
        return continuous;
    }

    public void setContinuous(Boolean continuous)
    {
        this.continuous = continuous;
    }

    @JsonProperty("doc_ids")
    public Collection<String> getDocumentIds()
    {
        return documentIds;
    }

    @JsonProperty("doc_ids")
    public void setDocumentIds(Collection<String> documentIds)
    {
        this.documentIds = documentIds;
    }

    public String getFilter()
    {
        return filter;
    }

    public void setFilter(String filter)
    {
        this.filter = filter;
    }

    @JsonProperty("query_params")
    public Object getQueryParameters()
    {
        return queryParameters;
    }

    @JsonProperty("query_params")
    public void setQueryParameters(Object queryParameters)
    {
        this.queryParameters = queryParameters;
    }

    @JsonProperty("user_ctx")
    public UserContext getUserContext()
    {
        return userContext;
    }

    @JsonProperty("user_ctx")
    public void setUserContext(UserContext userContext)
    {
        this.userContext = userContext;
    }

    @JsonProperty("_replication_id")
    public String getReplicationId()
    {
        return replicationId;
    }

    @JsonProperty("_replication_id")
    public void setReplicationId(String replicationId)
    {
        this.replicationId = replicationId;
    }

    @JsonProperty("_replication_state")
    public String getReplicationState()
    {
        return replicationState;
    }

    @JsonProperty("_replication_state")
    public void setReplicationState(String replicationState)
    {
        this.replicationState = replicationState;
    }

    @JsonProperty("_replication_state_time")
    public Calendar getReplicationStateTime()
    {
        return replicationStateTime;
    }

    @JsonProperty("_replication_state_time")
    public void setReplicationStateTime(Calendar replicationStateTime)
    {
        this.replicationStateTime = replicationStateTime;
    }

    @JsonAnySetter
    public void setUnknown(String key, Object value) {
        unknownFields().put(key, value);
    }

    @JsonIgnore
    public Map<String, Object> getUnknownFields() {
        return unknownFields();
    }

    private Map<String, Object> unknownFields() {
        if (unknownFields == null) {
            unknownFields = new HashMap<String, Object>();
        }
        return unknownFields;
    }
}
