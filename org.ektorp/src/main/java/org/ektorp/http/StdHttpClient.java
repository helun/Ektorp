package org.ektorp.http;

import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
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
	private final static Logger LOG = LoggerFactory
			.getLogger(StdHttpClient.class);

	public StdHttpClient(org.apache.http.client.HttpClient hc) {
		client = hc;
	}

	public HttpResponse delete(String uri) {
		return executeRequest(new HttpDelete(appendDefaultHostToUri(uri)));
	}

	public HttpResponse get(String uri) {
		return executeRequest(new HttpGet(appendDefaultHostToUri(uri)));
	}

	public HttpResponse post(String uri, String content) {
		return executePutPost(new HttpPost(appendDefaultHostToUri(uri)), content);
	}

	public HttpResponse post(String uri, InputStream content) {
		InputStreamEntity e = new InputStreamEntity(content, -1);
		e.setContentType("application/json");
		HttpPost post = new HttpPost(appendDefaultHostToUri(uri));
		post.setEntity(e);
		return executeRequest(post);
	}

	public HttpResponse put(String uri, String content) {
		return executePutPost(new HttpPut(appendDefaultHostToUri(uri)), content);
	}

	public HttpResponse put(String uri) {
		return executeRequest(new HttpPut(appendDefaultHostToUri(uri)));
	}

	public HttpResponse put(String uri, InputStream data, String contentType,
			long contentLength) {
		InputStreamEntity e = new InputStreamEntity(data, contentLength);
		e.setContentType(contentType);

		HttpPut hp = new HttpPut(appendDefaultHostToUri(uri));
		hp.setEntity(e);
		return executeRequest(hp);
	}

	public HttpResponse head(String uri) {
		return executeRequest(new HttpHead(appendDefaultHostToUri(uri)));
	}
	
	private String appendDefaultHostToUri(String uri) {
		HttpHost host = (HttpHost) client.getParams().getParameter(ClientPNames.DEFAULT_HOST);
		StringBuilder hostBuilder = new StringBuilder();
		hostBuilder.append(host.getSchemeName());
		hostBuilder.append("://");
		hostBuilder.append(host.getHostName());
		hostBuilder.append(":");
		hostBuilder.append(host.getPort());
		hostBuilder.append(uri);
		return hostBuilder.toString();
	}

	private HttpResponse executePutPost(HttpEntityEnclosingRequestBase request,
			String content) {
		try {
			if (LOG.isTraceEnabled()) {
				LOG.trace("Content: {}", content);
			}
			StringEntity e = new StringEntity(content, "UTF-8");
			e.setContentType("application/json");
			request.setEntity(e);
			return executeRequest(request);
		} catch (Exception e) {
			throw Exceptions.propagate(e);
		}
	}

	private HttpResponse executeRequest(HttpRequestBase request) {
		try {
			org.apache.http.HttpResponse rsp = client.execute(request);
			if (LOG.isTraceEnabled()) {
				LOG.trace(String.format("%s %s %s %s", request.getMethod(),
						request.getURI(), rsp.getStatusLine().getStatusCode(),
						rsp.getStatusLine().getReasonPhrase()));
			}
			return StdHttpResponse.of(rsp, request);
		} catch (Exception e) {
			throw Exceptions.propagate(e);
		}
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
		boolean caching = false;
		int maxObjectSizeBytes = 8192;
		int maxCacheEntries = 1000;

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

				ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(schemeRegistry);
				cm.setMaxTotal(maxConnections);
				cm.setDefaultMaxPerRoute(maxConnections);
				conman = cm;
			}

			if (cleanupIdleConnections) {
				IdleConnectionMonitor.monitor(conman);
			}
			return conman;
		}

		@SuppressWarnings("deprecation")
		private Scheme configureScheme() {
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
						}

						sslSocketFactory = relaxedSSLSettings ? new SSLSocketFactory(context, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER) : new SSLSocketFactory(context);

					}
					return new Scheme("https", port, sslSocketFactory);
                } catch (NoSuchMethodError e) {
                    try {
                        AndroidSSLSocketFactory androidSSLSocketFactory = new AndroidSSLSocketFactory((KeyStore)null);
                        androidSSLSocketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                        return new Scheme("https", androidSSLSocketFactory, port);
                    } catch (Exception e1) {
					    throw Exceptions.propagate(e1);
                    }
				} catch (Exception e) {
					throw Exceptions.propagate(e);
				}
			} else {
				return new Scheme("http", port, PlainSocketFactory.getSocketFactory());
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
			
			if (caching) {
				CacheConfig cacheConfig = new CacheConfig();  
				cacheConfig.setMaxCacheEntries(maxCacheEntries);
				cacheConfig.setMaxObjectSizeBytes(maxObjectSizeBytes);
				
				CachingHttpClient cachingHttpClient = new CachingHttpClient(client, cacheConfig);
				
				return cachingHttpClient;
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
			return new StdHttpClient(configureClient());
		}

	}

}
