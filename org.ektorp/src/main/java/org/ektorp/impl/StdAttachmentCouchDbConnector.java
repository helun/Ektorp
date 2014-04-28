package org.ektorp.impl;

import org.ektorp.AttachmentInputStream;
import org.ektorp.http.HttpResponse;
import org.ektorp.http.RestTemplate;
import org.ektorp.http.URI;
import org.ektorp.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StdAttachmentCouchDbConnector implements AttachmentCouchDbConnector {

    private static final Logger LOG = LoggerFactory.getLogger(StdCouchDbConnector.class);

    protected final URI dbURI;

    protected final RevisionResponseHandler revisionHandler;

    protected final RestTemplate restTemplate;

    public StdAttachmentCouchDbConnector(URI dbURI, RestTemplate restTemplate, RevisionResponseHandler revisionHandler) {
        this.dbURI = dbURI;
        this.restTemplate = restTemplate;
        this.revisionHandler = revisionHandler;
    }

    @Override
    public String createAttachment(String docId, AttachmentInputStream data) {
        return createAttachment(docId, null, data);
    }

    @Override
    public String createAttachment(String docId, String revision, AttachmentInputStream data) {
        assertDocIdHasValue(docId);
        URI uri = dbURI.append(docId).append(data.getId());
        if (revision != null) {
            uri.param("rev", revision);
        }
        return restTemplate.put(uri.toString(), data, data.getContentType(), data.getContentLength(), revisionHandler).getRevision();
    }

    @Override
    public AttachmentInputStream getAttachment(final String id, final String attachmentId) {
        assertDocIdHasValue(id);
        Assert.hasText(attachmentId, "attachmentId may not be null or empty");

        LOG.trace("fetching attachment for doc: {} attachmentId: {}", id, attachmentId);
        return getAttachment(attachmentId, dbURI.append(id).append(attachmentId));
    }

    @Override
    public AttachmentInputStream getAttachment(String id, String attachmentId, String revision) {
        assertDocIdHasValue(id);
        Assert.hasText(attachmentId, "attachmentId may not be null or empty");
        Assert.hasText(revision, "revision may not be null or empty");

        LOG.trace("fetching attachment for doc: {} attachmentId: {}", id, attachmentId);
        return getAttachment(attachmentId, dbURI.append(id).append(attachmentId).param("rev", revision));
    }

    private AttachmentInputStream getAttachment(String attachmentId, URI uri) {
        HttpResponse r = restTemplate.get(uri.toString());
        return new AttachmentInputStream(attachmentId, r.getContent(), r.getContentType(), r.getContentLength());
    }

    protected void assertDocIdHasValue(String docId) {
        Assert.hasText(docId, "document id cannot be empty");
    }

}
