package org.ektorp.spring;

import org.apache.http.conn.ssl.*;
import org.ektorp.http.*;
import org.ektorp.support.*;
import org.slf4j.*;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.annotation.*;
/**
 * FactoryBean that produces a HttpClient.
 * Configuration parameters are set through @Value annotations.
 * 
 * The application context must define properties along the line of:
 * <code>
 * <util:properties id="couchdbProperties" location="classpath:/couchdb.properties"/>
 * </code>
 * @author henrik lundgren
 *
 */
public class HttpClientFactoryBean implements FactoryBean<HttpClient> {

	private final static Logger LOG = LoggerFactory.getLogger(HttpClientFactoryBean.class);
	
	public @Value("#{couchdbProperties['url']?:'http://localhost:5984'}")	String url = "http://localhost:5984";
	public @Value("#{couchdbProperties['host']?:'localhost'}")				String host;
	public @Value("#{couchdbProperties['port']?:5984}")						int port;
	public @Value("#{couchdbProperties['maxConnections']?:20}") 			int maxConnections = 20;
	public @Value("#{couchdbProperties['connectionTimeout']?:1000}") 		int connectionTimeout = 1000;
	public @Value("#{couchdbProperties['socketTimeout']?:10000}")			int socketTimeout = 10000;
	public @Value("#{couchdbProperties['autoUpdateViewOnChange']?:false}") 	boolean autoUpdateViewOnChange;
	public @Value("#{couchdbProperties['username']}")						String username;
	public @Value("#{couchdbProperties['password']}")						String password;
	public @Value("#{couchdbProperties['testConnectionAtStartup']?:false}") boolean testConnectionAtStartup;
	public @Value("#{couchdbProperties['cleanupIdleConnections']?:true}") 	boolean cleanupIdleConnections = true;
	public @Value("#{couchdbProperties['enableSSL']?:false}") 				boolean enableSSL;
	public @Value("#{couchdbProperties['relaxedSSLSettings']?:false}") 		boolean relaxedSSLSettings;
	public @Value("#{couchdbProperties['caching']?:true}")		 			boolean caching = true;
	public @Value("#{couchdbProperties['maxCacheEntries']?:1000}")			int maxCacheEntries = 1000;
	public @Value("#{couchdbProperties['maxObjectSizeBytes']?:8192}")		int maxObjectSizeBytes = 8192;
	
	
	private SSLSocketFactory sslSocketFactory;
	
	public HttpClient getObject() throws Exception {
		LOG.debug("host: {}", host);
		LOG.debug("port: {}", port);
		LOG.debug("maxConnections: {}", maxConnections);
		LOG.debug("connectionTimeout: {}", connectionTimeout);
		LOG.debug("socketTimeout: {}", socketTimeout);
		LOG.debug("autoUpdateViewOnChange: {}", autoUpdateViewOnChange);
		LOG.debug("testConnectionAtStartup: {}", testConnectionAtStartup);
		LOG.debug("cleanupIdleConnections: {}", cleanupIdleConnections);
		LOG.debug("enableSSL: {}", enableSSL);
		LOG.debug("relaxedSSLSettings: {}", relaxedSSLSettings);
		
		HttpClient client = new StdHttpClient.Builder()
								.host(host)
								.port(port)
								.maxConnections(maxConnections)
								.connectionTimeout(connectionTimeout)
								.socketTimeout(socketTimeout)
								.username(username)
								.password(password)
								.cleanupIdleConnections(cleanupIdleConnections)
								.enableSSL(enableSSL)
								.relaxedSSLSettings(relaxedSSLSettings)
								.sslSocketFactory(sslSocketFactory)
								.caching(caching)
								.maxCacheEntries(maxCacheEntries)
								.maxObjectSizeBytes(maxObjectSizeBytes)
								.url(url)
								.build();
		
		if (testConnectionAtStartup) {
			testConnect(client);
		}
		
		configureAutoUpdateViewOnChange();
		return client;
	}
	
	private void configureAutoUpdateViewOnChange() {
		if (autoUpdateViewOnChange && !Boolean.getBoolean(CouchDbRepositorySupport.AUTO_UPDATE_VIEW_ON_CHANGE)) {
			System.setProperty(CouchDbRepositorySupport.AUTO_UPDATE_VIEW_ON_CHANGE, Boolean.TRUE.toString());
		}
	}

	private void testConnect(HttpClient client) {
		try {
			client.head("/");
		} catch (Exception e) {
			throw new BeanCreationException(String.format("Cannot connect to CouchDb@%s:%s", host, port), e);
		}
	}

	public Class<? extends HttpClient> getObjectType() {
		return HttpClient.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public void setAutoUpdateViewOnChange(boolean b) {
		this.autoUpdateViewOnChange = b;
	}
	
	public void setUsername(String user) {
		this.username = user;
	}
	
	public void setPassword(String s) {
		this.password = s;
	}
	
	public void setTestConnectionAtStartup(boolean b) {
		this.testConnectionAtStartup = b;
	}
	
	public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
		this.sslSocketFactory = sslSocketFactory;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public void setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	public void setCleanupIdleConnections(boolean cleanupIdleConnections) {
		this.cleanupIdleConnections = cleanupIdleConnections;
	}

	public void setEnableSSL(boolean enableSSL) {
		this.enableSSL = enableSSL;
	}

	public void setRelaxedSSLSettings(boolean relaxedSSLSettings) {
		this.relaxedSSLSettings = relaxedSSLSettings;
	}

	public void setCaching(boolean caching) {
		this.caching = caching;
	}

	public void setMaxCacheEntries(int maxCacheEntries) {
		this.maxCacheEntries = maxCacheEntries;
	}

	public void setMaxObjectSizeBytes(int maxObjectSizeBytes) {
		this.maxObjectSizeBytes = maxObjectSizeBytes;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
}
