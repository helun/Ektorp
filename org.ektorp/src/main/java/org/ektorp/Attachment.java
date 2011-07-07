package org.ektorp;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.*;
import org.codehaus.jackson.map.annotate.*;
import org.codehaus.jackson.map.annotate.JsonSerialize.*;
import org.ektorp.util.*;

/**
 * 
 * @author henrik lundgren
 *
 */
@JsonSerialize(include = Inclusion.NON_NULL)
public class Attachment implements Serializable {

	private static final long serialVersionUID = 1L;
	private String id;
	private String contentType;
	private long length;
	private String dataBase64;
	private boolean stub;
	private int revpos;
	private String digest;
	private Map<String, Object> anonymous;
	
	/**
	 * Constructor that takes data as String.
	 * The data must be base64 encoded single line of characters, so pre-process your data to remove any carriage returns and newlines
	 * 
	 * Useful if you want to save the attachment as an inline attachent.
	 * 
	 * @param id
	 * @param data base64-encoded
	 * @param contentType
	 * @param contentLength
	 */
	public Attachment(String id, String data, String contentType) {
		Assert.hasText(id, "attachmentId must have a value");
		Assert.hasText(contentType, "contentType must have a value");
		Assert.notNull(data, "data input stream cannot be null");
		this.id = id;
		this.contentType = contentType;
		this.dataBase64 = data;
		this.length = data.getBytes().length;
	}
	
	Attachment() {}

	@JsonProperty("content_type")
	public String getContentType() {
		return contentType;
	}

	@JsonProperty("content_type")
	void setContentType(String contentType) {
		this.contentType = contentType;
	}
	@JsonIgnore
	public long getContentLength() {
		return length;
	}
	/**
	 * Only populated if this attachment was created with data as String constructor.
	 * @return
	 */
	@JsonProperty("data")
	public String getDataBase64() {
		return dataBase64;
	}

	@JsonIgnore
	public String getId() {
		return id;
	}

	@JsonIgnore
	void setId(String id) {
		this.id = id;
	}

	void setLength(int contentLength) {
		this.length = contentLength;
	}

	public boolean isStub() {
		return stub;
	}

	void setStub(boolean stub) {
		this.stub = stub;
	}
	
	public int getRevpos() {
		return revpos;
	}
	
	public void setRevpos(int revpos) {
		this.revpos = revpos;
	}
	
	public String getDigest() {
		return digest;
	}

	/** 
	 * @return a Map containing fields that did not map to any other field in the class during object deserializarion from a JSON document.
	 */
	@JsonAnyGetter
	public Map<String, Object> getAnonymous() {
		return anonymous();
	}
	
	/**
	 * 
	 * @param key
	 * @param value
	 */
	@JsonAnySetter
	public void setAnonymous(String key, Object value) {
		anonymous().put(key, value);
	}
	/**
	 * Provides lay init for the anonymous Map
	 * @return
	 */
	private Map<String, Object> anonymous() {
		if (anonymous == null) {
			anonymous = new HashMap<String, Object>();
		}
		return anonymous;
	}
}
