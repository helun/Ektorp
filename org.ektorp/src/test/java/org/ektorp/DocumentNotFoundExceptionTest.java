package org.ektorp;

import static org.junit.Assert.*;

import org.junit.*;

public class DocumentNotFoundExceptionTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void give_response_body_has_not_been_set_then_isDocumentDeleted_should_resturn_false() {
		DocumentNotFoundException e = new DocumentNotFoundException("some_path");
		assertFalse(e.isDocumentDeleted());
	}

}
