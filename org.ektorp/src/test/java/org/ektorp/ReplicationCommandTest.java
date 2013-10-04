package org.ektorp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.ektorp.util.JSONComparator;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;



public class ReplicationCommandTest {

	ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void basic_params() throws Exception {
		
		ReplicationCommand rc = new ReplicationCommand.Builder()
									.source("example-database")
									.target("http://example.org/example-database")
									.build();
		
		String json = mapper.writeValueAsString(rc);
		String expected = IOUtils.toString(getClass().getResourceAsStream("basic_replication_command.json"));
		assertTrue(JSONComparator.areEqual(json, expected));
	}
	
	@Test
	public void all_params() throws Exception {
		
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("key", "value");
		
		ReplicationCommand rc = new ReplicationCommand.Builder()
									.source("http://example.org/example-database")
									.target("http://admin:password@127.0.0.1:5984/example-database")
									.proxy("http://localhost:8888")
									.filter("myddoc/myfilter")
									.cancel(true)
									.continuous(true)
									.createTarget(true)
									.docIds(Arrays.asList("foo", "bar", "baz"))
									.queryParams(queryParams)
									.build();
		
		String json = mapper.writeValueAsString(rc);
		String expected = IOUtils.toString(getClass().getResourceAsStream("full_replication_command.json"));
		assertTrue(JSONComparator.areEqual(json, expected));
	}

    @Test
    public void all_params_with_since_seq() throws Exception {

        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("key", "value");

        ReplicationCommand rc = new ReplicationCommand.Builder()
                .source("http://example.org/example-database")
                .target("http://admin:password@127.0.0.1:5984/example-database")
                .proxy("http://localhost:8888")
                .filter("myddoc/myfilter")
                .cancel(true)
                .continuous(true)
                .createTarget(true)
                .sinceSeq(123L)
                .docIds(Arrays.asList("foo", "bar", "baz"))
                .queryParams(queryParams)
                .build();

        String json = mapper.writeValueAsString(rc);

        String expected = IOUtils.toString(getClass().getResourceAsStream("full_replication_with_since_command.json"));
        assertTrue(JSONComparator.areEqual(json, expected));

        ReplicationCommand rcWithString = new ReplicationCommand.Builder()
                .source("http://example.org/example-database")
                .target("http://admin:password@127.0.0.1:5984/example-database")
                .proxy("http://localhost:8888")
                .filter("myddoc/myfilter")
                .cancel(true)
                .continuous(true)
                .createTarget(true)
                .sinceSeq("123")
                .docIds(Arrays.asList("foo", "bar", "baz"))
                .queryParams(queryParams)
                .build();

        assertTrue(JSONComparator.areEqual(mapper.writeValueAsString(rcWithString), IOUtils.toString(getClass().getResourceAsStream("full_replication_with_since_command.json"))));
    }

}
