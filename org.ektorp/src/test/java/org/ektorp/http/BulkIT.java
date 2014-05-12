package org.ektorp.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.lang.RandomStringUtils;
import org.ektorp.*;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;
import org.ektorp.impl.StdObjectMapperFactory;
import org.ektorp.impl.StreamedCouchDbConnector;
import org.ektorp.support.CouchDbDocument;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.StringReader;
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
public class BulkIT {

    private final static Logger LOG = LoggerFactory.getLogger(BulkIT.class);

    private HttpClient httpClient;

    private StdCouchDbConnector stdCouchDbConnector;

    private StreamedCouchDbConnector streamedCouchDbConnector;

    private CouchDbInstance dbInstance;

    private ObjectMapper mapper;

    private boolean createDatabaseIfNeeded = true;

    private boolean deleteDatabaseIfNeeded = true;

    @Before
    public void setUp() {
        StdHttpClient.Builder builder = new StdHttpClient.Builder();

        // read the configuration from the System properties (if you want to target another host like Cloudant)

        final String serverHost = System.getProperty("serverHost");
        final String serverPort = System.getProperty("serverPort");
        final String serverUsername = System.getProperty("serverUsername");
        final String serverPassword = System.getProperty("serverPassword");
        final String proxyHost = System.getProperty("proxyHost");
        final String proxyPort = System.getProperty("proxyPort");

        builder = builder.cleanupIdleConnections(true).caching(false).enableSSL(false);
        if (serverHost != null) {
            builder = builder.host(serverHost);
            builder = builder.socketTimeout(10000).connectionTimeout(5000);
        }
        if (serverPort != null) {
            builder = builder.port(Integer.parseInt(serverPort));
        }
        if (serverUsername != null) {
            builder = builder.username(serverUsername);
        }
        if (serverPassword != null) {
            builder = builder.password(serverPassword);
        }
        if (proxyHost != null && proxyPort != null) {
            builder = builder.proxy(proxyHost).proxyPort(Integer.parseInt(proxyPort));
        }


        httpClient = builder.build();
        dbInstance = new StdCouchDbInstance(httpClient);

        String databasePath = this.getClass().getSimpleName() + "-DataBase";

        if (deleteDatabaseIfNeeded) {
            if (dbInstance.checkIfDbExists(databasePath)) {
                dbInstance.deleteDatabase(databasePath);
            }
        }
        if (createDatabaseIfNeeded) {
            if (!dbInstance.checkIfDbExists(databasePath)) {
                dbInstance.createDatabase(databasePath);
            }
        } else {
            if (!dbInstance.checkIfDbExists(databasePath)) {
                throw new IllegalStateException("Database does not exists");
            }
        }

        stdCouchDbConnector = new StdCouchDbConnector(databasePath, dbInstance, new StdObjectMapperFactory());
        streamedCouchDbConnector = new StreamedCouchDbConnector(databasePath, dbInstance, new StdObjectMapperFactory());

        mapper = new ObjectMapper();
    }

    @After
    public void tearDown() {
        if (httpClient != null) {
            httpClient.shutdown();
        }
    }

    @Test
    public void shouldDoUpdateInBulkWithOneSmallInputStreamWithStdCouchDbConnector() throws Exception {
        doUpdateInBulkWithOneSmallInputStream(stdCouchDbConnector);
    }

    @Test
    public void shouldDoUpdateInBulkWithOneSmallInputStreamWithStreamedCouchDbConnector() throws Exception {
        doUpdateInBulkWithOneSmallInputStream(streamedCouchDbConnector);
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
        final int iterationsCount = 10;

        // create document "myid"
        try {
            db.create("myid", mapper.readTree("{\"i\":0}"));
        } catch (UpdateConflictException ex) {
            LOG.info("already exists - ignore : " + "myid");
        }

        long start = System.currentTimeMillis();
        for (int i = 1; i <= iterationsCount; i++) {
            LOG.info("Round " + i + " of " + iterationsCount);

            JsonNode doc = db.get(JsonNode.class, "myid");

            Collection<JsonNode> docList = Collections.singleton(doc);
            List<DocumentOperationResult> bulkResult = db.executeBulk(docList);
            if (!bulkResult.isEmpty()) {
                throw new Exception("Got DocumentOperationResult " + bulkResult);
            }
        }
        long rt = System.currentTimeMillis() - start;
        LOG.info("Running time: " + rt + " ms");
    }


