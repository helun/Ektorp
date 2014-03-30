package org.ektorp.impl;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;

/**
 * This bean is designed so that, when marshaled as JSON using Jackson, it is handled as a bulk request by CouchDB.<br>
 * Put this into a JacksonableEntity (org.apache.http.HttpEntity) in order to get HttpClient to marshal it on the request stream.<br>
 */
public class BulkDocumentBean<T> {

    @JsonProperty("all_or_nothing")
    private boolean allOrNothing = false;

    @JsonProperty("docs")
    private Collection<T> documents;

    public BulkDocumentBean() {
        super();
    }

    public BulkDocumentBean(Collection<T> documents, boolean allOrNothing) {
        super();
        this.allOrNothing = allOrNothing;
        this.documents = documents;
    }

    public boolean isAllOrNothing() {
        return allOrNothing;
    }

    public void setAllOrNothing(boolean allOrNothing) {
        this.allOrNothing = allOrNothing;
    }

    public Collection<T> getDocuments() {
        return documents;
    }

    public void setDocuments(Collection<T> documents) {
        this.documents = documents;
    }
}
