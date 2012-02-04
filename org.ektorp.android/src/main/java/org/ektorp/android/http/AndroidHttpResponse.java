package org.ektorp.android.http;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.ektorp.http.HttpResponse;
import org.ektorp.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AndroidHttpResponse implements HttpResponse {

	private final static Logger LOG = LoggerFactory.getLogger(AndroidHttpResponse.class);
	private final static HttpEntity NULL_ENTITY = new NullEntity();

	private final HttpEntity entity;
	private final StatusLine status;
	private final String requestURI;
	private final HttpRequestBase httpRequest;

	public static AndroidHttpResponse of(org.apache.http.HttpResponse rsp, HttpRequestBase httpRequest) {
		return new AndroidHttpResponse(rsp.getEntity(), rsp.getStatusLine(), httpRequest);
	}

	private AndroidHttpResponse(HttpEntity e, StatusLine status, HttpRequestBase httpRequest) {
		this.httpRequest = httpRequest;
		this.entity = e != null ? e : NULL_ENTITY;
		this.status = status;
		this.requestURI = httpRequest.getURI().toString();
	}

	@Override
	public void abort() {
		httpRequest.abort();
	}

	@Override
	public int getCode() {
		return status.getStatusCode();
	}

	@Override
	public InputStream getContent() {
		try {
			return new ConnectionReleasingInputStream(entity.getContent());
		} catch (Exception e) {
			throw Exceptions.propagate(e);
		}
	}

	@Override
	public long getContentLength() {
		return entity.getContentLength();
	}

	@Override
	public String getContentType() {
		return entity.getContentType().getValue();
	}

	@Override
	public String getRequestURI() {
		return requestURI;
	}

	@Override
	public boolean isSuccessful() {
		return getCode() < 300;
	}

	@Override
	public void releaseConnection() {
		try {
			entity.consumeContent();
		} catch (IOException e) {
			LOG.error("caught exception while releasing connection: {}", e.getMessage());
		}
	}

	private class ConnectionReleasingInputStream extends FilterInputStream {

		private ConnectionReleasingInputStream(InputStream src) {
			super(src);
		}


		public void close() throws IOException {
			releaseConnection();
		}

	}

	private static class NullEntity implements HttpEntity {

		final static Header contentType = new BasicHeader(HTTP.CONTENT_TYPE, "null");
		final static Header contentEncoding = new BasicHeader(HTTP.CONTENT_ENCODING, "UTF-8");


		public void consumeContent() throws IOException {

		}


		public InputStream getContent() throws IOException,
				IllegalStateException {
			return null;
		}


		public Header getContentEncoding() {
			return contentEncoding;
		}


		public long getContentLength() {
			return 0;
		}


		public Header getContentType() {
			return contentType;
		}


		public boolean isChunked() {
			return false;
		}


		public boolean isRepeatable() {
			return true;
		}


		public boolean isStreaming() {
			return false;
		}


		public void writeTo(OutputStream outstream) throws IOException {
			throw new UnsupportedOperationException("NullEntity cannot write");
		}

	}

}
