package org.ektorp.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.ektorp.AttachmentInputStream;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.DbAccessException;
import org.ektorp.DbInfo;
import org.ektorp.DbPath;
import org.ektorp.DesignDocInfo;
import org.ektorp.DocumentOperationResult;
import org.ektorp.Page;
import org.ektorp.PageRequest;
import org.ektorp.ReplicationCommand;
import org.ektorp.ReplicationStatus;
import org.ektorp.Revision;
import org.ektorp.StreamingViewResult;
import org.ektorp.UpdateConflictException;
import org.ektorp.ViewQuery;
import org.ektorp.ViewResult;
import org.ektorp.changes.ChangesCommand;
import org.ektorp.changes.ChangesFeed;
import org.ektorp.changes.DocumentChange;
import org.ektorp.http.HttpClient;
import org.ektorp.http.HttpResponse;
import org.ektorp.http.HttpStatus;
import org.ektorp.http.ResponseCallback;
import org.ektorp.http.RestTemplate;
import org.ektorp.http.StdResponseHandler;
import org.ektorp.http.URI;
import org.ektorp.impl.changes.ContinuousChangesFeed;
import org.ektorp.impl.changes.StdDocumentChange;
import org.ektorp.util.Assert;
import org.ektorp.util.Documents;
import org.ektorp.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author henrik lundgren
 *
 */
public class StdCouchDbConnector implements CouchDbConnector {

	private static final int DEFAULT_HEARTBEAT_INTERVAL = 10000;
	private static final Logger LOG = LoggerFactory
			.getLogger(StdCouchDbConnector.class);
	private static final ResponseCallback<Void> VOID_RESPONSE_HANDLER = new StdResponseHandler<Void>();
	private final ObjectMapper objectMapper;
	private JsonSerializer jsonSerializer;

	private final URI dbURI;
	private final String dbName;

	private final RestTemplate restTemplate;

	private final CouchDbInstance dbInstance;

	private final RevisionResponseHandler revisionHandler;
	private final DocIdResponseHandler docIdResponseHandler;
	
	private final ThreadLocalBulkBufferHolder bulkBufferManager = new ThreadLocalBulkBufferHolder();

	public StdCouchDbConnector(String databaseName, CouchDbInstance dbInstance) {
		this(databaseName, dbInstance, new StdObjectMapperFactory());
	}

	public StdCouchDbConnector(String databaseName, CouchDbInstance dbi,
			ObjectMapperFactory om) {
		Assert.hasText(databaseName, "DatabaseName may not be null or empty");
		Assert.notNull(dbi, "CouchDbInstance may not be null");
		Assert.notNull(om, "ObjectMapperFactory may not be null");

		DbPath dbp = DbPath.fromString(databaseName);
		dbName = dbp.getDbName();
		this.dbURI = URI.prototype(dbp.getPath());
		this.dbInstance = dbi;
		this.objectMapper = om.createObjectMapper(this);

		this.jsonSerializer = new StreamingJsonSerializer(objectMapper);
		this.jsonSerializer = new StreamingJsonSerializer(objectMapper);
		this.jsonSerializer = new StreamingJsonSerializer(objectMapper);
		this.restTemplate = new RestTemplate(dbi.getConnection());
		this.revisionHandler = new RevisionResponseHandler(objectMapper);
		this.docIdResponseHandler = new DocIdResponseHandler(objectMapper);
	}

	public String path() {
		return dbURI.toString();
	}

	public void create(final Object o) {
		Assert.notNull(o, "Document may not be null");
		Assert.isTrue(Documents.isNew(o), "Object must be new");

		String json = jsonSerializer.toJson(o);
		String id = Documents.getId(o);
		DocumentOperationResult result;
		if (id != null) {
			result = restTemplate.put(URIWithDocId(id), json, revisionHandler);
		} else {
			result = restTemplate.post(dbURI.toString(), json, revisionHandler);
			Documents.setId(o, result.getId());
		}
		Documents.setRevision(o, result.getRevision());
	}

