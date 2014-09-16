package org.ektorp.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.ektorp.*;
import org.ektorp.changes.ChangesCommand;
import org.ektorp.changes.ChangesFeed;
import org.ektorp.changes.DocumentChange;
import org.ektorp.http.*;
import org.ektorp.impl.changes.ContinuousChangesFeed;
import org.ektorp.impl.changes.StdDocumentChange;
import org.ektorp.util.Assert;
import org.ektorp.util.Documents;
import org.ektorp.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 *
 * @author henrik lundgren
 *
 */
public class StdCouchDbConnector implements CouchDbConnector {

    private static final int DEFAULT_HEARTBEAT_INTERVAL = 9000;
    private static final Logger LOG = LoggerFactory
            .getLogger(StdCouchDbConnector.class);
    private static final ResponseCallback<Void> VOID_RESPONSE_HANDLER = new StdResponseHandler<Void>();
    protected final ObjectMapper objectMapper;
    private JsonSerializer jsonSerializer;

    protected final URI dbURI;
    private final String dbName;

    protected final RestTemplate restTemplate;
    protected QueryExecutor queryExecutor;

    private final CouchDbInstance dbInstance;

	protected final RevisionResponseHandler revisionHandler;
    private final DocIdResponseHandler docIdResponseHandler;

    private LocalBulkBuffer localBulkBuffer;

    private BulkExecutor<Collection<?>> collectionBulkExecutor;

    private BulkExecutor<InputStream> inputStreamBulkExecutor;

    private final static Options EMPTY_OPTIONS = new Options();

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
        this.restTemplate = new RestTemplate(dbi.getConnection());
        this.revisionHandler = new RevisionResponseHandler(objectMapper);
        this.docIdResponseHandler = new DocIdResponseHandler(objectMapper);
        this.queryExecutor = new DefaultQueryExecutor(this.restTemplate);

        collectionBulkExecutor = new BulkOperationCollectionBulkExecutor(dbURI, restTemplate, objectMapper) {
            @Override
            protected JsonSerializer getJsonSerializer() {
                return jsonSerializer;
            }
        };
        localBulkBuffer = new DefaultLocalBulkBuffer() {
            @Override
            protected BulkExecutor getBulkExecutor() {
                return collectionBulkExecutor;
            }
        };

