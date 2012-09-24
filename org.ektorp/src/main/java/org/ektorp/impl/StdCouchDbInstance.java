package org.ektorp.impl;

import static java.lang.String.*;

import java.io.*;
import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
	private JsonSerializer jsonSerializer;

	public StdCouchDbInstance(HttpClient client) {
		this(client, new StdObjectMapperFactory());
	}

	public StdCouchDbInstance(HttpClient client, ObjectMapperFactory of) {
		Assert.notNull(client, "HttpClient may not be null");
		Assert.notNull(of, "ObjectMapperFactory may not be null");
		this.client = client;
		this.restTemplate = new RestTemplate(client);
		this.objectMapper = of.createObjectMapper();
		this.jsonSerializer = new StreamingJsonSerializer(objectMapper);
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

    @Override
    public CouchDbConnector getReplicatorConnector()
    {
        return createConnector("_replicator", false);
    }

    @Override
    public <T> T getConfiguration(final Class<T> c) {
       return getConfiguration(c, null, null);
    }

   @Override
   public <T> T getConfiguration(final Class<T> c, String section) {
       return getConfiguration(c, section, null);
   }

   @Override
   public <T> T getConfiguration(final Class<T> c, String section, String key) {
       Assert.notNull(c, "Class may not be null");
       String url = "/_config";
       if(section != null) {
           url = url + "/" + section;
           if(key != null) {
               url = url + "/" + key;
           }
       }
       return restTemplate.get(url,
          new StdResponseHandler<T>() {
             @Override
             public T success(HttpResponse hr) throws Exception {
                return objectMapper.readValue(hr.getContent(), c);
             }
          });
   }

   @Override
   public String getConfiguration(String section, String key) {
      return getConfiguration(String.class, section, key);
   }

   @Override
   public String setConfiguration(String section, String key, String value) {
      Assert.notNull(section, "Section may not be null");
      Assert.notNull(key, "Key may not be null");
      String url = "/_config/" + section + "/" + key;
      return restTemplate.put(url, jsonSerializer.toJson(value),
         new StdResponseHandler<String>() {
            @Override
            public String success(HttpResponse hr) throws Exception {
               String s = objectMapper.readValue(hr.getContent(), String.class);
               return s;
            }
         });
    }

   @Override
   public String deleteConfiguration(String section, String key) {
       Assert.notNull(section, "Section may not be null");
       Assert.notNull(key, "Key may not be null");
       String url = "/_config/" + section + "/" + key;
       return restTemplate.delete(url,
          new StdResponseHandler<String>() {
             @Override
             public String success(HttpResponse hr) throws Exception {
                String s = objectMapper.readValue(hr.getContent(), String.class);
                return s;
             }
         });
   }
}
