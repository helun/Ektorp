package org.ektorp.impl;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.annotate.*;
import org.codehaus.jackson.map.*;
import org.ektorp.*;
import org.junit.*;

public class BulkDocumentWriterTest {

	List<?> objects = Arrays.asList(new TestDoc("0", "r0", "f0"),
											new TestDoc("1", "r1", "f1"),
											BulkDeleteDocument.of(new TestDoc("2", "r2", "f2")));
	ObjectMapper mapper = new ObjectMapper();
	BulkDocumentWriter bw = new BulkDocumentWriter(mapper);
	
	@Test
	public void testWrite() throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bw.write(objects, false, bos);
		JsonNode root = mapper.readTree(bos.toString("UTF-8"));
		assertThatObjectsAreWritten(root);
	}

	@Test
	public void testWriteAllOrNothing() throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bw.write(objects, true, bos);
		
		JsonNode root = mapper.readTree(bos.toString("UTF-8"));
		assertTrue(root.get("all_or_nothing").getBooleanValue());
		assertThatObjectsAreWritten(root);
	}
	
	private void assertThatObjectsAreWritten(JsonNode root) {
		Iterator<JsonNode> docs = root.get("docs").getElements();
		
		JsonNode doc = docs.next();
		assertEquals("0", doc.get("_id").getTextValue());
		
		doc = docs.next();
		assertEquals("1", doc.get("_id").getTextValue());
		
		doc = docs.next();
		assertEquals("2", doc.get("_id").getTextValue());
	}

	public static class TestDoc {
		String id;
		String revision;
		String field;
		
		public TestDoc(String id, String rev, String field) {
			this.id = id;
			this.revision = rev;
			this.field = field;
		}
		@JsonProperty("_id")
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		@JsonProperty("_rev")
		public String getRevision() {
			return revision;
		}
		public void setRevision(String revision) {
			this.revision = revision;
		}
		public String getField() {
			return field;
		}
		public void setField(String field) {
			this.field = field;
		}
		
		
	}
}
