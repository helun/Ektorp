package org.ektorp.impl;

import com.fasterxml.jackson.databind.JsonNode;
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

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class StreamedCouchDbConnector extends StdCouchDbConnector {

    public StreamedCouchDbConnector(String databaseName, CouchDbInstance dbInstance) {
        super(databaseName, dbInstance);
    }

    @Override
    public void create(final Object o) {
        Assert.notNull(o, "Document may not be null");
        Assert.isTrue(Documents.isNew(o), "Object must be new");

        JacksonableEntity entity = new JacksonableEntity(o);
        entity.setObjectMapper(objectMapper);

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

        JacksonableEntity entity = new JacksonableEntity(node);
        entity.setObjectMapper(objectMapper);

        restTemplate.put(URIWithDocId(id), entity);
    }

    @Override
    public List<DocumentOperationResult> executeBulk(Collection<?> objects,
                                                     boolean allOrNothing) {

        // FIXME : the super method uses an ExecutorService to transform the Objects Collections to a JSON document using a PipedInputStream + a PipedOutputStream
        // TODO : override the method using kind of the same pattern as the JacksonableEntity to generate the Bulk request JSON document
        return super.executeBulk(objects, allOrNothing);
    }
    
    @Override
    public PurgeResult purge(Map<String, List<String>> revisionsToPurge) {
        JacksonableEntity entity = new JacksonableEntity(revisionsToPurge);
        entity.setObjectMapper(objectMapper);

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

        JacksonableEntity entity = new JacksonableEntity(o);
        entity.setObjectMapper(objectMapper);

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

