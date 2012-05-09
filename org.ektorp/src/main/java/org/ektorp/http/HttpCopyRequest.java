package org.ektorp.http;

import org.apache.http.client.methods.HttpRequestBase;

public class HttpCopyRequest extends HttpRequestBase {

	public final static String METHOD_NAME = "COPY";
	
	public HttpCopyRequest(String uri, String destinationUri) {
		super();
		setURI(java.net.URI.create(uri));
		addHeader("Destination", destinationUri);
	}
	
	@Override
	public String getMethod() {
		return METHOD_NAME;
	}

}
