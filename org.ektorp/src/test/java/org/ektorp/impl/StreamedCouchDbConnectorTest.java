package org.ektorp.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.apache.http.HttpEntity;
import org.ektorp.CouchDbConnector;
import org.ektorp.PurgeResult;
import org.ektorp.UpdateConflictException;
import org.ektorp.http.JacksonableEntity;
import org.ektorp.http.StdHttpClient;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.stubbing.answers.ThrowsException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class StreamedCouchDbConnectorTest extends StdCouchDbConnectorTest {

    private boolean bufferized;

    {
        setBufferized(false);
    }

    public void setBufferized(boolean value) {
        this.bufferized = value;
    }

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
        }) {

            @Override
            public CouchDbConnector createConnector(String path, boolean createIfNotExists) {
                CouchDbConnector db = new StreamedCouchDbConnector(path, this, this.getObjectMapperFactory()) {
                    @Override
                    protected HttpEntity createHttpEntity(Object o) {
                        return new JacksonableEntity(o, bufferized, objectMapper);
                    }
                };
                if (createIfNotExists) db.createDatabaseIfNotExists();
                return db;
            }

        };
        dbCon = dbInstance.createConnector("test_db/", false);

        td.setName("nisse");
        td.setAge(12);
    }

    @Test
    public void update() throws IOException {
        td.setId("some_id");
        td.setRevision("123D123");

        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        doAnswer(new MarshallEntityAndReturnAnswer(output, HttpResponseStub.valueOf(201, OK_RESPONSE_WITH_ID_AND_REV))).when(httpClient).put(anyString(), any(HttpEntity.class));
        dbCon.update(td);
        ArgumentCaptor<HttpEntity> ac = ArgumentCaptor.forClass(HttpEntity.class);
        verify(httpClient).put(eq("/test_db/some_id"), ac.capture());
        assertEquals("some_id", td.getId());
        assertEquals("123D123", td.getRevision());
        assertEqualJson("update.json", Charset.forName("UTF-8"), output.toByteArray());
    }

    @Test
    public void testCreate() throws IOException {
        td.setId("some_id");
        setupNegativeContains(td.getId());

        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        doAnswer(new MarshallEntityAndReturnAnswer(output, HttpResponseStub.valueOf(201, OK_RESPONSE_WITH_ID_AND_REV))).when(httpClient).put(anyString(), any(HttpEntity.class));
        dbCon.create(td);
        ArgumentCaptor<HttpEntity> ac = ArgumentCaptor.forClass(HttpEntity.class);
        verify(httpClient).put(eq("/test_db/some_id"), ac.capture());
        assertEquals("some_id", td.getId());
        assertEquals("123D123", td.getRevision());
        assertEqualJson("create.json", Charset.forName("UTF-8"), output.toByteArray());
    }

    @Test
    public void docId_should_be_escaped_when_id_contains_slash() throws UnsupportedEncodingException {
        String escapedId = "http%3A%2F%2Fsome%2Fopenid%3Fgoog";
        td.setId("http://some/openid?goog");
        setupNegativeContains(escapedId);
        doReturn(HttpResponseStub.valueOf(201, OK_RESPONSE_WITH_ID_AND_REV)).when(httpClient).put(anyString(), any(HttpEntity.class));
        dbCon.create(td);
        ArgumentCaptor<HttpEntity> ac = ArgumentCaptor.forClass(HttpEntity.class);
        verify(httpClient).put(eq("/test_db/" + escapedId), ac.capture());
        assertEquals("http://some/openid?goog", td.getId());
    }

    @Test
    public void testCreateFromJsonNode() throws Exception {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        doAnswer(new MarshallEntityAndReturnAnswer(output, HttpResponseStub.valueOf(201, OK_RESPONSE_WITH_ID_AND_REV))).when(httpClient).put(anyString(), any(HttpEntity.class));
        JsonNode root = new ObjectMapper().readValue(getClass().getResourceAsStream("create_from_json_node.json"),
                JsonNode.class);
        dbCon.create("some_id", root);
        ArgumentCaptor<HttpEntity> ac = ArgumentCaptor.forClass(HttpEntity.class);
        verify(httpClient).put(eq("/test_db/some_id"), ac.capture());
        assertEqualJson("create_from_json_node.json", Charset.forName("UTF-8"), output.toByteArray());
    }

    @Test
    public void save_document_with_utf8_charset() throws IOException {
        td.setId("some_id");
        td.setName("Örjan Åäö");
        setupNegativeContains(td.getId());

        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        doAnswer(new MarshallEntityAndReturnAnswer(output, HttpResponseStub.valueOf(201, OK_RESPONSE_WITH_ID_AND_REV))).when(httpClient).put(anyString(), any(HttpEntity.class));
        dbCon.create(td);
        ArgumentCaptor<HttpEntity> ac = ArgumentCaptor.forClass(HttpEntity.class);
        verify(httpClient).put(eq("/test_db/some_id"), ac.capture());
        assertEqualJson("charset.json", Charset.forName("UTF-8"), output.toByteArray());
    }

    @Test
    public void save_document_with_ISO_8859_1_charset() throws IOException {
        td.setId("some_id");
        td.setName("Örjan Åäö");
        setupNegativeContains(td.getId());

        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        doAnswer(new MarshallEntityAndReturnAnswer(output, HttpResponseStub.valueOf(201, OK_RESPONSE_WITH_ID_AND_REV))).when(httpClient).put(anyString(), any(HttpEntity.class));
        dbCon.create(td);
        ArgumentCaptor<HttpEntity> ac = ArgumentCaptor.forClass(HttpEntity.class);
        verify(httpClient).put(eq("/test_db/some_id"), ac.capture());
        assertEqualJson("charset-ISO-8859-1.json", Charset.forName("ISO-8859-1"), output.toByteArray());
    }

    @Test
    public void create_should_post_if_id_is_missing() throws IOException {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        doAnswer(new MarshallEntityAndReturnAnswer(output, HttpResponseStub.valueOf(201, OK_RESPONSE_WITH_ID_AND_REV))).when(httpClient).post(anyString(), any(HttpEntity.class));
        dbCon.create(td);
        ArgumentCaptor<HttpEntity> ac = ArgumentCaptor.forClass(HttpEntity.class);
        verify(httpClient).post(eq(TEST_DB_PATH), ac.capture());
        assertEquals("some_id", td.getId());
        assertEquals("123D123", td.getRevision());
        assertEqualJson("create_with_no_id.json", Charset.forName("UTF-8"), output.toByteArray());
    }

    @Test
    public void dates_should_be_serialized_in_ISO_8601_format() throws IOException {
        setupNegativeContains("some_id");

        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        doAnswer(new MarshallEntityAndReturnAnswer(output, HttpResponseStub.valueOf(201, OK_RESPONSE_WITH_ID_AND_REV))).when(httpClient).put(anyString(), any(HttpEntity.class));

        DateTime dt = new DateTime(2010, 4, 25, 20, 11, 24, 555, DateTimeZone.forID("+00:00"));
        Date d = dt.toDate();
        System.out.println(d);
        DateDoc dd = new DateDoc();
        dd.setId("some_id");
        dd.setDateTime(dt);
        dd.setDate(dt.toDate());

        dbCon.create(dd);

        ArgumentCaptor<JacksonableEntity> ac = ArgumentCaptor.forClass(JacksonableEntity.class);
        verify(httpClient).put(eq("/test_db/some_id"), ac.capture());
        byte[] json = output.toByteArray();
        assertEqualJson("dates.json", Charset.forName("UTF-8"), json);

        doReturn(HttpResponseStub.valueOf(201, output.toString("UTF-8"))).when(httpClient).get("/test_db/some_id");

        DateDoc deserialized = dbCon.get(DateDoc.class, dd.getId());
        assertEquals(dt, deserialized.getDateTime());
        assertEquals(d, deserialized.getDate());

    }

    @Test(expected = UpdateConflictException.class)
    public void throw_exception_when_in_conflict() {
        td.setId("some_id");
        td.setRevision("123D123");

        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        doAnswer(new MarshallEntityAndReturnAnswer(output, ResponseOnFileStub.newInstance(409, "update_conflict.json"))).when(httpClient).put(anyString(), any(HttpEntity.class));
        dbCon.update(td);
    }

    @Test
    public void testPurge() {
        String rsp = "{\"purged\" : { \"Billy\" : [ \"17-b3eb5ac6fbaef4428d712e66483dcb79\"]},\"purge_seq\" : 11}";

        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        doAnswer(new MarshallEntityAndReturnAnswer(output, HttpResponseStub.valueOf(200, rsp))).when(httpClient).post(eq("/test_db/_purge"), any(HttpEntity.class));
        Map<String, List<String>> revisionsToPurge = new HashMap<String, List<String>>();
        revisionsToPurge.put("Billy", Collections.singletonList("17-b3eb5ac6fbaef4428d712e66483dcb79"));
        PurgeResult r = dbCon.purge(revisionsToPurge);
        assertEquals(11, r.getPurgeSeq());
        assertTrue(r.getPurged().containsKey("Billy"));
    }

    private static class MarshallEntityAndReturnAnswer implements Answer {
        private final ByteArrayOutputStream output;
        private final Object result;

        public MarshallEntityAndReturnAnswer(ByteArrayOutputStream output, Object result) {
            this.output = output;
            this.result = result;
        }

        @Override
        public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
            HttpEntity httpEntity = (HttpEntity) invocationOnMock.getArguments()[1];
            httpEntity.writeTo(output);
            return result;
        }
    }
}
