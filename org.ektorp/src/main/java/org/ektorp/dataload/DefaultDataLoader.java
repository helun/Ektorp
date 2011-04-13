package org.ektorp.dataload;

import java.io.*;
import java.util.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.map.*;
import org.ektorp.*;
import org.ektorp.util.*;
import org.slf4j.*;
/**
 * 
 * Helper for DataLoaders
 * @author Henrik Lundgren created 7 nov 2009
 * 
 */
public class DefaultDataLoader {

	private final static Logger LOG = LoggerFactory.getLogger(DefaultDataLoader.class);
	
	private final ObjectMapper objectMapper;
	protected final CouchDbConnector db;

	public DefaultDataLoader(CouchDbConnector db) {
		this(db, new ObjectMapper());
	}
	
	public DefaultDataLoader(CouchDbConnector db, ObjectMapper objectMapper) {
		Assert.notNull(db, "CouchDbConnector cannot be null");
		Assert.notNull(objectMapper, "ObjectMapper cannot be null");
		this.db = db;
		this.objectMapper = objectMapper;
	}
	/**
	 * Reads documents from the reader and stores them in the database.
	 * @param in
	 */
	public void load(Reader in) {
		try {
			doLoad(in);
		} catch (Exception e) {
			throw Exceptions.propagate(e);
		}
	}

	private void doLoad(Reader in) throws IOException, JsonParseException,
			JsonMappingException {
		Set<String> allIds = new HashSet<String>(db.getAllDocIds());
		JsonNode jn = objectMapper.readValue(in, JsonNode.class);

		for (Iterator<JsonNode> i = jn.getElements(); i.hasNext();) {
			JsonNode n = i.next();
			String id = n.get("_id").getTextValue();
			if (!allIds.contains(id)) {
				LOG.info("adding {} to database", id);
				createDocument(n, id);
			}
		}
	}
	/**
	 * Can be overidden in order to customize document creation.
	 * @param n
	 * @param id
	 */
	protected void createDocument(JsonNode n, String id) {
		db.create(id, n);
	}
}
