package org.ektorp.http;

import java.io.*;
import java.security.KeyStore;

import javax.net.ssl.*;

import org.apache.http.*;
import org.apache.http.auth.*;
import org.apache.http.client.methods.*;
import org.apache.http.client.params.*;
import org.apache.http.conn.*;
import org.apache.http.conn.params.*;
import org.apache.http.conn.scheme.*;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.*;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.tsccm.*;
import org.apache.http.params.*;
import org.ektorp.util.*;
import org.slf4j.*;

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
		return executeRequest(new HttpDelete(uri));
	}

	public HttpResponse get(String uri) {
		return executeRequest(new HttpGet(uri));
	}

	public HttpResponse post(String uri, String content) {
		return executePutPost(new HttpPost(uri), content);
	}

	public HttpResponse post(String uri, InputStream content) {
		InputStreamEntity e = new InputStreamEntity(content, -1);
		e.setContentType("application/json");
		HttpPost post = new HttpPost(uri);
		post.setEntity(e);
		return executeRequest(post);
	}

	public HttpResponse put(String uri, String content) {
		return executePutPost(new HttpPut(uri), content);
	}

	public HttpResponse put(String uri) {
		return executeRequest(new HttpPut(uri));
	}

	public HttpResponse put(String uri, InputStream data, String contentType,
			long contentLength) {
		InputStreamEntity e = new InputStreamEntity(data, contentLength);
		e.setContentType(contentType);

		HttpPut hp = new HttpPut(uri);
		hp.setEntity(e);
		return executeRequest(hp);
	}

	public HttpResponse head(String uri) {
		return executeRequest(new HttpHead(uri));
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
			return StdHttpResponse.of(rsp, request.getURI().toString());
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
