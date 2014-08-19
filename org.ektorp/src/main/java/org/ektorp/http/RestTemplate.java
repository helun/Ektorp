package org.ektorp.http;

import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.ektorp.util.Exceptions;
/**
 * 
 * @author Henrik Lundgren
 * 
 */
public class RestTemplate {

	private final HttpClient client;

	public RestTemplate(HttpClient client) {
		this.client = client;
	}

	public <T> T get(String path, ResponseCallback<T> callback) {
		HttpResponse hr = client.get(path);
		return handleResponse(callback, hr);
	}

	public <T> T getUncached(String path, ResponseCallback<T> callback) {
		HttpResponse hr = client.getUncached(path);
		return handleResponse(callback, hr);
	}
	
	public HttpResponse get(String path) {
		return handleRawResponse(client.get(path));
	}
	
	public HttpResponse getUncached(String path) {
		return handleRawResponse(client.getUncached(path));
	}

	public void put(String path) {
		handleVoidResponse(client.put(path));
	}

	public <T> T put(String path, ResponseCallback<T> callback) {
		return handleResponse(callback, client.put(path));
	}

	public <T> T put(String path, String content, ResponseCallback<T> callback) {
		return handleResponse(callback, client.put(path, content));
	}

	public <T> T put(String path, HttpEntity httpEntity, ResponseCallback<T> callback) {
		return handleResponse(callback, client.put(path, httpEntity));
	}

	public <T> T copy(String path, String destinationUri, ResponseCallback<T> callback) {
		return handleResponse(callback, client.copy(path, destinationUri));
	}
	
	public void put(String path, String content) {
		handleVoidResponse(client.put(path, content));
	}

	public void put(String path, HttpEntity httpEntity) {
		handleVoidResponse(client.put(path, httpEntity));
	}

	public void put(String path, InputStream data, String contentType,
			long contentLength) {
		handleVoidResponse(client.put(path, data, contentType, contentLength));
	}

	public <T> T put(String path, InputStream data, String contentType,
			long contentLength, ResponseCallback<T> callback) {
		return handleResponse(callback, client.put(path, data, contentType, contentLength));
	}

	public <T> T post(String path, String content, ResponseCallback<T> callback) {
		return handleResponse(callback, client.post(path, content));
	}

	public <T> T post(String path, HttpEntity httpEntity, ResponseCallback<T> callback) {
		return handleResponse(callback, client.post(path, httpEntity));
	}

	public <T> T post(String path, InputStream content, ResponseCallback<T> callback) {
		return handleResponse(callback, client.post(path, content));
	}
	
	public <T> T postUncached(String path, String content, ResponseCallback<T> callback) {
		return handleResponse(callback, client.postUncached(path, content));
	}
	
	public HttpResponse post(String path, String content ) {
		return handleRawResponse(client.post(path,content));
	}
	
	public HttpResponse postUncached(String path, String content ) {
		return handleRawResponse(client.postUncached(path,content));
	}

	public <T> T delete(String path, ResponseCallback<T> callback) {
		return handleResponse(callback, client.delete(path));
	}
	
	public void delete(String path) {
		handleVoidResponse(client.delete(path));
	}

	public <T> T head(String path, ResponseCallback<T> callback) {
		return handleResponse(callback, client.head(path));
	}
	
	private void handleVoidResponse(HttpResponse hr) {
		if (hr == null)
			return;
		try {
			if (!hr.isSuccessful()) {
				new StdResponseHandler<Void>().error(hr);
			}
		} finally {
			hr.releaseConnection();
		}
	}
	
	private <T> T handleResponse(ResponseCallback<T> callback, HttpResponse hr) {
		try {
			return hr.isSuccessful() ? callback.success(hr) : callback.error(hr);
		} catch (Exception e) {
			throw Exceptions.propagate(e);
		} finally {
			hr.releaseConnection();
		}
	}
	
	private HttpResponse handleRawResponse(HttpResponse hr) {
		try {
			if (!hr.isSuccessful()) {
				throw StdResponseHandler.createDbAccessException(hr);
			}
			return hr;
		} catch (Exception e) {
			hr.releaseConnection();
			throw Exceptions.propagate(e);
		}
	}
}