	public void create(String id, Object node) {
		assertDocIdHasValue(id);
		Assert.notNull(node, "node may not be null");
		restTemplate.put(URIWithDocId(id), jsonSerializer.toJson(node));
	}

	private String URIWithDocId(String id) {
		return dbURI.append(id).toString();
	}

	public boolean contains(String id) {
		return restTemplate.head(URIWithDocId(id),
				new ResponseCallback<Boolean>() {

					public Boolean error(HttpResponse hr) {
						if (hr.getCode() == HttpStatus.NOT_FOUND) {
							return Boolean.FALSE;
						}
						throw new DbAccessException(hr.toString());
					}

					public Boolean success(HttpResponse hr) throws Exception {
						return Boolean.TRUE;
					}

				});
	}

	public String createAttachment(String docId, AttachmentInputStream data) {
		return createAttachment(docId, null, data);
	}

	public String createAttachment(String docId, String revision,
			AttachmentInputStream data) {
		assertDocIdHasValue(docId);
		URI uri = dbURI.append(docId).append(data.getId());
		if (revision != null) {
			uri.param("rev", revision);
		}
		return restTemplate.put(uri.toString(), data, data.getContentType(),
				data.getContentLength(), revisionHandler).getRevision();
	}

	public AttachmentInputStream getAttachment(final String id,
			final String attachmentId) {
		assertDocIdHasValue(id);
		Assert.hasText(attachmentId, "attachmentId must have a value");
		if (LOG.isTraceEnabled()) {
			LOG.trace("fetching attachment for doc: {} attachmentId: {}", id,
					attachmentId);
		}
		HttpResponse r = restTemplate.get(dbURI.append(id).append(attachmentId)
				.toString());
		return new AttachmentInputStream(attachmentId, r.getContent(),
				r.getContentType(), r.getContentLength());
	}

	public String delete(Object o) {
		Assert.notNull(o, "document may not be null");
		return delete(Documents.getId(o), Documents.getRevision(o));
	}

	public <T> T get(final Class<T> c, String id) {
		Assert.notNull(c, "Class may not be null");
		assertDocIdHasValue(id);
		return restTemplate.get(URIWithDocId(id), new StdResponseHandler<T>() {
			public T success(HttpResponse hr) throws Exception {
				return objectMapper.readValue(hr.getContent(), c);
			}
		});
	}

	public <T> T get(final Class<T> c, String id, String rev) {
		Assert.notNull(c, "Class may not be null");
		assertDocIdHasValue(id);
		Assert.hasText(rev, "Revision may not be null or empty");
		return restTemplate.get(dbURI.append(id).param("rev", rev).toString(),
				new StdResponseHandler<T>() {
					public T success(HttpResponse hr) throws Exception {
						return objectMapper.readValue(hr.getContent(), c);
					}
				});
	}
	
	public <T> T getWithConflicts(final Class<T> c, String id) {
		Assert.notNull(c, "Class may not be null");
		assertDocIdHasValue(id);
		return restTemplate.get(dbURI.append(id).param("conflicts", "true").toString(),
				new StdResponseHandler<T>() {
					public T success(HttpResponse hr) throws Exception {
						return objectMapper.readValue(hr.getContent(), c);
					}
				});
	}

	public List<Revision> getRevisions(String id) {
		assertDocIdHasValue(id);
		return restTemplate.get(dbURI.append(id).param("revs_info", "true")
				.toString(), new StdResponseHandler<List<Revision>>() {

			public List<Revision> success(HttpResponse hr) throws Exception {
				JsonNode root = objectMapper.readValue(hr.getContent(),
						JsonNode.class);
				List<Revision> revs = new ArrayList<Revision>();
				for (Iterator<JsonNode> i = root.get("_revs_info")
						.getElements(); i.hasNext();) {
					JsonNode rev = i.next();
					revs.add(new Revision(rev.get("rev").getTextValue(), rev
							.get("status").getTextValue()));
				}
				return revs;
			}

			public List<Revision> error(HttpResponse hr) {
				if (hr.getCode() == HttpStatus.NOT_FOUND) {
					return Collections.emptyList();
				}
				return super.error(hr);
			}
		});
	}

