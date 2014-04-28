package org.ektorp.impl;

import org.apache.http.HttpEntity;
import org.ektorp.AttachmentInputStream;

public interface AttachmentCouchDbConnector {

    String createAttachment(String docId, AttachmentInputStream data);

    String createAttachment(String docId, String revision, AttachmentInputStream data);

    String createAttachment(String docId, HttpEntity attachmentEntity, String attachmentName);

    String createAttachment(String docId, String revision, HttpEntity attachmentEntity, String attachmentName);

    AttachmentInputStream getAttachment(String id, String attachmentId);

    AttachmentInputStream getAttachment(String id, String attachmentId, String revision);

}
