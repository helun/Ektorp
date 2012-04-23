package org.ektorp.impl;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
		assertTrue(root.get("all_or_nothing").booleanValue());
		assertThatObjectsAreWritten(root);
	}

	@Test
    public void testCreateInputStreamWrapper() throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream("[{\"_id\":\"0\",\"key\":\"key_value\",\"value\":\"doc_value0\"},{\"_id\":\"1\",\"key\":\"key_value\",\"value\":\"doc_value1\"},{\"_id\":\"2\",\"key\":\"key_value\",\"value\":\"doc_value2\"}]".getBytes("UTF-8"));
        InputStream inputStream = bw.createInputStreamWrapper(true, bis);
        JsonNode root = mapper.readTree(inputStream);
        assertThatObjectsAreWritten(root);
    }

	private void assertThatObjectsAreWritten(JsonNode root) {
		Iterator<JsonNode> docs = root.get("docs").elements();

		JsonNode doc = docs.next();
		assertEquals("0", doc.get("_id").textValue());

		doc = docs.next();
		assertEquals("1", doc.get("_id").textValue());

		doc = docs.next();
		assertEquals("2", doc.get("_id").textValue());
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
