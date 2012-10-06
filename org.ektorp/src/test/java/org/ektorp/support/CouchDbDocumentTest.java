package org.ektorp.support;

import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.*;

public class CouchDbDocumentTest {

	ObjectMapper mapper = new ObjectMapper();

	@Test
	public void loadBasicDoc() throws Exception {
		TestDoc td = mapper.readValue(getClass().getResourceAsStream("basic_doc.json"), TestDoc.class);
		assertNotNull(td);
		assertEquals("some_id", td.getId());
		assertEquals("3-a1a9b39ee3cc39181b796a69cb48521c", td.getRevision());
		assertFalse(td.getAttachments().isEmpty());
		assertTrue(td.hasConflict());
		assertFalse(td.getConflicts().isEmpty());
		Revisions r = td.getRevisions();
		assertNotNull(r);
		assertEquals(3, r.getStart());
		// serialize just to provoke any serialization errors
		mapper.writeValueAsString(td);
	}

	public static class TestDoc extends CouchDbDocument {

		private static final long serialVersionUID = 1L;
		private String field;

		public String getField() {
			return field;
		}

		public void setField(String field) {
			this.field = field;
		}

	}

}
