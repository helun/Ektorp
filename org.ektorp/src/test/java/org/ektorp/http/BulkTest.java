package org.ektorp.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.RandomStringUtils;
import org.ektorp.*;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;
import org.ektorp.impl.StdObjectMapperFactory;
import org.ektorp.impl.StreamedCouchDbConnector;
import org.ektorp.support.CouchDbDocument;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/* throws:
Exception in thread "main" org.ektorp.DbAccessException: java.net.SocketException: Connection reset
        at org.ektorp.util.Exceptions.propagate(Exceptions.java:19)
        at org.ektorp.http.StdHttpClient.executeRequest(StdHttpClient.java:102)
        at org.ektorp.http.StdHttpClient.executePutPost(StdHttpClient.java:88)
        at org.ektorp.http.StdHttpClient.post(StdHttpClient.java:43)
        at org.ektorp.http.RestTemplate.post(RestTemplate.java:53)
        at org.ektorp.impl.StdCouchDbConnector.queryView(StdCouchDbConnector.java:284)
        at BulkTest.main(BulkTest.java:83)
Caused by: java.net.SocketException: Connection reset
        at java.net.SocketInputStream.read(SocketInputStream.java:185)
        at org.apache.http.impl.io.AbstractSessionInputBuffer.fillBuffer(AbstractSessionInputBuffer.java:149)
        at org.apache.http.impl.io.SocketInputBuffer.fillBuffer(SocketInputBuffer.java:110)
        at org.apache.http.impl.io.AbstractSessionInputBuffer.readLine(AbstractSessionInputBuffer.java:260)
        at org.apache.http.impl.conn.DefaultResponseParser.parseHead(DefaultResponseParser.java:98)
        at org.apache.http.impl.io.AbstractMessageParser.parse(AbstractMessageParser.java:252)
        at org.apache.http.impl.AbstractHttpClientConnection.receiveResponseHeader(AbstractHttpClientConnection.java:281)
        at org.apache.http.impl.conn.DefaultClientConnection.receiveResponseHeader(DefaultClientConnection.java:233)
        at org.apache.http.impl.conn.AbstractClientConnAdapter.receiveResponseHeader(AbstractClientConnAdapter.java:209)
        at org.apache.http.protocol.HttpRequestExecutor.doReceiveResponse(HttpRequestExecutor.java:298)
        at org.apache.http.protocol.HttpRequestExecutor.execute(HttpRequestExecutor.java:125)
        at org.apache.http.impl.client.DefaultRequestDirector.execute(DefaultRequestDirector.java:483)
        at org.apache.http.impl.client.AbstractHttpClient.execute(AbstractHttpClient.java:641)
        at org.apache.http.impl.client.AbstractHttpClient.execute(AbstractHttpClient.java:576)
        at org.apache.http.impl.client.AbstractHttpClient.execute(AbstractHttpClient.java:554)
        at org.ektorp.http.StdHttpClient.executeRequest(StdHttpClient.java:96)
        ... 5 more
*/
@Ignore
public class BulkTest {

    private final static Logger LOG = LoggerFactory.getLogger(StdCouchDbInstance.class);

    private HttpClient httpClient;

    private StdCouchDbConnector stdCouchDbConnector;

    private StreamedCouchDbConnector streamedCouchDbConnector;

    private CouchDbInstance dbInstance;

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        httpClient = new StdHttpClient.Builder().host("localhost").port(5984).cleanupIdleConnections(true).build();
        dbInstance = new StdCouchDbInstance(httpClient);

        String databasePath = this.getClass().getSimpleName() + "-DataBase";

        if (dbInstance.checkIfDbExists(databasePath)) {
            dbInstance.deleteDatabase(databasePath);
        }
        dbInstance.createDatabase(databasePath);

        stdCouchDbConnector = new StdCouchDbConnector(databasePath, dbInstance, new StdObjectMapperFactory());
        streamedCouchDbConnector = new StreamedCouchDbConnector(databasePath, dbInstance, new StdObjectMapperFactory());

