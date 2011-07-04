package org.ektorp.impl;

import static java.lang.String.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.*;
import java.util.*;

import org.apache.commons.io.*;
import org.codehaus.jackson.*;
import org.codehaus.jackson.map.*;
import org.ektorp.*;
import org.ektorp.http.*;
import org.ektorp.support.*;
import org.ektorp.util.*;
import org.joda.time.*;
import org.junit.*;
import org.mockito.*;
import org.mockito.internal.verification.*;

public class StdCouchDbConnectorTest {

	private static final String TEST_DB_PATH = "/test_db/";
	StdCouchDbConnector dbCon;
	StdHttpClient httpClient;
	TestDoc td = new TestDoc();
	
	@Before
	public void setUp() throws Exception {
		httpClient = mock(StdHttpClient.class);
		dbCon = new StdCouchDbConnector("test_db/", new StdCouchDbInstance(httpClient));
		
		td.name = "nisse";
		td.age = 12;
	}

	@Test
	public void testCreate() {
		td.setId("some_id");
		setupNegativeContains(td.getId());
		when(httpClient.put(anyString(), anyString())).thenReturn(HttpResponseStub.valueOf(201, "{\"ok\":true,\"id\":\"some_id\",\"rev\":\"123D123\"}"));
		dbCon.create(td);
		ArgumentCaptor<String> ac = ArgumentCaptor.forClass(String.class);
		verify(httpClient).put(eq("/test_db/some_id"), ac.capture());
		assertEquals("some_id", td.getId());
		assertEquals("123D123", td.getRevision());
		assertEqualJson("create.json", ac.getValue());
	}
	
	@Test
	public void docId_should_be_escaped_when_id_contains_slash() throws UnsupportedEncodingException {
		String escapedId = "http%3A%2F%2Fsome%2Fopenid%3Fgoog";
		td.setId("http://some/openid?goog");
		setupNegativeContains(escapedId);
		when(httpClient.put(anyString(), anyString())).thenReturn(HttpResponseStub.valueOf(201, "{\"ok\":true,\"id\":\"some_id\",\"rev\":\"123D123\"}"));
		dbCon.create(td);
		ArgumentCaptor<String> ac = ArgumentCaptor.forClass(String.class);
		verify(httpClient).put(eq("/test_db/" + escapedId), ac.capture());
		assertEquals("http://some/openid?goog", td.getId());
	}
	
	@Test
	public void testCreateFromJsonNode() throws Exception {
		when(httpClient.put(anyString(), anyString())).thenReturn(HttpResponseStub.valueOf(201, "{\"ok\":true,\"id\":\"some_id\",\"rev\":\"123D123\"}"));
		JsonNode root = new ObjectMapper().readValue(getClass().getResourceAsStream("create_from_json_node.json"), JsonNode.class);
		dbCon.create("some_id", root);
		String facit = IOUtils.toString(getClass().getResourceAsStream("create_from_json_node.json"), "UTF-8").trim();
		verify(httpClient).put("/test_db/some_id", facit);
	}
	
	@Test
	public void save_document_with_utf8_charset() {
		td.setId("some_id");
		td.name = "Örjan Åäö";
		setupNegativeContains(td.getId());
		when(httpClient.put(anyString(), anyString())).thenReturn(HttpResponseStub.valueOf(201, "{\"ok\":true,\"id\":\"some_id\",\"rev\":\"123D123\"}"));
		dbCon.create(td);
		ArgumentCaptor<String> ac = ArgumentCaptor.forClass(String.class);
		verify(httpClient).put(eq("/test_db/some_id"), ac.capture());
		assertEqualJson("charset.json", ac.getValue());
	}
	
	@Test
	public void create_should_post_if_id_is_missing() {		
		when(httpClient.post(anyString(), anyString())).thenReturn(HttpResponseStub.valueOf(201, "{\"ok\":true,\"id\":\"some_id\",\"rev\":\"123D123\"}"));
		dbCon.create(td);
		ArgumentCaptor<String> ac = ArgumentCaptor.forClass(String.class);
		verify(httpClient).post(eq(TEST_DB_PATH), ac.capture());
		assertEquals("some_id", td.getId());
		assertEquals("123D123", td.getRevision());
		assertEqualJson("create_with_no_id.json", ac.getValue());
	}
	