	public InputStream getAsStream(String id) {
		assertDocIdHasValue(id);
		HttpResponse r = restTemplate.get(dbURI.append(id).toString());
		return r.getContent();
	}

	public InputStream getAsStream(String id, String rev) {
		assertDocIdHasValue(id);
		Assert.hasText(rev, "Revision may not be null or empty");
		HttpResponse r = restTemplate.get(dbURI.append(id).param("rev", rev)
				.toString());
		return r.getContent();
	}

	public void update(final Object o) {
		Assert.notNull(o, "Document cannot be null");
		final String id = Documents.getId(o);
		assertDocIdHasValue(id);
		restTemplate.put(dbURI.append(id).toString(), jsonSerializer.toJson(o),
				new StdResponseHandler<Void>() {

					public Void success(HttpResponse hr) throws Exception {
						JsonNode n = objectMapper.readValue(hr.getContent(),
								JsonNode.class);
						Documents.setRevision(o, n.get("rev").getTextValue());
						return null;
					}

					public Void error(HttpResponse hr) {
						if (hr.getCode() == HttpStatus.CONFLICT) {
							throw new UpdateConflictException(id, Documents
									.getRevision(o));
						}
						return super.error(hr);
					}
				});
	}

	public String delete(String id, String revision) {
		assertDocIdHasValue(id);
		return restTemplate.delete(
				dbURI.append(id).param("rev", revision).toString(),
				revisionHandler).getRevision();
	}

	public List<String> getAllDocIds() {
		return restTemplate.get(dbURI.append("_all_docs").toString(),
				docIdResponseHandler);
	}

	public void createDatabaseIfNotExists() {
		if (!dbInstance.checkIfDbExists(new DbPath(dbName))) {
			dbInstance.createDatabase(dbName);
		}
	}

	public String getDatabaseName() {
		return dbName;
	}

	public <T> List<T> queryView(final ViewQuery query, final Class<T> type) {
		Assert.notNull(query, "query may not be null");
		query.dbPath(dbURI.toString());

		EmbeddedDocViewResponseHandler<T> rh = new EmbeddedDocViewResponseHandler<T>(
				type, objectMapper, query.isIgnoreNotFound());

		return query.hasMultipleKeys() ? restTemplate.post(query.buildQuery(),
				query.getKeysAsJson(), rh) : restTemplate.get(
				query.buildQuery(), rh);
	}
	
	public <T> Page<T> queryForPage(ViewQuery query, PageRequest pr, Class<T> type) {
		Assert.notNull(query, "query may not be null");
		Assert.notNull(pr, "PageRequest may not be null");
		Assert.notNull(type, "type may not be null");
		
		query.dbPath(dbURI.toString());
		if (LOG.isDebugEnabled()) {
			LOG.debug("startKey: {}", pr.getStartKey());
			LOG.debug("startDocId: {}", pr.getStartKeyDocId());
		}
		PageResponseHandler<T> ph = new PageResponseHandler<T>(pr, type, objectMapper, query.isIgnoreNotFound());
		query = PageRequest.applyPagingParameters(query, pr);
		return query.hasMultipleKeys() ? restTemplate.post(query.buildQuery(),
				query.getKeysAsJson(), ph) : restTemplate.get(
				query.buildQuery(), ph);
	}

	public ViewResult queryView(ViewQuery query) {
		Assert.notNull(query, "query cannot be null");
		query.dbPath(dbURI.toString());
		ResponseCallback<ViewResult> rh = new StdResponseHandler<ViewResult>() {

			public ViewResult success(HttpResponse hr) throws Exception {
				return new ViewResult(objectMapper.readTree(hr.getContent()));
			}

		};
		return query.hasMultipleKeys() ? restTemplate.post(query.buildQuery(),
				query.getKeysAsJson(), rh) : restTemplate.get(
				query.buildQuery(), rh);
	}

