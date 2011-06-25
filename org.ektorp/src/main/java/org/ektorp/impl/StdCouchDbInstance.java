package org.ektorp.impl;

import static java.lang.String.*;

import java.io.*;
import java.util.*;

import org.codehaus.jackson.map.*;
import org.codehaus.jackson.type.*;
import org.ektorp.*;
import org.ektorp.http.*;
import org.ektorp.util.*;
import org.slf4j.*;
/**
 * 
 * @author henrik lundgren
 *
 */
public class StdCouchDbInstance implements CouchDbInstance {

	private final static Logger LOG = LoggerFactory.getLogger(StdCouchDbInstance.class);
	private final static TypeReference<List<String>> STRING_LIST_TYPE_DEF = new TypeReference<List<String>>() {};
	
	private final HttpClient client;
	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;
	private final ObjectMapperFactory objectMapperFactory;
	
	public StdCouchDbInstance(HttpClient client) {
		this(client, new StdObjectMapperFactory());
	}
	
	public StdCouchDbInstance(HttpClient client, ObjectMapperFactory of) {
		Assert.notNull(client, "HttpClient may not be null");
		Assert.notNull(of, "ObjectMapperFactory may not be null");
		this.client = client;
		this.restTemplate = new RestTemplate(client);
		this.objectMapper = of.createObjectMapper();
		this.objectMapperFactory = of;
	}
	
	public void createDatabase(String path) {
		createDatabase(DbPath.fromString(path));
	}
	
	public void createDatabase(DbPath db) {
		if (checkIfDbExists(db)) {
			throw new DbAccessException(format("A database with path %s already exists", db.getPath()));
		}
		LOG.debug("creating db path: {}", db.getPath());
		restTemplate.put(db.getPath());
	}

	public void deleteDatabase(String path) {
		Assert.notNull(path);
		restTemplate.delete(DbPath.fromString(path).getPath());
	}

	@Override
	public boolean checkIfDbExists(DbPath db) {
	    return restTemplate.head(db.getPath(), new StdResponseHandler<Boolean>() {
		@Override
		public Boolean error(HttpResponse hr) {
		    return false;
		}
		@Override
		public Boolean success(HttpResponse hr) throws Exception {
		    return true;
		}
	    });
	}

	public List<String> getAllDatabases() {
		return restTemplate.get("/_all_dbs", new StdResponseHandler<List<String>>(){
			@Override
			public List<String> success(HttpResponse hr) throws Exception {
				return objectMapper.readValue(hr.getContent(), STRING_LIST_TYPE_DEF);
			}
		});
	}
	
	public ReplicationStatus replicate(ReplicationCommand cmd) {
		try {
			return restTemplate.post("/_replicate", objectMapper.writeValueAsString(cmd), new StdResponseHandler<ReplicationStatus>() {
				@Override
				public ReplicationStatus success(HttpResponse hr)
						throws Exception {
					return objectMapper.readValue(hr.getContent(), ReplicationStatus.class);
				}
			});	
		} catch (IOException e) {
			throw Exceptions.propagate(e);
		}
	}

	public HttpClient getConnection() {
		return client;
	}
	
	public CouchDbConnector createConnector(String path,
			boolean createIfNotExists) {
		CouchDbConnector db = new StdCouchDbConnector(path, this, objectMapperFactory);
		if (createIfNotExists) db.createDatabaseIfNotExists();
		return db;
	}
}
