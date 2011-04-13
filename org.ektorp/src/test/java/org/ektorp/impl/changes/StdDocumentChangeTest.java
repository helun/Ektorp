package org.ektorp.impl.changes;

import static org.junit.Assert.*;

import java.io.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.map.*;
import org.junit.*;

public class StdDocumentChangeTest {

	ObjectMapper mapper = new ObjectMapper();

	@Test
	public void test_normal_message() throws IOException {
		StdDocumentChange m = new StdDocumentChange(load("change_message.json"));
		assertMandatoryFields(m);
		assertNull(m.getDoc());
		assertTrue(m.getDocAsNode().isMissingNode());
		assertFalse(m.isDeleted());
	}

	private void assertMandatoryFields(StdDocumentChange m) {
		assertEquals(21, m.getSequence());
		assertEquals("doc_id", m.getId());
		assertEquals("doc_rev", m.getRevision());
	}

	@Test
	public void test_deleted_doc_message() throws IOException {
		StdDocumentChange m = new StdDocumentChange(load("change_message_w_deleted_doc.json"));
		assertMandatoryFields(m);
		assertTrue(m.isDeleted());
	}

	@Test
	public void test_message_with_included_doc() throws IOException {
		StdDocumentChange m = new StdDocumentChange(load("change_message_w_included_doc.json"));
		assertMandatoryFields(m);
		assertNotNull(m.getDoc());
		assertFalse(m.getDocAsNode().isMissingNode());
		assertNotNull(m.getDocAsNode().findValue("_id"));
		assertNotNull(m.getDocAsNode().findValue("_rev"));
	}
	
	private JsonNode load(String id) throws IOException {
		return mapper.readTree(getClass().getResourceAsStream(id));
	}

}
