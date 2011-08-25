package org.ektorp.spring;

import java.util.Properties;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.support.CouchDbRepositorySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.FactoryBean;
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
	
	public String url = "http://localhost:5984";
	public String host;
	public int port;
	public int maxConnections = 20;
	public int connectionTimeout = 1000;
	public int socketTimeout = 10000;
	public boolean autoUpdateViewOnChange;
	public String username;
	public String password;
	public boolean testConnectionAtStartup;
	public boolean cleanupIdleConnections = true;
	public boolean enableSSL;
	public boolean relaxedSSLSettings;
	public boolean caching = true;
	public int maxCacheEntries = 1000;
	public int maxObjectSizeBytes = 8192;
	
	private SSLSocketFactory sslSocketFactory;
	
	private Properties couchDBProperties;
	
	public HttpClient getObject() throws Exception {
		if (couchDBProperties != null) {
			new DirectFieldAccessor(this).setPropertyValues(couchDBProperties);
		}
		
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
	
	public void setProperties(Properties p) {
		this.couchDBProperties = p;
	}
	
}
