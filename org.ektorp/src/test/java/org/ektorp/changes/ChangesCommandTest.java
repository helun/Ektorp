package org.ektorp.changes;

import static org.junit.Assert.*;

import org.junit.*;

public class ChangesCommandTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void noParams_should_produce_a_valid_partial_URI() {
		assertEquals("_changes", new ChangesCommand.Builder().build().toString());
	}

	@Test
	public void allParams_should_produce_a_valid_partial_URI() {
		assertEquals("_changes?feed=continuous&since=10&filter=mydesigndoc%2Fmyfilter&include_docs=true&limit=1000&paramName=paramValue",
								new ChangesCommand.Builder()
										.continuous(true)
										.since(10)
										.filter("mydesigndoc/myfilter")
										.includeDocs(true)
										.limit(1000)
										.param("paramName", "paramValue")
										.build().toString());
	}
}
