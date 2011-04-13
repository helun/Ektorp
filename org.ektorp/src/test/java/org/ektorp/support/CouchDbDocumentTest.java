package org.ektorp.support;

import static org.junit.Assert.*;

import org.codehaus.jackson.map.*;
import org.junit.*;

public class CouchDbDocumentTest {

	@Test
	public void testSetId() throws Exception {
		String json = "{\"field\":\"nisse\",\"_id\":\"some_id\",\"_rev\":\"123D123\",\"_attachments\":{\"name\":{\"stub\":true,\"content_type\":\"text/plain\",\"length\":29}},\"_conflicts\":[\"some-conflicting-rev\"]}";
		ObjectMapper map = new ObjectMapper();
		TestDoc td = map.readValue(json, TestDoc.class);
		assertNotNull(td);
		assertEquals("some_id", td.getId());
		assertEquals("123D123", td.getRevision());
		assertFalse(td.getAttachments().isEmpty());
		assertTrue(td.hasConflict());
		assertFalse(td.getConflicts().isEmpty());
		
		// serialize just to provoke any serialization errors
		map.writeValueAsString(td);
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