        inputStreamBulkExecutor = new InputStreamWrapperBulkExecutor(dbURI, restTemplate, objectMapper);
    }

    public void setLocalBulkBuffer(LocalBulkBuffer localBulkBuffer) {
        this.localBulkBuffer = localBulkBuffer;
    }

    public void setCollectionBulkExecutor(BulkExecutor<Collection<?>> collectionBulkExecutor) {
        this.collectionBulkExecutor = collectionBulkExecutor;
    }

    public void setInputStreamBulkExecutor(BulkExecutor<InputStream> inputStreamBulkExecutor) {
        this.inputStreamBulkExecutor = inputStreamBulkExecutor;
    }

    /**
     * Set an alternative QueryExecutor instance in order to change the executeQuery implementation as you like.
     */
    public void setQueryExecutor(QueryExecutor queryExecutor) {
        this.queryExecutor = queryExecutor;
    }

    @Override
    public String path() {
        return dbURI.toString();
    }

    @Override
    public void create(final Object o) {
        Assert.notNull(o, "Document may not be null");
        Assert.isTrue(Documents.isNew(o), "Object must be new");

        String json = serializeToJson(o);
        String id = Documents.getId(o);
        DocumentOperationResult result;
        if (id != null && id.length() != 0) {
            result = restTemplate.put(URIWithDocId(id), json, revisionHandler);
        } else {
            result = restTemplate.post(dbURI.toString(), json, revisionHandler);
            Documents.setId(o, result.getId());
        }
        Documents.setRevision(o, result.getRevision());
    }

    @Override
    public void create(String id, Object node) {
        assertDocIdHasValue(id);
        Assert.notNull(node, "node may not be null");
        restTemplate.put(URIWithDocId(id), serializeToJson(node));
    }

    protected String URIWithDocId(String id) {
        return dbURI.append(id).toString();
    }

    @Override
    public boolean contains(String id) {
        return restTemplate.head(URIWithDocId(id),
                new ResponseCallback<Boolean>() {

                    @Override
                    public Boolean error(HttpResponse hr) {
                        if (hr.getCode() == HttpStatus.NOT_FOUND) {
                            return Boolean.FALSE;
                        }
                        throw new DbAccessException(hr.toString());
                    }

                    @Override
                    public Boolean success(HttpResponse hr) throws Exception {
                        return Boolean.TRUE;
                    }

                });
    }

    @Override
    public String createAttachment(String docId, AttachmentInputStream data) {
        return createAttachment(docId, null, data);
    }

    @Override
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

    @Override
    public AttachmentInputStream getAttachment(final String id,
            final String attachmentId) {
        assertDocIdHasValue(id);
        Assert.hasText(attachmentId, "attachmentId may not be null or empty");

		LOG.trace("fetching attachment for doc: {} attachmentId: {}", id, attachmentId);
        return getAttachment(attachmentId, dbURI.append(id).append(attachmentId));
    }

    @Override
    public AttachmentInputStream getAttachment(String id, String attachmentId,
    		String revision) {
    	assertDocIdHasValue(id);
        Assert.hasText(attachmentId, "attachmentId may not be null or empty");
        Assert.hasText(revision, "revision may not be null or empty");

		LOG.trace("fetching attachment for doc: {} attachmentId: {}", id, attachmentId);
        return getAttachment(attachmentId, dbURI.append(id).append(attachmentId).param("rev", revision));
    }

    private AttachmentInputStream getAttachment(String attachmentId, URI uri) {
    	HttpResponse r = restTemplate.get(uri.toString());
        return new AttachmentInputStream(attachmentId, r.getContent(),
                r.getContentType(), r.getContentLength());
    }

    @Override
    public String delete(Object o) {
        Assert.notNull(o, "document may not be null");
        return delete(Documents.getId(o), Documents.getRevision(o));
    }

    @Override
    public PurgeResult purge(Map<String, List<String>> revisionsToPurge) {
        return restTemplate.post(dbURI.append("_purge").toString(), serializeToJson(revisionsToPurge),
                new StdResponseHandler<PurgeResult>() {
                    @Override
                    public PurgeResult success(HttpResponse hr) throws Exception {
                        return objectMapper.readValue(hr.getContent(), PurgeResult.class);
                    }
                });
    }

    @Override
    public <T> T get(final Class<T> c, String id) {
        return get(c, id, EMPTY_OPTIONS);
    }

    @Override
    public <T> T get(final Class<T> c, String id, Options options) {
        Assert.notNull(c, "Class may not be null");
        assertDocIdHasValue(id);
        URI uri = dbURI.append(id);
        applyOptions(options, uri);
        return restTemplate.get(uri.toString(),
                new StdResponseHandler<T>() {
                    @Override
                    public T success(HttpResponse hr) throws Exception {
                        return objectMapper.readValue(hr.getContent(), c);
                    }
                });
    }

    @Override
    public <T> T get(final Class<T> c, String id, String rev) {
        Assert.notNull(c, "Class may not be null");
        assertDocIdHasValue(id);
        Assert.hasText(rev, "Revision may not be null or empty");
        return get(c, id, new Options().revision(rev));
    }

    @Override
    public <T> T getWithConflicts(final Class<T> c, String id) {
        Assert.notNull(c, "Class may not be null");
        assertDocIdHasValue(id);
        return get(c, id, new Options().includeConflicts());
    }

    @Override
    public <T> T find(Class<T> c, String id) {
        return find(c, id, EMPTY_OPTIONS);
    }

    @Override
    public <T> T find(final Class<T> c, String id, Options options) {
        Assert.notNull(c, "Class may not be null");
        assertDocIdHasValue(id);
        URI uri = dbURI.append(id);
        applyOptions(options, uri);
        return restTemplate.get(uri.toString(),
                new StdResponseHandler<T>() {
                    @Override
                    public T success(HttpResponse hr) throws Exception {
                        return objectMapper.readValue(hr.getContent(), c);
                    }

                    @Override
                    public T error(HttpResponse hr) {
                        return hr.getCode() == HttpStatus.NOT_FOUND ? null : super.error(hr);
                    }
                });
    }

    private void applyOptions(Options options, URI uri) {
        if (options != null && !options.isEmpty()) {
            uri.params(options.getOptions());
        }
    }

    @Override
    public List<Revision> getRevisions(String id) {
        assertDocIdHasValue(id);
        return restTemplate.get(dbURI.append(id).param("revs_info", "true")
                .toString(), new StdResponseHandler<List<Revision>>() {

            @Override
            public List<Revision> success(HttpResponse hr) throws Exception {
                JsonNode root = objectMapper.readValue(hr.getContent(),
                        JsonNode.class);
                List<Revision> revs = new ArrayList<Revision>();
                for (Iterator<JsonNode> i = root.get("_revs_info")
                        .elements(); i.hasNext();) {
                    JsonNode rev = i.next();
                    revs.add(new Revision(rev.get("rev").textValue(), rev
                            .get("status").textValue()));
                }
                return revs;
            }

            @Override
            public List<Revision> error(HttpResponse hr) {
                if (hr.getCode() == HttpStatus.NOT_FOUND) {
                    return Collections.emptyList();
                }
                return super.error(hr);
            }
        });
    }

    @Override
    public String getCurrentRevision(String id) {
    	assertDocIdHasValue(id);
    	return restTemplate.head(dbURI.append(id).toString(), new StdResponseHandler<String>(){
    		@Override
    		public String success(HttpResponse hr) throws Exception {
    			return hr.getETag();
    		}
    	});
    }
    
    
    @Override
    public InputStream getAsStream(String id) {
        assertDocIdHasValue(id);
        return getAsStream(id, EMPTY_OPTIONS);
    }

    @Override
    public InputStream getAsStream(String id, Options options) {
        URI uri = dbURI.append(id);
        applyOptions(options, uri);
        HttpResponse r = restTemplate.get(uri.toString());
        return r.getContent();
    }

    @Override
    public InputStream getAsStream(String id, String rev) {
        assertDocIdHasValue(id);
        Assert.hasText(rev, "Revision may not be null or empty");
        return getAsStream(id, new Options().revision(rev));
    }

    @Override
    public void update(final Object o) {
        Assert.notNull(o, "Document cannot be null");
        final String id = Documents.getId(o);
        assertDocIdHasValue(id);
        restTemplate.put(dbURI.append(id).toString(), serializeToJson(o),
                new StdResponseHandler<Void>() {

                    @Override
                    public Void success(HttpResponse hr) throws Exception {
                        JsonNode n = objectMapper.readValue(hr.getContent(),
                                JsonNode.class);
                        Documents.setRevision(o, n.get("rev").textValue());
                        return null;
                    }

                    @Override
                    public Void error(HttpResponse hr) {
                        if (hr.getCode() == HttpStatus.CONFLICT) {
                            throw new UpdateConflictException(id, Documents
                                    .getRevision(o));
                        }
                        return super.error(hr);
                    }
                });
    }

    @Override
    public String delete(String id, String revision) {
        assertDocIdHasValue(id);
        return restTemplate.delete(
                dbURI.append(id).param("rev", revision).toString(),
                revisionHandler).getRevision();
    }
    
    @Override
    public String copy(String sourceDocId, String targetDocId) {
    	return copy(sourceDocId, targetDocId, null);
    }
    
    @Override
    public String copy(String sourceDocId, String targetDocId,
    		String targetRevision) {
    	String destinationUri = targetRevision != null ? targetDocId + "?rev=" + targetRevision : targetDocId;
    	return restTemplate.copy(dbURI.append(sourceDocId).toString(), destinationUri, revisionHandler).getRevision();
    }

    @Override
    public List<String> getAllDocIds() {
        return restTemplate.get(dbURI.append("_all_docs").toString(),
                docIdResponseHandler);
    }

    @Override
    public void createDatabaseIfNotExists() {
		dbInstance.createDatabaseIfNotExists(dbName);
    }

    @Override
    public String getDatabaseName() {
        return dbName;
    }

    @Override
    public <T> List<T> queryView(final ViewQuery query, final Class<T> type) {
        Assert.notNull(query, "query may not be null");
        query.dbPath(dbURI.toString());

        EmbeddedDocViewResponseHandler<T> rh = new EmbeddedDocViewResponseHandler<T>(
                type, objectMapper, query.isIgnoreNotFound());

        return executeQuery(query, rh);
    }

	protected <T> T executeQuery(final ViewQuery query, ResponseCallback<T> rh) {
		return queryExecutor.executeQuery(query, rh);
	}

    @Override
    public <T> Page<T> queryForPage(ViewQuery query, PageRequest pr, Class<T> type) {
        Assert.notNull(query, "query may not be null");
        Assert.notNull(pr, "PageRequest may not be null");
        Assert.notNull(type, "type may not be null");

        query.dbPath(dbURI.toString());
		LOG.debug("startKey: {}", pr.getStartKey());
		LOG.debug("startDocId: {}", pr.getStartKeyDocId());
        PageResponseHandler<T> ph = new PageResponseHandler<T>(pr, type, objectMapper, query.isIgnoreNotFound());
        query = PageRequest.applyPagingParameters(query, pr);

        return executeQuery(query, ph);
    }

    @Override
    public ViewResult queryView(final ViewQuery query) {
        Assert.notNull(query, "query cannot be null");
        query.dbPath(dbURI.toString());
        ResponseCallback<ViewResult> rh = new StdResponseHandler<ViewResult>() {

            @Override
            public ViewResult success(HttpResponse hr) throws Exception {
                return new ViewResult(objectMapper.readTree(hr.getContent()), query.isIgnoreNotFound());
            }

        };

        return executeQuery(query, rh);
    }

    @Override
    public StreamingViewResult queryForStreamingView(ViewQuery query) {
        return new StreamingViewResult(objectMapper, queryForHttpResponse(query), query.isIgnoreNotFound());
    }

    @Override
    public InputStream queryForStream(ViewQuery query) {
        return queryForHttpResponse(query).getContent();
    }

    private HttpResponse queryForHttpResponse(ViewQuery query) {
        Assert.notNull(query, "query cannot be null");
        query.dbPath(dbURI.toString());
        return query.hasMultipleKeys() ? restTemplate.postUncached(query.buildQuery(),
                query.getKeysAsJson()) : restTemplate.getUncached(
                query.buildQuery());
    }

    @Override
    public String deleteAttachment(String docId, String revision,
            String attachmentId) {
        return restTemplate.delete(
                dbURI.append(docId).append(attachmentId).param("rev", revision)
                        .toString(), revisionHandler).getRevision();
    }

    protected void assertDocIdHasValue(String docId) {
        Assert.hasText(docId, "document id cannot be empty");
    }

    @Override
    public HttpClient getConnection() {
        return dbInstance.getConnection();
    }

    @Override
    public DbInfo getDbInfo() {
        return restTemplate.get(dbURI.toString(),
                new StdResponseHandler<DbInfo>() {

                    @Override
                    public DbInfo success(HttpResponse hr) throws Exception {
                        return objectMapper.readValue(hr.getContent(),
                                DbInfo.class);
                    }
                }
        );
    }

    @Override
    public Security getSecurity() {
        return restTemplate.get(securityPath(),
                new StdResponseHandler<Security>() {
                    @Override
                    public Security success(HttpResponse hr) throws Exception {
                        return objectMapper.readValue(hr.getContent(),
                                Security.class);
                    }
                }
        );
    }

    @Override
    public Status updateSecurity(Security security) {
        try {
            return restTemplate.put(securityPath(),
                    objectMapper.writeValueAsString(security),
                    new StdResponseHandler<Status>() {
                        @Override
                        public Status success(HttpResponse hr) throws Exception {
                            return objectMapper.readValue(hr.getContent(),
                                    Status.class);
                        }
                    }
            );
        } catch(JsonProcessingException e) {
            throw new IllegalStateException("Failed to update security: " + e.getMessage());
        }
    }

    @Override
    public DesignDocInfo getDesignDocInfo(String designDocId) {
        Assert.hasText(designDocId, "designDocId may not be null or empty");
        String uri = dbURI.append("_design").append(designDocId)
                .append("_info").toString();

        return restTemplate.get(uri, new StdResponseHandler<DesignDocInfo>() {

            @Override
            public DesignDocInfo success(HttpResponse hr) throws Exception {
                return objectMapper.readValue(hr.getContent(),
                        DesignDocInfo.class);
            }
        });
    }

    @Override
    public void compact() {
        restTemplate.post(dbURI.append("_compact").toString(), "not_used",
                VOID_RESPONSE_HANDLER);
    }

    @Override
    public void cleanupViews() {
        restTemplate.post(dbURI.append("_view_cleanup").toString(), "not_used",
                VOID_RESPONSE_HANDLER);
    }

    @Override
    public ReplicationStatus replicateFrom(String source) {
        ReplicationCommand cmd = new ReplicationCommand.Builder()
                .target(dbName).source(source).build();

        return dbInstance.replicate(cmd);
    }

    @Override
    public ReplicationStatus replicateFrom(String source,
            Collection<String> docIds) {
        ReplicationCommand cmd = new ReplicationCommand.Builder()
                .target(dbName).source(source).docIds(docIds).build();

        return dbInstance.replicate(cmd);
    }

    @Override
    public ReplicationStatus replicateTo(String target) {
        ReplicationCommand cmd = new ReplicationCommand.Builder()
                .target(target).source(dbName).build();

        return dbInstance.replicate(cmd);
    }

    @Override
    public ReplicationStatus replicateTo(String target,
            Collection<String> docIds) {
        ReplicationCommand cmd = new ReplicationCommand.Builder()
                .target(target).source(dbName).docIds(docIds).build();

        return dbInstance.replicate(cmd);
    }

    @Override
    public void compactViews(String designDocumentId) {
        Assert.hasText(designDocumentId,
                "designDocumentId may not be null or empty");
        restTemplate.post(dbURI.append("_compact").append(designDocumentId)
                .toString(), "not_used", VOID_RESPONSE_HANDLER);
    }

    @Override
    public List<DocumentOperationResult> executeAllOrNothing(InputStream inputStream) {
        return executeBulk(inputStream, true);
    }

    @Override
    public List<DocumentOperationResult> executeBulk(InputStream inputStream) {
        return executeBulk(inputStream, false);
    }

    public List<DocumentOperationResult> executeBulk(InputStream inputStream, boolean allOrNothing) {
        return inputStreamBulkExecutor.executeBulk(inputStream, allOrNothing);
    }

    @Override
    public List<DocumentOperationResult> executeAllOrNothing(Collection<?> objects) {
        return executeBulk(objects, true);
    }

    @Override
    public List<DocumentOperationResult> executeBulk(Collection<?> objects) {
        return executeBulk(objects, false);
    }

    @Override
    public void addToBulkBuffer(Object o) {
        this.localBulkBuffer.addToBulkBuffer(o);
    }

    @Override
    public void clearBulkBuffer() {
        this.localBulkBuffer.clearBulkBuffer();
    }

    @Override
    public List<DocumentOperationResult> flushBulkBuffer() {
        return this.localBulkBuffer.flushBulkBuffer();
    }

    /**
     * @deprecated override method serializeToJson in order to change Serialization to JSON.
     */
    @Deprecated
    public void setJsonSerializer(JsonSerializer js) {
        Assert.notNull(js, "JsonSerializer may not be null");
        this.jsonSerializer = js;
    }

    protected String serializeToJson(Object o) {
        return jsonSerializer.toJson(o);
    }

    public List<DocumentOperationResult> executeBulk(Collection<?> objects,
			boolean allOrNothing) {
		return collectionBulkExecutor.executeBulk(objects, allOrNothing);
	}

    @Override
    public int getRevisionLimit() {
        return restTemplate.get(dbURI.append("_revs_limit").toString(),
                new StdResponseHandler<Integer>() {
                    @Override
                    public Integer success(HttpResponse hr) throws Exception {
                        JsonNode rlimit = objectMapper.readTree(hr.getContent());
                        return rlimit.asInt();
                    }
                });
    }

    @Override
    public void setRevisionLimit(int limit) {
        restTemplate.put(dbURI.append("_revs_limit").toString(),
                Integer.toString(limit), VOID_RESPONSE_HANDLER);
    }

    private InputStream fetchChangesAsStream(ChangesCommand cmd) {
        HttpResponse r = restTemplate.get(dbURI.append(cmd.toString())
                .toString());
        return r.getContent();
    }

    @Override
    public List<DocumentChange> changes(ChangesCommand cmd) {
        if (cmd.continuous) {
            throw new IllegalArgumentException(
                    "ChangesCommand may not declare continous = true while calling changes");
        }

        ChangesCommand actualCmd = new ChangesCommand.Builder().merge(cmd)
                .continuous(false).build();

        List<DocumentChange> changes = new ArrayList<DocumentChange>();
        try {
            JsonNode node = objectMapper.readTree(fetchChangesAsStream(actualCmd));
            JsonNode results = node.findPath("results");

            for (JsonNode change : results) {
                changes.add(new StdDocumentChange(change));
            }
        } catch (IOException e) {
            throw Exceptions.propagate(e);
        }
        return changes;
    }

    @Override
    public StreamingChangesResult changesAsStream(ChangesCommand cmd) {
        if (cmd.continuous) {
            throw new IllegalArgumentException(
                    "ChangesCommand may not declare continous = true while calling changes");
        }

        ChangesCommand actualCmd = new ChangesCommand.Builder().merge(cmd)
                .continuous(false).build();

        HttpResponse response = restTemplate.get(dbURI.append(actualCmd.toString())
                .toString());

        return new StreamingChangesResult(objectMapper, response);
    }

    @Override
    public ChangesFeed changesFeed(ChangesCommand cmd) {
        int heartbeat = cmd.heartbeat > 0 ? cmd.heartbeat
                : DEFAULT_HEARTBEAT_INTERVAL;

        String since = cmd.since != null ? cmd.since : getDbInfo().getUpdateSeqAsString();

        ChangesCommand actualCmd = new ChangesCommand.Builder().merge(cmd)
                .continuous(true).heartbeat(heartbeat).since(since).build();

        return new ContinuousChangesFeed(dbName,
                restTemplate.getUncached(dbURI.append(actualCmd.toString()).toString()));
    }

    @Override
    public String callUpdateHandler(String designDocID, String function,
            String docID) {
        return callUpdateHandler(designDocID, function, docID, null);
    }

    @Override
    public String callUpdateHandler(String designDocID, String function, String docID, Map<String, String> params) {
        Assert.hasText(designDocID, "designDocID may not be null or empty");
        Assert.hasText(function, "functionName may not be null or empty");
        Assert.hasText(docID, "docId may not be null or empty");

        UpdateHandlerRequest req = new UpdateHandlerRequest();
        req.dbPath(dbURI.toString())
                .designDocId(designDocID)
                .functionName(function)
                .docId(docID)
                .params(params)
                .buildRequestUri();

        return callUpdateHandler(req);
    }

    private String serializeUpdateHandlerRequestBody(Object o) {
        if (o == null) {
            return "";
        } else if (o instanceof String) {
            return (String) o;
        } else {
            try {
                return objectMapper.writeValueAsString(o);
            } catch (Exception e) {
                throw Exceptions.propagate(e);
            }
        }
    }

    @Override
    public String callUpdateHandler(final UpdateHandlerRequest req) {
        Assert.hasText(req.getDesignDocId(), "designDocID may not be null or empty");
        Assert.hasText(req.getFunctionName(), "functionName may not be null or empty");
        Assert.hasText(req.getDocId(), "docId may not be null or empty");

        req.dbPath(dbURI.toString());

        return restTemplate.put(req.buildRequestUri(),
                serializeUpdateHandlerRequestBody(req.getBody()),
                new StdResponseHandler<String>() {

                    @Override
                    public String success(HttpResponse hr)
                            throws JsonProcessingException, IOException {
                        return IOUtils.toString(hr.getContent(), "UTF-8");
                    }

                });
    }

    @Override
    public <T> T callUpdateHandler(final UpdateHandlerRequest req, final Class<T> c) {
        Assert.hasText(req.getDesignDocId(), "designDocID may not be null or empty");
        Assert.hasText(req.getFunctionName(), "functionName may not be null or empty");
        Assert.hasText(req.getDocId(), "docId may not be null or empty");

        req.dbPath(dbURI.toString());

        return restTemplate.put(req.buildRequestUri(),
                serializeUpdateHandlerRequestBody(req.getBody()),
                new StdResponseHandler<T>() {

                    @Override
                    public T success(HttpResponse hr) throws Exception {
                        return objectMapper.readValue(hr.getContent(), c);
                    }

                    @Override
                    public T error(HttpResponse hr) {
                        if (hr.getCode() == HttpStatus.CONFLICT) {
                            throw new UpdateConflictException(req.getDocId(), "<Update Handler>");
                        }
                        return null;
                    }
                });
    }

    @Override
    public void ensureFullCommit() {
        restTemplate.post(dbURI.append("_ensure_full_commit").toString(), "", new StdResponseHandler<Void>());
    }

	@Override
	public void updateMultipart(String id, InputStream stream, String boundary, long length, Options options) {
		assertDocIdHasValue(id);
		Assert.hasText(boundary);
		URI uri = dbURI.append(id);
		applyOptions(options, uri);

		String contentType = String.format("multipart/related; boundary=\"%s\"", boundary);

		restTemplate.put(uri.toString(), stream, contentType, length);
	}

	@Override
	public void update(String id, InputStream document, long length, Options options)
	{
		assertDocIdHasValue(id);
		URI uri = dbURI.append(id);
		applyOptions(options, uri);

		restTemplate.put(uri.toString(), document, "application/json", length);
	}

    private String securityPath() {
        return String.format("%s_security", dbURI);
    }
}

