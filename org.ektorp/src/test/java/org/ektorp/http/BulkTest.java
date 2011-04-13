package org.ektorp.http;

import java.util.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.map.*;
import org.ektorp.*;
import org.ektorp.impl.*;
import org.junit.*;

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

        public static void main(String[] args) throws Exception {
                
                HttpClient httpClient = new StdHttpClient.Builder().host("localhost").port(5984).cleanupIdleConnections(true).build();
                CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
                CouchDbConnector db = dbInstance.createConnector("mutka_local", true);
                ObjectMapper mapper = new ObjectMapper();

                // create document "myid"
                try {                   
                        db.create("myid", mapper.readTree("{\"i\":0}"));
                } catch (UpdateConflictException ex) {
                        // already exists - ignore
                }
                long start = System.currentTimeMillis();
                for (int i = 1; i < 1000; i++) {
                        System.out.println("Round "+i);
                        
                        JsonNode doc = db.get(JsonNode.class, "myid");
                        
                        Collection<JsonNode> docList = Collections.singleton(doc);      
                        List<DocumentOperationResult> bulkResult = db.executeBulk(docList);
                        if (!bulkResult.isEmpty())
                                throw new Exception("Got DocumentOperationResult "+bulkResult.get(0));
                        
                        Collection<String> idList = Collections.singleton("myid");
                        ViewQuery q = new ViewQuery().allDocs().includeDocs(true).keys(idList);
                        db.queryView(q);
                }
                long rt = System.currentTimeMillis() - start;
                System.out.println("Running time: " + rt + " ms");
        }
}

