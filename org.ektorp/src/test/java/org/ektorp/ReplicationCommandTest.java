package org.ektorp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.ektorp.util.JSONComparator;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;


public class ReplicationCommandTest {

	private ObjectMapper mapper;

    @Before
    public void before() {
        mapper = new ObjectMapper();
    }

	@Test
	public void basic_params() throws Exception {
		
		ReplicationCommand rc = new ReplicationCommand.Builder()
									.source("example-database")
									.target("http://example.org/example-database")
									.build();
		
		String actual = mapper.writeValueAsString(rc);
		String expected = IOUtils.toString(getClass().getResourceAsStream("basic_replication_command.json"));
		assertTrue(JSONComparator.areEqual(actual, expected));
	}
	
	@Test
	public void all_params() throws Exception {
		
		Map<String, String> queryParams = new HashMap<String, String>();
		queryParams.put("key", "value");
		
		ReplicationCommand rc = new ReplicationCommand.Builder()
		                            .id("0a81b645497e6270611ec3419767a584+continuous+create_target")
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
		
		String actual = mapper.writeValueAsString(rc);
		String expected = IOUtils.toString(getClass().getResourceAsStream("full_replication_command.json"));
		assertTrue(JSONComparator.areEqual(actual, expected));
	}

    @Test
    public void all_params_since_long() throws Exception {

        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("key", "value");

        ReplicationCommand cmd = new ReplicationCommand.Builder()
                .id("0a81b645497e6270611ec3419767a584+continuous+create_target")
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

        String actual = mapper.writeValueAsString(cmd);
        String expected = IOUtils.toString(getClass().getResourceAsStream("full_replication_with_since_command.json"));
        assertTrue(JSONComparator.areEqual(actual, expected));
    }


    @Test
    public void all_params_since_string() throws Exception {

        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("key", "value");

        ReplicationCommand cmd = new ReplicationCommand.Builder()
                .id("0a81b645497e6270611ec3419767a584+continuous+create_target")
                .source("http://example.org/example-database")
                .target("http://admin:password@127.0.0.1:5984/example-database")
                .proxy("http://localhost:8888")
                .filter("myddoc/myfilter")
                .cancel(true)
                .continuous(true)
                .createTarget(true)
                .sinceSeq("11521-g1AAAAEneJzLYWBgYMlgTmGQS0lKzi9KdUhJMtZLSy3KSy3RS87JL01JzCvRA3JygOqYEhmS7P___5-VwZzEwMCdlAsUY08yNU5LMTPMItaMJAcgmVSPMMYSbEyiuUlSSooJ0cbksQBJhgYgBTRpP9QoObBRJoYmxqZmBiQadQBiFMxVHmCjjNPMki2BrsoCAEvDV_I")
                .docIds(Arrays.asList("foo", "bar", "baz"))
                .queryParams(queryParams)
                .build();

        String json = mapper.writeValueAsString(cmd);
        String expected = IOUtils.toString(getClass().getResourceAsStream("full_replication_with_since_as_cloudant_command.json"));
        assertTrue(JSONComparator.areEqual(json, expected));
    }


}
