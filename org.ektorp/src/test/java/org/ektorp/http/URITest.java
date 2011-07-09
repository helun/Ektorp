package org.ektorp.http;

import static org.junit.Assert.*;

import org.junit.*;

public class URITest {

	@Test
	public void basic_uri() {
		assertEquals("http://example.com:4510", URI.of("http://example.com:4510").toString());
	}
	
	@Test
	public void appended_path() {
		assertEquals("http://example.com:4510/my_db", URI.of("http://example.com:4510").append("my_db").toString());
	}

	@Test
	public void appended_path_with_slash() {
		assertEquals("/database/my_db", URI.of("/database/").append("my_db").toString());
	}
	
	@Test
	public void appended_path2() {
		assertEquals("http://example.com:4510/my_db/my_doc", URI.of("http://example.com:4510")
															.append("my_db")
															.append("my_doc").toString());
	}
	
	@Test
	public void params() {
		assertEquals("http://example.com:4510/my_db/my_doc?startKey=%22test%22&endKey=%22test%22", URI.of("http://example.com:4510")
															.append("my_db")
															.append("my_doc")
															.param("startKey", "\"test\"")
															.param("endKey", "\"test\"")
															.toString());
	}
	
	@Test
	public void multiCallToString() {
		URI uri = URI.of("http://example.com:4510")
		.append("my_db")
		.append("my_doc")
		.param("startKey", "\"test\"")
		.param("endKey", "\"test\"");
		uri.toString();
		assertEquals("http://example.com:4510/my_db/my_doc?startKey=%22test%22&endKey=%22test%22", uri.toString());
	}
}
