package org.ektorp.impl;

import java.io.*;

import org.ektorp.http.*;

public class HttpResponseStub implements HttpResponse {

	int code;
	String body;
	
	HttpResponseStub(int code, String body) {
		this.code = code;
		this.body = body;
	}
	
	public static HttpResponse valueOf(int code, String body) {
		return new HttpResponseStub(code, body);
	}
	
	public int getCode() {
		return code;
	}

	public InputStream getContent() {
		return new ByteArrayInputStream(body.getBytes());
	}

	public boolean isSuccessful() {
		return code < 300;
	}

	public String getContentType() {
		// TODO Auto-generated method stub
		return null;
	}

	public void releaseConnection() {
		// TODO Auto-generated method stub
		
	}
	
	public int getContentLength() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public String getRequestURI() {
		return "static/test/path";
	}

}
