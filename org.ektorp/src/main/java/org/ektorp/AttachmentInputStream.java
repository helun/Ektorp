package org.ektorp;

import java.io.*;

import org.ektorp.util.*;
/**
 * An InputStream that knows what content type is contains.
 * @author henrik lundgren
 *
 */
public class AttachmentInputStream extends FilterInputStream {

	private final String id;
	private final String contentType;
	private final long contentLength;
	
	public AttachmentInputStream(String id, InputStream data, String contentType) {
		this(id, data, contentType, -1);
	}
	
	public AttachmentInputStream(String id, InputStream data, String contentType, long size) {
		super(data);
		Assert.hasText(id, "id may not be null or empty");
		Assert.hasText(contentType, "contentType not be null or empty");
		this.id = id;
		this.contentType = contentType;
		this.contentLength = size;
	}
	
	public String getId() {
		return id;
	}
	
	public String getContentType() {
		return contentType;
	}
	
	public long getContentLength() {
		return contentLength;
	}

}
