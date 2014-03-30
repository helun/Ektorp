package org.ektorp.impl;

import org.ektorp.CouchDbConnector;
import org.ektorp.DocumentOperationResult;
import org.ektorp.LocalBulkBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DefaultLocalBulkBuffer implements LocalBulkBuffer {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultLocalBulkBuffer.class);

    private final ThreadLocalBulkBufferHolder bulkBufferManager = new ThreadLocalBulkBufferHolder();

    private final CouchDbConnector couchDbConnector;

    public DefaultLocalBulkBuffer(CouchDbConnector couchDbConnector) {
        this.couchDbConnector = couchDbConnector;
    }

    @Override
    public void addToBulkBuffer(Object o) {
        bulkBufferManager.add(o);
        LOG.debug("{} added to bulk buffer", o);
    }

    @Override
    public void clearBulkBuffer() {
        bulkBufferManager.clear();
        LOG.debug("bulk buffer cleared");
    }

    @Override
    public List<DocumentOperationResult> flushBulkBuffer() {
        try {
            Collection<?> buffer = bulkBufferManager.getCurrentBuffer();
            if (buffer != null && !buffer.isEmpty()) {
                LOG.debug("flushing bulk buffer");
                return couchDbConnector.executeBulk(buffer);
            } else {
                LOG.debug("bulk buffer was empty");
                return Collections.emptyList();
            }
        } finally {
            clearBulkBuffer();
        }
    }

}
