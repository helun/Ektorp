package org.ektorp.impl;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.ektorp.*;
import org.ektorp.http.HttpResponse;
import org.ektorp.http.HttpStatus;
import org.ektorp.http.StdHttpClient;
import org.ektorp.support.CouchDbDocument;
import org.ektorp.util.JSONComparator;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.stubbing.answers.ThrowsException;
import org.mockito.internal.verification.VerificationModeFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

import static java.lang.String.format;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class StdCouchDbConnectorTest {

    protected static final String OK_RESPONSE = "{\"ok\":true}";
    protected static final String OK_RESPONSE_WITH_ID_AND_REV = "{\"ok\":true,\"id\":\"some_id\",\"rev\":\"123D123\"}";
    protected static final String TEST_DB_PATH = "/test_db/";
    CouchDbConnector dbCon;
    StdHttpClient httpClient;
    TestDoc td = new TestDoc();

    @Before
    public void setUp() throws Exception {
        httpClient = mock(StdHttpClient.class, new ThrowsException(new UnsupportedOperationException()));
        StdCouchDbInstance dbInstance = new StdCouchDbInstance(httpClient, new StdObjectMapperFactory() {
            @Override
            public ObjectMapper createObjectMapper(CouchDbConnector connector) {
                ObjectMapper mapper = super.createObjectMapper(connector);
                mapper.registerModule(new JodaModule());
                return mapper;
            }
        });
        dbCon = dbInstance.createConnector("test_db/", false);

        td.name = "nisse";
        td.age = 12;
    }

    @Test
    public void testCreate() throws IOException {
        td.setId("some_id");
        setupNegativeContains(td.getId());
        doReturn(HttpResponseStub.valueOf(201, OK_RESPONSE_WITH_ID_AND_REV)).when(httpClient).put(anyString(), anyString());
        dbCon.create(td);
        ArgumentCaptor<String> ac = ArgumentCaptor.forClass(String.class);
        verify(httpClient).put(eq("/test_db/some_id"), ac.capture());
        assertEquals("some_id", td.getId());
        assertEquals("123D123", td.getRevision());
        assertEqualJson("create.json", Charset.forName("UTF-8"), ac.getValue());
    }

    @Test
    public void docId_should_be_escaped_when_id_contains_slash() throws UnsupportedEncodingException {
        String escapedId = "http%3A%2F%2Fsome%2Fopenid%3Fgoog";
        td.setId("http://some/openid?goog");
        setupNegativeContains(escapedId);
        doReturn(HttpResponseStub.valueOf(201, OK_RESPONSE_WITH_ID_AND_REV)).when(httpClient).put(anyString(), anyString());
        dbCon.create(td);
        ArgumentCaptor<String> ac = ArgumentCaptor.forClass(String.class);
        verify(httpClient).put(eq("/test_db/" + escapedId), ac.capture());
        assertEquals("http://some/openid?goog", td.getId());
    }

    @Test
    public void testCreateFromJsonNode() throws Exception {

        doReturn(HttpResponseStub.valueOf(201, OK_RESPONSE_WITH_ID_AND_REV)).when(httpClient).put(anyString(), anyString());

        JsonNode root;
        {
            InputStream resourceAsStream = null;
            try {
                resourceAsStream = StdCouchDbConnectorTest.class.getResourceAsStream("create_from_json_node.json");
                root = new ObjectMapper().readValue(resourceAsStream, JsonNode.class);
            } finally {
                IOUtils.closeQuietly(resourceAsStream);
            }
        }
        dbCon.create("some_id", root);

        String facit;
        {
            InputStream resourceAsStream = null;
            try {
                resourceAsStream = StdCouchDbConnectorTest.class.getResourceAsStream("create_from_json_node.json");
                facit = IOUtils.toString(resourceAsStream, "UTF-8").trim();
            } finally {
                IOUtils.closeQuietly(resourceAsStream);
            }
        }
        verify(httpClient).put("/test_db/some_id", facit);
    }

    @Test
    public void save_document_with_utf8_charset() throws IOException {
        td.setId("some_id");
        td.name = "Örjan Åäö";
        setupNegativeContains(td.getId());
        doReturn(HttpResponseStub.valueOf(201, OK_RESPONSE_WITH_ID_AND_REV)).when(httpClient).put(anyString(), anyString());
        dbCon.create(td);
        ArgumentCaptor<String> ac = ArgumentCaptor.forClass(String.class);
        verify(httpClient).put(eq("/test_db/some_id"), ac.capture());
        assertEqualJson("charset.json", Charset.forName("UTF-8"), ac.getValue());
    }

    @Test
    public void save_document_with_ISO_8859_1_charset() throws IOException {
        td.setId("some_id");
        td.name = "Örjan Åäö";
        setupNegativeContains(td.getId());
        doReturn(HttpResponseStub.valueOf(201, OK_RESPONSE_WITH_ID_AND_REV)).when(httpClient).put(anyString(), anyString());
        dbCon.create(td);
        ArgumentCaptor<String> ac = ArgumentCaptor.forClass(String.class);
        verify(httpClient).put(eq("/test_db/some_id"), ac.capture());
        assertEqualJson("charset-ISO-8859-1.json", Charset.forName("ISO-8859-1"), ac.getValue());
    }

    @Test
    public void create_should_post_if_id_is_missing() throws IOException {
        doReturn(HttpResponseStub.valueOf(201, OK_RESPONSE_WITH_ID_AND_REV)).when(httpClient).post(anyString(), anyString());
        dbCon.create(td);
        ArgumentCaptor<String> ac = ArgumentCaptor.forClass(String.class);
        verify(httpClient).post(eq(TEST_DB_PATH), ac.capture());
        assertEquals("some_id", td.getId());
        assertEquals("123D123", td.getRevision());
        assertEqualJson("create_with_no_id.json", Charset.forName("UTF-8"), ac.getValue());
    }

    @Test
    public void testDelete() {
        doReturn(HttpResponseStub.valueOf(200, "{\"ok\":true,\"rev\":\"123D123\"}")).when(httpClient).delete(anyString());
        dbCon.delete("some_id", "123D123");
        verify(httpClient).delete("/test_db/some_id?rev=123D123");
    }

    @Test
    public void testDeleteCouchDbDocument() {
        td.setId("some_id");
        td.setRevision("123D123");
        setupGetDocResponse();
        doReturn(HttpResponseStub.valueOf(200, "{\"ok\":true,\"rev\":\"123D123\"}")).when(httpClient).delete(anyString());
        dbCon.delete(td);
        verify(httpClient).delete("/test_db/some_id?rev=123D123");
    }

    @Test
    public void testGet() {
        setupGetDocResponse();
        TestDoc getted = dbCon.get(TestDoc.class, "some_id");
        verify(httpClient).get(eq("/test_db/some_id"));

        assertNotNull(getted);
        assertEquals("some_id", getted.getId());
        assertEquals("123D123", getted.getRevision());
        assertEquals("nisse", getted.name);
        assertEquals(12, getted.age);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testGetWithConflicts() {
        setupGetDocResponse();
        dbCon.getWithConflicts(TestDoc.class, "some_id");
        verify(httpClient).get(eq("/test_db/some_id?conflicts=true"));
    }

    private void setupGetDocResponse() {
        doReturn(HttpResponseStub.valueOf(200,
                "{\"name\":\"nisse\",\"age\":12,\"_id\":\"some_id\",\"_rev\":\"123D123\"}")).when(httpClient).get(anyString());
    }

    private void setupGetDocResponse(String... ids) {
        for (String id : ids) {
            doReturn(HttpResponseStub.valueOf(200, "{\"name\":\"nisse\",\"age\":12,\"_id\":\" " + id
                    + "\",\"_rev\":\"123D123\"}")).when(httpClient).get(TEST_DB_PATH + id);
        }
    }

    @Test(expected = DocumentNotFoundException.class)
    public void throw_exception_when_doc_is_missing() {
        doReturn(HttpResponseStub.valueOf(404, "")).when(httpClient).get(anyString());
        dbCon.get(TestDoc.class, "some_id");
    }

    @Test
    public void update() throws IOException {
        td.setId("some_id");
        td.setRevision("123D123");
        doReturn(HttpResponseStub.valueOf(201, OK_RESPONSE_WITH_ID_AND_REV)).when(httpClient).put(anyString(), anyString());
        dbCon.update(td);
        ArgumentCaptor<String> ac = ArgumentCaptor.forClass(String.class);
        verify(httpClient).put(eq("/test_db/some_id"), ac.capture());
        assertEquals("some_id", td.getId());
        assertEquals("123D123", td.getRevision());
        assertEqualJson("update.json", Charset.forName("UTF-8"), ac.getValue());
    }

    @Test(expected = UpdateConflictException.class)
    public void throw_exception_when_in_conflict() {
        td.setId("some_id");
        td.setRevision("123D123");
        doReturn(ResponseOnFileStub.newInstance(409, "update_conflict.json")).when(httpClient).put(anyString(), anyString());
        dbCon.update(td);
    }

    @Test(expected = UpdateConflictException.class)
    public void throw_exception_when_create_attachment_in_conflict() {
        td.setId("some_id");
        td.setRevision("123D123");
        doReturn(ResponseOnFileStub.newInstance(409, "update_conflict.json")).when(httpClient).put(anyString(), any(InputStream.class), anyString(), anyInt());
        dbCon.createAttachment("id", "rev", new AttachmentInputStream("attach_id", IOUtils.toInputStream("data"),
                "whatever"));
    }

    @Test
    public void should_create_db_if_missing() {
        doReturn(HttpResponseStub.valueOf(404, "{\"error\":\"not_found\",\"reason\":\"no_db_file\"}")).when(httpClient).head("/test_db/");
		doReturn(HttpResponseStub.valueOf(HttpStatus.CREATED, OK_RESPONSE)).when(httpClient).put("/test_db/");
		dbCon.createDatabaseIfNotExists();
		verify(httpClient).put("/test_db/");
    }

    @Test
    public void should_not_create_db_if_already_exists() {
        doReturn(HttpResponseStub
                .valueOf(
                        200,
                        "{\"test_db\":\"global\",\"doc_count\":1,\"doc_del_count\":0,\"update_seq\":3,\"purge_seq\":0,\"compact_running\":false,\"disk_size\":100,\"instance_start_time\":\"130\",\"disk_format_version\":5,\"committed_update_seq\":3}"))
                .when(httpClient).head("/test_db/");

        dbCon.createDatabaseIfNotExists();
        verify(httpClient, VerificationModeFactory.times(0)).put(anyString());
    }

    @Test
    public void return_all_docIds_in_db() {
        doReturn(HttpResponseStub.valueOf(200,
                "{\"total_rows\": 3, \"offset\": 0, \"rows\": [" +
                        "{\"id\": \"doc1\", \"key\": \"doc1\", \"value\": {\"rev\": \"4324BB\"}}," +
                        "{\"id\": \"doc2\", \"key\": \"doc2\", \"value\": {\"rev\":\"2441HF\"}}," +
                        "{\"id\": \"doc3\", \"key\": \"doc3\", \"value\": {\"rev\":\"74EC24\"}}]}"))
                .when(httpClient).get("/test_db/_all_docs");
        List<String> all = dbCon.getAllDocIds();
        assertEquals(3, all.size());
        assertEquals("doc1", all.get(0));
        assertEquals("doc2", all.get(1));
        assertEquals("doc3", all.get(2));

        doReturn(HttpResponseStub.valueOf(200,"{\"offset\":0,\"total_rows\":2, \"rows\":["+
            "{\"id\":\"07b5e058-1125-4cc9-b908-04af5e0f0a45\",\"value\":{\"_conflicts\":[],\"rev\":\"1-e18520bb-c91e-46f6-bbf8-ed13ab907b9e\"},\"key\":\"07b5e058-1125-4cc9-b908-04af5e0f0a45\"},"+
            "{\"id\":\"52977cfe-c809-4276-b1a4-bc1f3f6303e4\",\"value\":{\"_conflicts\":[],\"rev\":\"1-6af4da06-2554-4a74-8677-05d8b308d922\"},\"key\":\"52977cfe-c809-4276-b1a4-bc1f3f6303e4\"}]}"))
                .when(httpClient).get("/test_db/_all_docs");
        all = dbCon.getAllDocIds();
        assertEquals(2, all.size());
        assertEquals("07b5e058-1125-4cc9-b908-04af5e0f0a45", all.get(0));
        assertEquals("52977cfe-c809-4276-b1a4-bc1f3f6303e4", all.get(1));
    }

    @Test
    public void return_all_revisions() {
        doReturn(ResponseOnFileStub.newInstance(200, "revisions.json")).when(httpClient).get("/test_db/some_doc_id?revs_info=true");
        List<Revision> l = dbCon.getRevisions("some_doc_id");
        assertNotNull(l);
        assertEquals(8, l.size());
        assertEquals(new Revision("8-8395fd3a7a2dd04022cc1330a4d20e66", "available"), l.get(0));
    }

    @Test
    public void return_current_revision() {
        final String revision = UUID.randomUUID().toString();
        doReturn(
                new HttpResponseStub(200, "") {
                    @Override
                    public String getETag() {
                        return revision;
                    }
                }).when(httpClient).head("/test_db/some_doc_id");
        String currentRevision = dbCon.getCurrentRevision("some_doc_id");
        assertEquals(revision, currentRevision);
    }

    @Test
    public void return_null_revisions_when_doc_is_missing() {
        doReturn(HttpResponseStub.valueOf(404, "")).when(httpClient).get("/test_db/some_doc_id?revs_info=true");
        List<Revision> l = dbCon.getRevisions("some_doc_id");
        assertNotNull(l);
        assertTrue(l.isEmpty());
    }

    @Test
    public void return_attachment_with_open_data_stream() throws Exception {
        ResponseOnFileStub rsp = ResponseOnFileStub.newInstance(200, "attachment.txt", "text", 12);
        doReturn(rsp).when(httpClient).get("/test_db/some_doc_id/some_attachment");
        AttachmentInputStream a = dbCon.getAttachment("some_doc_id", "some_attachment");
        assertNotNull(a);
        assertFalse(rsp.isConnectionReleased());
        assertEquals("detta är ett påhäng med ett ö i", IOUtils.toString(a, "UTF-8"));
        assertEquals(rsp.getContentType(), a.getContentType());
        assertEquals(rsp.getContentLength(), a.getContentLength());
    }

    @Test
    public void read_document_as_stream() throws Exception {
        // content type is really json but it doesn't matter here.
        ResponseOnFileStub rsp = ResponseOnFileStub.newInstance(200, "attachment.txt");
        doReturn(rsp).when(httpClient).get("/test_db/some_doc_id");
        InputStream data = dbCon.getAsStream("some_doc_id");
        assertNotNull(data);
        assertFalse(rsp.isConnectionReleased());
        assertEquals("detta är ett påhäng med ett ö i", IOUtils.toString(data, "UTF-8"));
    }

    @Test
    public void should_stream_attachmed_content() {
        doReturn(ResponseOnFileStub.newInstance(200, "create_attachment_rsp.json")).when(httpClient).put(anyString(), any(InputStream.class), anyString(), anyInt());

        dbCon.createAttachment("docid", new AttachmentInputStream("attachment_id", IOUtils.toInputStream("content"),
                "text/html", 12l));

        verify(httpClient).put(eq("/test_db/docid/attachment_id"), any(InputStream.class), eq("text/html"), eq(12l));
    }

    @Test
    public void load_query_result() {
        setupGetDocResponse("doc_id1", "doc_id2");

        ViewQuery query = new ViewQuery()
                .dbPath(TEST_DB_PATH)
                .designDocId("_design/testdoc")
                .viewName("test_view")
                .key("key_value");

        doReturn(ResponseOnFileStub.newInstance(200, "view_result.json")).when(httpClient).getUncached(query.buildQuery());

        ViewResult result = dbCon.queryView(query);

        assertEquals(2, result.getSize());
        assertEquals("doc_id1", result.getRows().get(0).getId());
        assertEquals("doc_id2", result.getRows().get(1).getId());
    }

    @Test
    public void documents_embedded_in_view_result_should_be_read_directly() {
        ViewQuery query = new ViewQuery()
                .dbPath(TEST_DB_PATH)
                .designDocId("_design/testdoc")
                .viewName("test_view")
                .key("key_value");

        doReturn(ResponseOnFileStub.newInstance(200, "view_result_with_embedded_docs.json")).when(httpClient).getUncached(query.buildQuery());

        List<TestDoc> result = dbCon.queryView(query, TestDoc.class);

        assertEquals(2, result.size());
        assertEquals(TestDoc.class, result.get(0).getClass());
        assertEquals("doc_id1", result.get(0).getId());
        assertEquals("doc_id2", result.get(1).getId());
        verify(httpClient, times(1)).getUncached(anyString());
    }

    @Test
    public void queries_with_include_doc_should_read_docs_directly() {
        ViewQuery query = new ViewQuery()
                .dbPath(TEST_DB_PATH)
                .designDocId("_design/testdoc")
                .viewName("test_view")
                .includeDocs(true)
                .key("key_value")
                .cacheOk(true);

        doReturn(ResponseOnFileStub.newInstance(200, "view_result_with_included_docs.json")).when(httpClient).get(query.buildQuery());

        List<TestDoc> result = dbCon.queryView(query, TestDoc.class);

        assertEquals(2, result.size());
        assertEquals(TestDoc.class, result.get(0).getClass());
        assertEquals("doc_id1", result.get(0).getId());
        assertEquals("doc_id2", result.get(1).getId());
        verify(httpClient, times(1)).get(anyString());
    }

    @Test
    public void queries_with_ignore_not_found() {
        ViewQuery query = new ViewQuery()
                .dbPath(TEST_DB_PATH)
                .designDocId("_design/testdoc")
                .viewName("test_view")
                .includeDocs(true)
                .keys(Arrays.asList("doc_id0", "doc_id1", "doc_id2", "doc_id3", "doc_id4", "doc_id5", "doc_id6"));
        query.setIgnoreNotFound(true);

        doReturn(ResponseOnFileStub.newInstance(200, "view_result_with_ignored_docs.json")).when(httpClient).postUncached(anyString(), anyString());

        List<TestDoc> result = dbCon.queryView(query, TestDoc.class);

        assertEquals(3, result.size());
        assertEquals(TestDoc.class, result.get(0).getClass());
        assertEquals("doc_id1", result.get(0).getId());
        assertEquals("doc_id3", result.get(1).getId());
        assertEquals("doc_id6", result.get(2).getId());
        verify(httpClient).postUncached(query.buildQuery(), query.getKeysAsJson());
    }

    @Test
    public void multiple_query_keys_should_be_posted() {
        List<Object> keys = new ArrayList<Object>();
        keys.add("key1");
        keys.add("key2");
        keys.add("key3");

        ViewQuery query = new ViewQuery()
                .dbPath(TEST_DB_PATH)
                .designDocId("_design/testdoc")
                .viewName("test_view")
                .keys(keys);

        doReturn(ResponseOnFileStub.newInstance(200, "view_result_with_embedded_docs.json")).when(httpClient).postUncached(anyString(), anyString());
        dbCon.queryView(query, TestDoc.class);
        verify(httpClient).postUncached(query.buildQuery(), query.getKeysAsJson());
    }

    @Test
    public void multiple_query_keys_should_be_posted_2() {
        List<Object> keys = new ArrayList<Object>();
        keys.add("key1");
        keys.add("key2");
        keys.add("key3");

        ViewQuery query = new ViewQuery()
                .dbPath(TEST_DB_PATH)
                .designDocId("_design/testdoc")
                .viewName("test_view")
                .keys(keys);

        doReturn(ResponseOnFileStub.newInstance(200, "view_result.json")).when(httpClient).postUncached(anyString(), anyString());
        dbCon.queryView(query);
        verify(httpClient).postUncached(query.buildQuery(), query.getKeysAsJson());
    }

    @Test
    public void multiple_query_keys_should_be_posted_3() throws IOException {
        List<Object> keys = new ArrayList<Object>();
        keys.add("key1");
        keys.add("key2");
        keys.add("key3");

        ViewQuery query = new ViewQuery()
                .dbPath(TEST_DB_PATH)
                .designDocId("_design/testdoc")
                .viewName("test_view")
                .keys(keys);

        doReturn(ResponseOnFileStub.newInstance(200, "view_result.json")).when(httpClient).postUncached(anyString(), anyString());
        dbCon.queryForStream(query).close();
        verify(httpClient).postUncached(query.buildQuery(), query.getKeysAsJson());
    }

    @Test
    public void dates_should_be_serialized_in_ISO_8601_format() throws IOException {
        setupNegativeContains("some_id");
        doReturn(HttpResponseStub.valueOf(201, OK_RESPONSE_WITH_ID_AND_REV)).when(httpClient).put(anyString(), anyString());

        DateTime dt = new DateTime(2010, 4, 25, 20, 11, 24, 555, DateTimeZone.forID("+00:00"));
        Date d = dt.toDate();
        System.out.println(d);
        DateDoc dd = new DateDoc();
        dd.setId("some_id");
        dd.setDateTime(dt);
        dd.setDate(dt.toDate());

        dbCon.create(dd);

        ArgumentCaptor<String> ac = ArgumentCaptor.forClass(String.class);
        verify(httpClient).put(eq("/test_db/some_id"), ac.capture());
        String json = ac.getValue();
        assertEqualJson("dates.json", Charset.forName("UTF-8"), json);

        doReturn(HttpResponseStub.valueOf(201, json)).when(httpClient).get("/test_db/some_id");

        DateDoc deserialized = dbCon.get(DateDoc.class, dd.getId());
        assertEquals(dt, deserialized.getDateTime());
        assertEquals(d, deserialized.getDate());

    }

    @Test
    public void given_that_doc_exists_then_contains_should_return_true() {
        setupPositiveContains("some_id");
        assertTrue(dbCon.contains("some_id"));
    }

    private void setupPositiveContains(String id) {
        doReturn(HttpResponseStub.valueOf(200, "")).when(httpClient).head("/test_db/" + id);
    }

    protected void setupNegativeContains(String id) {
        doReturn(HttpResponseStub.valueOf(404, "")).when(httpClient).head("/test_db/" + id);
    }

    @Test
    public void given_that_doc_does_not_exists_then_contains_should_return_false() {
        setupNegativeContains("some_id");
        assertFalse(dbCon.contains("some_id"));
    }

    @Test
    public void testGetDbInfo() {
        doReturn(ResponseOnFileStub.newInstance(200, "db_info.json")).when(httpClient).get(TEST_DB_PATH);
        DbInfo info = dbCon.getDbInfo();
        assertNotNull(info);
        assertEquals("dj", info.getDbName());
        assertFalse(info.isCompactRunning());
        assertEquals(12377, info.getDiskSize());
        assertEquals(1267612389906234l, info.getInstanceStartTime());
        assertEquals(1, info.getDocCount());
        assertEquals(1, info.getDocDelCount());
        assertEquals(5, info.getDiskFormatVersion());
        assertEquals(1, info.getPurgeSeq());
        assertEquals(4, info.getUpdateSeq());
    }

    @Test
    public void test_replicateTo() {
        String targetDB = "http://somehost:5984/source_db";
        CouchDbInstance mockInstance = mock(CouchDbInstance.class);
        ReplicationStatus status = new ReplicationStatus();
        when(mockInstance.replicate(any(ReplicationCommand.class))).thenReturn(status);

        StdCouchDbConnector db = new StdCouchDbConnector("test_db", mockInstance);
        db.replicateTo(targetDB);
        ArgumentCaptor<ReplicationCommand> ac = ArgumentCaptor.forClass(ReplicationCommand.class);
        verify(mockInstance).replicate(ac.capture());
        assertEquals("test_db", ac.getValue().source);
        assertEquals(targetDB, ac.getValue().target);
    }

    @Test
    public void test_replicateFrom() {
        String sourceDB = "http://somehost:5984/source_db";
        CouchDbInstance mockInstance = mock(CouchDbInstance.class);
        ReplicationStatus status = new ReplicationStatus();
        when(mockInstance.replicate(any(ReplicationCommand.class))).thenReturn(status);

        StdCouchDbConnector db = new StdCouchDbConnector("test_db", mockInstance);
        db.replicateFrom(sourceDB);
        ArgumentCaptor<ReplicationCommand> ac = ArgumentCaptor.forClass(ReplicationCommand.class);
        verify(mockInstance).replicate(ac.capture());
        assertEquals("test_db", ac.getValue().target);
        assertEquals(sourceDB, ac.getValue().source);
    }

    @Test
    public void testGetRevsLimit() {
        doReturn(HttpResponseStub.valueOf(201, "\"1500\"")).when(httpClient).get("/test_db/_revs_limit");
        assertEquals(1500, dbCon.getRevisionLimit());
    }

    @Test
    public void testSetRevsLimit() {
        HttpResponse rsp = mock(HttpResponse.class);
        doReturn(Boolean.TRUE).when(rsp).isSuccessful();
        doReturn(new ReaderInputStream(new StringReader(OK_RESPONSE))).when(rsp).getContent();
        doReturn(rsp).when(httpClient).put(anyString(), anyString());
        dbCon.setRevisionLimit(500);
        verify(httpClient).put("/test_db/_revs_limit", "500");
        verify(rsp).releaseConnection();
    }

    @Test
    public void test_that_compact_relases_connection() {
        HttpResponse rsp = setupResponseOnPost();
        dbCon.compact();
        verify(httpClient).post(eq("/test_db/_compact"), anyString());
        verify(rsp).releaseConnection();
    }

    private HttpResponse setupResponseOnPost() {
        HttpResponse rsp = mock(HttpResponse.class);
        doReturn(Boolean.TRUE).when(rsp).isSuccessful();
        doReturn(new ReaderInputStream(new StringReader(OK_RESPONSE))).when(rsp).getContent();
        doReturn(rsp).when(httpClient).post(anyString(), anyString());
        return rsp;
    }

    @Test
    public void test_that_compactView_relases_connection() {
        HttpResponse rsp = setupResponseOnPost();
        dbCon.compactViews("_design/someDoc");
        verify(httpClient).post(eq("/test_db/_compact/_design/someDoc"), anyString());
        verify(rsp).releaseConnection();
    }

    @Test
    public void test_that_cleanupViews_relases_connection() {
        HttpResponse rsp = setupResponseOnPost();
        dbCon.cleanupViews();
        verify(httpClient).post(eq("/test_db/_view_cleanup"), anyString());
        verify(rsp).releaseConnection();
    }

    @Test
    public void testGetDesignDocInfo() {
        doReturn(ResponseOnFileStub.newInstance(200, "../design_doc_info.json")).when(httpClient).get("/test_db/_design/exampleDesignDoc/_info");
        DesignDocInfo info = dbCon.getDesignDocInfo("exampleDesignDoc");
        assertNotNull(info);
    }

    @Test
    public void testCallUpdateHandler() {
        doReturn(HttpResponseStub.valueOf(201, "response string")).when(httpClient).put("/test_db/_design/designDocID/_update/functionName/docID?key=value", "");
        Map<String, String> params = Collections.singletonMap("key", "value");
        assertEquals("response string", dbCon.callUpdateHandler("_design/designDocID", "functionName", "docID", params));
    }

    @Test
    public void testCallUpdateHanderWithObject() {

        doReturn(HttpResponseStub.valueOf(201,
                "{\"name\":\"nisse\",\"age\":12,\"_id\":\"some_id\",\"_rev\":\"123D123\"}")).when(
                httpClient).put("/test_db/_design/designDocID/_update/functionName/docID",
                "{\"value\":\"value\",\"param\":\"param\"}");

        UpdateHandlerRequest req = new UpdateHandlerRequest();
        req.designDocId("_design/designDocID")
                .functionName("functionName")
                .docId("docID")
                .body(new TestRequest("param", "value"));

        TestDoc updated = dbCon.callUpdateHandler(req, TestDoc.class);

        assertNotNull(updated);
        assertEquals("some_id", updated.getId());
        assertEquals("123D123", updated.getRevision());
        assertEquals("nisse", updated.name);
        assertEquals(12, updated.age);
    }

    @Test
    public void testEnsureFullCommit() {
        doReturn(HttpResponseStub.valueOf(200, "{\"ok\" : true, \"instance_start_time\" : \"1288186189373361\"}")).when(httpClient).post("/test_db/_ensure_full_commit", "");
        dbCon.ensureFullCommit();
        verify(httpClient).post("/test_db/_ensure_full_commit", "");
    }

    @Test
    public void testPurge() {
        String rsp = "{\"purged\" : { \"Billy\" : [ \"17-b3eb5ac6fbaef4428d712e66483dcb79\"]},\"purge_seq\" : 11}";
        doReturn(HttpResponseStub.valueOf(200, rsp)).when(httpClient).post(eq("/test_db/_purge"), anyString());
        Map<String, List<String>> revisionsToPurge = new HashMap<String, List<String>>();
        revisionsToPurge.put("Billy", Collections.singletonList("17-b3eb5ac6fbaef4428d712e66483dcb79"));
        PurgeResult r = dbCon.purge(revisionsToPurge);
        assertEquals(11, r.getPurgeSeq());
        assertTrue(r.getPurged().containsKey("Billy"));
    }

    @Test
    public void updateMultipart_should_perform_put_operation_with_path_set_to_db_followed_by_id() {
        String id = UUID.randomUUID().toString();

        String expectedPath = "/test_db/" + id;
        doReturn(null).when(httpClient).put(eq(expectedPath), any(InputStream.class), anyString(), anyLong());

        dbCon.updateMultipart(id, null, "abc", 0, null);

        verify(httpClient).put(eq(expectedPath), any(InputStream.class), anyString(), anyLong());
    }

    @Test
    public void updateMultipart_should_perform_put_operation_with_path_using_any_given_options() {
        String id = UUID.randomUUID().toString();
        String paramName = "some_param";
        String paramValue = "false";
        Options options = new Options().param(paramName, paramValue);

        String expectedPath = "/test_db/" + id + "?some_param=false";
        doReturn(null).when(httpClient).put(eq(expectedPath), any(InputStream.class), anyString(), anyLong());
        dbCon.updateMultipart(id, null, "abc", 0, options);

        verify(httpClient).put(eq(expectedPath), any(InputStream.class), anyString(), anyLong());
    }

    @Test
    public void updateMultipart_should_perform_put_operation_with_the_given_InputStream() {
        InputStream stream = mock(InputStream.class);

        doReturn(null).when(httpClient).put(anyString(), eq(stream), anyString(), anyLong());
        dbCon.updateMultipart("a", stream, "abc", 0, null);

        verify(httpClient).put(anyString(), eq(stream), anyString(), anyLong());
    }

    @Test
    public void updateMultipart_should_perform_put_operation_with_content_type_set_to_multipart_related_with_boundary() {
        String boundary = UUID.randomUUID().toString();
        String expectedContentType = "multipart/related; boundary=\"" + boundary + "\"";
        doReturn(null).when(httpClient).put(anyString(), any(InputStream.class), eq(expectedContentType), anyLong());
        dbCon.updateMultipart("a", null, boundary, 0, null);
        verify(httpClient).put(anyString(), any(InputStream.class), eq(expectedContentType), anyLong());
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateMultipart_should_throw_if_boundary_is_null() {
        dbCon.updateMultipart("a", null, null, 0, null);
    }

    @Test
    public void updateMultipart_should_perform_put_operation_with_content_type_set_to_length() {
        long length = 1000l;
        doReturn(null).when(httpClient).put(anyString(), any(InputStream.class), anyString(), eq(length));

        dbCon.updateMultipart("a", null, "abc", length, null);

        verify(httpClient).put(anyString(), any(InputStream.class), anyString(), eq(length));
    }

    @Test
    public void update_with_stream_should_perform_put_operation_with_content_type_set_to_application_json() {
        String documentId = UUID.randomUUID().toString();
        InputStream inputStream = mock(InputStream.class);
        long documentLength = 999l;
        String paramName = "some_param";
        String paramValue = "false";
        Options options = new Options().param(paramName, paramValue);

        String expectedPath = "/test_db/" + documentId + "?some_param=false";
        String expectedContentType = "application/json";

        doReturn(null).when(httpClient).put(expectedPath, inputStream, expectedContentType, documentLength);
        dbCon.update(documentId, inputStream, documentLength, options);


        verify(httpClient).put(expectedPath, inputStream, expectedContentType, documentLength);
    }

    @Test
    public void testCopy() {
        String src = "sourceId";
        String target = "targetId";
        doReturn(HttpResponseStub.valueOf(201, OK_RESPONSE_WITH_ID_AND_REV)).when(httpClient).copy(anyString(), anyString());
        dbCon.copy(src, target);
        String expectedPath = "/test_db/" + src;
        verify(httpClient).copy(expectedPath, target);
    }

    @Test
    public void testCopyRev() {
        String src = "sourceId";
        String target = "targetId";
        String rev = "revision";
        doReturn(HttpResponseStub.valueOf(201, OK_RESPONSE_WITH_ID_AND_REV)).when(httpClient).copy(anyString(), anyString());
        dbCon.copy(src, target, rev);
        String expectedPath = "/test_db/" + src;
        String expectedTarget = target + "?rev=" + rev;
        verify(httpClient).copy(expectedPath, expectedTarget);
    }

    @Test
    public void testSecurityConfigurationValue() {
        doReturn(HttpResponseStub.valueOf(200, "{\"admins\":{\"names\":[\"admin\"],\"roles\":[\"admin\"]},\"members\":{\"names\":[\"user\"],\"roles\":[\"users\"]}}")).when(httpClient).get(anyString());
        Security sec = dbCon.getSecurity();
        assertNotNull(sec);
        assertNotNull(sec.getAdmins());
        assertNotNull(sec.getMembers());

        assertEquals(sec.getAdmins().getNames().get(0), "admin");
        assertEquals(sec.getAdmins().getRoles().get(0), "admin");
        assertEquals(sec.getMembers().getNames().get(0), "user");
        assertEquals(sec.getMembers().getRoles().get(0), "users");
    }

    @Test
    public void testUpdateSecurityConfigurationValue() {
        doReturn(HttpResponseStub.valueOf(200, "{\"ok\":true}")).when(httpClient).put(anyString(), anyString());
        Security security = new Security();
        Status status = dbCon.updateSecurity(security);

        assertTrue(status.isOk());
    }

    @SuppressWarnings("serial")
    static class DateDoc extends CouchDbDocument {

        private Date date;
        private DateTime dateTime;

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public DateTime getDateTime() {
            return dateTime;
        }

        public void setDateTime(DateTime dateTime) {
            this.dateTime = dateTime;
        }
    }

    @SuppressWarnings("serial")
    public static class TestDoc extends CouchDbDocument {
        private String name;
        private int age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

    }

    @JsonPropertyOrder({"value", "param"})
    static class TestRequest {
        private String param;
        private String value;

        public TestRequest(String param, String value) {
            this.param = param;
            this.value = value;
        }

        public String getParam() {
            return param;
        }

        public void setParam(String param) {
            this.param = param;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    protected static void assertEqualJson(String expectedFileName, Charset expectedFileCharset, String actual) throws IOException {
        Reader expectedReader = new StringReader(getString(expectedFileName, expectedFileCharset));
        Reader actualReader = new StringReader(actual);
        assertTrue(format("expected: %s was: %s", getString(expectedFileName, expectedFileCharset), actual), JSONComparator.areEqual(expectedReader, actualReader));
    }

    protected static void assertEqualJson(String expectedFileName, Charset expectedFileCharset, byte[] actual) throws IOException {
        Reader expectedReader = new StringReader(getString(expectedFileName, expectedFileCharset));
        Reader actualReader = new InputStreamReader(new ByteArrayInputStream(actual), Charset.forName("UTF-8"));
        assertTrue(format("expected: %s was: %s", getString(expectedFileName, expectedFileCharset), new String(actual, Charset.forName("UTF-8"))), JSONComparator.areEqual(expectedReader, actualReader));
    }

    protected static String getString(String resourceName, Charset charset) throws IOException {
        InputStream inputStream = null;
        Reader reader = null;
        try {
            inputStream = StdCouchDbConnectorTest.class.getResourceAsStream(resourceName);
            reader = new InputStreamReader(inputStream, charset);
            return IOUtils.toString(reader);
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(inputStream);
        }
    }

    protected byte[] getBytes(String resourceName) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = StdCouchDbConnectorTest.class.getResourceAsStream(resourceName);
            return IOUtils.toByteArray(inputStream);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

}
