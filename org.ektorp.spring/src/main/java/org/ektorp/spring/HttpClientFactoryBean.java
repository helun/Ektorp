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
	
	public @Value("#{couchdbProperties['host']?:'localhost'}")				String host;
	public @Value("#{couchdbProperties['port']?:5984}")						int port;
	public @Value("#{couchdbProperties['maxConnections']?:20}") 			int maxConnections;
	public @Value("#{couchdbProperties['connectionTimeout']?:1000}") 		int connectionTimeout;
	public @Value("#{couchdbProperties['socketTimeout']?:10000}")			int socketTimeout;
	public @Value("#{couchdbProperties['autoUpdateViewOnChange']?:false}") 	boolean autoUpdateViewOnChange;
	public @Value("#{couchdbProperties['username']}")						String username;
	public @Value("#{couchdbProperties['password']}")						String password;
	public @Value("#{couchdbProperties['testConnectionAtStartup']?:false}") boolean testConnectionAtStartup;
	public @Value("#{couchdbProperties['cleanupIdleConnections']?:true}") 	boolean cleanupIdleConnections;
	public @Value("#{couchdbProperties['enableSSL']?:false}") 				boolean enableSSL;
	public @Value("#{couchdbProperties['relaxedSSLSettings']?:false}") 		boolean relaxedSSLSettings;
	
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
	
}
