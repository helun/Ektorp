package org.ektorp.android.http;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.*;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.ektorp.http.*;
import org.ektorp.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.util.Map;

public class AndroidHttpClient implements HttpClient {

	private final org.apache.http.client.HttpClient client;
	private final org.apache.http.client.HttpClient backend;
	private final static Logger LOG = LoggerFactory
			.getLogger(AndroidHttpClient.class);

	public AndroidHttpClient(org.apache.http.client.HttpClient hc) {
		this(hc, hc);
	}
	public AndroidHttpClient(org.apache.http.client.HttpClient hc,
			org.apache.http.client.HttpClient backend) {
		this.client = hc;
		this.backend = backend;
	}

	@Override
	public HttpResponse delete(String uri) {
		return executeRequest(new HttpDelete(uri));
	}

	@Override
	public HttpResponse get(String uri) {
		return executeRequest(new HttpGet(uri));
	}

	@Override
	public HttpResponse get(String uri, Map<String, String> headers) {
		return executeRequest(new HttpGet(uri), headers);
	}

	@Override
	public HttpResponse getUncached(String uri) {
		return executeRequest(new HttpGet(uri), true);
	}

	@Override
	public HttpResponse head(String uri) {
		return executeRequest(new HttpHead(uri));
	}

	@Override
	public HttpResponse post(String uri, String content) {
		return executePutPost(new HttpPost(uri), content, false);
	}

	@Override
	public HttpResponse post(String uri, InputStream content) {
		InputStreamEntity e = new InputStreamEntity(content, -1);
		e.setContentType("application/json");
		HttpPost post = new HttpPost(uri);
		post.setEntity(e);
		return executeRequest(post);
	}

	@Override
	public HttpResponse postUncached(String uri, String content) {
		return executePutPost(new HttpPost(uri), content, true);
	}

	@Override
	public HttpResponse put(String uri) {
		return executeRequest(new HttpPut(uri));
	}

	@Override
	public HttpResponse put(String uri, String content) {
		return executePutPost(new HttpPut(uri), content, false);
	}

	@Override
	public HttpResponse put(String uri, InputStream data, String contentType,
			long contentLength) {
		InputStreamEntity e = new InputStreamEntity(data, contentLength);
		e.setContentType(contentType);

		HttpPut hp = new HttpPut(uri);
		hp.setEntity(e);
		return executeRequest(hp);
	}
	
	@Override
	public HttpResponse copy(String sourceUri, String destination) {
		return executeRequest(new HttpCopyRequest(sourceUri, destination), true);
	}

	private HttpResponse executePutPost(HttpEntityEnclosingRequestBase request,
			String content, boolean useBackend) {
		try {
			LOG.trace("Content: {}", content);
			StringEntity e = new StringEntity(content, "UTF-8");
			e.setContentType("application/json");
			request.setEntity(e);
			return executeRequest(request, useBackend);
		} catch (Exception e) {
			throw Exceptions.propagate(e);
		}
	}

	private HttpResponse executeRequest(HttpRequestBase request, Map<String, String> headers) {
		for(Map.Entry<String, String> header : headers.entrySet()) {
			request.setHeader(header.getKey(), header.getValue());
		}
		return executeRequest(request);
	}

	private HttpResponse executeRequest(HttpRequestBase request, boolean useBackend) {
		try {
			org.apache.http.HttpResponse rsp;
			if (useBackend) {
				rsp = backend.execute(request);
			} else {
				rsp = client.execute((HttpHost)client.getParams().getParameter(ClientPNames.DEFAULT_HOST), request);
			}
			LOG.trace("{} {} {} {}", new Object[] { request.getMethod(), request.getURI(),
					rsp.getStatusLine().getStatusCode(), rsp.getStatusLine().getReasonPhrase() });
			return AndroidHttpResponse.of(rsp, request);
		} catch (Exception e) {
			throw Exceptions.propagate(e);
		}
	}

	private HttpResponse executeRequest(HttpRequestBase request) {
		return executeRequest(request, false);
	}

        public void shutdown() {
                client.getConnectionManager().shutdown();
        }

	public static class Builder {
		String host = "localhost";
		int port = 5984;
		int maxConnections = 20;
		int connectionTimeout = 1000;
		int socketTimeout = 10000;
		ClientConnectionManager conman;
		int proxyPort = -1;
		String proxy = null;

		boolean enableSSL = false;
		boolean relaxedSSLSettings = false;
		SSLSocketFactory sslSocketFactory;

		String username;
		String password;

		boolean cleanupIdleConnections = true;
		boolean useExpectContinue = true;
		boolean caching = true;
		int maxObjectSizeBytes = 8192;
		int maxCacheEntries = 1000;

		public Builder url(String s) throws MalformedURLException {
			if (s == null) return this;
			return this.url(new URL(s));
		}
		/**
		 * Will set host, port and possible enables SSL based on the properties if the supplied URL.
		 * This method overrides the properties: host, port and enableSSL.
		 * @param url
		 * @return
		 */
		public Builder url(URL url){
			this.host = url.getHost();
			this.port = url.getPort();
			enableSSL("https".equals(url.getProtocol()));
			return this;
		}

