package org.ektorp.support;

import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.*;

import com.dw.couchdb.dto.Revisions;

import java.io.InputStream;

public class CouchDbDocumentTest {

	ObjectMapper mapper = new ObjectMapper();

	@Test
	public void loadBasicDoc() throws Exception {
        TestDoc td;
        InputStream resourceAsStream = null;
        try {
            resourceAsStream = getClass().getResourceAsStream("basic_doc.json");
            td = mapper.readValue(resourceAsStream, TestDoc.class);
        } finally {
            IOUtils.closeQuietly(resourceAsStream);
        }
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
