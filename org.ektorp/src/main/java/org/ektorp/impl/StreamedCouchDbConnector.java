package org.ektorp.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.HttpEntity;
import org.ektorp.CouchDbInstance;
import org.ektorp.DocumentOperationResult;
import org.ektorp.PurgeResult;
import org.ektorp.UpdateConflictException;
import org.ektorp.http.HttpResponse;
import org.ektorp.http.HttpStatus;
import org.ektorp.http.JacksonableEntity;
import org.ektorp.http.StdResponseHandler;
import org.ektorp.util.Assert;
import org.ektorp.util.Documents;

import java.util.List;
import java.util.Map;

public class StreamedCouchDbConnector extends StdCouchDbConnector {

    public StreamedCouchDbConnector(String databaseName, CouchDbInstance dbInstance) {
        super(databaseName, dbInstance);
    }

    public StreamedCouchDbConnector(String databaseName, CouchDbInstance dbi, ObjectMapperFactory om) {
        super(databaseName, dbi, om);
    }

    {
        setCollectionBulkExecutor(new EntityCollectionBulkExecutor(dbURI, restTemplate, objectMapper));
    }

    protected HttpEntity createHttpEntity(Object o) {
        return new JacksonableEntity(o, objectMapper);
    }

    @Override
    public void create(final Object o) {
        Assert.notNull(o, "Document may not be null");
        Assert.isTrue(Documents.isNew(o), "Object must be new");

        HttpEntity entity = createHttpEntity(o);

        String id = Documents.getId(o);
        DocumentOperationResult result;
        if (id != null && id.length() != 0) {
            result = restTemplate.put(URIWithDocId(id), entity, revisionHandler);
        } else {
            result = restTemplate.post(dbURI.toString(), entity, revisionHandler);
            Documents.setId(o, result.getId());
        }
        Documents.setRevision(o, result.getRevision());
    }

    @Override
    public void create(String id, Object node) {
        assertDocIdHasValue(id);
        Assert.notNull(node, "node may not be null");

        HttpEntity entity = createHttpEntity(node);

        restTemplate.put(URIWithDocId(id), entity);
    }

    @Override
    public PurgeResult purge(Map<String, List<String>> revisionsToPurge) {
        HttpEntity entity = createHttpEntity(revisionsToPurge);

        return restTemplate.post(dbURI.append("_purge").toString(), entity,
                new StdResponseHandler<PurgeResult>() {
                    @Override
                    public PurgeResult success(HttpResponse hr) throws Exception {
                        return objectMapper.readValue(hr.getContent(), PurgeResult.class);
                    }
                });
    }

    @Override
    public void update(final Object o) {
        Assert.notNull(o, "Document cannot be null");
        final String id = Documents.getId(o);
        assertDocIdHasValue(id);

        HttpEntity entity = createHttpEntity(o);

        restTemplate.put(dbURI.append(id).toString(), entity,
                new StdResponseHandler<Void>() {

                    @Override
                    public Void success(HttpResponse hr) throws Exception {
                        JsonNode n = objectMapper.readValue(hr.getContent(),
                                JsonNode.class);
                        Documents.setRevision(o, n.get("rev").textValue());
                        return null;
                    }

                    @Override
                    public Void error(HttpResponse hr) {
                        if (hr.getCode() == HttpStatus.CONFLICT) {
                            throw new UpdateConflictException(id, Documents
                                    .getRevision(o));
                        }
                        return super.error(hr);
                    }
                });
    }
}

