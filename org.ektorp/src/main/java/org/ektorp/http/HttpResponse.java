package org.ektorp.http;

import java.io.*;

public interface HttpResponse {

	boolean isSuccessful();
	int getCode();
	String getRequestURI();
	String getContentType();
	long getContentLength();
	InputStream getContent();
	String getETag();
	void releaseConnection();
	void abort();
}