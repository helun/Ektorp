package org.ektorp.impl;

import java.io.*;

import org.apache.commons.io.*;
import org.ektorp.http.*;
import org.junit.*;

public class ResponseOnFileStub implements HttpResponse {

	int code;
	InputStream in;
	boolean connectionReleased;
	String contentType = "application/json";
	int contentLength;
	String fileName;

	public static ResponseOnFileStub newInstance(int code, String fileName) {
		ResponseOnFileStub r = new ResponseOnFileStub();
		r.code = code;
		r.in = r.getClass().getResourceAsStream(fileName);
		try {
			int available = r.in.available();
			Assert.assertEquals(true, available > 0);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		r.fileName = fileName;
		return r;
	}

	public static ResponseOnFileStub newInstance(int code, String fileName,
			String contentType, int contentLength) {
		ResponseOnFileStub r = new ResponseOnFileStub();
		r.code = code;
		r.in = r.getClass().getResourceAsStream(fileName);
		r.contentLength = contentLength;
		r.contentType = contentType;
		return r;
	}
	
	public static ResponseOnFileStub newInstance(int code, InputStream in,
			String contentType, int contentLength) {
		ResponseOnFileStub r = new ResponseOnFileStub();
		r.code = code;
		r.in = in;
		r.contentLength = contentLength;
		r.contentType = contentType;
		return r;
	}

	public int getCode() {
		return code;
	}

	public String getContentType() {
		return contentType;
	}

	public InputStream getContent() {
		return in;
	}

	public boolean isSuccessful() {
		return code < 300;
	}

	public void abort() {
		releaseConnection();
	}
	
	public void releaseConnection() {
		connectionReleased = true;
		IOUtils.closeQuietly(in);
	}

	public boolean isConnectionReleased() {
		return connectionReleased;
	}

	public long getContentLength() {
		return contentLength;
	}

	public String getRequestURI() {
		return "static/test/path";
	}

	@Override
	public String toString() {
		return fileName;
	}
}
