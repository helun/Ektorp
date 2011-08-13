package org.ektorp.http;

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
import org.ektorp.util.Exceptions;

/**
 * 
 * @author henriklundgren
 *
 */
public class StdHttpResponse implements HttpResponse {

	// private final static Logger LOG = LoggerFactory.getLogger(StdHttpResponse.class);
	private final static HttpEntity NULL_ENTITY = new NullEntity();
	
	private final HttpEntity entity;
	private final StatusLine status;
	private final String requestURI;
	private final HttpRequestBase httpRequest;
	
	public static StdHttpResponse of(org.apache.http.HttpResponse rsp, HttpRequestBase httpRequest) {
		return new StdHttpResponse(rsp.getEntity(), rsp.getStatusLine(), httpRequest);
	}
	
	private StdHttpResponse(HttpEntity e, StatusLine status, HttpRequestBase httpRequest) {
		this.httpRequest = httpRequest;
		this.entity = e != null ? e : NULL_ENTITY;
		this.status = status;
		this.requestURI = httpRequest.getURI().toString();
	}
	
	
	public int getCode() {
		return status.getStatusCode();
	}

	public String getReason() {
		return status.getReasonPhrase();
	}
	
	public String getRequestURI() {
		return requestURI;
	}
	
	
	public int getContentLength() {
		return (int) entity.getContentLength();
	}

	
	public String getContentType() {
		return entity.getContentType().getValue();
	}

	
	public InputStream getContent() {
		try {
			return new ConnectionReleasingInputStream(entity.getContent());
		} catch (Exception e) {
			throw Exceptions.propagate(e);
		}
	}

	
	public boolean isSuccessful() {
		return getCode() < 300;
	}

	
	public void releaseConnection() {
		try {
			if (entity.getContent() != null) {
				entity.getContent().close();
			}; 
		} catch (IOException e) {
			// ignore
		}
	}
	
	public void abort() {
		httpRequest.abort();
	}
	
	
	public String toString() {
		return status.getStatusCode() + ":" + status.getReasonPhrase();
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
