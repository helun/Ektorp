package org.ektorp;

import static org.junit.Assert.*;

import java.util.*;

import org.apache.commons.io.*;
import org.codehaus.jackson.map.*;
import org.ektorp.util.*;
import org.junit.*;


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
									.docIds(Arrays.asList(new String[] {"foo", "bar", "baz"}))
									.queryParams(queryParams)
									.build();
		
		String json = mapper.writeValueAsString(rc);
		String expected = IOUtils.toString(getClass().getResourceAsStream("full_replication_command.json"));
		assertTrue(JSONComparator.areEqual(json, expected));
	}
}
