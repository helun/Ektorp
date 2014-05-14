package org.ektorp.http;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DecompressingHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.ektorp.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author henrik lundgren
 * 
 */
public class StdHttpClient implements HttpClient {

	private final org.apache.http.client.HttpClient client;
	private final org.apache.http.client.HttpClient backend;
	private final static Logger LOG = LoggerFactory
			.getLogger(StdHttpClient.class);

	public StdHttpClient(org.apache.http.client.HttpClient hc) {
		this(hc, hc);
	}
	public StdHttpClient(org.apache.http.client.HttpClient hc, 
			org.apache.http.client.HttpClient backend) {
		this.client = hc;
		this.backend = backend;
	}

	public org.apache.http.client.HttpClient getClient() {
		return client;
	}

	public org.apache.http.client.HttpClient getBackend() {
		return backend;
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
	public HttpResponse postUncached(String uri, String content) {
		return executePutPost(new HttpPost(uri), content, true);
	}

	@Override
	public HttpResponse post(String uri, String content) {
		return executePutPost(new HttpPost(uri), content, false);
	}

	@Override
	public HttpResponse post(String uri, InputStream content) {
		InputStreamEntity e = new InputStreamEntity(content, -1);
		e.setContentType("application/json");
		return post(uri, e);
	}

	@Override
	public HttpResponse post(String uri, HttpEntity httpEntity) {
		HttpPost post = new HttpPost(uri);
		post.setEntity(httpEntity);
		return executeRequest(post, true);
	}

	@Override
	public HttpResponse put(String uri, String content) {
		return executePutPost(new HttpPut(uri), content, false);
	}

	@Override
	public HttpResponse put(String uri) {
		return executeRequest(new HttpPut(uri));
	}

	@Override
	public HttpResponse put(String uri, InputStream data, String contentType,
			long contentLength) {
		InputStreamEntity e = new InputStreamEntity(data, contentLength);
		e.setContentType(contentType);
		return put(uri, e);
	}

	@Override
	public HttpResponse put(String uri, HttpEntity httpEntity) {
		HttpPut hp = new HttpPut(uri);
		hp.setEntity(httpEntity);
		return executeRequest(hp);
	}

	@Override
	public HttpResponse head(String uri) {
		return executeRequest(new HttpHead(uri));
	}

	protected HttpResponse executePutPost(HttpEntityEnclosingRequestBase request,
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



	protected HttpResponse executeRequest(HttpRequestBase request, Map<String, String> headers) {
		for(Map.Entry<String, String> header : headers.entrySet()) {
			request.setHeader(header.getKey(), header.getValue());
		}
		return executeRequest(request);
	}

	protected HttpResponse executeRequest(HttpUriRequest request, boolean useBackend) {
		try {
			org.apache.http.HttpResponse rsp;
			if (useBackend) {
				rsp = backend.execute(request);
			} else {
				rsp = client.execute(getHttpHost(), request);
			}
			LOG.trace("{} {} {} {}", new Object[] { request.getMethod(), request.getURI(),
					rsp.getStatusLine().getStatusCode(), rsp.getStatusLine().getReasonPhrase() });
			return createHttpResponse(rsp, request);
		} catch (Exception e) {
			throw Exceptions.propagate(e);
		}		
	}

	protected HttpResponse createHttpResponse(org.apache.http.HttpResponse rsp, HttpUriRequest httpRequest) {
		return new StdHttpResponse(rsp.getEntity(), rsp.getStatusLine(), httpRequest, rsp.getFirstHeader("ETag"));
	}

	protected HttpResponse executeRequest(HttpRequestBase request) {
		return executeRequest(request, false);
	}

	@Override
	public HttpResponse copy(String sourceUri, String destination) {
		return executeRequest(new HttpCopyRequest(sourceUri, destination), true);
	}
	
	public void shutdown() {
		client.getConnectionManager().shutdown();
	}

	protected HttpHost getHttpHost() {
		return (HttpHost)client.getParams().getParameter(ClientPNames.DEFAULT_HOST);
	}

	public static class Builder {
		protected String host = "localhost";
		protected int port = 5984;
		protected int maxConnections = 20;
		protected int connectionTimeout = 1000;
		protected int socketTimeout = 10000;
		protected ClientConnectionManager conman;
		protected int proxyPort = -1;
		protected String proxy = null;

		protected boolean enableSSL = false;
		protected boolean relaxedSSLSettings = false;
		protected SSLSocketFactory sslSocketFactory;

		protected String username;
		protected String password;

		protected boolean cleanupIdleConnections = true;
		protected boolean useExpectContinue = true;
		protected boolean caching = true;
		protected boolean compression; // Default is false;
		protected int maxObjectSizeBytes = 8192;
		protected int maxCacheEntries = 1000;

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
			if (url.getUserInfo() != null) {
				String[] userInfoParts = url.getUserInfo().split(":");
				if (userInfoParts.length == 2) {
					this.username = userInfoParts[0];
					this.password = userInfoParts[1];
				}
			}
			enableSSL("https".equals(url.getProtocol()));
			if (this.port == -1) {
				if (this.enableSSL) {
					this.port = 443;
				} else {
					this.port = 80;
				}
			}
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
		 * Controls if the http client should send Accept-Encoding: gzip,deflate
		 * header and handle Content-Encoding responses. This enable compression
		 * on the server; although not supported natively by CouchDB, you can
		 * use a reverse proxy, such as nginx, in front of CouchDB to achieve
		 * this.
		 * <p>
		 * Disabled by default (for backward compatibility).
		 * 
		 * @param b
		 * @return This builder
		 */
		public Builder compression(boolean b){
			compression = b;
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

				PoolingClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
				cm.setMaxTotal(maxConnections);
				cm.setDefaultMaxPerRoute(maxConnections);
				conman = cm;
			}

			if (cleanupIdleConnections) {
				IdleConnectionMonitor.monitor(conman);
			}
			return conman;
		}

		protected Scheme configureScheme() {
			if (enableSSL) {
				try {
					if (sslSocketFactory == null) {
						SSLContext context = SSLContext.getInstance("TLS");

						if (relaxedSSLSettings) {
							context.init(
									null,
									new TrustManager[] { new X509TrustManager() {
										public java.security.cert.X509Certificate[] getAcceptedIssuers() {
											return null;
										}

										public void checkClientTrusted(
												java.security.cert.X509Certificate[] certs,
												String authType) {
										}

										public void checkServerTrusted(
												java.security.cert.X509Certificate[] certs,
												String authType) {
										}
									} }, null);
						} else {
							context.init(null, null, null);
						}

						sslSocketFactory = relaxedSSLSettings ? new SSLSocketFactory(context, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER) : new SSLSocketFactory(context);

					}
					return new Scheme("https", port, sslSocketFactory);
                } catch (Exception e) {
					throw Exceptions.propagate(e);
				}
			} else {
				return new Scheme("http", port, PlainSocketFactory.getSocketFactory());
			}
		}

		public org.apache.http.client.HttpClient configureClient() {
			HttpParams params = configureHttpParams();
			ClientConnectionManager connectionManager = configureConnectionManager(params);
			DefaultHttpClient client = new DefaultHttpClient(connectionManager, params);
			if (username != null && password != null) {
				client.getCredentialsProvider().setCredentials(
						new AuthScope(host, port, AuthScope.ANY_REALM),
						new UsernamePasswordCredentials(username, password));
				client.addRequestInterceptor(
						new PreemptiveAuthRequestInterceptor(), 0);
			}
			
			if (compression) {
				return new DecompressingHttpClient(client);
			}
			return client;
		}

		/**
		 * this method is protected so that you can Override it
		 */
		protected HttpParams configureHttpParams() {
			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setUseExpectContinue(params, useExpectContinue);
			HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
			HttpConnectionParams.setSoTimeout(params, socketTimeout);
			HttpConnectionParams.setTcpNoDelay(params, Boolean.TRUE);

			String protocol = "http";

			if (enableSSL) {
				protocol = "https";
			}

			params.setParameter(ClientPNames.DEFAULT_HOST, new HttpHost(host, port, protocol));
			if (proxy != null) {
				params.setParameter(ConnRoutePNames.DEFAULT_PROXY, new HttpHost(proxy, proxyPort, protocol));
			}
			return params;
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
		 * @param s
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
			org.apache.http.client.HttpClient client = configureClient();
			org.apache.http.client.HttpClient cachingHttpClient = client;

			if (caching) {
				cachingHttpClient = WithCachingBuilder.withCaching(client, maxCacheEntries, maxObjectSizeBytes);
			}
			return new StdHttpClient(cachingHttpClient, client);
		}

	}

        // separate class to avoid runtime dependency to httpclient-cache unless using caching
	public static class WithCachingBuilder {
		public static org.apache.http.client.HttpClient withCaching(org.apache.http.client.HttpClient client, int maxCacheEntries, int maxObjectSizeBytes) {
			CacheConfig cacheConfig = new CacheConfig();  
			cacheConfig.setMaxCacheEntries(maxCacheEntries);
			cacheConfig.setMaxObjectSize(maxObjectSizeBytes);
			return new CachingHttpClient(client, cacheConfig);
		}
	}
}