	public StreamingViewResult queryForStreamingView(ViewQuery query) {
		return new StreamingViewResult(objectMapper,queryForStream(query));
	}
	
	public InputStream queryForStream(ViewQuery query) {
		Assert.notNull(query, "query cannot be null");
		query.dbPath(dbURI.toString());
		return query.hasMultipleKeys() ? restTemplate.postUncached(query.buildQuery(),
				query.getKeysAsJson()).getContent() : restTemplate.getUncached(
				query.buildQuery()).getContent();
	}

	public String deleteAttachment(String docId, String revision,
			String attachmentId) {
		return restTemplate.delete(
				dbURI.append(docId).append(attachmentId).param("rev", revision)
						.toString(), revisionHandler).getRevision();
	}

	private void assertDocIdHasValue(String docId) {
		Assert.hasText(docId, "document id cannot be empty");
	}

	public HttpClient getConnection() {
		return dbInstance.getConnection();
	}

	public DbInfo getDbInfo() {
		return restTemplate.get(dbURI.toString(),
				new StdResponseHandler<DbInfo>() {

					public DbInfo success(HttpResponse hr) throws Exception {
						return objectMapper.readValue(hr.getContent(),
								DbInfo.class);
					}
				});
	}

	public DesignDocInfo getDesignDocInfo(String designDocId) {
		Assert.hasText(designDocId, "designDocId may not be null or empty");
		String uri = dbURI.append("_design").append(designDocId)
				.append("_info").toString();

		return restTemplate.get(uri, new StdResponseHandler<DesignDocInfo>() {

			public DesignDocInfo success(HttpResponse hr) throws Exception {
				return objectMapper.readValue(hr.getContent(),
						DesignDocInfo.class);
			}
		});
	}

	public void compact() {
		restTemplate.post(dbURI.append("_compact").toString(), "not_used",
				VOID_RESPONSE_HANDLER);
	}

	public void cleanupViews() {
		restTemplate.post(dbURI.append("_view_cleanup").toString(), "not_used",
				VOID_RESPONSE_HANDLER);
	}

	public ReplicationStatus replicateFrom(String source) {
		ReplicationCommand cmd = new ReplicationCommand.Builder()
				.target(dbName).source(source).build();

		return dbInstance.replicate(cmd);
	}

	public ReplicationStatus replicateFrom(String source,
			Collection<String> docIds) {
		ReplicationCommand cmd = new ReplicationCommand.Builder()
				.target(dbName).source(source).docIds(docIds).build();

		return dbInstance.replicate(cmd);
	}

	public ReplicationStatus replicateTo(String target) {
		ReplicationCommand cmd = new ReplicationCommand.Builder()
				.target(target).source(dbName).build();

		return dbInstance.replicate(cmd);
	}

	public ReplicationStatus replicateTo(String target,
			Collection<String> docIds) {
		ReplicationCommand cmd = new ReplicationCommand.Builder()
				.target(target).source(dbName).docIds(docIds).build();

		return dbInstance.replicate(cmd);
	}

	public void compactViews(String designDocumentId) {
		Assert.hasText(designDocumentId,
				"designDocumentId may not be null or empty");
		restTemplate.post(dbURI.append("_compact").append(designDocumentId)
				.toString(), "not_used", VOID_RESPONSE_HANDLER);
	}

	public List<DocumentOperationResult> executeAllOrNothing(
			Collection<?> objects) {
		return executeBulk(objects, true);
	}

	public List<DocumentOperationResult> executeBulk(Collection<?> objects) {
		return executeBulk(objects, false);
	}

	public void addToBulkBuffer(Object o) {
		bulkBufferManager.add(o);
		LOG.debug("{} added to bulk buffer", o);
	}
	
	public void clearBulkBuffer() {
		bulkBufferManager.clear();
		LOG.debug("bulk buffer cleared");
	}
	