        mapper = new ObjectMapper();
    }

    @After
    public void tearDown() {
        httpClient.shutdown();
    }

    @Test
    public void shouldUpdateInBulkWithOneElementWithStdCouchDbConnector() throws Exception {
        doUpdateInBulkWithOneElement(stdCouchDbConnector);
    }

    @Test
    public void shouldUpdateInBulkWithOneElementWithStreamedCouchDbConnector() throws Exception {
        doUpdateInBulkWithOneElement(streamedCouchDbConnector);
    }

    @Test
    public void shouldUpdateInBulkWithManyElementsWithStdCouchDbConnector() throws Exception {
        doUpdateInBulkWithManyElements(stdCouchDbConnector);
    }

    @Test
    public void shouldUpdateInBulkWithManyElementsWithStreamedCouchDbConnector() throws Exception {
        doUpdateInBulkWithManyElements(streamedCouchDbConnector);
    }

    public void doUpdateInBulkWithOneElement(CouchDbConnector db) throws Exception {
        final int iterationsCount = 1000;

        // create document "myid"
        try {
            db.create("myid", mapper.readTree("{\"i\":0}"));
        } catch (UpdateConflictException ex) {
            LOG.info("already exists - ignore : " + "myid");
        }

        long start = System.currentTimeMillis();
        for (int i = 1; i < iterationsCount; i++) {
            LOG.info("Round " + i + " of " + iterationsCount);

            JsonNode doc = db.get(JsonNode.class, "myid");

            Collection<JsonNode> docList = Collections.singleton(doc);
            List<DocumentOperationResult> bulkResult = db.executeBulk(docList);
            if (!bulkResult.isEmpty()) {
                throw new Exception("Got DocumentOperationResult " + bulkResult);
            }
            Collection<String> idList = Collections.singleton("myid");
            ViewQuery q = new ViewQuery().allDocs().includeDocs(true).keys(idList);
            db.queryView(q);
        }
        long rt = System.currentTimeMillis() - start;
        LOG.info("Running time: " + rt + " ms");
    }


    public void doUpdateInBulkWithManyElements(CouchDbConnector db) throws Exception {
        final int iterationsCount = 20;
        final int elementsCount = 1000;

        final List<String> allDocIds = new ArrayList<String>();
        for (int i = 0; i < elementsCount; i++) {
            String currentId = "TestDocumentBean-" + i;
            TestDocumentBean bean = new TestDocumentBean(RandomStringUtils.randomAlphanumeric(32), RandomStringUtils.randomAlphanumeric(16), new Date(), 0);
            try {
                db.create(currentId, bean);
            } catch (UpdateConflictException ex) {
                LOG.info("already exists - ignore : " + currentId);
            }
            allDocIds.add(currentId);
        }

        ViewQuery q = new ViewQuery().allDocs().includeDocs(true).keys(allDocIds);

        long start = System.currentTimeMillis();
        long bulkOpsTotalDuration = 0;
        for (int i = 1; i <= iterationsCount; i++) {
            LOG.info("Round " + i + " of " + iterationsCount);

            List<TestDocumentBean> docList = db.queryView(q, TestDocumentBean.class);
            for (TestDocumentBean b : docList) {
                // check version is as expected
                if (b.version != i - 1) {
                    throw new IllegalStateException("Bean state is not as expected : " + b);
                }
                b.version = i;
            }

            long bulkOpStart = System.currentTimeMillis();
            List<DocumentOperationResult> bulkResult = db.executeBulk(docList);
            bulkOpsTotalDuration += (System.currentTimeMillis() - bulkOpStart);
            if (!bulkResult.isEmpty()) {
                throw new Exception("Got DocumentOperationResult " + bulkResult);
            }
        }

        List<TestDocumentBean> docList = db.queryView(q, TestDocumentBean.class);
        for (TestDocumentBean b : docList) {
            // check version is as expected
            if (b.version != iterationsCount) {
                throw new IllegalStateException("Bean state is not as expected : " + b);
            }
        }

        long rt = System.currentTimeMillis() - start;
        LOG.info("Running time: " + rt + " ms, bulkOpsTotalDuration = " + bulkOpsTotalDuration + " ms");
    }


    public static class TestDocumentBean extends CouchDbDocument {
        public String lastName;
        public String firstName;
        public Date dateOfBirth;
        public int version;

        public TestDocumentBean() {

        }

        public TestDocumentBean(String lastName, String firstName, Date dateOfBirth, int version) {
            this.lastName = lastName;
            this.firstName = firstName;
            this.dateOfBirth = dateOfBirth;
            this.version = version;
        }
    }

}