		public Builder host(String s) {
			host = s;
			return this;
		}

		public Builder proxyPort(int p) {
			proxyPort = p;
			return this;
		}

		public Builder proxy(String s) {
			proxy = s;
			return this;
		}
		/**
		 * Controls if the http client should cache response entities.
		 * Default is true.
		 * @param b
		 * @return
		 */
		public Builder caching(boolean b) {
			caching = b;
			return this;
		}

		public Builder maxCacheEntries(int m) {
			maxCacheEntries = m;
			return this;
		}
		public Builder maxObjectSizeBytes(int m) {
			maxObjectSizeBytes = m;
			return this;
		}

		public ClientConnectionManager configureConnectionManager(
				HttpParams params) {
			if (conman == null) {
				SchemeRegistry schemeRegistry = new SchemeRegistry();
				schemeRegistry.register(configureScheme());

				ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
				conman = cm;
			}

			if (cleanupIdleConnections) {
				IdleConnectionMonitor.monitor(conman);
			}
			return conman;
		}

		private Scheme configureScheme() {
			if (enableSSL) {
                try {
                    if (this.sslSocketFactory == null ) {
                        AndroidSSLSocketFactory androidSSLSocketFactory = new AndroidSSLSocketFactory((KeyStore)null);
                        androidSSLSocketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                        return new Scheme("https", androidSSLSocketFactory, port);
                    } else {
                        return new Scheme("https", this.sslSocketFactory, port);
                    }
				} catch (Exception e) {
					throw Exceptions.propagate(e);
				}
			} else {
				return new Scheme("http", PlainSocketFactory.getSocketFactory(), port);
			}
		}

		public org.apache.http.client.HttpClient configureClient() {
			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setUseExpectContinue(params, useExpectContinue);
			HttpConnectionParams
					.setConnectionTimeout(params, connectionTimeout);
			HttpConnectionParams.setSoTimeout(params, socketTimeout);
			HttpConnectionParams.setTcpNoDelay(params, Boolean.TRUE);

            String protocol = "http";

			if (enableSSL)
                protocol = "https";

			params.setParameter(ClientPNames.DEFAULT_HOST, new HttpHost(host,
					port, protocol));
			if (proxy != null) {
				params.setParameter(ConnRoutePNames.DEFAULT_PROXY,
						new HttpHost(proxy, proxyPort, protocol));
			}
			DefaultHttpClient client = new DefaultHttpClient(
					configureConnectionManager(params), params);
			if (username != null && password != null) {
				client.getCredentialsProvider().setCredentials(
						new AuthScope(host, port, AuthScope.ANY_REALM),
						new UsernamePasswordCredentials(username, password));
				client.addRequestInterceptor(
						new PreemptiveAuthRequestInterceptor(), 0);
			}



			return client;
		}

		public Builder port(int i) {
			port = i;
			return this;
		}

		public Builder username(String s) {
			username = s;
			return this;
		}

		public Builder password(String s) {
			password = s;
			return this;
		}

		public Builder maxConnections(int i) {
			maxConnections = i;
			return this;
		}

		public Builder connectionTimeout(int i) {
			connectionTimeout = i;
			return this;
		}

		public Builder socketTimeout(int i) {
			socketTimeout = i;
			return this;
		}

		/**
		 * If set to true, a monitor thread will be started that cleans up idle
		 * connections every 30 seconds.
		 *
		 * @param b
		 * @return
		 */
		public Builder cleanupIdleConnections(boolean b) {
			cleanupIdleConnections = b;
			return this;
		}

		/**
		 * Bring your own Connection Manager. If this parameters is set, the
		 * parameters port, maxConnections, connectionTimeout and socketTimeout
		 * are ignored.
		 *
		 * @param cm
		 * @return
		 */
		public Builder connectionManager(ClientConnectionManager cm) {
			conman = cm;
			return this;
		}

		/**
		 * Set to true in order to enable SSL sockets. Note that the CouchDB
		 * host must be accessible through a https:// path Default is false.
		 *
		 * @param b
		 * @return
		 */
		public Builder enableSSL(boolean b) {
			enableSSL = b;
			return this;
		}

		/**
		 * Bring your own SSLSocketFactory. Note that schemeName must be also be
		 * configured to "https". Will override any setting of
		 * relaxedSSLSettings.
		 *
		 * @param f
		 * @return
		 */
		public Builder sslSocketFactory(SSLSocketFactory f) {
			sslSocketFactory = f;
			return this;
		}

		/**
		 * If set to true all SSL certificates and hosts will be trusted. This
		 * might be handy during development. default is false.
		 *
		 * @param b
		 * @return
		 */
		public Builder relaxedSSLSettings(boolean b) {
			relaxedSSLSettings = b;
			return this;
		}

		/**
		 * Activates 'Expect: 100-Continue' handshake with CouchDB.
		 * Using expect continue can reduce stale connection problems for PUT / POST operations.
		 * body. Enabled by default.
		 *
		 * @param b
		 * @return
		 */
		public Builder useExpectContinue(boolean b) {
			useExpectContinue = b;
			return this;
		}

		public HttpClient build() {
			return new AndroidHttpClient(configureClient());
		}

	}

}
