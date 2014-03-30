package org.ektorp.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ektorp.DocumentOperationResult;
import org.ektorp.http.RestTemplate;
import org.ektorp.http.URI;

import java.util.Collection;
import java.util.List;

public abstract class BulkOperationExecutor implements BulkExecutor {

    protected URI dbURI;

    protected RestTemplate restTemplate;

    protected ObjectMapper objectMapper;

    public BulkOperationExecutor() {

    }

    public BulkOperationExecutor(URI dbURI, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.dbURI = dbURI;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    protected abstract JsonSerializer getJsonSerializer();

    @Override
    public List<DocumentOperationResult> executeBulk(Collection<?> objects, boolean allOrNothing) {
        BulkOperation op = getJsonSerializer().createBulkOperation(objects, allOrNothing);
        try {
            List<DocumentOperationResult> result = restTemplate.post(
                    dbURI.append("_bulk_docs").toString(),
                    op.getData(),
                    new BulkOperationResponseHandler(objects, objectMapper));
            op.awaitCompletion();
            return result;
        } finally {
            op.close();
        }
    }

}
