package org.ektorp.impl;

import org.ektorp.AttachmentInputStream;

public interface AttachmentCouchDbConnector {

    String createAttachment(String docId, AttachmentInputStream data);

    String createAttachment(String docId, String revision, AttachmentInputStream data);

    AttachmentInputStream getAttachment(String id, String attachmentId);

    AttachmentInputStream getAttachment(String id, String attachmentId, String revision);

}