    public void doUpdateInBulkWithManyElements(CouchDbConnector db) {
        final int iterationsCount = 10;
        final int elementsCount = 20;

        final List<String> allDocIds = new ArrayList<String>();

        for (int i = 0; i < elementsCount; i++) {
            String currentId = "TestDocumentBean-" + i;
            allDocIds.add(currentId);
        }

        final Map<String, String> currentRevisionById = new HashMap<String, String>();
        if (!deleteDatabaseIfNeeded) {
            for (String id : allDocIds) {
                try {
                    String currentRevisionId;
                    currentRevisionId = db.getCurrentRevision(id);
                    currentRevisionById.put(id, currentRevisionId);
                } catch (DocumentNotFoundException e) {
                    // should never occur
                    LOG.info("DocumentNotFoundException when searching for revision of document " + id, e);
                }
            }
            LOG.info("currentRevisionById = " + currentRevisionById);
        }

        for (int i = 0; i < elementsCount; i++) {
            String currentId = "TestDocumentBean-" + i;
            TestDocumentBean bean = new TestDocumentBean(RandomStringUtils.randomAlphanumeric(32), RandomStringUtils.randomAlphanumeric(16), System.currentTimeMillis(), 0);
            String currentRevision = currentRevisionById.get(currentId);
            if (currentRevision != null) {
                bean.setId(currentId);
                bean.setRevision(currentRevision);
                db.update(bean);
            } else {
                db.create(currentId, bean);
            }
        }


        final ViewQuery q = new ViewQuery().allDocs().includeDocs(true).keys(allDocIds);

        long start = System.currentTimeMillis();
        long bulkOpsTotalDuration = 0;
        for (int i = 1; i <= iterationsCount; i++) {
            LOG.info("Round " + i + " of " + iterationsCount);

            List<TestDocumentBean> docList = db.queryView(q, TestDocumentBean.class);
            for (TestDocumentBean b : docList) {
                Assert.assertNotNull(b.firstName);
                Assert.assertNotNull(b.getLastName());
                Assert.assertNotNull(b.dateOfBirth);
                // check version is as expected
                Assert.assertEquals("Bean state is not as expected", i - 1, b.getVersion());
                b.setVersion(i);
            }

            long bulkOpStart = System.currentTimeMillis();
            List<DocumentOperationResult> bulkResult = db.executeBulk(docList);
            bulkOpsTotalDuration += (System.currentTimeMillis() - bulkOpStart);
            if (!bulkResult.isEmpty()) {
                throw new RuntimeException("Got DocumentOperationResult " + bulkResult);
            }
        }

        List<TestDocumentBean> docList = db.queryView(q, TestDocumentBean.class);
        for (TestDocumentBean b : docList) {
            // check version is as expected
            if (b.getVersion() != iterationsCount) {
                throw new IllegalStateException("Bean state is not as expected : " + b);
            }
        }

        long rt = System.currentTimeMillis() - start;
        LOG.info("Running time: " + rt + " ms, bulkOpsTotalDuration = " + bulkOpsTotalDuration + " ms");
    }

    public void doUpdateInBulkWithOneSmallInputStream(CouchDbConnector db) throws Exception {
        final int iterationsCount = 10;

        // create or update the document, with initial "i" value of 0
        final String id = "myid";
        ObjectNode originalJsonObject = (ObjectNode) mapper.readTree("{\"i\":0}");
        String currentRevisionId;
        try {
            currentRevisionId = db.getCurrentRevision(id);
        } catch (DocumentNotFoundException e) {
            currentRevisionId = null;
        }
        if (currentRevisionId == null) {
            db.create("myid", originalJsonObject);
        } else {
            originalJsonObject.put("_id", id);
            originalJsonObject.put("_rev", currentRevisionId);
            db.update(originalJsonObject);
        }

        long start = System.currentTimeMillis();
        long bulkOpsTotalDuration = 0;
        for (int i = 1; i <= iterationsCount; i++) {
            LOG.info("Round " + i + " of " + iterationsCount);

            ObjectNode doc = db.get(ObjectNode.class, "myid");
            int iFieldValue = doc.get("i").asInt();
            if (iFieldValue != i - 1) {
                throw new IllegalStateException("Bean state is not as expected : " + doc);
            }
            doc.put("i", i);

            InputStream bulkDocumentAsInputStream = new ReaderInputStream(new StringReader("[" + mapper.writeValueAsString(doc) + "]"));

            long bulkOpStart = System.currentTimeMillis();
            List<DocumentOperationResult> bulkResult = db.executeBulk(bulkDocumentAsInputStream);
            bulkOpsTotalDuration += (System.currentTimeMillis() - bulkOpStart);
            if (!bulkResult.isEmpty()) {
                throw new Exception("Got DocumentOperationResult " + bulkResult);
            }
        }
        long rt = System.currentTimeMillis() - start;
        LOG.info("Running time: " + rt + " ms, bulkOpsTotalDuration = " + bulkOpsTotalDuration + " ms");
    }


    public static class TestDocumentBean extends CouchDbDocument {
        private String lastName;
        private String firstName;
        private Long dateOfBirth;
        private int version = -1;

        public TestDocumentBean() {

        }

        public TestDocumentBean(String lastName, String firstName, Long dateOfBirth, int version) {
            this.lastName = lastName;
            this.firstName = firstName;
            this.dateOfBirth = dateOfBirth;
            this.version = version;
        }

        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public Long getDateOfBirth() {
            return dateOfBirth;
        }

        public void setDateOfBirth(Long dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
    }

}

