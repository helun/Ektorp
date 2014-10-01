package org.ektorp.impl;

import java.io.*;
import java.nio.charset.Charset;

import org.apache.commons.io.input.ReaderInputStream;
import org.ektorp.http.*;

public class HttpResponseStub implements HttpResponse {

    public static HttpResponse valueOf(int code, String body) {
        return new HttpResponseStub(code, body);
    }

    private final int code;

    private final String body;

    private final Charset charset = Charset.forName("UTF-8");

    HttpResponseStub(int code, String body) {
		this.code = code;
		this.body = body;
	}

	public int getCode() {
		return code;
	}

	public InputStream getContent() {
		return new ReaderInputStream(new StringReader(body), charset);
	}

	public String getETag() {
		// TODO Auto-generated method stub
		return null;
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
	
	public long getContentLength() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void abort() {
		// TODO Auto-generated method stub	
	}
	
	public String getRequestURI() {
		return "static/test/path";
	}

}
