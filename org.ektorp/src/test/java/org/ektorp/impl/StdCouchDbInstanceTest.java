package org.ektorp.impl;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.ektorp.*;
import org.ektorp.http.*;
import org.junit.*;


public class StdCouchDbInstanceTest {
	
	HttpClient client = mock(HttpClient.class);
	CouchDbInstance instance = new StdCouchDbInstance(client);

	@Test
	public void testCreateDatabase() {
		when(client.get("/_all_dbs")).thenReturn(HttpResponseStub.valueOf(200, "[\"somedatabase\", \"anotherdatabase\"]"));
		when(client.put(anyString())).thenReturn(HttpResponseStub.valueOf(201, "{\"ok\": true}"));
		instance.createDatabase("testdb/");
		verify(client).put("/testdb/");
	}
	
	@Test
	public void testDatabaseWithSlashInPath() {
		when(client.get("/_all_dbs")).thenReturn(HttpResponseStub.valueOf(200, "[\"somedatabase\", \"anotherdatabase\"]"));
		when(client.put(anyString())).thenReturn(HttpResponseStub.valueOf(201, "{\"ok\": true}"));
		instance.createDatabase("test_inv/qaz/");
		verify(client).put("/test_inv%2Fqaz/");
	}

	@Test
	public void shouldFailWhenDbExists() {
		when(client.get("/_all_dbs")).thenReturn(HttpResponseStub.valueOf(200, "[\"somedatabase\", \"anotherdatabase\"]"));
		try {
			instance.createDatabase("somedatabase/");
			fail("RuntimeException expected");
		} catch (Exception e) {
			// expected
		}
	}
	
	@Test
	public void testDeleteDatabase() {
		instance.deleteDatabase("somedatabase");
		verify(client).delete("/somedatabase/");
	}

	@Test
	public void testGetAllDatabases() {
		when(client.get("/_all_dbs")).thenReturn(HttpResponseStub.valueOf(200, "[\"somedatabase\", \"anotherdatabase\"]"));
		List<String> all = instance.getAllDatabases();
		assertEquals(2, all.size());
		assertEquals("somedatabase", all.get(0));
		assertEquals("anotherdatabase", all.get(1));
	}

}
