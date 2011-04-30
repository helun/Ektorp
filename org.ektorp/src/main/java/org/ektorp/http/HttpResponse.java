package org.ektorp.http;

import java.io.*;

public interface HttpResponse {

	boolean isSuccessful();
	int getCode();
	String getRequestURI();
	String getContentType();
	int getContentLength();
	InputStream getContent();
	void releaseConnection();
	void abort();
}