	@Test
	public void testDelete() {
		when(httpClient.delete(anyString())).thenReturn(HttpResponseStub.valueOf(200, "{\"ok\":true,\"rev\":\"123D123\"}"));
		dbCon.delete("some_id", "123D123");
		verify(httpClient).delete("/test_db/some_id?rev=123D123");
	}

	@Test
	public void testDeleteCouchDbDocument() {
		td.setId("some_id");
		td.setRevision("123D123");
		setupGetDocResponse();
		when(httpClient.delete(anyString())).thenReturn(HttpResponseStub.valueOf(200, "{\"ok\":true,\"rev\":\"123D123\"}"));
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
	
	@Test
	public void testGetWithConflicts() {
		setupGetDocResponse();
		dbCon.getWithConflicts(TestDoc.class, "some_id");
		verify(httpClient).get(eq("/test_db/some_id?conflicts=true"));
	}

	private void setupGetDocResponse() {
		when(httpClient.get(anyString())).thenReturn(HttpResponseStub.valueOf(200, "{\"name\":\"nisse\",\"age\":12,\"_id\":\"some_id\",\"_rev\":\"123D123\"}"));
	}
	
	private void setupGetDocResponse(String... ids) {
		for (String id : ids) {
			when(httpClient.get(TEST_DB_PATH + id)).thenReturn(HttpResponseStub.valueOf(200, "{\"name\":\"nisse\",\"age\":12,\"_id\":\" "+ id + "\",\"_rev\":\"123D123\"}"));
		}
	}

	@Test(expected=DocumentNotFoundException.class)
	public void throw_exception_when_doc_is_missing() {
		when(httpClient.get(anyString())).thenReturn(HttpResponseStub.valueOf(404, ""));
		dbCon.get(TestDoc.class, "some_id");
	}
	
	@Test
	public void update() {
		td.setId("some_id");
		td.setRevision("123D123");
		when(httpClient.put(anyString(), anyString())).thenReturn(HttpResponseStub.valueOf(201, "{\"ok\":true,\"id\":\"some_id\",\"rev\":\"123D123\"}"));
		dbCon.update(td);
		ArgumentCaptor<String> ac = ArgumentCaptor.forClass(String.class);
		verify(httpClient).put(eq("/test_db/some_id"), ac.capture());
		assertEquals("some_id", td.getId());
		assertEquals("123D123", td.getRevision());
		assertEqualJson("update.json", ac.getValue());
	}
	
	@Test(expected=UpdateConflictException.class)
	public void throw_exception_when_in_conflict() {
		td.setId("some_id");
		td.setRevision("123D123");
		when(httpClient.put(anyString(), anyString())).thenReturn(ResponseOnFileStub.newInstance(409, "update_conflict.json"));
		dbCon.update(td);
	}
	
	@Test(expected=UpdateConflictException.class)
	public void throw_exception_when_create_attachment_in_conflict() {
		td.setId("some_id");
		td.setRevision("123D123");
		when(httpClient.put(anyString(), any(InputStream.class), anyString(), anyInt())).thenReturn(ResponseOnFileStub.newInstance(409, "update_conflict.json"));
		dbCon.createAttachment("id", "rev", new AttachmentInputStream("attach_id", IOUtils.toInputStream("data"), "whatever"));
	}
	
	@Test
	public void should_create_db_if_missing() {
		when(httpClient.head("/test_db/")).thenReturn(HttpResponseStub.valueOf(404, "{\"error\":\"not_found\",\"reason\":\"no_db_file\"}"));
		
		dbCon.createDatabaseIfNotExists();
		verify(httpClient).put("/test_db/");
	}
	
	@Test
	public void should_not_create_db_if_already_exists() {
		when(httpClient.head("/test_db/")).thenReturn(HttpResponseStub.valueOf(200, "{\"test_db\":\"global\",\"doc_count\":1,\"doc_del_count\":0,\"update_seq\":3,\"purge_seq\":0,\"compact_running\":false,\"disk_size\":100,\"instance_start_time\":\"130\",\"disk_format_version\":5,\"committed_update_seq\":3}"));
		
		dbCon.createDatabaseIfNotExists();
		verify(httpClient, VerificationModeFactory.times(0)).put(anyString());
	}
	
	@Test
	public void return_all_docIds_in_db() {
		when(httpClient.get("/test_db/_all_docs")).thenReturn(HttpResponseStub.valueOf(200, 
				"{\"total_rows\": 3, \"offset\": 0, \"rows\": [" +
				"{\"id\": \"doc1\", \"key\": \"doc1\", \"value\": {\"rev\": \"4324BB\"}}," +
				"{\"id\": \"doc2\", \"key\": \"doc2\", \"value\": {\"rev\":\"2441HF\"}}," +
				"{\"id\": \"doc3\", \"key\": \"doc3\", \"value\": {\"rev\":\"74EC24\"}}]}"));
		List<String> all = dbCon.getAllDocIds();
		assertEquals(3, all.size());
		assertEquals("doc1", all.get(0));
		assertEquals("doc2", all.get(1));
		assertEquals("doc3", all.get(2));
	}

	@Test
	public void return_all_revisions() {
		when(httpClient.get("/test_db/some_doc_id?revs_info=true"))
			.thenReturn(ResponseOnFileStub.newInstance(200, "revisions.json"));
		List<Revision> l = dbCon.getRevisions("some_doc_id");
		assertNotNull(l);
		assertEquals(8, l.size());
		assertEquals(new Revision("8-8395fd3a7a2dd04022cc1330a4d20e66","available"), l.get(0));
	}
	
	@Test
	public void return_null_revisions_when_doc_is_missing() {
		when(httpClient.get("/test_db/some_doc_id?revs_info=true"))
			.thenReturn(HttpResponseStub.valueOf(404, ""));
		List<Revision> l = dbCon.getRevisions("some_doc_id");
		assertNotNull(l);
		assertTrue(l.isEmpty());
	}
	
	@Test
	public void return_attachment_with_open_data_stream() throws Exception {
		ResponseOnFileStub rsp = ResponseOnFileStub.newInstance(200, "attachment.txt", "text", 12);
		when(httpClient.get("/test_db/some_doc_id/some_attachment")).thenReturn(rsp);
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
		when(httpClient.get("/test_db/some_doc_id")).thenReturn(rsp);
		InputStream data = dbCon.getAsStream("some_doc_id");
		assertNotNull(data);
		assertFalse(rsp.isConnectionReleased());
		assertEquals("detta är ett påhäng med ett ö i", IOUtils.toString(data, "UTF-8"));
	}
	
	@Test
	public void should_stream_attachmed_content() {
		when(httpClient.put(anyString(), any(InputStream.class), anyString(), anyInt())).thenReturn(ResponseOnFileStub.newInstance(200, "create_attachment_rsp.json"));
		
		dbCon.createAttachment("docid", new AttachmentInputStream("attachment_id",IOUtils.toInputStream("content"), "text/html", 12l));
		
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
		
		when(httpClient.get(query.buildQuery())).thenReturn(ResponseOnFileStub.newInstance(200, "view_result.json"));
		
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
	
		when(httpClient.get(query.buildQuery())).thenReturn(ResponseOnFileStub.newInstance(200, "view_result_with_embedded_docs.json"));
		
		List<TestDoc> result = dbCon.queryView(query, TestDoc.class);
		
		assertEquals(2, result.size());
		assertEquals(TestDoc.class, result.get(0).getClass());
		assertEquals("doc_id1", result.get(0).getId());
		assertEquals("doc_id2", result.get(1).getId());
		verify(httpClient, times(1)).get(anyString());
	}
	
	@Test
	public void queries_with_include_doc_should_read_docs_directly() {
		ViewQuery query = new ViewQuery()
			.dbPath(TEST_DB_PATH)
			.designDocId("_design/testdoc")
			.viewName("test_view")
			.includeDocs(true)
			.key("key_value");
	
		when(httpClient.get(query.buildQuery())).thenReturn(ResponseOnFileStub.newInstance(200, "view_result_with_included_docs.json"));
		
		List<TestDoc> result = dbCon.queryView(query, TestDoc.class);
		
		assertEquals(2, result.size());
		assertEquals(TestDoc.class, result.get(0).getClass());
		assertEquals("doc_id1", result.get(0).getId());
		assertEquals("doc_id2", result.get(1).getId());
		verify(httpClient, times(1)).get(anyString());
	}
	
	@Test
	public void multiple_query_keys_should_be_posted() {
		List<Object> keys = new ArrayList<Object>();
		keys.add("key1");
		keys.add("key2");
		keys.add("key3");;
		
		ViewQuery query = new ViewQuery()
			.dbPath(TEST_DB_PATH)
			.designDocId("_design/testdoc")
			.viewName("test_view")
			.keys(keys);
		
		when(httpClient.post(anyString(), anyString())).thenReturn(ResponseOnFileStub.newInstance(200, "view_result_with_embedded_docs.json"));
		dbCon.queryView(query, TestDoc.class);
		verify(httpClient).post(query.buildQuery(), query.getKeysAsJson());
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
		
		when(httpClient.post(anyString(), anyString())).thenReturn(ResponseOnFileStub.newInstance(200, "view_result.json"));
		dbCon.queryView(query);
		verify(httpClient).post(query.buildQuery(), query.getKeysAsJson());
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
		
		when(httpClient.postUncached(anyString(), anyString())).thenReturn(ResponseOnFileStub.newInstance(200, "view_result.json"));
		dbCon.queryForStream(query).close();
		verify(httpClient).postUncached(query.buildQuery(), query.getKeysAsJson());
	}
	
	@Test
	public void dates_should_be_serialized_in_ISO_8601_format() {
		setupNegativeContains("some_id");
		when(httpClient.put(anyString(), anyString())).thenReturn(HttpResponseStub.valueOf(201, "{\"ok\":true,\"id\":\"some_id\",\"rev\":\"123D123\"}"));
		
		DateTime dt = new DateTime(2010, 4, 25, 20, 11, 24, 555,DateTimeZone.forID("+00:00"));
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
		assertEqualJson("dates.json", json);
		
		when(httpClient.get("/test_db/some_id")).thenReturn(HttpResponseStub.valueOf(201, json));
		
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
		when(httpClient.head("/test_db/" + id)).thenReturn(HttpResponseStub.valueOf(200, ""));
	}
	
	private void setupNegativeContains(String id) {
		when(httpClient.head("/test_db/" + id)).thenReturn(HttpResponseStub.valueOf(404, ""));
	}
	
	@Test
	public void given_that_doc_does_not_exists_then_contains_should_return_false() {
		setupNegativeContains("some_id");
		assertFalse(dbCon.contains("some_id"));
	}
	
	@Test
	public void testGetDbInfo() {
		when(httpClient.get(TEST_DB_PATH)).thenReturn(ResponseOnFileStub.newInstance(200, "db_info.json"));
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
		when(httpClient.get("/test_db/_revs_limit")).thenReturn(HttpResponseStub.valueOf(201, "\"1500\""));
		assertEquals(1500, dbCon.getRevisionLimit());
	}
	
	@Test
	public void testSetRevsLimit() {
		HttpResponse rsp = mock(HttpResponse.class);
		when(rsp.isSuccessful()).thenReturn(Boolean.TRUE);
		when(httpClient.put(anyString(),anyString())).thenReturn(rsp);
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
		when(rsp.isSuccessful()).thenReturn(Boolean.TRUE);
		when(httpClient.post(anyString(),anyString())).thenReturn(rsp);
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
		when(httpClient.get("/test_db/_design/exampleDesignDoc/_info")).thenReturn(ResponseOnFileStub.newInstance(200, "../design_doc_info.json"));
		DesignDocInfo info = dbCon.getDesignDocInfo("exampleDesignDoc");
		assertNotNull(info);
	}
	
	@Test
	public void testCallUpdateHandler() {
		when(httpClient.put("/test_db/_design/designDocID/_update/functionName/docID?key=value", "")).thenReturn(HttpResponseStub.valueOf(201, "response string"));
		Map<String, String> params = Collections.singletonMap("key", "value");
		assertEquals("response string", dbCon.callUpdateHandler("_design/designDocID", "functionName", "docID", params));
	}
	
	@Test
	public void testEnsureFullCommit() {
		when(httpClient.post("/test_db/_ensure_full_commit", "")).thenReturn(HttpResponseStub.valueOf(200, "{\"ok\" : true, \"instance_start_time\" : \"1288186189373361\"}"));
		dbCon.ensureFullCommit();
		verify(httpClient).post("/test_db/_ensure_full_commit", "");
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
	static class TestDoc extends CouchDbDocument {
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
	
	private void assertEqualJson(String expectedFileName, String actual) {
		String facit = getString(expectedFileName);
		assertTrue(format("expected: %s was: %s", facit, actual), JSONComparator.areEqual(facit, actual));
	}
	
	private String getString(String resourceName) {
		try {
			return IOUtils.toString(getClass().getResourceAsStream(resourceName), "UTF-8");
		} catch (IOException e) {
			throw Exceptions.propagate(e);
		}
	}

}
