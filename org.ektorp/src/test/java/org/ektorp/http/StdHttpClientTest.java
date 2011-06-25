package org.ektorp.http;

import static org.junit.Assert.*;

import org.junit.Test;

public class StdHttpClientTest {

	@Test
	public void build_from_url() throws Exception {
		StdHttpClient.Builder b = new StdHttpClient.Builder().url("http://somehost:8989");
		assertEquals("somehost", b.host);
		assertEquals(8989, b.port);
		assertFalse(b.enableSSL);
	}

	@Test
	public void build_from_ssl_url() throws Exception {
		StdHttpClient.Builder b = new StdHttpClient.Builder().url("https://somehost:8989");
		assertTrue(b.enableSSL);
	}
}