	public List<DocumentOperationResult> flushBulkBuffer() {
		try {
			Collection<?> buffer = bulkBufferManager.getCurrentBuffer();
			if (buffer != null && !buffer.isEmpty()) {
				LOG.debug("flushing bulk buffer");
				return executeBulk(buffer);
			} else {
				LOG.debug("bulk buffer was empty");
				return Collections.emptyList();
			}	
		} finally {
			clearBulkBuffer();
		}
		
	}
	
	public void setJsonSerializer(JsonSerializer js) {
		Assert.notNull(js, "JsonSerializer may not be null");
		this.jsonSerializer = js;
	}

	private List<DocumentOperationResult> executeBulk(Collection<?> objects,
			boolean allOrNothing) {
		BulkOperation op = jsonSerializer.createBulkOperation(objects,
				allOrNothing);
		List<DocumentOperationResult> result = restTemplate.post(
				dbURI.append("_bulk_docs").toString(), op.getData(),
				new BulkOperationResponseHandler(objects, objectMapper));
		op.awaitCompletion();
		return result;
	}

	public int getRevisionLimit() {
		return restTemplate.get(dbURI.append("_revs_limit").toString(),
				new StdResponseHandler<Integer>() {
					@Override
					public Integer success(HttpResponse hr) throws Exception {
						JsonNode rlimit = objectMapper.readTree(hr.getContent());
						return rlimit.getValueAsInt();
					}
				});
	}

	public void setRevisionLimit(int limit) {
		restTemplate.put(dbURI.append("_revs_limit").toString(),
				Integer.toString(limit), VOID_RESPONSE_HANDLER);
	}

	private InputStream changesAsStream(ChangesCommand cmd) {
		HttpResponse r = restTemplate.get(dbURI.append(cmd.toString())
				.toString());
		return r.getContent();
	}

	public List<DocumentChange> changes(ChangesCommand cmd) {
		if (cmd.continuous) {
			throw new IllegalArgumentException(
					"ChangesCommand may not declare continous = true while calling changes");
		}

		ChangesCommand actualCmd = new ChangesCommand.Builder().merge(cmd)
				.continuous(false).build();

		List<DocumentChange> changes = new ArrayList<DocumentChange>();
		try {
			JsonNode node = objectMapper.readTree(changesAsStream(actualCmd));
			JsonNode results = node.findPath("results");

			for (JsonNode change : results) {
				changes.add(new StdDocumentChange(change));
			}
		} catch (IOException e) {
			throw Exceptions.propagate(e);
		}
		return changes;
	}

	public ChangesFeed changesFeed(ChangesCommand cmd) {
		int heartbeat = cmd.heartbeat > 0 ? cmd.heartbeat
				: DEFAULT_HEARTBEAT_INTERVAL;
		
		String since = cmd.since != null ? cmd.since : getDbInfo().getUpdateSeqAsString();

		ChangesCommand actualCmd = new ChangesCommand.Builder().merge(cmd)
				.continuous(true).heartbeat(heartbeat).since(since).build();

		return new ContinuousChangesFeed(dbName, restTemplate.getUncached(dbURI.append(actualCmd.toString()).toString()));
	}
	
	@Override
	public String callUpdateHandler(String designDocID, String function,
			String docID) {
		return callUpdateHandler(designDocID, function, docID, null);
	}
	
	public String callUpdateHandler(String designDocID, String function, String docID, Map<String, String> params) {
		Assert.hasText(designDocID, "designDocID may not be null or empty");
		Assert.hasText(function, "functionName may not be null or empty");
		Assert.hasText(docID, "docId may not be null or empty");
 		URI uri = dbURI.append(designDocID).append("_update").append(function)
 				.append(docID);
 		if(params != null && !params.isEmpty()) {
 			for (Map.Entry<String, String> p : params.entrySet()) {
 	 			uri.param(p.getKey(), p.getValue());
 	 		}	
 		}
 		
 		return restTemplate.put(uri.toString(), "", new StdResponseHandler<String>() {
 
 			public String success(HttpResponse hr)
 					throws JsonProcessingException, IOException {
 				return IOUtils.toString(hr.getContent(), "UTF-8");
 			}
 
 		});
 
 	}

}
