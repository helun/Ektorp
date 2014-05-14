package org.ektorp.android.http;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;
import org.ektorp.http.*;
import org.ektorp.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;

public class AndroidHttpClient extends StdHttpClient {

    private final static Logger LOG = LoggerFactory
            .getLogger(AndroidHttpClient.class);

    public AndroidHttpClient(org.apache.http.client.HttpClient hc) {
        super(hc);
    }

    public AndroidHttpClient(org.apache.http.client.HttpClient hc, org.apache.http.client.HttpClient backend) {
        super(hc, backend);
    }

    @Override
    public HttpResponse post(String uri, InputStream content) {
        InputStreamEntity e = new InputStreamEntity(content, -1);
        e.setContentType("application/json");
        HttpPost post = new HttpPost(uri);
        post.setEntity(e);
        return executeRequest(post, true);
    }

    @Override
    protected HttpResponse createHttpResponse(org.apache.http.HttpResponse rsp, HttpUriRequest httpRequest) {
        return AndroidHttpResponse.of(rsp, (HttpRequestBase) httpRequest);
    }

    @Override
    protected HttpResponse executeRequest(HttpRequestBase request) {
        return executeRequest(request, false);
    }

    public static class Builder extends StdHttpClient.Builder {


        @Override
        public Builder url(URL url) {
            this.host = url.getHost();
            this.port = url.getPort();
            enableSSL("https".equals(url.getProtocol()));
            return this;
        }

        @Override
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

        @Override
        protected Scheme configureScheme() {
            if (enableSSL) {
                try {
                    if (this.sslSocketFactory == null ) {
                        AndroidSSLSocketFactory androidSSLSocketFactory = new AndroidSSLSocketFactory((KeyStore)null);
                        androidSSLSocketFactory.setHostnameVerifier(SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
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

        // TODO : the only difference with super method is about when the compression parameter is true, but it is false by default, so we should consider deleting the Overriding method 
        @Override
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


            return client;
        }

        @Override
        public HttpClient build() {
            return new AndroidHttpClient(configureClient());
        }

    }

}
